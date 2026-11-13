package ted.com.tudoemdia.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.categoria.CategoriaRequest;
import ted.com.tudoemdia.dto.categoria.CategoriaResponse;
import ted.com.tudoemdia.exception.ConflitoException;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Categoria;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.repository.CategoriaRepository;
import ted.com.tudoemdia.repository.DocumentoRepository;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.RecorrenciaRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

// RF11: RN40 a RN42
@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;
	private final ObrigacaoRepository obrigacaoRepository;
	private final DocumentoRepository documentoRepository;
	private final RecorrenciaRepository recorrenciaRepository;
	private final ControleAcesso controleAcesso;

	public CategoriaService(CategoriaRepository categoriaRepository, ObrigacaoRepository obrigacaoRepository,
			DocumentoRepository documentoRepository, RecorrenciaRepository recorrenciaRepository,
			ControleAcesso controleAcesso) {
		this.categoriaRepository = categoriaRepository;
		this.obrigacaoRepository = obrigacaoRepository;
		this.documentoRepository = documentoRepository;
		this.recorrenciaRepository = recorrenciaRepository;
		this.controleAcesso = controleAcesso;
	}

	// Categorias padrão do sistema mais as criadas pela empresa
	@Transactional(readOnly = true)
	public List<CategoriaResponse> listar(String empresaId) {
		controleAcesso.exigirVinculo(empresaId);
		return categoriaRepository.findDisponiveis(empresaId).stream().map(CategoriaResponse::de).toList();
	}

	@Transactional
	public CategoriaResponse criar(String empresaId, CategoriaRequest dados) {
		UsuarioEmpresa vinculo = controleAcesso.exigir(empresaId, Permissao.GERENCIAR_CATEGORIAS);
		String nome = dados.nome().trim();
		boolean jaExiste = categoriaRepository.findDisponiveis(empresaId).stream()
				.anyMatch(categoria -> categoria.getNome().equalsIgnoreCase(nome));
		if (jaExiste) {
			throw new ConflitoException("Já existe uma categoria com este nome.");
		}
		Categoria categoria = new Categoria();
		categoria.setEmpresa(vinculo.getEmpresa());
		categoria.setNome(nome);
		return CategoriaResponse.de(categoriaRepository.save(categoria));
	}

	@Transactional
	public void excluir(String empresaId, String categoriaId) {
		controleAcesso.exigir(empresaId, Permissao.GERENCIAR_CATEGORIAS);
		Categoria categoria = buscarDisponivel(empresaId, categoriaId);
		if (categoria.getEmpresa() == null) {
			throw new RegraNegocioException("As categorias padrão do sistema não podem ser excluídas.");
		}
		// RN42
		if (obrigacaoRepository.existsByCategoriaId(categoriaId) || documentoRepository.existsByCategoriaId(categoriaId)
				|| recorrenciaRepository.existsByCategoriaId(categoriaId)) {
			throw new ConflitoException("Uma categoria em uso não pode ser excluída.");
		}
		categoriaRepository.delete(categoria);
	}

	// Categoria que a empresa pode usar: padrão ou dela própria
	public Categoria buscarDisponivel(String empresaId, String categoriaId) {
		return categoriaRepository.findDisponivel(categoriaId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada."));
	}
}
