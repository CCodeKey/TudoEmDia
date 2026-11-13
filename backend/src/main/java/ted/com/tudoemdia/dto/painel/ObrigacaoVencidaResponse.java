package ted.com.tudoemdia.dto.painel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ObrigacaoVencidaResponse(String id, String descricao, String categoria, BigDecimal valor,
		LocalDate dataVencimento, long diasAtraso) {
}
