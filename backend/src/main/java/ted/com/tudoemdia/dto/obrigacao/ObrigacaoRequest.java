package ted.com.tudoemdia.dto.obrigacao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

// RN26, RN27; recorrência opcional (RF16)
public record ObrigacaoRequest(
		@NotBlank(message = "Informe a descrição.") @Size(max = 500) String descricao,
		@NotNull(message = "Informe o valor.")
		@DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
		@Digits(integer = 8, fraction = 2, message = "O valor deve ter no máximo duas casas decimais.")
		BigDecimal valor,
		@NotBlank(message = "Informe a categoria.") String categoriaId,
		@NotNull(message = "Informe a data de vencimento.") LocalDate dataVencimento,
		@Valid RecorrenciaRequest recorrencia) {
}
