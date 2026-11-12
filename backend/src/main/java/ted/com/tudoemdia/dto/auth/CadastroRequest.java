package ted.com.tudoemdia.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// RN02, RN03, RN04
public record CadastroRequest(
		@NotBlank(message = "Informe o nome.") @Size(max = 255) String nome,
		@NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 255) String email,
		@NotBlank(message = "Informe a senha.")
		@Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
		@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha deve ter pelo menos uma letra e um número.")
		String senha,
		@AssertTrue(message = "É necessário aceitar os termos de uso e a política de privacidade.") boolean aceiteTermos) {
}
