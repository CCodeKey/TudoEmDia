package ted.com.tudoemdia.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.pagamento.PagamentoRequest;
import ted.com.tudoemdia.dto.pagamento.PagamentoResponse;
import ted.com.tudoemdia.exception.ConflitoException;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Pagamento;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.PagamentoRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

// RF18 e RF19: RN66 a RN73
@Service
public class PagamentoService {

	private static final LocalDate INICIO_HISTORICO = LocalDate.of(1900, 1, 1);
	private static final LocalDate FIM_HISTORICO = LocalDate.of(9999, 12, 31);

	private final PagamentoRepository pagamentoRepository;
	private final ObrigacaoRepository obrigacaoRepository;
	private final ControleAcesso controleAcesso;
	private final Clock clock;

	public PagamentoService(PagamentoRepository pagamentoRepository, ObrigacaoRepository obrigacaoRepository,
			ControleAcesso controleAcesso, Clock clock) {
		this.pagamentoRepository = pagamentoRepository;
		this.obrigacaoRepository = obrigacaoRepository;
		this.controleAcesso = controleAcesso;
		this.clock = clock;
	}

	// Registro do pagamento e mudança da situação na mesma transação (RNF09)
	@Transactional
	public PagamentoResponse registrar(String empresaId, String obrigacaoId, PagamentoRequest dados) {
		controleAcesso.exigir(empresaId, Permissao.REGISTRAR_PAGAMENTOS);
		Obrigacao obrigacao = obrigacaoRepository.findByIdAndEmpresaId(obrigacaoId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Obrigação não encontrada."));
		// Sem pagamento parcial; no máximo um registro vigente (RN69)
		if (pagamentoRepository.existsByObrigacaoIdAndCanceladoFalse(obrigacaoId)) {
			throw new ConflitoException("Esta obrigação já possui pagamento registrado.");
		}
		if (dados.dataPagamento().isAfter(LocalDate.now(clock))) {
			throw new RegraNegocioException("A data de pagamento não pode ser futura.");
		}
		Pagamento pagamento = new Pagamento();
		pagamento.setObrigacao(obrigacao);
		pagamento.setUsuario(controleAcesso.referenciaUsuarioAtual());
		// Pode diferir do valor da obrigação por juros ou multa (RN68)
		pagamento.setValorPago(dados.valorPago() != null ? dados.valorPago() : obrigacao.getValor());
		pagamento.setDataPagamento(dados.dataPagamento());
		pagamentoRepository.saveAndFlush(pagamento);
		obrigacao.setStatus(StatusObrigacao.PAGA);
		return PagamentoResponse.de(pagamento);
	}

	// Só o proprietário cancela; o registro fica no histórico (RN70, RN73)
	@Transactional
	public PagamentoResponse cancelar(String empresaId, String pagamentoId) {
		controleAcesso.exigir(empresaId, Permissao.CANCELAR_PAGAMENTOS);
		Pagamento pagamento = pagamentoRepository.findByIdAndObrigacaoEmpresaId(pagamentoId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado."));
		if (pagamento.isCancelado()) {
			throw new RegraNegocioException("Este pagamento já foi cancelado.");
		}
		pagamento.setCancelado(true);
		pagamento.setUsuarioCancelamento(controleAcesso.referenciaUsuarioAtual());
		pagamento.setDataCancelamento(LocalDateTime.now(clock));

		// A situação volta a depender do vencimento (RN46)
		Obrigacao obrigacao = pagamento.getObrigacao();
		obrigacao.setStatus(Situacoes.daObrigacao(obrigacao.getDataVencimento(), false, LocalDate.now(clock)));
		return PagamentoResponse.de(pagamento);
	}

	// Período opcional, pela data do pagamento (RN71, RN72)
	@Transactional(readOnly = true)
	public List<PagamentoResponse> historico(String empresaId, LocalDate inicio, LocalDate fim) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_HISTORICO_PAGAMENTOS);
		return pagamentoRepository
				.findByObrigacaoEmpresaIdAndDataPagamentoBetweenOrderByDataPagamentoDescDataRegistroDesc(empresaId,
						inicio != null ? inicio : INICIO_HISTORICO, fim != null ? fim : FIM_HISTORICO)
				.stream().map(PagamentoResponse::de).toList();
	}
}
