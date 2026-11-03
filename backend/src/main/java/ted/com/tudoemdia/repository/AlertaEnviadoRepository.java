package ted.com.tudoemdia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ted.com.tudoemdia.model.AlertaEnviado;

public interface AlertaEnviadoRepository extends JpaRepository<AlertaEnviado, String> {

	// Prevenção de envio duplicado (RN78, RNF10)
	boolean existsByObrigacaoIdAndUsuarioIdAndDiasAntecedencia(String obrigacaoId, String usuarioId,
			int diasAntecedencia);
}
