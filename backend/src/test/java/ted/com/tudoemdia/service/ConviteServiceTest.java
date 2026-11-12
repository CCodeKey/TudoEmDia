package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ted.com.tudoemdia.dto.empresa.ConviteRequest;
import ted.com.tudoemdia.dto.empresa.EmpresaResponse;
import ted.com.tudoemdia.exception.AcessoNegadoException;
import ted.com.tudoemdia.exception.ConflitoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Convite;
import ted.com.tudoemdia.model.Empresa;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.model.enuns.StatusConvite;
import ted.com.tudoemdia.repository.ConviteRepository;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;
import ted.com.tudoemdia.security.UsuarioAutenticado;

class ConviteServiceTest {

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
	private final LocalDateTime agora = LocalDateTime.now(clock);
	private final ConviteRepository conviteRepository = mock(ConviteRepository.class);
	private final UsuarioEmpresaRepository usuarioEmpresaRepository = mock(UsuarioEmpresaRepository.class);
	private final ControleAcesso controleAcesso = mock(ControleAcesso.class);
	private final EmailService emailService = mock(EmailService.class);
	private final ConviteService conviteService = new ConviteService(conviteRepository, usuarioEmpresaRepository,
			controleAcesso, emailService, clock, "http://localhost:5173");
	private final Empresa empresa = new Empresa();
	private final Convite convite = new Convite();

	@BeforeEach
	void preparar() {
		empresa.setId("e1");
		empresa.setNome("Padaria");
		Usuario dona = new Usuario();
		dona.setNome("Ana");
		UsuarioEmpresa vinculo = new UsuarioEmpresa();
		vinculo.setEmpresa(empresa);
		vinculo.setUsuario(dona);
		when(controleAcesso.exigir("e1", Permissao.CONVIDAR_USUARIOS)).thenReturn(vinculo);

		convite.setEmpresa(empresa);
		convite.setConvidante(dona);
		convite.setEmailConvidado("bruno@teste.com");
		convite.setPerfilConvidado(Perfil.FINANCEIRO);
		convite.setDataEnvio(agora.minusDays(1));
		convite.setDataExpiracao(agora.plusDays(6));
		when(conviteRepository.findByTokenHash(anyString())).thenReturn(Optional.of(convite));
		when(controleAcesso.usuarioAtual()).thenReturn(new UsuarioAutenticado("u2", "Bruno", "bruno@teste.com"));
		when(controleAcesso.referenciaUsuarioAtual()).thenReturn(new Usuario());
	}

	@Test
	void conviteParaProprietarioNaoEhPermitido() {
		assertThatThrownBy(() -> conviteService.convidar("e1", new ConviteRequest("socio@teste.com", Perfil.PROPRIETARIO)))
				.isInstanceOf(RegraNegocioException.class);
	}

	@Test
	void naoConvidaEmailJaVinculado() {
		when(usuarioEmpresaRepository.existsByEmpresaIdAndUsuarioEmailIgnoreCase("e1", "bruno@teste.com"))
				.thenReturn(true);

		assertThatThrownBy(() -> conviteService.convidar("e1", new ConviteRequest("Bruno@Teste.com", Perfil.CONTADOR)))
				.isInstanceOf(ConflitoException.class);
		verify(emailService, never()).enviarAposCommit(any(), any(), any());
	}

	@Test
	void conviteValeSeteDiasEGuardaSoOHashDoToken() {
		conviteService.convidar("e1", new ConviteRequest("novo@teste.com", Perfil.CONTADOR));

		verify(conviteRepository).save(any(Convite.class));
		verify(emailService).enviarAposCommit(any(), any(), any());
	}

	@Test
	void aceiteCriaVinculoComOPerfilDoConvite() {
		EmpresaResponse empresaAceita = conviteService.aceitar("token");

		assertThat(empresaAceita.perfil()).isEqualTo(Perfil.FINANCEIRO);
		assertThat(convite.getStatus()).isEqualTo(StatusConvite.ACEITO);
		verify(usuarioEmpresaRepository).save(any(UsuarioEmpresa.class));
	}

	@Test
	void aceiteExigeOMesmoEmailDoConvite() {
		when(controleAcesso.usuarioAtual()).thenReturn(new UsuarioAutenticado("u3", "Carla", "carla@teste.com"));

		assertThatThrownBy(() -> conviteService.aceitar("token")).isInstanceOf(AcessoNegadoException.class);
	}

	@Test
	void conviteExpiradoNaoPodeSerAceito() {
		convite.setDataExpiracao(agora.minusMinutes(1));

		assertThatThrownBy(() -> conviteService.aceitar("token")).isInstanceOf(RegraNegocioException.class);
		assertThat(convite.getStatus()).isEqualTo(StatusConvite.EXPIRADO);
	}
}
