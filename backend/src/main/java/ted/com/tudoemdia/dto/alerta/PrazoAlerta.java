package ted.com.tudoemdia.dto.alerta;

import jakarta.validation.constraints.NotNull;

// diasAntecedencia: 7, 3, 1 ou 0 (no dia do vencimento)
public record PrazoAlerta(
		@NotNull(message = "Informe os dias de antecedência.") Integer diasAntecedencia,
		@NotNull(message = "Informe se o prazo está ativo.") Boolean ativo) {
}
