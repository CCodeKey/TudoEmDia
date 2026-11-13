package ted.com.tudoemdia.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.auth.CadastroRequest;
import ted.com.tudoemdia.dto.auth.UsuarioResponse;
import ted.com.tudoemdia.exception.ConflitoException;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.repository.UsuarioRepository;

@Service
public class UsuarioService {

	static final String ASSUNTO_BOAS_VINDAS = "Boas-vindas ao TudoEmDia";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final Clock clock;
	private final String frontendUrl;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
			EmailService emailService, Clock clock, @Value("${app.frontend-url}") String frontendUrl) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
		this.clock = clock;
		this.frontendUrl = frontendUrl;
	}

	// RF01: RN01 a RN04
	@Transactional
	public UsuarioResponse cadastrar(CadastroRequest dados) {
		String email = normalizarEmail(dados.email());
		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			throw new ConflitoException("Já existe uma conta com este e-mail.");
		}
		Usuario usuario = new Usuario();
		usuario.setNome(dados.nome().trim());
		usuario.setEmail(email);
		usuario.setSenha(passwordEncoder.encode(dados.senha()));
		// Registro do consentimento (RNF14)
		usuario.setDataAceiteTermos(LocalDateTime.now(clock));
		usuarioRepository.save(usuario);

		// Só sai depois que a conta for gravada; uma falha no envio não desfaz o cadastro
		emailService.enviarAposCommit(email, ASSUNTO_BOAS_VINDAS, textoBoasVindas(usuario.getNome()));
		return UsuarioResponse.de(usuario);
	}

	public static String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String textoBoasVindas(String nome) {
		String primeiroNome = nome.split("\\s+")[0];
		return """
				Olá, %s!

				Sua conta no TudoEmDia foi criada. Agora as obrigações da sua empresa podem ficar todas em um só lugar.

				Para começar:
				1. Cadastre sua empresa com o CNPJ. Você será o proprietário.
				2. Lance as contas, impostos e boletos, com o vencimento de cada um.
				3. Convide sua equipe e seu contador, cada um com o perfil certo.

				Você receberá alertas por e-mail 7, 3 e 1 dia antes de cada vencimento e no próprio dia.

				Acesse: %s

				Se você não criou esta conta, ignore este e-mail.

				Equipe TudoEmDia
				""".formatted(primeiroNome, frontendUrl);
	}
}
