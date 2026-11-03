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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario_empresa",
		uniqueConstraints = @UniqueConstraint(columnNames = { "id_usuario", "id_empresa" }))
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEmpresa {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_usuario_empresa", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	@Enumerated(EnumType.STRING)
	@Column(name = "perfil", nullable = false, length = 20)
	private Perfil perfil;

	// Desativar o acesso preserva o histórico (RN19, RN21, RN81)
	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;

	// Destinatário dos alertas da empresa; por padrão todos recebem (RN75)
	@Column(name = "recebe_alertas", nullable = false)
	private boolean recebeAlertas = true;
}
