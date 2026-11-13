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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

// Um único envio por obrigação, destinatário e prazo (RN78, RNF10)
@Entity
@Table(name = "alerta_enviado",
		uniqueConstraints = @UniqueConstraint(columnNames = { "id_obrigacao", "id_usuario", "dias_antecedencia" }))
@Getter
@Setter
@NoArgsConstructor
public class AlertaEnviado {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_alerta", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_obrigacao", nullable = false)
	private Obrigacao obrigacao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;

	@Column(name = "dias_antecedencia", nullable = false)
	private int diasAntecedencia;

	@CreationTimestamp
	@Column(name = "data_envio", nullable = false, updatable = false)
	private LocalDateTime dataEnvio;
}
