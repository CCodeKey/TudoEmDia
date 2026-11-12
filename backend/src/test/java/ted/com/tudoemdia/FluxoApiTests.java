package ted.com.tudoemdia;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Fluxos completos pela API HTTP, com sessão, CSRF e banco Postgres real (descartável)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "app.armazenamento.diretorio=target/uploads-teste")
@Import(BancoDeTesteConfig.class)
class FluxoApiTests {

	private static final JsonMapper JSON = JsonMapper.builder().build();
	private static final LocalDate HOJE = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 1, 2, 3 };

	@Value("${local.server.port}")
	private int porta;

	@Test
	void proprietarioGerenciaObrigacoesPagamentosAnexosERelatorios() throws Exception {
		Cliente ana = novoUsuario("Ana", "ana@teste.com");
		String empresa = criarEmpresa(ana, "Padaria da Ana", "11.222.333/0001-81");
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
		Resposta anexo = ana.multipart(base + "/obrigacoes/" + luzId + "/anexos", "boleto.png", PNG);
		assertThat(anexo.status()).isEqualTo(201);
		HttpResponse<byte[]> download = ana.baixar(base + "/anexos/" + anexo.texto("id") + "/arquivo");
		assertThat(download.statusCode()).isEqualTo(200);
		assertThat(download.body()).isEqualTo(PNG);
		assertThat(ana.multipart(base + "/obrigacoes/" + luzId + "/anexos", "falso.pdf", PNG).status())
				.isEqualTo(422);

		// Painel, histórico e relatório
		Resposta painel = ana.get(base + "/painel?mes=" + YearMonth.from(HOJE));
		assertThat(painel.status()).isEqualTo(200);
		assertThat(painel.corpo().get("vencidas").size()).isGreaterThanOrEqualTo(1);
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
	void usuarioNaoAcessaDadosDeEmpresaSemVinculo() throws Exception {
		Cliente dona = novoUsuario("Beatriz", "beatriz@teste.com");
		String empresa = criarEmpresa(dona, "Loja da Beatriz", "11.444.777/0001-61");

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
		criarEmpresa(carla, "Estúdio da Carla", "12.ABC.345/01DE-35");
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

	private static String criarEmpresa(Cliente cliente, String nome, String cnpj) throws Exception {
		Resposta empresa = cliente.enviar("POST", "/api/empresas",
				"{\"nome\":\"%s\",\"cnpj\":\"%s\"}".formatted(nome, cnpj));
		assertThat(empresa.status()).isEqualTo(201);
		assertThat(empresa.texto("perfil")).isEqualTo("PROPRIETARIO");
		return empresa.texto("id");
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

		Resposta multipart(String caminho, String nomeArquivo, byte[] conteudo)
				throws IOException, InterruptedException {
			String fronteira = "fronteira" + UUID.randomUUID();
			ByteArrayOutputStream corpo = new ByteArrayOutputStream();
			corpo.write(("--" + fronteira + "\r\nContent-Disposition: form-data; name=\"arquivo\"; filename=\""
					+ nomeArquivo + "\"\r\nContent-Type: application/octet-stream\r\n\r\n")
					.getBytes(StandardCharsets.UTF_8));
			corpo.write(conteudo);
			corpo.write(("\r\n--" + fronteira + "--\r\n").getBytes(StandardCharsets.UTF_8));
			HttpRequest requisicao = requisicao(caminho)
					.header("Content-Type", "multipart/form-data; boundary=" + fronteira)
					.POST(BodyPublishers.ofByteArray(corpo.toByteArray())).build();
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
