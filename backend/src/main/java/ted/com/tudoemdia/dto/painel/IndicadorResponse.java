package ted.com.tudoemdia.dto.painel;

import java.math.BigDecimal;

// percentual e faixa são nulos quando nenhuma obrigação do mês venceu até hoje
public record IndicadorResponse(BigDecimal percentual, FaixaIndicador faixa, long pagasEmDia, long total) {
}
