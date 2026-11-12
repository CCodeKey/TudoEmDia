package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CnpjTest {

	@Test
	void aceitaCnpjNumericoComOuSemMascara() {
		assertThat(Cnpj.valido(Cnpj.normalizar("11.222.333/0001-81"))).isTrue();
		assertThat(Cnpj.valido(Cnpj.normalizar("11222333000181"))).isTrue();
	}

	@Test
	void aceitaCnpjAlfanumerico() {
		assertThat(Cnpj.valido(Cnpj.normalizar("12.abc.345/01de-35"))).isTrue();
	}

	@Test
	void recusaDigitoVerificadorErradoOuSequenciaRepetida() {
		assertThat(Cnpj.valido("11222333000182")).isFalse();
		assertThat(Cnpj.valido("11111111111111")).isFalse();
		assertThat(Cnpj.valido("1122233300018")).isFalse();
		assertThat(Cnpj.valido("12ABC34501DE3A")).isFalse();
	}

	@Test
	void formataComMascara() {
		assertThat(Cnpj.formatar("11222333000181")).isEqualTo("11.222.333/0001-81");
	}
}
