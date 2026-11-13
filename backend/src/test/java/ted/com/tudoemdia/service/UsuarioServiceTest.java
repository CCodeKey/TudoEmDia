package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import ted.com.tudoemdia.dto.auth.CadastroRequest;
import ted.com.tudoemdia.exception.ConflitoException;
import ted.com.tudoemdia.repository.UsuarioRepository;

class UsuarioServiceTest {

	private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final EmailService emailService = mock(EmailService.class);
	private final UsuarioService usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, emailService,
			Clock.fixed(Instant.parse("2026-11-13T15:00:00Z"), ZoneId.of("America/Sao_Paulo")), "http://localhost:5173");

	@Test
	void cadastroEnviaEmailDeBoasVindasSemASenha() {
		when(passwordEncoder.encode("Senha123")).thenReturn("hash");

		usuarioService.cadastrar(new CadastroRequest("Hellen Lima", " Hellen@Teste.com ", "Senha123", true));

		ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
		verify(emailService).enviarAposCommit(eq("hellen@teste.com"), eq(UsuarioService.ASSUNTO_BOAS_VINDAS),
				texto.capture());
		assertThat(texto.getValue()).contains("Olá, Hellen!", "http://localhost:5173").doesNotContain("Senha123");
	}

	@Test
	void emailJaCadastradoNaoRecebeBoasVindasDeNovo() {
		when(usuarioRepository.existsByEmailIgnoreCase("hellen@teste.com")).thenReturn(true);

		assertThatThrownBy(() -> usuarioService
				.cadastrar(new CadastroRequest("Hellen", "hellen@teste.com", "Senha123", true)))
				.isInstanceOf(ConflitoException.class);
		verify(emailService, never()).enviarAposCommit(anyString(), anyString(), any());
	}
}
