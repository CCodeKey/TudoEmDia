package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ted.com.tudoemdia.dto.pagamento.PagamentoRequest;
import ted.com.tudoemdia.dto.pagamento.PagamentoResponse;
import ted.com.tudoemdia.exception.ConflitoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Pagamento;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.PagamentoRepository;
import ted.com.tudoemdia.security.ControleAcesso;

class PagamentoServiceTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 11, 12);

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
	private final PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
	private final ObrigacaoRepository obrigacaoRepository = mock(ObrigacaoRepository.class);
	private final ControleAcesso controleAcesso = mock(ControleAcesso.class);
	private final PagamentoService pagamentoService = new PagamentoService(pagamentoRepository, obrigacaoRepository,
			controleAcesso, clock);
	private final Obrigacao obrigacao = new Obrigacao();
	private final Usuario usuario = new Usuario();

	@BeforeEach
	void preparar() {
		obrigacao.setId("o1");
		obrigacao.setDescricao("Aluguel");
		obrigacao.setValor(new BigDecimal("150.00"));
		obrigacao.setDataVencimento(HOJE.minusDays(2));
		obrigacao.setStatus(StatusObrigacao.VENCIDA);
		usuario.setNome("Ana");
		when(obrigacaoRepository.findByIdAndEmpresaId("o1", "e1")).thenReturn(Optional.of(obrigacao));
		when(controleAcesso.referenciaUsuarioAtual()).thenReturn(usuario);
		when(pagamentoRepository.saveAndFlush(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
	}

	@Test
	void registraComOValorDaObrigacaoEMarcaComoPaga() {
		PagamentoResponse resposta = pagamentoService.registrar("e1", "o1", new PagamentoRequest(HOJE, null));

		assertThat(resposta.valorPago()).isEqualByComparingTo("150.00");
		assertThat(resposta.pagoComAtraso()).isTrue();
		assertThat(resposta.registradoPor()).isEqualTo("Ana");
		assertThat(obrigacao.getStatus()).isEqualTo(StatusObrigacao.PAGA);
	}

	@Test
	void aceitaValorPagoDiferenteParaJurosOuMulta() {
		PagamentoResponse resposta = pagamentoService.registrar("e1", "o1",
				new PagamentoRequest(HOJE, new BigDecimal("162.30")));

		assertThat(resposta.valorPago()).isEqualByComparingTo("162.30");
	}

	@Test
	void recusaDataDePagamentoFutura() {
		assertThatThrownBy(() -> pagamentoService.registrar("e1", "o1", new PagamentoRequest(HOJE.plusDays(1), null)))
				.isInstanceOf(RegraNegocioException.class);
	}

	@Test
	void recusaSegundoPagamentoVigente() {
		when(pagamentoRepository.existsByObrigacaoIdAndCanceladoFalse("o1")).thenReturn(true);

		assertThatThrownBy(() -> pagamentoService.registrar("e1", "o1", new PagamentoRequest(HOJE, null)))
				.isInstanceOf(ConflitoException.class);
	}

	@Test
	void cancelamentoMantemORegistroERecalculaASituacao() {
		Pagamento pagamento = new Pagamento();
		pagamento.setObrigacao(obrigacao);
		pagamento.setUsuario(usuario);
		pagamento.setValorPago(obrigacao.getValor());
		pagamento.setDataPagamento(HOJE);
		obrigacao.setStatus(StatusObrigacao.PAGA);
		when(pagamentoRepository.findByIdAndObrigacaoEmpresaId("p1", "e1")).thenReturn(Optional.of(pagamento));

		PagamentoResponse resposta = pagamentoService.cancelar("e1", "p1");

		assertThat(resposta.cancelado()).isTrue();
		assertThat(resposta.canceladoPor()).isEqualTo("Ana");
		assertThat(obrigacao.getStatus()).isEqualTo(StatusObrigacao.VENCIDA);
	}
}
