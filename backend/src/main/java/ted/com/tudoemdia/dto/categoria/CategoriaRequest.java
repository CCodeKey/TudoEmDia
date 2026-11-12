package ted.com.tudoemdia.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(@NotBlank(message = "Informe o nome da categoria.") @Size(max = 100) String nome) {
}
