package ted.com.tudoemdia;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ted.com.tudoemdia.config.AplicacaoConfig;

@SpringBootApplication
public class TudoemdiaApplication {

	public static void main(String[] args) {
		// As regras de prazo usam o horário de Brasília (RN46, RN79)
		TimeZone.setDefault(TimeZone.getTimeZone(AplicacaoConfig.FUSO_HORARIO));
		SpringApplication.run(TudoemdiaApplication.class, args);
	}

}
