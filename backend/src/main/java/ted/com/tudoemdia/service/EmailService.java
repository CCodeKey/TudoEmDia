package ted.com.tudoemdia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	// Até 3 novas tentativas por mensagem (RNF10)
	private static final int NOVAS_TENTATIVAS = 3;
	private static final long ESPERA_ENTRE_TENTATIVAS_MS = 2000;

	private final ObjectProvider<JavaMailSender> mailSender;
	private final String host;
	private final String remetente;

	public EmailService(ObjectProvider<JavaMailSender> mailSender, @Value("${spring.mail.host:}") String host,
			@Value("${app.email.remetente}") String remetente) {
		this.mailSender = mailSender;
		this.host = host;
		this.remetente = remetente;
	}

	public boolean habilitado() {
		return StringUtils.hasText(host) && mailSender.getIfAvailable() != null;
	}

	// Lança MailException se todas as tentativas falharem
	public void enviar(String para, String assunto, String texto) {
		if (!habilitado()) {
			// Só em desenvolvimento, sem servidor de e-mail configurado
			log.warn("E-mail não configurado (MAIL_HOST); a mensagem não foi enviada.\nAssunto: {}\n{}", assunto, texto);
			return;
		}
		SimpleMailMessage mensagem = new SimpleMailMessage();
		mensagem.setFrom(remetente);
		mensagem.setTo(para);
		mensagem.setSubject(assunto);
		mensagem.setText(texto);
		for (int tentativa = 1;; tentativa++) {
			try {
				mailSender.getObject().send(mensagem);
				return;
			} catch (MailException e) {
				if (tentativa > NOVAS_TENTATIVAS) {
					log.error("Falha ao enviar o e-mail \"{}\" após {} tentativas", assunto, tentativa, e);
					throw e;
				}
				log.warn("Falha ao enviar o e-mail \"{}\" (tentativa {}); tentando novamente", assunto, tentativa);
				aguardar();
			}
		}
	}

	// Envia só depois que a transação for confirmada, para não avisar algo que foi desfeito
	public void enviarAposCommit(String para, String assunto, String texto) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			enviar(para, assunto, texto);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					enviar(para, assunto, texto);
				} catch (MailException e) {
					// Já registrado em log; a operação principal foi concluída
				}
			}
		});
	}

	private void aguardar() {
		try {
			Thread.sleep(ESPERA_ENTRE_TENTATIVAS_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
