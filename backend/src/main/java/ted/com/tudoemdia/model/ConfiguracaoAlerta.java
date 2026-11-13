package ted.com.tudoemdia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Um registro por prazo (7, 3, 1 e 0 dias) e empresa (RN76)
@Entity
@Table(name = "configuracao_alerta",
		uniqueConstraints = @UniqueConstraint(columnNames = { "id_empresa", "dias_antecedencia" }))
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracaoAlerta {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_configuracao", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	@Column(name = "dias_antecedencia", nullable = false)
	private int diasAntecedencia;

	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;
}
