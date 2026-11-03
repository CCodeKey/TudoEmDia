package ted.com.tudoemdia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recorrencia")
@Getter
@Setter
@NoArgsConstructor
public class Recorrencia {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_recorrencia", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	// Dados copiados para cada nova ocorrência (RN63)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_categoria", nullable = false)
	private Categoria categoria;

	@Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
	private String descricao;

	@Column(name = "valor", nullable = false, precision = 10, scale = 2)
	private BigDecimal valor;

	@Enumerated(EnumType.STRING)
	@Column(name = "periodicidade", nullable = false, length = 10)
	private Periodicidade periodicidade;

	// O dia deste vencimento é a referência para os meses sem dia 29, 30 ou 31 (RN59)
	@Column(name = "data_inicio", nullable = false)
	private LocalDate dataInicio;

	// Sem data final, a recorrência é indeterminada (RN60, RN65)
	@Column(name = "data_fim")
	private LocalDate dataFim;

	@Column(name = "ativa", nullable = false)
	private boolean ativa = true;
}
