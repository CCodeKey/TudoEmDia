package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import ted.com.tudoemdia.dto.alerta.ConfiguracaoAlertasRequest;
import ted.com.tudoemdia.dto.alerta.PrazoAlerta;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.AlertaEnviado;
import ted.com.tudoemdia.model.ConfiguracaoAlerta;
import ted.com.tudoemdia.model.Empresa;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.repository.AlertaEnviadoRepository;
import ted.com.tudoemdia.repository.ConfiguracaoAlertaRepository;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.repository.UsuarioRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

class AlertaServiceTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 11, 12);

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
	private final ConfiguracaoAlertaRepository configuracaoRepository = mock(ConfiguracaoAlertaRepository.class);
	private final AlertaEnviadoRepository alertaEnviadoRepository = mock(AlertaEnviadoRepository.class);
	private final ObrigacaoRepository obrigacaoRepository = mock(ObrigacaoRepository.class);
	private final UsuarioEmpresaRepository usuarioEmpresaRepository = mock(UsuarioEmpresaRepository.class);
	private final ControleAcesso controleAcesso = mock(ControleAcesso.class);
	private final EmailService emailService = mock(EmailService.class);
	private final TransactionTemplate transacao = mock(TransactionTemplate.class);
	private final AlertaService alertaService = criarServico(clock);
	private final Empresa empresa = new Empresa();

	@BeforeEach
	@SuppressWarnings("unchecked")
	void preparar() {
		empresa.setId("e1");
		empresa.setNome("Padaria");
		UsuarioEmpresa vinculo = new UsuarioEmpresa();
		vinculo.setEmpresa(empresa);
		when(controleAcesso.exigir("e1", Permissao.CONFIGURAR_ALERTAS)).thenReturn(vinculo);
		when(configuracaoRepository.findByEmpresaIdAndDiasAntecedencia(eq("e1"), anyInt())).thenReturn(Optional.empty());
		when(transacao.execute(any())).thenAnswer(
				invocacao -> ((TransactionCallback<Object>) invocacao.getArgument(0)).doInTransaction(null));
	}

	@Test
	void peloMenosUmPrazoPrecisaFicarAtivo() {
		ConfiguracaoAlertasRequest todosDesligados = new ConfiguracaoAlertasRequest(List.of(
				new PrazoAlerta(7, false), new PrazoAlerta(3, false), new PrazoAlerta(1, false), new PrazoAlerta(0, false)));

		assertThatThrownBy(() -> alertaService.configurar("e1", todosDesligados))
				.isInstanceOf(RegraNegocioException.class);
	}

	@Test
	void soAceitaOsPrazosDefinidos() {
		assertThatThrownBy(() -> alertaService.configurar("e1",
				new ConfiguracaoAlertasRequest(List.of(new PrazoAlerta(5, true)))))
				.isInstanceOf(RegraNegocioException.class);
	}

	@Test
	void desligaUmPrazoMantendoOsDemais() {
		List<PrazoAlerta> prazos = alertaService.configurar("e1",
				new ConfiguracaoAlertasRequest(List.of(new PrazoAlerta(7, false))));

		assertThat(prazos).containsExactly(new PrazoAlerta(7, false), new PrazoAlerta(3, true),
				new PrazoAlerta(1, true), new PrazoAlerta(0, true));
	}

	@Test
	void semEmailConfiguradoNaoProcessaAlertas() {
		when(emailService.habilitado()).thenReturn(false);

		alertaService.enviarAlertasDoDia();

		verifyNoInteractions(configuracaoRepository, obrigacaoRepository);
	}

	@Test
	void enviaUmaVezPorObrigacaoPrazoEDestinatario() {
		prepararAlertaDeTresDias();

		alertaService.enviarAlertasDoDia();
		verify(emailService, times(1)).enviar(eq("ana@teste.com"), anyString(), anyString());
		verify(alertaEnviadoRepository).saveAndFlush(any(AlertaEnviado.class));

		// Na segunda execução, o registro já existe (RN78)
		when(alertaEnviadoRepository.existsByObrigacaoIdAndUsuarioIdAndDiasAntecedencia("o1", "u1", 3)).thenReturn(true);
		alertaService.enviarAlertasDoDia();
		verify(emailService, times(1)).enviar(eq("ana@teste.com"), anyString(), anyString());
	}

	@Test
	void falhaNoEnvioNaoInterrompeOsDemaisAlertas() {
		prepararAlertaDeTresDias();
		doThrow(new IllegalStateException("SMTP fora do ar")).when(emailService).enviar(any(), any(), any());

		// A falha é contada e registrada em log, sem derrubar a rotina
		alertaService.enviarAlertasDoDia();

		verify(emailService).enviar(any(), any(), any());
	}

	@Test
	void obrigacaoCriadaDepoisDas8hRecebeOAlertaDoDiaNaHora() {
		prepararAlertaDeTresDias();
		List<ConfiguracaoAlerta> prazosDaEmpresa = configuracaoRepository.findByAtivoTrue();
		when(configuracaoRepository.findByEmpresaIdOrderByDiasAntecedenciaDesc("e1")).thenReturn(prazosDaEmpresa);

		// O relógio do teste marca 12h em Brasília: a rotina das 8h já passou
		alertaService.verificarAposAlteracao("e1");

		verify(emailService).enviar(eq("ana@teste.com"), anyString(), anyString());
	}

	@Test
	void antesDas8hDeixaParaARotinaDoDia() {
		prepararAlertaDeTresDias();
		Clock madrugada = Clock.fixed(Instant.parse("2026-11-12T09:00:00Z"), ZoneId.of("America/Sao_Paulo"));

		criarServico(madrugada).verificarAposAlteracao("e1");

		verifyNoInteractions(configuracaoRepository);
		verify(emailService, never()).enviar(any(), any(), any());
	}

	// O executor roda na mesma thread, para o teste esperar o envio
	private AlertaService criarServico(Clock relogio) {
		return new AlertaService(configuracaoRepository, alertaEnviadoRepository, obrigacaoRepository,
				usuarioEmpresaRepository, mock(UsuarioRepository.class), controleAcesso, emailService, transacao,
				Runnable::run, relogio, "http://localhost:5173");
	}

	private void prepararAlertaDeTresDias() {
		when(emailService.habilitado()).thenReturn(true);
		ConfiguracaoAlerta configuracao = new ConfiguracaoAlerta();
		configuracao.setEmpresa(empresa);
		configuracao.setDiasAntecedencia(3);
		when(configuracaoRepository.findByAtivoTrue()).thenReturn(List.of(configuracao));

		Obrigacao obrigacao = new Obrigacao();
		obrigacao.setId("o1");
		obrigacao.setDescricao("Aluguel");
		obrigacao.setValor(new BigDecimal("1500.00"));
		obrigacao.setDataVencimento(HOJE.plusDays(3));
		when(obrigacaoRepository.findByEmpresaIdAndStatusAndDataVencimento("e1", StatusObrigacao.PENDENTE,
				HOJE.plusDays(3))).thenReturn(List.of(obrigacao));

		Usuario ana = new Usuario();
		ana.setId("u1");
		ana.setEmail("ana@teste.com");
		UsuarioEmpresa destinatario = new UsuarioEmpresa();
		destinatario.setUsuario(ana);
		when(usuarioEmpresaRepository.findByEmpresaIdAndAtivoTrueAndRecebeAlertasTrue("e1"))
				.thenReturn(List.of(destinatario));
	}
}
