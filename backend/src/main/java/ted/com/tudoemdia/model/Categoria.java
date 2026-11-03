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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_categoria", length = 36)
	private String id;

	// Nulo para as categorias padrão do sistema (RN41)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_empresa")
	private Empresa empresa;

	@Column(name = "nome", nullable = false, columnDefinition = "TEXT")
	private String nome;
}
