package ted.com.tudoemdia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_usuario", length = 36)
	private String id;

	@Column(name = "nome", nullable = false)
	private String nome;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	// Hash BCrypt (RNF02)
	@Column(name = "senha", nullable = false, length = 60)
	private String senha;

	// Consentimento com os termos e a política de privacidade (RN04, RNF14)
	@Column(name = "data_aceite_termos", nullable = false)
	private LocalDateTime dataAceiteTermos;

	// Bloqueio após 5 tentativas erradas seguidas (RN06)
	@Column(name = "tentativas_login_falhas", nullable = false)
	private int tentativasLoginFalhas;

	@Column(name = "bloqueado_ate")
	private LocalDateTime bloqueadoAte;

	@CreationTimestamp
	@Column(name = "data_criacao", nullable = false, updatable = false)
	private LocalDateTime dataCriacao;
}
