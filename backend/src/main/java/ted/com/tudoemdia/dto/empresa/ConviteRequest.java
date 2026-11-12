package ted.com.tudoemdia.dto.empresa;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ted.com.tudoemdia.model.enuns.Perfil;

// RN14
public record ConviteRequest(
		@NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 255) String email,
		@NotNull(message = "Informe o perfil.") Perfil perfil) {
}
