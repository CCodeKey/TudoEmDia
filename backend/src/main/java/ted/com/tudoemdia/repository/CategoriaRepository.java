package ted.com.tudoemdia.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ted.com.tudoemdia.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, String> {

	// Categorias padrão do sistema e as criadas pela empresa (RN41, RN42)
	@Query("""
			select c from Categoria c
			where c.empresa is null or c.empresa.id = :empresaId
			order by c.nome
			""")
	List<Categoria> findDisponiveis(@Param("empresaId") String empresaId);

	@Query("""
			select c from Categoria c
			where c.id = :id and (c.empresa is null or c.empresa.id = :empresaId)
			""")
	Optional<Categoria> findDisponivel(@Param("id") String id, @Param("empresaId") String empresaId);

	Optional<Categoria> findByIdAndEmpresaId(String id, String empresaId);

	boolean existsByEmpresaIdAndNomeIgnoreCase(String empresaId, String nome);
}
