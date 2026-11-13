package ted.com.tudoemdia.dto.obrigacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import ted.com.tudoemdia.dto.categoria.CategoriaResponse;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Pagamento;
import ted.com.tudoemdia.model.Recorrencia;
import ted.com.tudoemdia.model.enuns.Periodicidade;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;

public record ObrigacaoResponse(
		String id,
		String descricao,
		BigDecimal valor,
		LocalDate dataVencimento,
		StatusObrigacao status,
		CategoriaResponse categoria,
		String recorrenciaId,
		Periodicidade periodicidade,
		boolean pagaComAtraso, // RN47
		Long diasAtraso,
		LocalDateTime dataAtualizacao,
		String atualizadoPor) { // RN35

	// pagamento: o registro vigente da obrigação, ou null
	public static ObrigacaoResponse de(Obrigacao obrigacao, Pagamento pagamento, LocalDate hoje) {
		Recorrencia recorrencia = obrigacao.getRecorrencia();
		boolean pagaComAtraso = pagamento != null && pagamento.getDataPagamento().isAfter(obrigacao.getDataVencimento());
		Long diasAtraso = obrigacao.getStatus() == StatusObrigacao.VENCIDA
				? ChronoUnit.DAYS.between(obrigacao.getDataVencimento(), hoje)
				: null;
		return new ObrigacaoResponse(
				obrigacao.getId(),
				obrigacao.getDescricao(),
				obrigacao.getValor(),
				obrigacao.getDataVencimento(),
				obrigacao.getStatus(),
				CategoriaResponse.de(obrigacao.getCategoria()),
				recorrencia != null ? recorrencia.getId() : null,
				recorrencia != null ? recorrencia.getPeriodicidade() : null,
				pagaComAtraso,
				diasAtraso,
				obrigacao.getDataAtualizacao(),
				obrigacao.getUsuarioAtualizacao() != null ? obrigacao.getUsuarioAtualizacao().getNome() : null);
	}
}
