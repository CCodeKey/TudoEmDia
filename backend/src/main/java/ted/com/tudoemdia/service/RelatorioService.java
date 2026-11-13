package ted.com.tudoemdia.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.obrigacao.FiltroObrigacoes;
import ted.com.tudoemdia.model.Empresa;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Pagamento;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;
import ted.com.tudoemdia.service.RelatorioPdf.Coluna;

// RF27: RN91 a RN94
@Service
public class RelatorioService {

	private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
	private static final List<Coluna> COLUNAS = List.of(
			new Coluna("Vencimento", 70, false),
			new Coluna("Descrição", 185, false),
			new Coluna("Categoria", 100, false),
			new Coluna("Situação", 95, false),
			new Coluna("Valor", 75, true),
			new Coluna("Pago em", 65, false),
			new Coluna("Valor pago", 75, true),
			new Coluna("Registrado por", 97, false));

	private final ObrigacaoService obrigacaoService;
	private final CategoriaService categoriaService;
	private final ControleAcesso controleAcesso;
	private final Clock clock;

	public RelatorioService(ObrigacaoService obrigacaoService, CategoriaService categoriaService,
			ControleAcesso controleAcesso, Clock clock) {
		this.obrigacaoService = obrigacaoService;
		this.categoriaService = categoriaService;
		this.controleAcesso = controleAcesso;
		this.clock = clock;
	}

	// Mesmos filtros da consulta, só com dados da empresa selecionada (RN92, RN94)
	@Transactional(readOnly = true)
	public byte[] obrigacoesEPagamentos(String empresaId, FiltroObrigacoes filtro) {
		UsuarioEmpresa vinculo = controleAcesso.exigir(empresaId, Permissao.EXPORTAR_RELATORIOS);
		List<Obrigacao> obrigacoes = obrigacaoService.buscarFiltradas(empresaId, filtro);
		Map<String, Pagamento> pagamentos = obrigacaoService.pagamentosVigentes(obrigacoes);
		NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

		List<List<String>> linhas = new ArrayList<>();
		BigDecimal totalValor = BigDecimal.ZERO;
		BigDecimal totalPago = BigDecimal.ZERO;
		for (Obrigacao obrigacao : obrigacoes) {
			Pagamento pagamento = pagamentos.get(obrigacao.getId());
			totalValor = totalValor.add(obrigacao.getValor());
			if (pagamento != null) {
				totalPago = totalPago.add(pagamento.getValorPago());
			}
			linhas.add(List.of(
					obrigacao.getDataVencimento().format(DATA),
					obrigacao.getDescricao(),
					obrigacao.getCategoria().getNome(),
					situacao(obrigacao, pagamento),
					moeda.format(obrigacao.getValor()),
					pagamento != null ? pagamento.getDataPagamento().format(DATA) : "",
					pagamento != null ? moeda.format(pagamento.getValorPago()) : "",
					pagamento != null ? pagamento.getUsuario().getNome() : ""));
		}
		List<String> totais = List.of("Total", obrigacoes.size() + " obrigação(ões)", "", "",
				moeda.format(totalValor), "", moeda.format(totalPago), "");

		// Empresa, período, data de geração e usuário (RN93)
		Empresa empresa = vinculo.getEmpresa();
		String categoria = filtro.categoriaId() != null
				? categoriaService.buscarDisponivel(empresaId, filtro.categoriaId()).getNome()
				: "Todas";
		List<String> informacoes = List.of(
				"Empresa: " + empresa.getNome() + " (CNPJ " + Cnpj.formatar(empresa.getCnpj()) + ")",
				"Período: " + periodo(filtro.inicio(), filtro.fim()),
				"Categoria: " + categoria + "   |   Situação: "
						+ (filtro.status() != null ? nomeSituacao(filtro.status()) : "Todas"),
				"Gerado em " + LocalDateTime.now(clock).format(DATA_HORA) + " por "
						+ vinculo.getUsuario().getNome());

		return RelatorioPdf.gerar("Relatório de obrigações e pagamentos", informacoes, COLUNAS, linhas, totais);
	}

	private static String periodo(LocalDate inicio, LocalDate fim) {
		if (inicio == null && fim == null) {
			return "todos os vencimentos";
		}
		if (inicio == null) {
			return "vencimentos até " + fim.format(DATA);
		}
		if (fim == null) {
			return "vencimentos a partir de " + inicio.format(DATA);
		}
		return inicio.format(DATA) + " a " + fim.format(DATA);
	}

	private static String situacao(Obrigacao obrigacao, Pagamento pagamento) {
		if (pagamento != null && pagamento.getDataPagamento().isAfter(obrigacao.getDataVencimento())) {
			return "Paga com atraso"; // RN47
		}
		return nomeSituacao(Objects.requireNonNull(obrigacao.getStatus()));
	}

	private static String nomeSituacao(StatusObrigacao status) {
		return switch (status) {
			case PENDENTE -> "Pendente";
			case PAGA -> "Paga";
			case VENCIDA -> "Vencida";
		};
	}
}
