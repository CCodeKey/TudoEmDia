package ted.com.tudoemdia.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ted.com.tudoemdia.model.Convite;
import ted.com.tudoemdia.model.enuns.StatusConvite;

public interface ConviteRepository extends JpaRepository<Convite, String> {

	Optional<Convite> findByTokenHash(String tokenHash);

	Optional<Convite> findByIdAndEmpresaId(String id, String empresaId);

	List<Convite> findByEmpresaIdOrderByDataEnvioDesc(String empresaId);

	// Convites recebidos e enviados pelo titular dos dados (RNF14)
	List<Convite> findByEmailConvidadoIgnoreCase(String email);

	List<Convite> findByConvidanteIdAndStatus(String usuarioId, StatusConvite status);

	// Convite ainda válido para o mesmo e-mail
	boolean existsByEmpresaIdAndEmailConvidadoIgnoreCaseAndStatusAndDataExpiracaoAfter(String empresaId,
			String email, StatusConvite status, LocalDateTime agora);

	// Convites não aceitos em 7 dias (RN15)
	@Modifying
	@Query("""
			update Convite c set c.status = ted.com.tudoemdia.model.enuns.StatusConvite.EXPIRADO
			where c.status = ted.com.tudoemdia.model.enuns.StatusConvite.PENDENTE and c.dataExpiracao < :agora
			""")
	int expirarPendentes(@Param("agora") LocalDateTime agora);
}
