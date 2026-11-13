package ted.com.tudoemdia.dto.auth;

import java.time.LocalDateTime;
import java.util.List;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.model.enuns.StatusConvite;

// Dados pessoais mantidos sobre o usuário, para o direito de acesso do titular (RNF14)
public record DadosTitularResponse(
		String id,
		String nome,
		String email,
		LocalDateTime dataCadastro,
		LocalDateTime dataAceiteTermos,
		List<Vinculo> vinculos,
		List<ConviteRecebido> convitesRecebidos,
		long pagamentosRegistrados) {

	public record Vinculo(String empresa, Perfil perfil, boolean ativo, boolean recebeAlertas) {
	}

	public record ConviteRecebido(String empresa, Perfil perfil, StatusConvite status, LocalDateTime dataEnvio) {
	}
}
