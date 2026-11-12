package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import ted.com.tudoemdia.dto.auth.LoginRequest;
import ted.com.tudoemdia.exception.MuitasTentativasException;
import ted.com.tudoemdia.exception.NaoAutenticadoException;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.repository.UsuarioRepository;
import ted.com.tudoemdia.security.UsuarioAutenticado;

class AuthServiceTest {

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
	private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final AuthService authService = new AuthService(usuarioRepository, passwordEncoder, clock);
	private final Usuario usuario = new Usuario();

	@BeforeEach
	void prepararUsuario() {
		usuario.setId("u1");
		usuario.setNome("Ana");
		usuario.setEmail("ana@empresa.com");
		usuario.setSenha("hash");
		when(usuarioRepository.findByEmailIgnoreCase("ana@empresa.com")).thenReturn(Optional.of(usuario));
	}

	@Test
	void autenticaComSenhaCorretaEZeraTentativas() {
		usuario.setTentativasLoginFalhas(3);
		when(passwordEncoder.matches("Senha123", "hash")).thenReturn(true);

		UsuarioAutenticado autenticado = authService.autenticar(new LoginRequest(" Ana@Empresa.com ", "Senha123"));

		assertThat(autenticado.id()).isEqualTo("u1");
		assertThat(usuario.getTentativasLoginFalhas()).isZero();
	}

	@Test
	void bloqueiaPor15MinutosAposCincoTentativasErradas() {
		when(passwordEncoder.matches(any(), any())).thenReturn(false);
		LoginRequest login = new LoginRequest("ana@empresa.com", "errada");

		for (int i = 0; i < AuthService.MAXIMO_TENTATIVAS; i++) {
			assertThatThrownBy(() -> authService.autenticar(login)).isInstanceOf(NaoAutenticadoException.class);
		}

		assertThat(usuario.getBloqueadoAte()).isEqualTo(LocalDateTime.now(clock).plusMinutes(15));
		when(passwordEncoder.matches(any(), any())).thenReturn(true);
		assertThatThrownBy(() -> authService.autenticar(login)).isInstanceOf(MuitasTentativasException.class);
	}

	@Test
	void mensagemNaoRevelaSeOErroEstaNoEmailOuNaSenha() {
		when(passwordEncoder.matches(any(), any())).thenReturn(false);

		assertThatThrownBy(() -> authService.autenticar(new LoginRequest("ana@empresa.com", "errada")))
				.hasMessage(AuthService.CREDENCIAIS_INVALIDAS);
		assertThatThrownBy(() -> authService.autenticar(new LoginRequest("ninguem@empresa.com", "errada")))
				.hasMessage(AuthService.CREDENCIAIS_INVALIDAS);
	}
}
