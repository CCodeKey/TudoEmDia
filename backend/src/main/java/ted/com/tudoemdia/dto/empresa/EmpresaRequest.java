package ted.com.tudoemdia.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
		@NotBlank(message = "Informe o nome da empresa.") @Size(max = 255) String nome,
		@NotBlank(message = "Informe o CNPJ.") @Size(max = 18) String cnpj) {
}
