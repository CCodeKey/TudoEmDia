package ted.com.tudoemdia.dto.empresa;

import java.time.LocalDateTime;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.model.enuns.StatusConvite;

// Dados exibidos a quem abre o link do convite, antes de entrar ou se cadastrar (RN16)
public record ConvitePublicoResponse(String empresa, String email, Perfil perfil, StatusConvite status,
		LocalDateTime dataExpiracao) {
}
