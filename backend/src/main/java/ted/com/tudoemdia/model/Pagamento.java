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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

// Registros do histórico não são editados nem apagados (RN73);
// no máximo um pagamento não cancelado por obrigação (RN69)
@Entity
@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
public class Pagamento {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id_pagamento", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_obrigacao", nullable = false, updatable = false)
	private Obrigacao obrigacao;

	// Usuário que registrou o pagamento (RN72)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_usuario", nullable = false, updatable = false)
	private Usuario usuario;

	// Pode diferir do valor da obrigação por juros ou multa (RN68)
	@Column(name = "valor_pago", nullable = false, precision = 10, scale = 2, updatable = false)
	private BigDecimal valorPago;

	@Column(name = "data_pagamento", nullable = false, updatable = false)
	private LocalDate dataPagamento;

	@CreationTimestamp
	@Column(name = "data_registro", nullable = false, updatable = false)
	private LocalDateTime dataRegistro;

	// Cancelamento de registro feito por engano (RN70)
	@Column(name = "cancelado", nullable = false)
	private boolean cancelado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_cancelamento")
	private Usuario usuarioCancelamento;

	@Column(name = "data_cancelamento")
	private LocalDateTime dataCancelamento;
}
