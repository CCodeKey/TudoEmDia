package ted.com.tudoemdia.dto.empresa;

import java.time.LocalDateTime;
import ted.com.tudoemdia.model.Convite;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.model.enuns.StatusConvite;

public record ConviteResponse(String id, String email, Perfil perfil, StatusConvite status, LocalDateTime dataEnvio,
		LocalDateTime dataExpiracao, String convidadoPor) {

	public static ConviteResponse de(Convite convite, StatusConvite status) {
		return new ConviteResponse(convite.getId(), convite.getEmailConvidado(), convite.getPerfilConvidado(), status,
				convite.getDataEnvio(), convite.getDataExpiracao(), convite.getConvidante().getNome());
	}
}
