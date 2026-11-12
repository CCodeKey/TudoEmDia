package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import ted.com.tudoemdia.model.enuns.StatusDocumento;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;

class SituacoesTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 11, 12);

	@Test
	void obrigacaoSemPagamentoVencendoHojeOuDepoisEstaPendente() {
		assertThat(Situacoes.daObrigacao(HOJE, false, HOJE)).isEqualTo(StatusObrigacao.PENDENTE);
		assertThat(Situacoes.daObrigacao(HOJE.plusDays(3), false, HOJE)).isEqualTo(StatusObrigacao.PENDENTE);
	}

	@Test
	void obrigacaoSemPagamentoComVencimentoPassadoEstaVencida() {
		assertThat(Situacoes.daObrigacao(HOJE.minusDays(1), false, HOJE)).isEqualTo(StatusObrigacao.VENCIDA);
	}

	@Test
	void obrigacaoComPagamentoEstaPagaIndependenteDoVencimento() {
		assertThat(Situacoes.daObrigacao(HOJE.minusDays(30), true, HOJE)).isEqualTo(StatusObrigacao.PAGA);
	}

	@Test
	void documentoSemValidadeOuDentroDelaEstaAtivo() {
		assertThat(Situacoes.doDocumento(null, false, HOJE)).isEqualTo(StatusDocumento.ATIVO);
		assertThat(Situacoes.doDocumento(HOJE, false, HOJE)).isEqualTo(StatusDocumento.ATIVO);
	}

	@Test
	void documentoComValidadePassadaEstaVencidoSalvoSeArquivado() {
		assertThat(Situacoes.doDocumento(HOJE.minusDays(1), false, HOJE)).isEqualTo(StatusDocumento.VENCIDO);
		assertThat(Situacoes.doDocumento(HOJE.minusDays(1), true, HOJE)).isEqualTo(StatusDocumento.ARQUIVADO);
	}
}
