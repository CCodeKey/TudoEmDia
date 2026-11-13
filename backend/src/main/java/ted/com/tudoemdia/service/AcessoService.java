package ted.com.tudoemdia.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.empresa.VinculoAtualizacaoRequest;
import ted.com.tudoemdia.dto.empresa.VinculoResponse;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

// RF05: usuários vinculados à empresa e seus acessos
@Service
public class AcessoService {

	private final UsuarioEmpresaRepository usuarioEmpresaRepository;
	private final ControleAcesso controleAcesso;

	public AcessoService(UsuarioEmpresaRepository usuarioEmpresaRepository, ControleAcesso controleAcesso) {
		this.usuarioEmpresaRepository = usuarioEmpresaRepository;
		this.controleAcesso = controleAcesso;
	}

	// RN18
	@Transactional(readOnly = true)
	public List<VinculoResponse> listar(String empresaId) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_USUARIOS);
		return usuarioEmpresaRepository.findByEmpresaId(empresaId).stream()
				.map(VinculoResponse::de)
				.sorted(Comparator.comparing(VinculoResponse::nome, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	// RN19, RN20, RN21, RN75
	@Transactional
	public VinculoResponse atualizar(String empresaId, String vinculoId, VinculoAtualizacaoRequest dados) {
		if (dados.perfil() == null && dados.ativo() == null && dados.recebeAlertas() == null) {
			throw new RegraNegocioException("Informe ao menos uma alteração.");
		}
		if (dados.perfil() != null || dados.ativo() != null) {
			controleAcesso.exigir(empresaId, Permissao.GERENCIAR_ACESSOS);
		}
		if (dados.recebeAlertas() != null) {
			controleAcesso.exigir(empresaId, Permissao.CONFIGURAR_ALERTAS);
		}
		UsuarioEmpresa vinculo = usuarioEmpresaRepository.findByIdAndEmpresaId(vinculoId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado nesta empresa."));

		Perfil novoPerfil = dados.perfil() != null ? dados.perfil() : vinculo.getPerfil();
		boolean novoAtivo = dados.ativo() != null ? dados.ativo() : vinculo.isAtivo();
		boolean eraProprietarioAtivo = vinculo.isAtivo() && vinculo.getPerfil() == Perfil.PROPRIETARIO;
		boolean continuaProprietarioAtivo = novoAtivo && novoPerfil == Perfil.PROPRIETARIO;

		// Toda empresa mantém ao menos um proprietário ativo (RN12, RN20)
		if (eraProprietarioAtivo && !continuaProprietarioAtivo
				&& usuarioEmpresaRepository.countByEmpresaIdAndPerfilAndAtivoTrue(empresaId, Perfil.PROPRIETARIO) <= 1) {
			throw new RegraNegocioException("A empresa precisa ter pelo menos um proprietário ativo.");
		}

		// Desativar preserva o vínculo e o histórico do usuário (RN21)
		vinculo.setPerfil(novoPerfil);
		vinculo.setAtivo(novoAtivo);
		if (dados.recebeAlertas() != null) {
			vinculo.setRecebeAlertas(dados.recebeAlertas());
		}
		return VinculoResponse.de(vinculo);
	}
}
