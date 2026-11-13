package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import ted.com.tudoemdia.dto.obrigacao.EscopoExclusao;
import ted.com.tudoemdia.dto.obrigacao.FiltroObrigacoes;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoAtualizacaoRequest;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoRequest;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoResponse;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.AnexoObrigacao;
import ted.com.tudoemdia.model.Categoria;
import ted.com.tudoemdia.model.Empresa;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Pagamento;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.repository.AlertaEnviadoRepository;
import ted.com.tudoemdia.repository.AnexoObrigacaoRepository;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.PagamentoRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

class ObrigacaoServiceTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 11, 12);

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
	private final ObrigacaoRepository obrigacaoRepository = mock(ObrigacaoRepository.class);
	private final PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
	private final AnexoObrigacaoRepository anexoRepository = mock(AnexoObrigacaoRepository.class);
	private final ArmazenamentoService armazenamentoService = mock(ArmazenamentoService.class);
	private final CategoriaService categoriaService = mock(CategoriaService.class);
	private final ControleAcesso controleAcesso = mock(ControleAcesso.class);
	private final ExclusaoObrigacaoService exclusaoService = new ExclusaoObrigacaoService(obrigacaoRepository,
			pagamentoRepository, anexoRepository, mock(AlertaEnviadoRepository.class), armazenamentoService);
	private final ObrigacaoService obrigacaoService = new ObrigacaoService(obrigacaoRepository, pagamentoRepository,
			anexoRepository, categoriaService, mock(RecorrenciaService.class), exclusaoService, mock(AlertaService.class),
			controleAcesso, clock);
	private final Categoria categoria = new Categoria();
	private final Obrigacao obrigacao = new Obrigacao();

	@BeforeEach
	void preparar() {
		categoria.setId("c1");
		categoria.setNome("Impostos");
		UsuarioEmpresa vinculo = new UsuarioEmpresa();
		vinculo.setEmpresa(new Empresa());
		when(controleAcesso.exigir(any(), any(Permissao.class))).thenReturn(vinculo);
		when(categoriaService.buscarDisponivel("e1", "c1")).thenReturn(categoria);

		obrigacao.setId("o1");
		obrigacao.setCategoria(categoria);
		obrigacao.setDescricao("DAS");
		obrigacao.setValor(new BigDecimal("80.00"));
		obrigacao.setDataVencimento(HOJE.minusDays(5));
		obrigacao.setStatus(StatusObrigacao.PAGA);
		when(obrigacaoRepository.findByIdAndEmpresaId("o1", "e1")).thenReturn(Optional.of(obrigacao));
	}

	@Test
	void vencimentoPassadoJaNasceVencida() {
		ObrigacaoResponse criada = obrigacaoService.criar("e1",
				new ObrigacaoRequest(" DAS ", new BigDecimal("80.00"), "c1", HOJE.minusDays(1), null));

		assertThat(criada.status()).isEqualTo(StatusObrigacao.VENCIDA);
		assertThat(criada.descricao()).isEqualTo("DAS");
	}

	@Test
	void obrigacaoPagaNaoTemValorAlterado() {
		when(pagamentoRepository.findByObrigacaoIdAndCanceladoFalse("o1")).thenReturn(Optional.of(pagamento()));

		assertThatThrownBy(() -> obrigacaoService.atualizar("e1", "o1",
				new ObrigacaoAtualizacaoRequest("DAS", new BigDecimal("90.00"), "c1", obrigacao.getDataVencimento())))
				.isInstanceOf(RegraNegocioException.class);
	}

	@Test
	void obrigacaoPagaPodeTerADescricaoEditada() {
		when(pagamentoRepository.findByObrigacaoIdAndCanceladoFalse("o1")).thenReturn(Optional.of(pagamento()));

		ObrigacaoResponse editada = obrigacaoService.atualizar("e1", "o1", new ObrigacaoAtualizacaoRequest(
				"DAS de outubro", new BigDecimal("80.0"), "c1", obrigacao.getDataVencimento()));

		assertThat(editada.descricao()).isEqualTo("DAS de outubro");
		assertThat(editada.status()).isEqualTo(StatusObrigacao.PAGA);
	}

	@Test
	void obrigacaoComHistoricoDePagamentoNaoEhExcluida() {
		when(pagamentoRepository.existsByObrigacaoId("o1")).thenReturn(true);

		assertThatThrownBy(() -> obrigacaoService.excluir("e1", "o1", EscopoExclusao.SOMENTE_ESTA))
				.isInstanceOf(RegraNegocioException.class);
		verify(obrigacaoRepository, never()).delete(any(Obrigacao.class));
	}

	@Test
	void exclusaoRemoveAnexosDoDiscoAposConfirmar() {
		AnexoObrigacao anexo = new AnexoObrigacao();
		anexo.setCaminhoArquivo("obrigacoes/e1/boleto.pdf");
		when(anexoRepository.findByObrigacaoId("o1")).thenReturn(List.of(anexo));

		obrigacaoService.excluir("e1", "o1", EscopoExclusao.SOMENTE_ESTA);

		verify(armazenamentoService).removerAposCommit("obrigacoes/e1/boleto.pdf");
		verify(obrigacaoRepository).delete(obrigacao);
	}

	@Test
	void periodoComInicioDepoisDoFimEhRecusado() {
		assertThatThrownBy(() -> obrigacaoService.listar("e1", new FiltroObrigacoes(HOJE, HOJE.minusDays(1), null, null)))
				.isInstanceOf(RegraNegocioException.class);
	}

	private Pagamento pagamento() {
		Pagamento pagamento = new Pagamento();
		pagamento.setObrigacao(obrigacao);
		pagamento.setDataPagamento(HOJE.minusDays(5));
		return pagamento;
	}
}
