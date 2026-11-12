package ted.com.tudoemdia.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ted.com.tudoemdia.model.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, String> {

	// No máximo um pagamento vigente por obrigação (RN69)
	Optional<Pagamento> findByObrigacaoIdAndCanceladoFalse(String obrigacaoId);

	List<Pagamento> findByObrigacaoIdInAndCanceladoFalse(Collection<String> obrigacaoIds);

	// Obrigação paga não pode ser excluída nem ter valor/vencimento alterados (RN34, RN38)
	boolean existsByObrigacaoIdAndCanceladoFalse(String obrigacaoId);

	// Qualquer registro, inclusive cancelado, impede a exclusão da obrigação (RN38, RN73)
	boolean existsByObrigacaoId(String obrigacaoId);

	Optional<Pagamento> findByIdAndObrigacaoEmpresaId(String id, String empresaId);

	// Histórico da empresa, incluindo cancelamentos (RN70, RN72)
	List<Pagamento> findByObrigacaoEmpresaIdAndDataPagamentoBetweenOrderByDataPagamentoDescDataRegistroDesc(
			String empresaId, LocalDate inicio, LocalDate fim);

	List<Pagamento> findByObrigacaoIdOrderByDataRegistroDesc(String obrigacaoId);

	// Pagamentos registrados pelo titular dos dados (RNF14)
	long countByUsuarioId(String usuarioId);
}
