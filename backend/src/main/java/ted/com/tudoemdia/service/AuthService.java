package ted.com.tudoemdia.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.auth.LoginRequest;
import ted.com.tudoemdia.exception.ApiException;
import ted.com.tudoemdia.exception.MuitasTentativasException;
import ted.com.tudoemdia.exception.NaoAutenticadoException;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.repository.UsuarioRepository;
import ted.com.tudoemdia.security.UsuarioAutenticado;

@Service
public class AuthService {

	// RN06
	static final int MAXIMO_TENTATIVAS = 5;
	static final Duration TEMPO_BLOQUEIO = Duration.ofMinutes(15);

	// Não informa se o erro está no e-mail ou na senha (RN08)
	static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos.";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, Clock clock) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
	}

	// As tentativas erradas são gravadas mesmo quando a autenticação falha (RN05, RN06)
	@Transactional(noRollbackFor = ApiException.class)
	public UsuarioAutenticado autenticar(LoginRequest dados) {
		LocalDateTime agora = LocalDateTime.now(clock);
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(UsuarioService.normalizarEmail(dados.email()))
				.orElseThrow(() -> new NaoAutenticadoException(CREDENCIAIS_INVALIDAS));

		if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(agora)) {
			throw new MuitasTentativasException("Muitas tentativas de acesso. Tente novamente em alguns minutos.");
		}
		if (!passwordEncoder.matches(dados.senha(), usuario.getSenha())) {
			registrarFalha(usuario, agora);
			throw new NaoAutenticadoException(CREDENCIAIS_INVALIDAS);
		}
		usuario.setTentativasLoginFalhas(0);
		usuario.setBloqueadoAte(null);
		return new UsuarioAutenticado(usuario.getId(), usuario.getNome(), usuario.getEmail());
	}

	private void registrarFalha(Usuario usuario, LocalDateTime agora) {
		int tentativas = usuario.getTentativasLoginFalhas() + 1;
		if (tentativas >= MAXIMO_TENTATIVAS) {
			usuario.setBloqueadoAte(agora.plus(TEMPO_BLOQUEIO));
			tentativas = 0;
		}
		usuario.setTentativasLoginFalhas(tentativas);
	}
}
