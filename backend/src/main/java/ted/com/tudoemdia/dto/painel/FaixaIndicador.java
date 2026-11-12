package ted.com.tudoemdia.dto.painel;

// RN90
public enum FaixaIndicador {
	VERDE,
	AMARELO,
	VERMELHO;

	public static FaixaIndicador para(double percentual) {
		if (percentual >= 90) {
			return VERDE;
		}
		return percentual >= 70 ? AMARELO : VERMELHO;
	}
}
