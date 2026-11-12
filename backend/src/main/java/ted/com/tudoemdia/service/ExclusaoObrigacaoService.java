package ted.com.tudoemdia.service;

import org.springframework.stereotype.Service;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.AnexoObrigacao;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.repository.AlertaEnviadoRepository;
import ted.com.tudoemdia.repository.AnexoObrigacaoRepository;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.PagamentoRepository;

// Remoção de uma obrigação e dos registros dependentes; usado na exclusão e na alteração de recorrências
@Service
public class ExclusaoObrigacaoService {

	private final ObrigacaoRepository obrigacaoRepository;
	private final PagamentoRepository pagamentoRepository;
	private final AnexoObrigacaoRepository anexoRepository;
	private final AlertaEnviadoRepository alertaEnviadoRepository;
	private final ArmazenamentoService armazenamentoService;

	public ExclusaoObrigacaoService(ObrigacaoRepository obrigacaoRepository, PagamentoRepository pagamentoRepository,
			AnexoObrigacaoRepository anexoRepository, AlertaEnviadoRepository alertaEnviadoRepository,
			ArmazenamentoService armazenamentoService) {
		this.obrigacaoRepository = obrigacaoRepository;
		this.pagamentoRepository = pagamentoRepository;
		this.anexoRepository = anexoRepository;
		this.alertaEnviadoRepository = alertaEnviadoRepository;
		this.armazenamentoService = armazenamentoService;
	}

	public boolean podeRemover(Obrigacao obrigacao) {
		return !pagamentoRepository.existsByObrigacaoId(obrigacao.getId());
	}

	// Deve ser chamado dentro de uma transação
	public void remover(Obrigacao obrigacao) {
		// Pagamentos, mesmo cancelados, fazem parte do histórico (RN38, RN73)
		if (!podeRemover(obrigacao)) {
			throw new RegraNegocioException(
					"Obrigação com pagamento registrado não pode ser excluída, para preservar o histórico.");
		}
		for (AnexoObrigacao anexo : anexoRepository.findByObrigacaoId(obrigacao.getId())) {
			armazenamentoService.removerAposCommit(anexo.getCaminhoArquivo());
			anexoRepository.delete(anexo);
		}
		alertaEnviadoRepository.deleteByObrigacaoId(obrigacao.getId());
		obrigacaoRepository.delete(obrigacao);
	}
}
