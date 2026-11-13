package ted.com.tudoemdia.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ted.com.tudoemdia.model.Recorrencia;

public interface RecorrenciaRepository extends JpaRepository<Recorrencia, String> {

	Optional<Recorrencia> findByIdAndEmpresaId(String id, String empresaId);

	List<Recorrencia> findByEmpresaIdOrderByDescricaoAsc(String empresaId);

	// Processadas pela rotina diária de geração (RN62)
	List<Recorrencia> findByAtivaTrue();

	// Categoria em uso não pode ser excluída (RN42)
	boolean existsByCategoriaId(String categoriaId);
}
