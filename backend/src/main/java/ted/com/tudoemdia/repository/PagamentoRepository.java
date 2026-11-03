package ted.com.tudoemdia.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ted.com.tudoemdia.model.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, String> {

	// No máximo um pagamento vigente por obrigação (RN69)
	Optional<Pagamento> findByObrigacaoIdAndCanceladoFalse(String obrigacaoId);

	// Obrigação paga não pode ser excluída nem ter valor/vencimento alterados (RN34, RN38)
	boolean existsByObrigacaoIdAndCanceladoFalse(String obrigacaoId);

	// Histórico da empresa, incluindo cancelamentos (RN70, RN72)
	List<Pagamento> findByObrigacaoEmpresaIdOrderByDataRegistroDesc(String empresaId);

	List<Pagamento> findByObrigacaoIdOrderByDataRegistroDesc(String obrigacaoId);
}
