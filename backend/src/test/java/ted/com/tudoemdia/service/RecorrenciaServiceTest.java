package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Recorrencia;
import ted.com.tudoemdia.model.enuns.Periodicidade;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.RecorrenciaRepository;
import ted.com.tudoemdia.security.ControleAcesso;

class RecorrenciaServiceTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 11, 12);

	private final ObrigacaoRepository obrigacaoRepository = mock(ObrigacaoRepository.class);
	private final RecorrenciaService recorrenciaService = new RecorrenciaService(mock(RecorrenciaRepository.class),
			obrigacaoRepository, mock(ExclusaoObrigacaoService.class), mock(CategoriaService.class),
			mock(ControleAcesso.class), Clock.systemDefaultZone());
	private final List<Obrigacao> geradas = new ArrayList<>();
	private final Recorrencia recorrencia = new Recorrencia();

	@BeforeEach
	void preparar() {
		recorrencia.setId("r1");
		recorrencia.setDescricao("Aluguel");
		recorrencia.setValor(new BigDecimal("1200.00"));
		recorrencia.setPeriodicidade(Periodicidade.MENSAL);
		when(obrigacaoRepository.save(any())).thenAnswer(invocacao -> {
			geradas.add(invocacao.getArgument(0));
			return invocacao.getArgument(0);
		});
	}

	@Test
	void vencimentoNoDia31VaiParaOUltimoDiaDosMesesMaisCurtos() {
		LocalDate inicio = LocalDate.of(2026, 1, 31);

		assertThat(RecorrenciaService.ocorrencia(inicio, Periodicidade.MENSAL, 1)).isEqualTo("2026-02-28");
		assertThat(RecorrenciaService.ocorrencia(inicio, Periodicidade.MENSAL, 2)).isEqualTo("2026-03-31");
		assertThat(RecorrenciaService.ocorrencia(inicio, Periodicidade.MENSAL, 3)).isEqualTo("2026-04-30");
	}

	@Test
	void recorrenciaAnualEm29DeFevereiro() {
		LocalDate inicio = LocalDate.of(2028, 2, 29);

		assertThat(RecorrenciaService.ocorrencia(inicio, Periodicidade.ANUAL, 1)).isEqualTo("2029-02-28");
		assertThat(RecorrenciaService.ocorrencia(inicio, Periodicidade.ANUAL, 4)).isEqualTo("2032-02-29");
	}

	@Test
	void recuperaOcorrenciasAtrasadasAteTerUmaFutura() {
		recorrencia.setDataInicio(LocalDate.of(2026, 9, 10));
		ultimaOcorrencia(LocalDate.of(2026, 9, 10));

		int quantidade = recorrenciaService.gerarOcorrencias(recorrencia, HOJE);

		assertThat(quantidade).isEqualTo(3);
		assertThat(geradas).extracting(Obrigacao::getDataVencimento)
				.containsExactly(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 11, 10), LocalDate.of(2026, 12, 10));
		assertThat(geradas).extracting(Obrigacao::getStatus)
				.containsExactly(StatusObrigacao.VENCIDA, StatusObrigacao.VENCIDA, StatusObrigacao.PENDENTE);
		assertThat(geradas.get(2).getDescricao()).isEqualTo("Aluguel");
	}

	@Test
	void naoGeraQuandoJaExisteOcorrenciaFutura() {
		recorrencia.setDataInicio(LocalDate.of(2026, 9, 10));
		ultimaOcorrencia(LocalDate.of(2026, 12, 10));

		assertThat(recorrenciaService.gerarOcorrencias(recorrencia, HOJE)).isZero();
		verify(obrigacaoRepository, never()).save(any());
	}

	@Test
	void paraNaDataFinal() {
		recorrencia.setDataInicio(LocalDate.of(2026, 9, 10));
		recorrencia.setDataFim(LocalDate.of(2026, 10, 31));
		ultimaOcorrencia(LocalDate.of(2026, 9, 10));

		recorrenciaService.gerarOcorrencias(recorrencia, HOJE);

		assertThat(geradas).extracting(Obrigacao::getDataVencimento).containsExactly(LocalDate.of(2026, 10, 10));
	}

	@Test
	void naoGeraParaRecorrenciaInativa() {
		recorrencia.setDataInicio(LocalDate.of(2026, 9, 10));
		recorrencia.setAtiva(false);

		assertThat(recorrenciaService.gerarOcorrencias(recorrencia, HOJE)).isZero();
	}

	private void ultimaOcorrencia(LocalDate vencimento) {
		Obrigacao ultima = new Obrigacao();
		ultima.setDataVencimento(vencimento);
		when(obrigacaoRepository.findFirstByRecorrenciaIdOrderByDataVencimentoDesc("r1")).thenReturn(Optional.of(ultima));
	}
}
