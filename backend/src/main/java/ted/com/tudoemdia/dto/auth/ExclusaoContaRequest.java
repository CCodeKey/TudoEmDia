package ted.com.tudoemdia.dto.auth;

import jakarta.validation.constraints.NotBlank;

// A senha confirma que é o próprio titular quem pede a exclusão
public record ExclusaoContaRequest(@NotBlank(message = "Informe a senha para confirmar.") String senha) {
}
