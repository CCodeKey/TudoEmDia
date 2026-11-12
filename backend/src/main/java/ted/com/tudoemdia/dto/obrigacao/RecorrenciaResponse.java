package ted.com.tudoemdia.dto.obrigacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import ted.com.tudoemdia.dto.categoria.CategoriaResponse;
import ted.com.tudoemdia.model.Recorrencia;
import ted.com.tudoemdia.model.enuns.Periodicidade;

public record RecorrenciaResponse(String id, String descricao, BigDecimal valor, CategoriaResponse categoria,
		Periodicidade periodicidade, LocalDate dataInicio, LocalDate dataFim, boolean ativa) {

	public static RecorrenciaResponse de(Recorrencia recorrencia) {
		return new RecorrenciaResponse(recorrencia.getId(), recorrencia.getDescricao(), recorrencia.getValor(),
				CategoriaResponse.de(recorrencia.getCategoria()), recorrencia.getPeriodicidade(),
				recorrencia.getDataInicio(), recorrencia.getDataFim(), recorrencia.isAtiva());
	}
}
