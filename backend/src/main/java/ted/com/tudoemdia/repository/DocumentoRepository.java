package ted.com.tudoemdia.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ted.com.tudoemdia.model.Documento;

public interface DocumentoRepository extends JpaRepository<Documento, String> {

	List<Documento> findByEmpresaIdOrderByDescricaoAsc(String empresaId);

	// Acesso restrito à empresa do usuário (RN52, RN53)
	Optional<Documento> findByIdAndEmpresaId(String id, String empresaId);

	// Categoria em uso não pode ser excluída (RN42)
	boolean existsByCategoriaId(String categoriaId);

	// Documentos com validade expirada passam a Vencido (RN51)
	@Modifying
	@Query("""
			update Documento d set d.status = ted.com.tudoemdia.model.enuns.StatusDocumento.VENCIDO
			where d.status = ted.com.tudoemdia.model.enuns.StatusDocumento.ATIVO and d.dataValidade < :hoje
			""")
	int marcarVencidos(@Param("hoje") LocalDate hoje);
}
