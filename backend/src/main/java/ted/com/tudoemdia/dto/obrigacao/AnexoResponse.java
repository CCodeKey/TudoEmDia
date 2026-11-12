package ted.com.tudoemdia.dto.obrigacao;

import java.time.LocalDateTime;
import ted.com.tudoemdia.model.AnexoObrigacao;

// O caminho interno do arquivo nunca é exposto (RN53)
public record AnexoResponse(String id, String nomeArquivo, LocalDateTime dataEnvio) {

	public static AnexoResponse de(AnexoObrigacao anexo) {
		return new AnexoResponse(anexo.getId(), anexo.getNomeArquivo(), anexo.getDataEnvio());
	}
}
