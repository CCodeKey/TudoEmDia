package ted.com.tudoemdia.dto.documento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Enviado como campos de formulário junto com o arquivo (RN49, RN51)
public record DocumentoRequest(
		@NotBlank(message = "Informe a descrição.") @Size(max = 500) String descricao,
		@NotBlank(message = "Informe a categoria.") String categoriaId,
		LocalDate dataValidade) {
}
