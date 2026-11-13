package ted.com.tudoemdia.dto.painel;

import java.math.BigDecimal;
import java.util.List;

public record PainelResponse(
		String mes,
		BigDecimal totalAPagar, // RN84
		BigDecimal totalPago, // RN85
		List<ObrigacaoVencidaResponse> vencidas, // RN86
		List<TotalCategoriaResponse> porCategoria, // RN87, RN88
		IndicadorResponse indicador) { // RN89, RN90
}
