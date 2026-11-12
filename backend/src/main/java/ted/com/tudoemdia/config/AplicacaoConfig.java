package ted.com.tudoemdia.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableScheduling
public class AplicacaoConfig {

	public static final String FUSO_HORARIO = "America/Sao_Paulo";

	// Relógio no horário de Brasília; injetado para que as regras de data sejam testáveis
	@Bean
	public Clock clock() {
		return Clock.system(ZoneId.of(FUSO_HORARIO));
	}

	@Bean
	public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
		return new TransactionTemplate(transactionManager);
	}
}
