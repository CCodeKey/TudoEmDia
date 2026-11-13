package ted.com.tudoemdia;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(BancoDeTesteConfig.class)
class TudoemdiaApplicationTests {

	@Test
	void contextLoads() {
	}

}
