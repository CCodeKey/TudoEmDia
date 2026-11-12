package ted.com.tudoemdia.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ted.com.tudoemdia.dto.ArquivoDownload;
import ted.com.tudoemdia.dto.obrigacao.AnexoResponse;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;
import ted.com.tudoemdia.model.AnexoObrigacao;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.repository.AnexoObrigacaoRepository;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

// Anexos de obrigações, como boletos e guias (RF07, RN28)
@Service
public class AnexoService {

	private final AnexoObrigacaoRepository anexoRepository;
	private final ObrigacaoRepository obrigacaoRepository;
	private final ArmazenamentoService armazenamentoService;
	private final ControleAcesso controleAcesso;

	public AnexoService(AnexoObrigacaoRepository anexoRepository, ObrigacaoRepository obrigacaoRepository,
			ArmazenamentoService armazenamentoService, ControleAcesso controleAcesso) {
		this.anexoRepository = anexoRepository;
		this.obrigacaoRepository = obrigacaoRepository;
		this.armazenamentoService = armazenamentoService;
		this.controleAcesso = controleAcesso;
	}

	@Transactional(readOnly = true)
	public List<AnexoResponse> listar(String empresaId, String obrigacaoId) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_OBRIGACOES);
		buscarObrigacao(empresaId, obrigacaoId);
		return anexoRepository.findByObrigacaoId(obrigacaoId).stream().map(AnexoResponse::de).toList();
	}

	@Transactional
	public AnexoResponse adicionar(String empresaId, String obrigacaoId, MultipartFile arquivo) {
		controleAcesso.exigir(empresaId, Permissao.EDITAR_OBRIGACOES);
		Obrigacao obrigacao = buscarObrigacao(empresaId, obrigacaoId);
		AnexoObrigacao anexo = new AnexoObrigacao();
		anexo.setObrigacao(obrigacao);
		anexo.setNomeArquivo(armazenamentoService.nomeOriginal(arquivo));
		anexo.setCaminhoArquivo(armazenamentoService.salvar(arquivo, "obrigacoes/" + empresaId));
		return AnexoResponse.de(anexoRepository.saveAndFlush(anexo));
	}

	// Sem link público: só usuário vinculado à empresa baixa o arquivo (RN53)
	@Transactional(readOnly = true)
	public ArquivoDownload baixar(String empresaId, String anexoId) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_OBRIGACOES);
		AnexoObrigacao anexo = buscarAnexo(empresaId, anexoId);
		return armazenamentoService.carregar(anexo.getCaminhoArquivo(), anexo.getNomeArquivo());
	}

	@Transactional
	public void remover(String empresaId, String anexoId) {
		controleAcesso.exigir(empresaId, Permissao.EDITAR_OBRIGACOES);
		AnexoObrigacao anexo = buscarAnexo(empresaId, anexoId);
		armazenamentoService.removerAposCommit(anexo.getCaminhoArquivo());
		anexoRepository.delete(anexo);
	}

	private Obrigacao buscarObrigacao(String empresaId, String obrigacaoId) {
		return obrigacaoRepository.findByIdAndEmpresaId(obrigacaoId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Obrigação não encontrada."));
	}

	private AnexoObrigacao buscarAnexo(String empresaId, String anexoId) {
		return anexoRepository.findByIdAndObrigacaoEmpresaId(anexoId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Anexo não encontrado."));
	}
}
