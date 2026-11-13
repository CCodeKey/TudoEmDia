package ted.com.tudoemdia.dto.pagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Pagamento;

// RN70, RN72
public record PagamentoResponse(
		String id,
		String obrigacaoId,
		String obrigacaoDescricao,
		LocalDate dataVencimento,
		BigDecimal valorPago,
		LocalDate dataPagamento,
		boolean pagoComAtraso,
		String registradoPor,
		LocalDateTime dataRegistro,
		boolean cancelado,
		String canceladoPor,
		LocalDateTime dataCancelamento) {

	public static PagamentoResponse de(Pagamento pagamento) {
		Obrigacao obrigacao = pagamento.getObrigacao();
		return new PagamentoResponse(
				pagamento.getId(),
				obrigacao.getId(),
				obrigacao.getDescricao(),
				obrigacao.getDataVencimento(),
				pagamento.getValorPago(),
				pagamento.getDataPagamento(),
				pagamento.getDataPagamento().isAfter(obrigacao.getDataVencimento()),
				pagamento.getUsuario().getNome(),
				pagamento.getDataRegistro(),
				pagamento.isCancelado(),
				pagamento.getUsuarioCancelamento() != null ? pagamento.getUsuarioCancelamento().getNome() : null,
				pagamento.getDataCancelamento());
	}
}
