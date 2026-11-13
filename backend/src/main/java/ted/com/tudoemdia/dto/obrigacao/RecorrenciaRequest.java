package ted.com.tudoemdia.dto.obrigacao;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import ted.com.tudoemdia.model.enuns.Periodicidade;

// Torna uma obrigação recorrente; sem data final, a recorrência é indeterminada (RN58, RN60)
public record RecorrenciaRequest(
		@NotNull(message = "Informe a periodicidade.") Periodicidade periodicidade,
		LocalDate dataFim) {
}
