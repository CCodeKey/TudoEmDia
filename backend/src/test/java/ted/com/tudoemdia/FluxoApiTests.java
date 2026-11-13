package ted.com.tudoemdia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ted.com.tudoemdia.service.EmailService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Fluxos completos pela API HTTP, com sessão, CSRF e banco Postgres real (descartável).
// Roda com o CSRF ligado, como em produção, para garantir que religá-lo continua funcionando
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "app.armazenamento.diretorio=target/uploads-teste", "app.seguranca.csrf-habilitado=true" })
@Import(BancoDeTesteConfig.class)
class FluxoApiTests {

	private static final JsonMapper JSON = JsonMapper.builder().build();
	private static final LocalDate HOJE = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 1, 2, 3 };
	private static final byte[] PDF = "%PDF-1.4 teste".getBytes(StandardCharsets.US_ASCII);
	private static final Pattern LINK_CONVITE = Pattern.compile("/convites/([A-Za-z0-9_-]+)");

	@Value("${local.server.port}")
	private int porta;

	// Espiona o envio para capturar o link do convite, sem impedir o envio real
	@MockitoSpyBean
	private EmailService emailService;

	@Test
	void proprietarioGerenciaObrigacoesPagamentosAnexosERelatorios() throws Exception {
		Cliente ana = novoUsuario("Ana", "ana@teste.com");
		String empresa = criarEmpresa(ana, "Padaria da Ana");
		String base = "/api/empresas/" + empresa;

		assertThat(ana.get(base + "/permissoes").corpo().get("permissoes").size()).isEqualTo(20);
		JsonNode categorias = ana.get(base + "/categorias").corpo();
		assertThat(categorias.size()).isEqualTo(5);
		String categoria = categorias.get(0).get("id").asString();

		// Recorrente com vencimento passado: nasce vencida e gera as ocorrências até uma futura (RN29, RN62)
		Resposta aluguel = ana.enviar("POST", base + "/obrigacoes", """
				{"descricao":"Aluguel","valor":1500.00,"categoriaId":"%s","dataVencimento":"%s",
				 "recorrencia":{"periodicidade":"MENSAL"}}""".formatted(categoria, HOJE.minusMonths(2)));
		assertThat(aluguel.status()).isEqualTo(201);
		assertThat(aluguel.texto("status")).isEqualTo("VENCIDA");
		String aluguelId = aluguel.texto("id");

		JsonNode lista = ana.get(base + "/obrigacoes").corpo();
		assertThat(lista.size()).isGreaterThanOrEqualTo(3);
		boolean temFutura = false;
		for (JsonNode obrigacao : lista) {
			temFutura |= LocalDate.parse(obrigacao.get("dataVencimento").asString()).isAfter(HOJE);
		}
		assertThat(temFutura).isTrue();

		Resposta luz = ana.enviar("POST", base + "/obrigacoes", """
				{"descricao":"Conta de luz","valor":99.90,"categoriaId":"%s","dataVencimento":"%s"}"""
				.formatted(categoria, HOJE.plusDays(5)));
		assertThat(luz.texto("status")).isEqualTo("PENDENTE");
		String luzId = luz.texto("id");

		// Validação de campos (RN26, RN27)
		Resposta invalida = ana.enviar("POST", base + "/obrigacoes", """
				{"descricao":"","valor":0,"categoriaId":"%s","dataVencimento":"%s"}""".formatted(categoria, HOJE));
		assertThat(invalida.status()).isEqualTo(400);
		assertThat(invalida.corpo().get("campos").has("valor")).isTrue();

		// Pagamento (RN66 a RN69)
		Resposta pagamento = ana.enviar("POST", base + "/obrigacoes/" + aluguelId + "/pagamento",
				"{\"dataPagamento\":\"%s\"}".formatted(HOJE));
		assertThat(pagamento.status()).isEqualTo(201);
		assertThat(pagamento.corpo().get("pagoComAtraso").asBoolean()).isTrue();
		assertThat(ana.enviar("POST", base + "/obrigacoes/" + aluguelId + "/pagamento",
				"{\"dataPagamento\":\"%s\"}".formatted(HOJE)).status()).isEqualTo(409);
		assertThat(ana.enviar("POST", base + "/obrigacoes/" + luzId + "/pagamento",
				"{\"dataPagamento\":\"%s\"}".formatted(HOJE.plusDays(1))).status()).isEqualTo(422);

		// Obrigação paga: sem alterar valor nem excluir (RN34, RN38)
		assertThat(ana.enviar("PUT", base + "/obrigacoes/" + aluguelId, """
				{"descricao":"Aluguel","valor":1600.00,"categoriaId":"%s","dataVencimento":"%s"}"""
				.formatted(categoria, HOJE.minusMonths(2))).status()).isEqualTo(422);
		assertThat(ana.enviar("DELETE", base + "/obrigacoes/" + aluguelId, null).status()).isEqualTo(422);

		// Anexos: só PDF, JPG ou PNG de verdade; download pela API (RN28, RN53)
		Resposta anexo = ana.multipart("POST", base + "/obrigacoes/" + luzId + "/anexos", Map.of(), "boleto.png", PNG);
		assertThat(anexo.status()).isEqualTo(201);
		HttpResponse<byte[]> download = ana.baixar(base + "/anexos/" + anexo.texto("id") + "/arquivo");
		assertThat(download.statusCode()).isEqualTo(200);
		assertThat(download.body()).isEqualTo(PNG);
		assertThat(ana.multipart("POST", base + "/obrigacoes/" + luzId + "/anexos", Map.of(), "falso.pdf", PNG).status())
				.isEqualTo(422);

		// Painel, histórico e relatório
		Resposta painel = ana.get(base + "/painel?mes=" + YearMonth.from(HOJE));
		assertThat(painel.status()).isEqualTo(200);
		assertThat(painel.corpo().get("vencidas").size()).isGreaterThanOrEqualTo(1);
		assertThat(ana.get(base + "/painel?mes=abc").status()).isEqualTo(400);
		assertThat(ana.get(base + "/pagamentos").corpo().size()).isEqualTo(1);
		HttpResponse<byte[]> relatorio = ana.baixar(base + "/relatorios/obrigacoes");
		assertThat(relatorio.statusCode()).isEqualTo(200);
		assertThat(relatorio.headers().firstValue("Content-Type")).hasValue("application/pdf");
		assertThat(new String(relatorio.body(), 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");

		// Cancelamento volta a obrigação para vencida, mantendo o histórico (RN70)
		String pagamentoId = pagamento.texto("id");
		assertThat(ana.enviar("POST", base + "/pagamentos/" + pagamentoId + "/cancelamento", null).corpo()
				.get("cancelado").asBoolean()).isTrue();
		assertThat(ana.get(base + "/obrigacoes/" + aluguelId).corpo().get("obrigacao").get("status").asString())
				.isEqualTo("VENCIDA");
		assertThat(ana.get(base + "/pagamentos").corpo().size()).isEqualTo(1);

		// Exclusão de obrigação sem pagamento remove também os anexos
		assertThat(ana.enviar("DELETE", base + "/obrigacoes/" + luzId, null).status()).isEqualTo(204);
		assertThat(ana.get(base + "/obrigacoes/" + luzId).status()).isEqualTo(404);

		// Alertas: pelo menos um prazo ativo (RN76)
		assertThat(ana.get(base + "/alertas").corpo().size()).isEqualTo(4);
		assertThat(ana.enviar("PUT", base + "/alertas", """
				{"prazos":[{"diasAntecedencia":7,"ativo":false},{"diasAntecedencia":3,"ativo":false},
				 {"diasAntecedencia":1,"ativo":false},{"diasAntecedencia":0,"ativo":false}]}""").status())
				.isEqualTo(422);

		// Convites (RN14, RN17)
		assertThat(ana.enviar("POST", base + "/convites", "{\"email\":\"contador@teste.com\",\"perfil\":\"CONTADOR\"}")
				.status()).isEqualTo(201);
		assertThat(ana.enviar("POST", base + "/convites", "{\"email\":\"contador@teste.com\",\"perfil\":\"CONTADOR\"}")
				.status()).isEqualTo(409);
		assertThat(ana.enviar("POST", base + "/convites", "{\"email\":\"socio@teste.com\",\"perfil\":\"PROPRIETARIO\"}")
				.status()).isEqualTo(422);

		assertThat(ana.enviar("POST", "/api/auth/logout", null).status()).isEqualTo(204);
		assertThat(ana.get("/api/auth/me").status()).isEqualTo(401);
	}

	@Test
	void conviteAceitoPeloLinkDaPerfisComAcessoCerto() throws Exception {
		Cliente dona = novoUsuario("Elisa", "elisa@teste.com");
		String empresa = criarEmpresa(dona, "Mercado da Elisa");
		String base = "/api/empresas/" + empresa;
		String categoria = dona.get(base + "/categorias").corpo().get(0).get("id").asString();
		String obrigacao = dona.enviar("POST", base + "/obrigacoes", """
				{"descricao":"DAS","valor":80,"categoriaId":"%s","dataVencimento":"%s"}""".formatted(categoria, HOJE))
				.texto("id");
		String pagamento = dona.enviar("POST", base + "/obrigacoes/" + obrigacao + "/pagamento",
				"{\"dataPagamento\":\"%s\"}".formatted(HOJE)).texto("id");

		// Contador: consulta e exporta, mas não altera nada
		Cliente contador = aceitarConvite(dona, base, "contador.elisa@teste.com", "CONTADOR", "Fábio");
		assertThat(contador.get(base + "/permissoes").corpo().get("permissoes").size()).isEqualTo(6);
		assertThat(contador.get(base + "/obrigacoes").status()).isEqualTo(200);
		assertThat(contador.get(base + "/pagamentos").status()).isEqualTo(200);
		assertThat(contador.baixar(base + "/relatorios/obrigacoes").statusCode()).isEqualTo(200);
		assertThat(contador.enviar("POST", base + "/obrigacoes", """
				{"descricao":"x","valor":1,"categoriaId":"%s","dataVencimento":"%s"}""".formatted(categoria, HOJE))
				.status()).isEqualTo(403);
		assertThat(contador.enviar("POST", base + "/obrigacoes/" + obrigacao + "/pagamento",
				"{\"dataPagamento\":\"%s\"}".formatted(HOJE)).status()).isEqualTo(403);
		assertThat(contador.get(base + "/usuarios").status()).isEqualTo(403);
		assertThat(contador.multipart("POST", base + "/documentos",
				Map.of("descricao", "Alvará", "categoriaId", categoria), "alvara.pdf", PDF).status()).isEqualTo(403);

		// Financeiro: cuida das obrigações, mas não de documentos, equipe, alertas nem cancelamentos
		Cliente financeiro = aceitarConvite(dona, base, "financeiro.elisa@teste.com", "FINANCEIRO", "Gilda");
		assertThat(financeiro.enviar("POST", base + "/obrigacoes", """
				{"descricao":"Fornecedor","valor":300,"categoriaId":"%s","dataVencimento":"%s"}"""
				.formatted(categoria, HOJE.plusDays(10))).status()).isEqualTo(201);
		assertThat(financeiro.multipart("POST", base + "/documentos",
				Map.of("descricao", "Alvará", "categoriaId", categoria), "alvara.pdf", PDF).status()).isEqualTo(403);
		assertThat(financeiro.enviar("POST", base + "/pagamentos/" + pagamento + "/cancelamento", null).status())
				.isEqualTo(403);
		assertThat(financeiro.get(base + "/alertas").status()).isEqualTo(403);
		assertThat(financeiro.enviar("POST", base + "/convites",
				"{\"email\":\"outro@teste.com\",\"perfil\":\"CONTADOR\"}").status()).isEqualTo(403);

		// Proprietária vê os três, desativa o contador e não pode remover a si mesma (RN19, RN20)
		JsonNode membros = dona.get(base + "/usuarios").corpo();
		assertThat(membros.size()).isEqualTo(3);
		String vinculoContador = idDoMembro(membros, "contador.elisa@teste.com");
		String vinculoDona = idDoMembro(membros, "elisa@teste.com");
		assertThat(dona.enviar("PATCH", base + "/usuarios/" + vinculoContador, "{\"ativo\":false}").status())
				.isEqualTo(200);
		assertThat(contador.get(base + "/obrigacoes").status()).isEqualTo(403);
		assertThat(dona.enviar("PATCH", base + "/usuarios/" + vinculoDona, "{\"ativo\":false}").status())
				.isEqualTo(422);

		// O link do convite não vale duas vezes
		assertThat(contador.enviar("POST", "/api/convites/token-inexistente/aceite", null).status()).isEqualTo(404);
	}

	@Test
	void documentosComValidadeArquivoEExclusao() throws Exception {
		Cliente dono = novoUsuario("Igor", "igor@teste.com");
		String base = "/api/empresas/" + criarEmpresa(dono, "Oficina do Igor");
		String categoria = dono.get(base + "/categorias").corpo().get(4).get("id").asString();

		// Validade passada: documento já nasce vencido (RN51)
		Resposta documento = dono.multipart("POST", base + "/documentos",
				Map.of("descricao", "Certidão negativa", "categoriaId", categoria,
						"dataValidade", HOJE.minusDays(1).toString()),
				"certidão.pdf", PDF);
		assertThat(documento.status()).isEqualTo(201);
		assertThat(documento.texto("status")).isEqualTo("VENCIDO");
		assertThat(documento.texto("nomeArquivo")).isEqualTo("certidão.pdf");
		String id = documento.texto("id");

		// Sem arquivo ou com arquivo inválido (RN49, RN50)
		assertThat(dono.multipart("POST", base + "/documentos",
				Map.of("descricao", "Contrato", "categoriaId", categoria), "contrato.exe", PDF).status()).isEqualTo(422);

		// Nova validade e depois arquivamento, com registro de quem alterou (RN56)
		Resposta atualizado = dono.enviar("PUT", base + "/documentos/" + id, """
				{"descricao":"Certidão negativa 2027","categoriaId":"%s","dataValidade":"%s","arquivado":false}"""
				.formatted(categoria, HOJE.plusYears(1)));
		assertThat(atualizado.texto("status")).isEqualTo("ATIVO");
		assertThat(atualizado.texto("atualizadoPor")).isEqualTo("Igor");
		assertThat(dono.enviar("PUT", base + "/documentos/" + id, """
				{"descricao":"Certidão negativa 2027","categoriaId":"%s","arquivado":true}""".formatted(categoria))
				.texto("status")).isEqualTo("ARQUIVADO");
		assertThat(dono.get(base + "/documentos?status=ARQUIVADO").corpo().size()).isEqualTo(1);

		// Troca do arquivo e download do novo
		assertThat(dono.multipart("PUT", base + "/documentos/" + id + "/arquivo", Map.of(), "nova.png", PNG).status())
				.isEqualTo(200);
		HttpResponse<byte[]> arquivo = dono.baixar(base + "/documentos/" + id + "/arquivo");
		assertThat(arquivo.body()).isEqualTo(PNG);
		assertThat(arquivo.headers().firstValue("Content-Type")).hasValue("image/png");

		assertThat(dono.enviar("DELETE", base + "/documentos/" + id, null).status()).isEqualTo(204);
		assertThat(dono.baixar(base + "/documentos/" + id + "/arquivo").statusCode()).isEqualTo(404);
	}

	@Test
	void recorrenciaECategorias() throws Exception {
		Cliente dona = novoUsuario("Joana", "joana@teste.com");
		String base = "/api/empresas/" + criarEmpresa(dona, "Ateliê da Joana");

		// Categoria própria; categoria padrão não pode ser excluída (RN41, RN42)
		Resposta categoria = dona.enviar("POST", base + "/categorias", "{\"nome\":\"Tecidos\"}");
		assertThat(categoria.status()).isEqualTo(201);
		assertThat(dona.enviar("POST", base + "/categorias", "{\"nome\":\"tecidos\"}").status()).isEqualTo(409);
		String padrao = dona.get(base + "/categorias").corpo().get(0).get("id").asString();
		assertThat(dona.enviar("DELETE", base + "/categorias/" + padrao, null).status()).isEqualTo(422);

		Resposta tecido = dona.enviar("POST", base + "/obrigacoes", """
				{"descricao":"Tecido","valor":500,"categoriaId":"%s","dataVencimento":"%s",
				 "recorrencia":{"periodicidade":"MENSAL"}}""".formatted(categoria.texto("id"), HOJE.plusDays(1)));
		String recorrencia = tecido.texto("recorrenciaId");
		assertThat(dona.enviar("DELETE", base + "/categorias/" + categoria.texto("id"), null).status()).isEqualTo(409);

		// Alterar a recorrência muda só as ocorrências futuras não pagas (RN61)
		Resposta alterada = dona.enviar("PUT", base + "/recorrencias/" + recorrencia, """
				{"descricao":"Tecido importado","valor":650,"categoriaId":"%s","periodicidade":"MENSAL","ativa":true}"""
				.formatted(categoria.texto("id")));
		assertThat(alterada.status()).isEqualTo(200);
		JsonNode ocorrencias = dona.get(base + "/obrigacoes").corpo();
		for (JsonNode ocorrencia : ocorrencias) {
			assertThat(ocorrencia.get("descricao").asString()).isEqualTo("Tecido importado");
			assertThat(ocorrencia.get("valor").decimalValue()).isEqualByComparingTo("650");
		}

		// Excluir esta e as próximas encerra a recorrência; sem ocorrências restantes, ela é removida (RN39)
		assertThat(dona.enviar("DELETE", base + "/obrigacoes/" + tecido.texto("id") + "?escopo=ESTA_E_FUTURAS", null)
				.status()).isEqualTo(204);
		assertThat(dona.get(base + "/obrigacoes").corpo().size()).isZero();
		assertThat(dona.get(base + "/recorrencias").corpo().size()).isZero();

		// Com uma ocorrência paga antes, a recorrência é só encerrada e o histórico fica
		Resposta mensalidade = dona.enviar("POST", base + "/obrigacoes", """
				{"descricao":"Mensalidade","valor":90,"categoriaId":"%s","dataVencimento":"%s",
				 "recorrencia":{"periodicidade":"MENSAL"}}""".formatted(padrao, HOJE.minusMonths(1)));
		dona.enviar("POST", base + "/obrigacoes/" + mensalidade.texto("id") + "/pagamento",
				"{\"dataPagamento\":\"%s\"}".formatted(HOJE));
		// A lista vem ordenada pelo vencimento: a primeira pendente é a mais próxima
		String proxima = dona.get(base + "/obrigacoes?status=PENDENTE").corpo().get(0).get("id").asString();
		assertThat(dona.enviar("DELETE", base + "/obrigacoes/" + proxima + "?escopo=ESTA_E_FUTURAS", null).status())
				.isEqualTo(204);
		assertThat(dona.get(base + "/recorrencias").corpo().get(0).get("ativa").asBoolean()).isFalse();
		assertThat(dona.get(base + "/obrigacoes").corpo().size()).isEqualTo(1);

		// Sem uso, a categoria própria pode ser excluída
		assertThat(dona.enviar("DELETE", base + "/categorias/" + categoria.texto("id"), null).status()).isEqualTo(204);
	}

	@Test
	void loginBloqueiaAposCincoTentativasErradas() throws Exception {
		novoUsuario("Kátia", "katia@teste.com");
		Cliente tentativa = new Cliente();
		tentativa.get("/api/auth/csrf");
		String errado = "{\"email\":\"katia@teste.com\",\"senha\":\"errada123\"}";
		for (int i = 0; i < 5; i++) {
			Resposta resposta = tentativa.enviar("POST", "/api/auth/login", errado);
			assertThat(resposta.status()).isEqualTo(401);
			assertThat(resposta.texto("detail")).isEqualTo("E-mail ou senha inválidos.");
		}
		// Bloqueado por 15 minutos, mesmo com a senha certa (RN06)
		assertThat(tentativa.enviar("POST", "/api/auth/login", "{\"email\":\"katia@teste.com\",\"senha\":\"Senha123\"}")
				.status()).isEqualTo(429);
	}

	@Test
	void usuarioNaoAcessaDadosDeEmpresaSemVinculo() throws Exception {
		Cliente dona = novoUsuario("Beatriz", "beatriz@teste.com");
		String empresa = criarEmpresa(dona, "Loja da Beatriz");

		Cliente outro = novoUsuario("Bruno", "bruno@teste.com");
		assertThat(outro.get("/api/empresas").corpo().size()).isZero();
		assertThat(outro.get("/api/empresas/" + empresa + "/obrigacoes").status()).isEqualTo(403);
		assertThat(outro.get("/api/empresas/" + empresa + "/documentos").status()).isEqualTo(403);

		Cliente anonimo = new Cliente();
		assertThat(anonimo.get("/api/empresas/" + empresa + "/obrigacoes").status()).isEqualTo(401);
	}

	@Test
	void titularConsultaEExcluiAConta() throws Exception {
		Cliente carla = novoUsuario("Carla", "carla@teste.com");
		criarEmpresa(carla, "Estúdio da Carla");
		assertThat(carla.get("/api/conta/dados").corpo().get("vinculos").size()).isEqualTo(1);
		// Única proprietária não pode sair (RN12)
		assertThat(carla.enviar("POST", "/api/conta/exclusao", "{\"senha\":\"Senha123\"}").status()).isEqualTo(422);

		Cliente davi = novoUsuario("Davi", "davi@teste.com");
		assertThat(davi.enviar("POST", "/api/conta/exclusao", "{\"senha\":\"errada1\"}").status()).isEqualTo(403);
		assertThat(davi.enviar("POST", "/api/conta/exclusao", "{\"senha\":\"Senha123\"}").status()).isEqualTo(204);

		Cliente novaTentativa = new Cliente();
		novaTentativa.get("/api/auth/csrf");
		assertThat(novaTentativa.enviar("POST", "/api/auth/login",
				"{\"email\":\"davi@teste.com\",\"senha\":\"Senha123\"}").status()).isEqualTo(401);
	}

	private Cliente novoUsuario(String nome, String email) throws Exception {
		Cliente cliente = new Cliente();
		cliente.get("/api/auth/csrf");
		assertThat(cliente.enviar("POST", "/api/auth/cadastro", """
				{"nome":"%s","email":"%s","senha":"Senha123","aceiteTermos":true}""".formatted(nome, email))
				.status()).isEqualTo(201);
		assertThat(cliente.enviar("POST", "/api/auth/login", """
				{"email":"%s","senha":"Senha123"}""".formatted(email)).status()).isEqualTo(200);
		assertThat(cliente.get("/api/auth/me").texto("email")).isEqualTo(email);
		return cliente;
	}

	private static String criarEmpresa(Cliente cliente, String nome) throws Exception {
		Resposta empresa = cliente.enviar("POST", "/api/empresas",
				"{\"nome\":\"%s\",\"cnpj\":\"%s\"}".formatted(nome, cnpjValido()));
		assertThat(empresa.status()).isEqualTo(201);
		assertThat(empresa.texto("perfil")).isEqualTo("PROPRIETARIO");
		return empresa.texto("id");
	}

	// Convida, captura o link do e-mail e aceita com uma conta nova do mesmo e-mail (RN16)
	private Cliente aceitarConvite(Cliente dono, String base, String email, String perfil, String nome)
			throws Exception {
		assertThat(dono.enviar("POST", base + "/convites", "{\"email\":\"%s\",\"perfil\":\"%s\"}".formatted(email, perfil))
				.status()).isEqualTo(201);
		ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
		verify(emailService).enviarAposCommit(eq(email), anyString(), texto.capture());
		Matcher link = LINK_CONVITE.matcher(texto.getValue());
		assertThat(link.find()).isTrue();
		String token = link.group(1);

		Cliente visitante = new Cliente();
		Resposta convite = visitante.get("/api/convites/" + token);
		assertThat(convite.status()).isEqualTo(200);
		assertThat(convite.texto("email")).isEqualTo(email);

		Cliente convidado = novoUsuario(nome, email);
		Resposta aceite = convidado.enviar("POST", "/api/convites/" + token + "/aceite", null);
		assertThat(aceite.status()).isEqualTo(200);
		assertThat(aceite.texto("perfil")).isEqualTo(perfil);
		assertThat(convidado.enviar("POST", "/api/convites/" + token + "/aceite", null).status()).isEqualTo(422);
		return convidado;
	}

	private static String idDoMembro(JsonNode membros, String email) {
		for (JsonNode membro : membros) {
			if (membro.get("email").asString().equals(email)) {
				return membro.get("id").asString();
			}
		}
		throw new AssertionError("Membro não encontrado: " + email);
	}

	// CNPJ numérico válido e diferente a cada chamada, já que o banco do teste é compartilhado entre os casos
	private static String cnpjValido() {
		int[] digitos = new int[14];
		for (int i = 0; i < 8; i++) {
			digitos[i] = ThreadLocalRandom.current().nextInt(10);
		}
		digitos[11] = 1;
		digitos[12] = digitoVerificador(digitos, new int[] { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 });
		digitos[13] = digitoVerificador(digitos, new int[] { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 });
		StringBuilder cnpj = new StringBuilder();
		for (int digito : digitos) {
			cnpj.append(digito);
		}
		return cnpj.toString();
	}

	private static int digitoVerificador(int[] digitos, int[] pesos) {
		int soma = 0;
		for (int i = 0; i < pesos.length; i++) {
			soma += digitos[i] * pesos[i];
		}
		int resto = soma % 11;
		return resto < 2 ? 0 : 11 - resto;
	}

	private record Resposta(int status, JsonNode corpo) {

		String texto(String campo) {
			return corpo.get(campo).asString();
		}
	}

	// Navegador simplificado: guarda os cookies de sessão e envia o token CSRF
	private class Cliente {

		private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
		private final HttpClient http = HttpClient.newBuilder().cookieHandler(cookies).build();

		Resposta get(String caminho) throws IOException, InterruptedException {
			return resposta(http.send(requisicao(caminho).GET().build(), BodyHandlers.ofString()));
		}

		Resposta enviar(String metodo, String caminho, String json) throws IOException, InterruptedException {
			HttpRequest.Builder requisicao = requisicao(caminho).header("Content-Type", "application/json")
					.method(metodo, json != null ? BodyPublishers.ofString(json) : BodyPublishers.noBody());
			return resposta(http.send(requisicao.build(), BodyHandlers.ofString()));
		}

		Resposta multipart(String metodo, String caminho, Map<String, String> campos, String nomeArquivo,
				byte[] conteudo) throws IOException, InterruptedException {
			String fronteira = "fronteira" + UUID.randomUUID();
			ByteArrayOutputStream corpo = new ByteArrayOutputStream();
			for (Map.Entry<String, String> campo : new LinkedHashMap<>(campos).entrySet()) {
				corpo.write(("--" + fronteira + "\r\nContent-Disposition: form-data; name=\"" + campo.getKey()
						+ "\"\r\n\r\n" + campo.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
			}
			corpo.write(("--" + fronteira + "\r\nContent-Disposition: form-data; name=\"arquivo\"; filename=\""
					+ nomeArquivo + "\"\r\nContent-Type: application/octet-stream\r\n\r\n")
					.getBytes(StandardCharsets.UTF_8));
			corpo.write(conteudo);
			corpo.write(("\r\n--" + fronteira + "--\r\n").getBytes(StandardCharsets.UTF_8));
			HttpRequest requisicao = requisicao(caminho)
					.header("Content-Type", "multipart/form-data; boundary=" + fronteira)
					.method(metodo, BodyPublishers.ofByteArray(corpo.toByteArray())).build();
			return resposta(http.send(requisicao, BodyHandlers.ofString()));
		}

		HttpResponse<byte[]> baixar(String caminho) throws IOException, InterruptedException {
			return http.send(requisicao(caminho).GET().build(), BodyHandlers.ofByteArray());
		}

		private HttpRequest.Builder requisicao(String caminho) {
			HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho));
			cookies.getCookieStore().getCookies().stream()
					.filter(cookie -> cookie.getName().equals("XSRF-TOKEN")).findFirst()
					.map(HttpCookie::getValue)
					.ifPresent(token -> requisicao.header("X-XSRF-TOKEN", token));
			return requisicao;
		}

		private static Resposta resposta(HttpResponse<String> resposta) {
			String corpo = resposta.body();
			return new Resposta(resposta.statusCode(), corpo == null || corpo.isBlank() ? null : JSON.readTree(corpo));
		}
	}
}
