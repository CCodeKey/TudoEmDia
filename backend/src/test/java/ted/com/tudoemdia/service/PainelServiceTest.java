package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ted.com.tudoemdia.dto.painel.FaixaIndicador;
import ted.com.tudoemdia.dto.painel.IndicadorResponse;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Pagamento;

class PainelServiceTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 11, 12);
	private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
	private static final LocalDate FIM = LocalDate.of(2026, 11, 30);

	@Test
	void indicadorConsideraSoAsObrigacoesVencidasAteHoje() {
		Obrigacao emDia = obrigacao("o1", 5);
		Obrigacao comAtraso = obrigacao("o2", 10);
		Obrigacao naoPaga = obrigacao("o3", 8);
		Obrigacao futura = obrigacao("o4", 20);
		Map<String, Pagamento> pagamentos = Map.of("o1", pagamento(5), "o2", pagamento(11));

		IndicadorResponse indicador = PainelService.calcularIndicador(List.of(emDia, comAtraso, naoPaga, futura),
				pagamentos, HOJE, INICIO, FIM);

		assertThat(indicador.total()).isEqualTo(3);
		assertThat(indicador.pagasEmDia()).isEqualTo(1);
		assertThat(indicador.percentual()).isEqualByComparingTo("33.3");
		assertThat(indicador.faixa()).isEqualTo(FaixaIndicador.VERMELHO);
	}

	@Test
	void mesFuturoNaoTemIndicador() {
		IndicadorResponse indicador = PainelService.calcularIndicador(List.of(obrigacao("o1", 5)), Map.of(),
				LocalDate.of(2026, 10, 20), INICIO, FIM);

		assertThat(indicador.percentual()).isNull();
		assertThat(indicador.faixa()).isNull();
	}

	@Test
	void faixasDoIndicador() {
		assertThat(FaixaIndicador.para(100)).isEqualTo(FaixaIndicador.VERDE);
		assertThat(FaixaIndicador.para(90)).isEqualTo(FaixaIndicador.VERDE);
		assertThat(FaixaIndicador.para(89.9)).isEqualTo(FaixaIndicador.AMARELO);
		assertThat(FaixaIndicador.para(70)).isEqualTo(FaixaIndicador.AMARELO);
		assertThat(FaixaIndicador.para(69.9)).isEqualTo(FaixaIndicador.VERMELHO);
	}

	private static Obrigacao obrigacao(String id, int diaVencimento) {
		Obrigacao obrigacao = new Obrigacao();
		obrigacao.setId(id);
		obrigacao.setDataVencimento(INICIO.withDayOfMonth(diaVencimento));
		return obrigacao;
	}

	private static Pagamento pagamento(int diaPagamento) {
		Pagamento pagamento = new Pagamento();
		pagamento.setDataPagamento(INICIO.withDayOfMonth(diaPagamento));
		return pagamento;
	}
}
