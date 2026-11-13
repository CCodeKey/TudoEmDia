package ted.com.tudoemdia.service;

import java.time.LocalDate;
import ted.com.tudoemdia.model.enuns.StatusDocumento;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;

// Cálculo das situações de obrigações e documentos
public final class Situacoes {

	private Situacoes() {
	}

	// RN43, RN44, RN45
	public static StatusObrigacao daObrigacao(LocalDate vencimento, boolean paga, LocalDate hoje) {
		if (paga) {
			return StatusObrigacao.PAGA;
		}
		return vencimento.isBefore(hoje) ? StatusObrigacao.VENCIDA : StatusObrigacao.PENDENTE;
	}

	// RN51: ATIVO corresponde a "Válido"
	public static StatusDocumento doDocumento(LocalDate validade, boolean arquivado, LocalDate hoje) {
		if (arquivado) {
			return StatusDocumento.ARQUIVADO;
		}
		return validade != null && validade.isBefore(hoje) ? StatusDocumento.VENCIDO : StatusDocumento.ATIVO;
	}
}
