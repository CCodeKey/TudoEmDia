package ted.com.tudoemdia.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ted.com.tudoemdia.exception.AcessoNegadoException;
import ted.com.tudoemdia.exception.NaoAutenticadoException;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.repository.UsuarioRepository;

// Verifica no servidor, a cada operação, o vínculo e o perfil do usuário na empresa (RN22, RN23, RNF03)
@Component
public class ControleAcesso {

	private final UsuarioEmpresaRepository usuarioEmpresaRepository;
	private final UsuarioRepository usuarioRepository;

	public ControleAcesso(UsuarioEmpresaRepository usuarioEmpresaRepository, UsuarioRepository usuarioRepository) {
		this.usuarioEmpresaRepository = usuarioEmpresaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	public UsuarioAutenticado usuarioAtual() {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacao != null && autenticacao.getPrincipal() instanceof UsuarioAutenticado usuario) {
			return usuario;
		}
		throw new NaoAutenticadoException();
	}

	// Referência ao usuário logado, para gravar autoria sem consultar o banco
	public Usuario referenciaUsuarioAtual() {
		return usuarioRepository.getReferenceById(usuarioAtual().id());
	}

	// Vínculo ativo com a empresa, sem exigir permissão específica
	public UsuarioEmpresa exigirVinculo(String empresaId) {
		return usuarioEmpresaRepository.findByUsuarioIdAndEmpresaIdAndAtivoTrue(usuarioAtual().id(), empresaId)
				.orElseThrow(() -> new AcessoNegadoException("Você não tem acesso a esta empresa."));
	}

	public UsuarioEmpresa exigir(String empresaId, Permissao permissao) {
		UsuarioEmpresa vinculo = exigirVinculo(empresaId);
		if (!permissao.permite(vinculo.getPerfil())) {
			throw new AcessoNegadoException("Seu perfil não permite esta operação.");
		}
		return vinculo;
	}
}
