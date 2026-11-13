package ted.com.tudoemdia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;

@Entity
@Table(name = "obrigacao",
		// Uma única ocorrência por recorrência e data (RN64)
		uniqueConstraints = @UniqueConstraint(columnNames = { "id_recorrencia", "data_vencimento" }),
		indexes = @Index(name = "idx_obrigacao_empresa_vencimento", columnList = "id_empresa, data_vencimento"))
@Getter
@Setter
@NoArgsConstructor
public class Obrigacao {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_obrigacao", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_categoria", nullable = false)
	private Categoria categoria;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_recorrencia")
	private Recorrencia recorrencia;

	@Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
	private String descricao;

	@Column(name = "valor", nullable = false, precision = 10, scale = 2)
	private BigDecimal valor;

	@Column(name = "data_vencimento", nullable = false)
	private LocalDate dataVencimento;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private StatusObrigacao status = StatusObrigacao.PENDENTE;

	@CreationTimestamp
	@Column(name = "data_criacao", nullable = false, updatable = false)
	private LocalDateTime dataCriacao;

	// Auditoria de edição (RN35)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_atualizacao")
	private Usuario usuarioAtualizacao;

	@UpdateTimestamp
	@Column(name = "data_atualizacao")
	private LocalDateTime dataAtualizacao;
}
