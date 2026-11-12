package ted.com.tudoemdia.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
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

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, Clock clock) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
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
		return UsuarioResponse.de(usuarioRepository.save(usuario));
	}

	public static String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
