package ted.com.tudoemdia.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ted.com.tudoemdia.model.AnexoObrigacao;

public interface AnexoObrigacaoRepository extends JpaRepository<AnexoObrigacao, String> {

	List<AnexoObrigacao> findByObrigacaoId(String obrigacaoId);

	// Acesso ao arquivo restrito à empresa do usuário (RN53)
	Optional<AnexoObrigacao> findByIdAndObrigacaoEmpresaId(String id, String empresaId);
}
