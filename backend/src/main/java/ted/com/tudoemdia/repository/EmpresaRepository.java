package ted.com.tudoemdia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ted.com.tudoemdia.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {

	// CNPJ único (RN09)
	boolean existsByCnpj(String cnpj);
}
