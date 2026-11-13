package ted.com.tudoemdia.dto.obrigacao;

import java.time.LocalDate;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;

// Filtros da consulta e do relatório; todos opcionais (RN32, RN92)
public record FiltroObrigacoes(LocalDate inicio, LocalDate fim, String categoriaId, StatusObrigacao status) {
}
