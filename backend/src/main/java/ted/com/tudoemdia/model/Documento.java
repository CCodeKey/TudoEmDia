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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ted.com.tudoemdia.model.enuns.StatusDocumento;

@Entity
@Table(name = "documento")
@Getter
@Setter
@NoArgsConstructor
public class Documento {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_documento", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	// Descrição e categoria são obrigatórias (RN49)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_categoria", nullable = false)
	private Categoria categoria;

	@Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
	private String descricao;

	// Opcional (RN51)
	@Column(name = "data_validade")
	private LocalDate dataValidade;

	@Column(name = "nome_arquivo", nullable = false, columnDefinition = "TEXT")
	private String nomeArquivo;

	// Caminho interno; nunca exposto ao usuário (RN53)
	@Column(name = "caminho_arquivo", nullable = false, columnDefinition = "TEXT")
	private String caminhoArquivo;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private StatusDocumento status = StatusDocumento.ATIVO;

	@CreationTimestamp
	@Column(name = "data_criacao", nullable = false, updatable = false)
	private LocalDateTime dataCriacao;

	// Auditoria de edição (RN56)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_atualizacao")
	private Usuario usuarioAtualizacao;

	@UpdateTimestamp
	@Column(name = "data_atualizacao")
	private LocalDateTime dataAtualizacao;
}
