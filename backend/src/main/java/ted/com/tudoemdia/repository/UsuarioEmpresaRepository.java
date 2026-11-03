package ted.com.tudoemdia.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ted.com.tudoemdia.model.Perfil;
import ted.com.tudoemdia.model.UsuarioEmpresa;

public interface UsuarioEmpresaRepository extends JpaRepository<UsuarioEmpresa, String> {

	// Verificação de vínculo e perfil a cada requisição (RN23, RNF03)
	Optional<UsuarioEmpresa> findByUsuarioIdAndEmpresaIdAndAtivoTrue(String usuarioId, String empresaId);

	Optional<UsuarioEmpresa> findByIdAndEmpresaId(String id, String empresaId);

	// Empresas que o usuário pode selecionar (RN11)
	List<UsuarioEmpresa> findByUsuarioIdAndAtivoTrue(String usuarioId);

	// Usuários vinculados (RN18)
	List<UsuarioEmpresa> findByEmpresaId(String empresaId);

	// E-mail já vinculado à empresa (RN17)
	boolean existsByEmpresaIdAndUsuarioEmailIgnoreCase(String empresaId, String email);

	// Pelo menos um proprietário ativo (RN12, RN20)
	long countByEmpresaIdAndPerfilAndAtivoTrue(String empresaId, Perfil perfil);

	// Destinatários de alertas (RN75, RN81)
	List<UsuarioEmpresa> findByEmpresaIdAndAtivoTrueAndRecebeAlertasTrue(String empresaId);
}
