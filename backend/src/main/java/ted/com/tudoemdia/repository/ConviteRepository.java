package ted.com.tudoemdia.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ted.com.tudoemdia.model.Convite;
import ted.com.tudoemdia.model.StatusConvite;

public interface ConviteRepository extends JpaRepository<Convite, String> {

	Optional<Convite> findByTokenHash(String tokenHash);

	boolean existsByEmpresaIdAndEmailConvidadoIgnoreCaseAndStatus(String empresaId, String email,
			StatusConvite status);

	// Convites não aceitos em 7 dias (RN15)
	@Modifying
	@Query("""
			update Convite c set c.status = ted.com.tudoemdia.model.StatusConvite.EXPIRADO
			where c.status = ted.com.tudoemdia.model.StatusConvite.PENDENTE and c.dataExpiracao < :agora
			""")
	int expirarPendentes(@Param("agora") LocalDateTime agora);
}
