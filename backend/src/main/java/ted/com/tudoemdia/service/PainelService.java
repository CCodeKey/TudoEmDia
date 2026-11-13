package ted.com.tudoemdia.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.painel.FaixaIndicador;
import ted.com.tudoemdia.dto.painel.IndicadorResponse;
import ted.com.tudoemdia.dto.painel.ObrigacaoVencidaResponse;
import ted.com.tudoemdia.dto.painel.PainelResponse;
import ted.com.tudoemdia.dto.painel.TotalCategoriaResponse;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Pagamento;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

// RF22 a RF26: RN82 a RN90
@Service
public class PainelService {

	private final ObrigacaoRepository obrigacaoRepository;
	private final ObrigacaoService obrigacaoService;
	private final ControleAcesso controleAcesso;
	private final Clock clock;

	public PainelService(ObrigacaoRepository obrigacaoRepository, ObrigacaoService obrigacaoService,
			ControleAcesso controleAcesso, Clock clock) {
		this.obrigacaoRepository = obrigacaoRepository;
		this.obrigacaoService = obrigacaoService;
		this.controleAcesso = controleAcesso;
		this.clock = clock;
	}

	// Sem mês informado, abre no mês atual (RN83)
	@Transactional(readOnly = true)
	public PainelResponse gerar(String empresaId, YearMonth mes) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_PAINEL);
		LocalDate hoje = LocalDate.now(clock);
		YearMonth mesSelecionado = mes != null ? mes : YearMonth.from(hoje);
		LocalDate inicio = mesSelecionado.atDay(1);
		LocalDate fim = mesSelecionado.atEndOfMonth();

		BigDecimal totalAPagar = obrigacaoRepository.somarValor(empresaId,
				List.of(StatusObrigacao.PENDENTE, StatusObrigacao.VENCIDA), inicio, fim);

		List<Obrigacao> doMes = obrigacaoRepository.findByEmpresaIdAndDataVencimentoBetween(empresaId, inicio, fim);
		Map<String, Pagamento> pagamentos = obrigacaoService.pagamentosVigentes(doMes);
		BigDecimal totalPago = pagamentos.values().stream().map(Pagamento::getValorPago)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<ObrigacaoVencidaResponse> vencidas = obrigacaoRepository
				.findByEmpresaIdAndStatusOrderByDataVencimentoAsc(empresaId, StatusObrigacao.VENCIDA).stream()
				.map(obrigacao -> new ObrigacaoVencidaResponse(obrigacao.getId(), obrigacao.getDescricao(),
						obrigacao.getCategoria().getNome(), obrigacao.getValor(), obrigacao.getDataVencimento(),
						ChronoUnit.DAYS.between(obrigacao.getDataVencimento(), hoje)))
				.toList();

		List<TotalCategoriaResponse> porCategoria = obrigacaoRepository.somarPorCategoria(empresaId, inicio, fim)
				.stream().map(total -> new TotalCategoriaResponse(total.getCategoria(), total.getTotal())).toList();

		return new PainelResponse(mesSelecionado.toString(), totalAPagar, totalPago, vencidas, porCategoria,
				calcularIndicador(doMes, pagamentos, hoje, inicio, fim));
	}

	// Pagas até o vencimento ÷ obrigações com vencimento até hoje no mês × 100 (RN89, RN90)
	static IndicadorResponse calcularIndicador(List<Obrigacao> doMes, Map<String, Pagamento> pagamentos,
			LocalDate hoje, LocalDate inicio, LocalDate fim) {
		LocalDate limite = hoje.isBefore(fim) ? hoje : fim;
		List<Obrigacao> consideradas = doMes.stream()
				.filter(obrigacao -> !obrigacao.getDataVencimento().isBefore(inicio)
						&& !obrigacao.getDataVencimento().isAfter(limite))
				.toList();
		if (consideradas.isEmpty()) {
			return new IndicadorResponse(null, null, 0, 0);
		}
		long pagasEmDia = consideradas.stream().filter(obrigacao -> {
			Pagamento pagamento = pagamentos.get(obrigacao.getId());
			return pagamento != null && !pagamento.getDataPagamento().isAfter(obrigacao.getDataVencimento());
		}).count();
		long total = consideradas.size();
		BigDecimal percentual = BigDecimal.valueOf(pagasEmDia * 100)
				.divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
		return new IndicadorResponse(percentual, FaixaIndicador.para(pagasEmDia * 100.0 / total), pagasEmDia, total);
	}
}
