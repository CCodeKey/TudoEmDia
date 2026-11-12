package ted.com.tudoemdia.service;

import java.util.Locale;

// Validação de CNPJ numérico e alfanumérico (Receita Federal, a partir de julho de 2026) (RN09)
public final class Cnpj {

	private static final int[] PESOS_DV1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
	private static final int[] PESOS_DV2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

	private Cnpj() {
	}

	// Remove máscara e espaços; letras ficam em maiúsculas
	public static String normalizar(String cnpj) {
		return cnpj == null ? "" : cnpj.replaceAll("[\\s./-]", "").toUpperCase(Locale.ROOT);
	}

	public static boolean valido(String cnpj) {
		if (!cnpj.matches("[0-9A-Z]{12}[0-9]{2}") || cnpj.chars().distinct().count() == 1) {
			return false;
		}
		int dv1 = digitoVerificador(cnpj, PESOS_DV1);
		int dv2 = digitoVerificador(cnpj, PESOS_DV2);
		return cnpj.charAt(12) - '0' == dv1 && cnpj.charAt(13) - '0' == dv2;
	}

	public static String formatar(String cnpj) {
		if (cnpj == null || cnpj.length() != 14) {
			return cnpj;
		}
		return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8) + "/"
				+ cnpj.substring(8, 12) + "-" + cnpj.substring(12);
	}

	// Cada caractere vale seu código ASCII menos 48: '0'..'9' = 0..9, 'A' = 17, 'B' = 18...
	private static int digitoVerificador(String cnpj, int[] pesos) {
		int soma = 0;
		for (int i = 0; i < pesos.length; i++) {
			soma += (cnpj.charAt(i) - '0') * pesos[i];
		}
		int resto = soma % 11;
		return resto < 2 ? 0 : 11 - resto;
	}
}
