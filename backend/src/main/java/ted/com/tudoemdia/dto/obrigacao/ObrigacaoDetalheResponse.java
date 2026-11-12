package ted.com.tudoemdia.dto.obrigacao;

import java.util.List;
import ted.com.tudoemdia.dto.pagamento.PagamentoResponse;

// pagamento: o registro vigente, ou null se a obrigação não estiver paga
public record ObrigacaoDetalheResponse(ObrigacaoResponse obrigacao, List<AnexoResponse> anexos,
		PagamentoResponse pagamento) {
}
