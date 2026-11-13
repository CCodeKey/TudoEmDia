package ted.com.tudoemdia.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.empresa.ConvitePublicoResponse;
import ted.com.tudoemdia.dto.empresa.ConviteRequest;
import ted.com.tudoemdia.dto.empresa.ConviteResponse;
import ted.com.tudoemdia.dto.empresa.EmpresaResponse;
import ted.com.tudoemdia.exception.AcessoNegadoException;
import ted.com.tudoemdia.exception.ApiException;
import ted.com.tudoemdia.exception.ConflitoException;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Convite;
import ted.com.tudoemdia.model.Empresa;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.model.enuns.StatusConvite;
import ted.com.tudoemdia.repository.ConviteRepository;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;
import ted.com.tudoemdia.security.UsuarioAutenticado;

// RF04: RN13 a RN17
@Service
public class ConviteService {

	static final Duration VALIDADE = Duration.ofDays(7); // RN15
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final SecureRandom geradorToken = new SecureRandom();
	private final ConviteRepository conviteRepository;
	private final UsuarioEmpresaRepository usuarioEmpresaRepository;
	private final ControleAcesso controleAcesso;
	private final EmailService emailService;
	private final Clock clock;
	private final String frontendUrl;

	public ConviteService(ConviteRepository conviteRepository, UsuarioEmpresaRepository usuarioEmpresaRepository,
			ControleAcesso controleAcesso, EmailService emailService, Clock clock,
			@Value("${app.frontend-url}") String frontendUrl) {
		this.conviteRepository = conviteRepository;
		this.usuarioEmpresaRepository = usuarioEmpresaRepository;
		this.controleAcesso = controleAcesso;
		this.emailService = emailService;
		this.clock = clock;
		this.frontendUrl = frontendUrl;
	}

	@Transactional
	public ConviteResponse convidar(String empresaId, ConviteRequest dados) {
		UsuarioEmpresa vinculo = controleAcesso.exigir(empresaId, Permissao.CONVIDAR_USUARIOS);
		if (dados.perfil() == Perfil.PROPRIETARIO) {
			throw new RegraNegocioException("O convite deve ser para o perfil financeiro ou contador.");
		}
		String email = UsuarioService.normalizarEmail(dados.email());
		LocalDateTime agora = LocalDateTime.now(clock);
		if (usuarioEmpresaRepository.existsByEmpresaIdAndUsuarioEmailIgnoreCase(empresaId, email)) {
			throw new ConflitoException("Este e-mail já está vinculado à empresa.");
		}
		if (conviteRepository.existsByEmpresaIdAndEmailConvidadoIgnoreCaseAndStatusAndDataExpiracaoAfter(empresaId,
				email, StatusConvite.PENDENTE, agora)) {
			throw new ConflitoException("Já existe um convite pendente para este e-mail.");
		}

		// Só o hash do token fica no banco; o token vai apenas no link do e-mail
		String token = gerarToken();
		Empresa empresa = vinculo.getEmpresa();
		Convite convite = new Convite();
		convite.setEmpresa(empresa);
		convite.setConvidante(vinculo.getUsuario());
		convite.setEmailConvidado(email);
		convite.setPerfilConvidado(dados.perfil());
		convite.setDataEnvio(agora);
		convite.setDataExpiracao(agora.plus(VALIDADE));
		convite.setTokenHash(hash(token));
		conviteRepository.save(convite);

		emailService.enviarAposCommit(email, "Convite para acessar " + empresa.getNome() + " no TudoEmDia",
				textoConvite(convite, token));
		return ConviteResponse.de(convite, convite.getStatus());
	}

	@Transactional(readOnly = true)
	public List<ConviteResponse> listar(String empresaId) {
		controleAcesso.exigir(empresaId, Permissao.CONVIDAR_USUARIOS);
		LocalDateTime agora = LocalDateTime.now(clock);
		return conviteRepository.findByEmpresaIdOrderByDataEnvioDesc(empresaId).stream()
				.map(convite -> ConviteResponse.de(convite, statusAtual(convite, agora)))
				.toList();
	}

