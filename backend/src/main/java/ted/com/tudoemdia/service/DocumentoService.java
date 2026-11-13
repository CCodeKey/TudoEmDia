package ted.com.tudoemdia.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ted.com.tudoemdia.dto.ArquivoDownload;
import ted.com.tudoemdia.dto.documento.DocumentoAtualizacaoRequest;
import ted.com.tudoemdia.dto.documento.DocumentoRequest;
import ted.com.tudoemdia.dto.documento.DocumentoResponse;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;
import ted.com.tudoemdia.model.Documento;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.StatusDocumento;
import ted.com.tudoemdia.repository.DocumentoRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

// RF13 a RF15: RN48 a RN56
@Service
public class DocumentoService {

	private final DocumentoRepository documentoRepository;
	private final CategoriaService categoriaService;
	private final ArmazenamentoService armazenamentoService;
	private final ControleAcesso controleAcesso;
	private final Clock clock;

	public DocumentoService(DocumentoRepository documentoRepository, CategoriaService categoriaService,
			ArmazenamentoService armazenamentoService, ControleAcesso controleAcesso, Clock clock) {
		this.documentoRepository = documentoRepository;
		this.categoriaService = categoriaService;
		this.armazenamentoService = armazenamentoService;
		this.controleAcesso = controleAcesso;
		this.clock = clock;
	}

	// Filtros opcionais por categoria e situação (RN52)
	@Transactional(readOnly = true)
	public List<DocumentoResponse> listar(String empresaId, String categoriaId, StatusDocumento status) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_DOCUMENTOS);
		return documentoRepository.findByEmpresaIdOrderByDescricaoAsc(empresaId).stream()
				.filter(documento -> categoriaId == null || documento.getCategoria().getId().equals(categoriaId))
				.filter(documento -> status == null || documento.getStatus() == status)
				.map(DocumentoResponse::de)
				.toList();
	}

	@Transactional(readOnly = true)
	public DocumentoResponse detalhar(String empresaId, String documentoId) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_DOCUMENTOS);
		return DocumentoResponse.de(buscar(empresaId, documentoId));
	}

	@Transactional
	public DocumentoResponse criar(String empresaId, DocumentoRequest dados, MultipartFile arquivo) {
		UsuarioEmpresa vinculo = controleAcesso.exigir(empresaId, Permissao.CADASTRAR_DOCUMENTOS);
		Documento documento = new Documento();
		documento.setEmpresa(vinculo.getEmpresa());
		documento.setCategoria(categoriaService.buscarDisponivel(empresaId, dados.categoriaId()));
		documento.setDescricao(dados.descricao().trim());
		documento.setDataValidade(dados.dataValidade());
		documento.setStatus(Situacoes.doDocumento(dados.dataValidade(), false, LocalDate.now(clock)));
		documento.setNomeArquivo(armazenamentoService.nomeOriginal(arquivo));
		documento.setCaminhoArquivo(armazenamentoService.salvar(arquivo, "documentos/" + empresaId));
		return DocumentoResponse.de(documentoRepository.saveAndFlush(documento));
	}

	@Transactional
	public DocumentoResponse atualizar(String empresaId, String documentoId, DocumentoAtualizacaoRequest dados) {
		controleAcesso.exigir(empresaId, Permissao.EDITAR_DOCUMENTOS);
		Documento documento = buscar(empresaId, documentoId);
		documento.setCategoria(categoriaService.buscarDisponivel(empresaId, dados.categoriaId()));
		documento.setDescricao(dados.descricao().trim());
		documento.setDataValidade(dados.dataValidade());
		documento.setStatus(Situacoes.doDocumento(dados.dataValidade(), dados.arquivado(), LocalDate.now(clock)));
		documento.setUsuarioAtualizacao(controleAcesso.referenciaUsuarioAtual());
		return DocumentoResponse.de(documentoRepository.saveAndFlush(documento));
	}

	@Transactional
	public DocumentoResponse substituirArquivo(String empresaId, String documentoId, MultipartFile arquivo) {
		controleAcesso.exigir(empresaId, Permissao.EDITAR_DOCUMENTOS);
		Documento documento = buscar(empresaId, documentoId);
		String caminhoAnterior = documento.getCaminhoArquivo();
		documento.setNomeArquivo(armazenamentoService.nomeOriginal(arquivo));
		documento.setCaminhoArquivo(armazenamentoService.salvar(arquivo, "documentos/" + empresaId));
		documento.setUsuarioAtualizacao(controleAcesso.referenciaUsuarioAtual());
		armazenamentoService.removerAposCommit(caminhoAnterior);
		return DocumentoResponse.de(documentoRepository.saveAndFlush(documento));
	}

	// Sem link público: só usuário vinculado à empresa baixa o arquivo (RN53)
	@Transactional(readOnly = true)
	public ArquivoDownload baixar(String empresaId, String documentoId) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_DOCUMENTOS);
		Documento documento = buscar(empresaId, documentoId);
		return armazenamentoService.carregar(documento.getCaminhoArquivo(), documento.getNomeArquivo());
	}

	// A confirmação (RN55) é pedida pelo front antes da chamada
	@Transactional
	public void excluir(String empresaId, String documentoId) {
		controleAcesso.exigir(empresaId, Permissao.EXCLUIR_DOCUMENTOS);
		Documento documento = buscar(empresaId, documentoId);
		armazenamentoService.removerAposCommit(documento.getCaminhoArquivo());
		documentoRepository.delete(documento);
	}

	private Documento buscar(String empresaId, String documentoId) {
		return documentoRepository.findByIdAndEmpresaId(documentoId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Documento não encontrado."));
	}
}
