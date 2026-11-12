package ted.com.tudoemdia.dto.pagamento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

// Sem valorPago, usa o valor da obrigação (RN67, RN68)
public record PagamentoRequest(
		@NotNull(message = "Informe a data do pagamento.") LocalDate dataPagamento,
		@DecimalMin(value = "0.01", message = "O valor pago deve ser maior que zero.")
		@Digits(integer = 8, fraction = 2, message = "O valor pago deve ter no máximo duas casas decimais.")
		BigDecimal valorPago) {
}
