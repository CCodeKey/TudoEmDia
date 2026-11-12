package ted.com.tudoemdia.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;

// JpaSpecificationExecutor atende aos filtros por período, categoria e situação (RN32, RN92)
public interface ObrigacaoRepository extends JpaRepository<Obrigacao, String>, JpaSpecificationExecutor<Obrigacao> {

	Optional<Obrigacao> findByIdAndEmpresaId(String id, String empresaId);

	// Vencidas, da mais antiga para a mais recente (RN86)
	List<Obrigacao> findByEmpresaIdAndStatusOrderByDataVencimentoAsc(String empresaId, StatusObrigacao status);

	// Obrigações que geram alerta na data (RN77)
	List<Obrigacao> findByEmpresaIdAndStatusAndDataVencimento(String empresaId, StatusObrigacao status,
			LocalDate dataVencimento);

	// Obrigações do mês, para o painel (RN87, RN89)
	List<Obrigacao> findByEmpresaIdAndDataVencimentoBetween(String empresaId, LocalDate inicio, LocalDate fim);

	// Categoria em uso não pode ser excluída (RN42)
	boolean existsByCategoriaId(String categoriaId);

	// Uma ocorrência por recorrência e data (RN64)
	boolean existsByRecorrenciaIdAndDataVencimento(String recorrenciaId, LocalDate dataVencimento);

	// Última ocorrência gerada, base para a próxima (RN62)
	Optional<Obrigacao> findFirstByRecorrenciaIdOrderByDataVencimentoDesc(String recorrenciaId);

	// Ocorrências futuras afetadas por exclusão ou alteração da recorrência (RN39, RN61)
	List<Obrigacao> findByRecorrenciaIdAndDataVencimentoAfterAndStatusNot(String recorrenciaId, LocalDate data,
			StatusObrigacao status);

	// Recálculo diário da situação (RN44, RN46)
	@Modifying
	@Query("""
			update Obrigacao o set o.status = ted.com.tudoemdia.model.enuns.StatusObrigacao.VENCIDA
			where o.status = ted.com.tudoemdia.model.enuns.StatusObrigacao.PENDENTE and o.dataVencimento < :hoje
			""")
	int marcarVencidas(@Param("hoje") LocalDate hoje);

	// Total a pagar no mês (RN84)
	@Query("""
			select coalesce(sum(o.valor), 0) from Obrigacao o
			where o.empresa.id = :empresaId and o.status in :status
			and o.dataVencimento between :inicio and :fim
			""")
	BigDecimal somarValor(@Param("empresaId") String empresaId, @Param("status") Collection<StatusObrigacao> status,
			@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

	// Distribuição por categoria; categorias sem obrigações não aparecem (RN87, RN88)
	@Query("""
			select c.nome as categoria, sum(o.valor) as total from Obrigacao o join o.categoria c
			where o.empresa.id = :empresaId and o.dataVencimento between :inicio and :fim
			group by c.nome
			order by sum(o.valor) desc
			""")
	List<TotalPorCategoria> somarPorCategoria(@Param("empresaId") String empresaId,
			@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

	interface TotalPorCategoria {

		String getCategoria();

		BigDecimal getTotal();
	}
}