	@Transactional
	public void cancelar(String empresaId, String conviteId) {
		controleAcesso.exigir(empresaId, Permissao.CONVIDAR_USUARIOS);
		Convite convite = conviteRepository.findByIdAndEmpresaId(conviteId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Convite não encontrado."));
		if (statusAtual(convite, LocalDateTime.now(clock)) != StatusConvite.PENDENTE) {
			throw new RegraNegocioException("Somente convites pendentes podem ser cancelados.");
		}
		convite.setStatus(StatusConvite.CANCELADO);
	}

	// Consulta pública pelo link, para a tela de aceite ou de cadastro (RN16)
	@Transactional(readOnly = true)
	public ConvitePublicoResponse consultar(String token) {
		Convite convite = buscarPorToken(token);
		return new ConvitePublicoResponse(convite.getEmpresa().getNome(), convite.getEmailConvidado(),
				convite.getPerfilConvidado(), statusAtual(convite, LocalDateTime.now(clock)),
				convite.getDataExpiracao());
	}

	// O convite expirado é marcado mesmo quando o aceite é recusado
	@Transactional(noRollbackFor = ApiException.class)
	public EmpresaResponse aceitar(String token) {
		UsuarioAutenticado usuario = controleAcesso.usuarioAtual();
		Convite convite = buscarPorToken(token);
		StatusConvite status = statusAtual(convite, LocalDateTime.now(clock));
		if (status == StatusConvite.EXPIRADO && convite.getStatus() == StatusConvite.PENDENTE) {
			convite.setStatus(StatusConvite.EXPIRADO);
		}
		if (status != StatusConvite.PENDENTE) {
			throw new RegraNegocioException(status == StatusConvite.EXPIRADO
					? "Este convite expirou. Peça um novo convite ao proprietário."
					: "Este convite não está mais disponível.");
		}
		// A conta precisa ter o mesmo e-mail do convite (RN16)
		if (!convite.getEmailConvidado().equalsIgnoreCase(usuario.email())) {
			throw new AcessoNegadoException("Este convite foi enviado para outro e-mail.");
		}
		String empresaId = convite.getEmpresa().getId();
		if (usuarioEmpresaRepository.existsByEmpresaIdAndUsuarioEmailIgnoreCase(empresaId, usuario.email())) {
			throw new ConflitoException("Você já está vinculado a esta empresa.");
		}
		UsuarioEmpresa vinculo = new UsuarioEmpresa();
		vinculo.setUsuario(controleAcesso.referenciaUsuarioAtual());
		vinculo.setEmpresa(convite.getEmpresa());
		vinculo.setPerfil(convite.getPerfilConvidado());
		usuarioEmpresaRepository.save(vinculo);
		convite.setStatus(StatusConvite.ACEITO);
		return EmpresaResponse.de(vinculo);
	}

	private Convite buscarPorToken(String token) {
		return conviteRepository.findByTokenHash(hash(token))
				.orElseThrow(() -> new RecursoNaoEncontradoException("Convite não encontrado."));
	}

	// A expiração vale mesmo antes de a rotina diária marcar o convite
	private static StatusConvite statusAtual(Convite convite, LocalDateTime agora) {
		if (convite.getStatus() == StatusConvite.PENDENTE && convite.getDataExpiracao().isBefore(agora)) {
			return StatusConvite.EXPIRADO;
		}
		return convite.getStatus();
	}

	private String textoConvite(Convite convite, String token) {
		String perfil = convite.getPerfilConvidado() == Perfil.FINANCEIRO ? "financeiro" : "contador";
		return """
				Olá!

				%s convidou você para acessar a empresa %s no TudoEmDia, com o perfil %s.

				Para aceitar, acesse o link abaixo. Se ainda não tiver conta, cadastre-se com este mesmo e-mail.
				%s/convites/%s

				O convite é válido até %s.
				""".formatted(convite.getConvidante().getNome(), convite.getEmpresa().getNome(), perfil, frontendUrl,
				token, convite.getDataExpiracao().format(FORMATO_DATA));
	}

	private String gerarToken() {
		byte[] bytes = new byte[32];
		geradorToken.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
