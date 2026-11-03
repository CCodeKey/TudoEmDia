package ted.com.tudoemdia.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ted.com.tudoemdia.model.ConfiguracaoAlerta;

public interface ConfiguracaoAlertaRepository extends JpaRepository<ConfiguracaoAlerta, String> {

	List<ConfiguracaoAlerta> findByEmpresaIdOrderByDiasAntecedenciaDesc(String empresaId);

	Optional<ConfiguracaoAlerta> findByEmpresaIdAndDiasAntecedencia(String empresaId, int diasAntecedencia);

	// Pelo menos um prazo ativo (RN76)
	long countByEmpresaIdAndAtivoTrue(String empresaId);

	// Prazos processados pela rotina de envio (RN79)
	List<ConfiguracaoAlerta> findByAtivoTrue();
}
