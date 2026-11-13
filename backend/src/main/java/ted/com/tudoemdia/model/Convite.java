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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.model.enuns.StatusConvite;

@Entity
@Table(name = "convite")
@Getter
@Setter
@NoArgsConstructor
public class Convite {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_convite", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_usuario_convidante", nullable = false)
	private Usuario convidante;

	@Column(name = "email_convidado", nullable = false)
	private String emailConvidado;

	// Apenas FINANCEIRO ou CONTADOR podem ser convidados (RN14)
	@Enumerated(EnumType.STRING)
	@Column(name = "perfil_convidado", nullable = false, length = 20)
	private Perfil perfilConvidado;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private StatusConvite status = StatusConvite.PENDENTE;

	@Column(name = "data_envio", nullable = false)
	private LocalDateTime dataEnvio;

	@Column(name = "token_hash", nullable = false, unique = true)
	private String tokenHash;

	// 7 dias após o envio (RN15)
	@Column(name = "data_expiracao", nullable = false)
	private LocalDateTime dataExpiracao;
}
