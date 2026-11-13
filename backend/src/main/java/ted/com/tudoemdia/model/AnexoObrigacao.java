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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "anexo_obrigacao")
@Getter
@Setter
@NoArgsConstructor
public class AnexoObrigacao {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_anexo", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_obrigacao", nullable = false)
	private Obrigacao obrigacao;

	@Column(name = "nome_arquivo", nullable = false, columnDefinition = "TEXT")
	private String nomeArquivo;

	// Caminho interno; nunca exposto ao usuário (RN53)
	@Column(name = "caminho_arquivo", nullable = false, columnDefinition = "TEXT")
	private String caminhoArquivo;

	@CreationTimestamp
	@Column(name = "data_envio", nullable = false, updatable = false)
	private LocalDateTime dataEnvio;
}
