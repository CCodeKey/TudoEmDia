package ted.com.tudoemdia.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ted.com.tudoemdia.config.AplicacaoConfig;
import ted.com.tudoemdia.model.Recorrencia;
import ted.com.tudoemdia.repository.ConviteRepository;
import ted.com.tudoemdia.repository.DocumentoRepository;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.RecorrenciaRepository;

// Rotinas agendadas; todas idempotentes, podem ser executadas de novo sem efeito duplicado (docs/07-arquitetura.md)
@Service
public class RotinaDiariaService {

	private static final Logger log = LoggerFactory.getLogger(RotinaDiariaService.class);

	private final ObrigacaoRepository obrigacaoRepository;
	private final DocumentoRepository documentoRepository;
	private final ConviteRepository conviteRepository;
	private final RecorrenciaRepository recorrenciaRepository;
	private final RecorrenciaService recorrenciaService;
	private final AlertaService alertaService;
	private final TransactionTemplate transacao;
	private final Clock clock;

	public RotinaDiariaService(ObrigacaoRepository obrigacaoRepository, DocumentoRepository documentoRepository,
			ConviteRepository conviteRepository, RecorrenciaRepository recorrenciaRepository,
			RecorrenciaService recorrenciaService, AlertaService alertaService, TransactionTemplate transacao,
			Clock clock) {
		this.obrigacaoRepository = obrigacaoRepository;
		this.documentoRepository = documentoRepository;
		this.conviteRepository = conviteRepository;
		this.recorrenciaRepository = recorrenciaRepository;
		this.recorrenciaService = recorrenciaService;
		this.alertaService = alertaService;
		this.transacao = transacao;
		this.clock = clock;
	}

	// Recalcula situações à 0h e gera as próximas ocorrências (RN46, RN62)
	@Scheduled(cron = "0 0 0 * * *", zone = AplicacaoConfig.FUSO_HORARIO)
	public void atualizarSituacoes() {
		LocalDate hoje = LocalDate.now(clock);
		Integer vencidas = transacao.execute(status -> obrigacaoRepository.marcarVencidas(hoje));
		Integer documentos = transacao.execute(status -> documentoRepository.marcarVencidos(hoje));
		Integer convites = transacao.execute(status -> conviteRepository.expirarPendentes(LocalDateTime.now(clock)));

		List<String> recorrencias = transacao
				.execute(status -> recorrenciaRepository.findByAtivaTrue().stream().map(Recorrencia::getId).toList());
		int ocorrencias = 0;
		for (String recorrenciaId : recorrencias) {
			try {
				ocorrencias += recorrenciaService.gerarOcorrencias(recorrenciaId);
			} catch (RuntimeException e) {
				log.error("Falha ao gerar ocorrências da recorrência {}", recorrenciaId, e);
			}
		}
		log.info("Rotina diária: {} obrigações vencidas, {} documentos vencidos, {} convites expirados, "
				+ "{} ocorrências geradas", vencidas, documentos, convites, ocorrencias);
	}

	// Alertas de vencimento às 8h (RN79)
	@Scheduled(cron = "0 0 8 * * *", zone = AplicacaoConfig.FUSO_HORARIO)
	public void enviarAlertas() {
		alertaService.enviarAlertasDoDia();
	}

	// Recupera o que não rodou enquanto a aplicação estava parada
	@EventListener(ApplicationReadyEvent.class)
	public void aoIniciar() {
		try {
			atualizarSituacoes();
			if (!LocalTime.now(clock).isBefore(AlertaService.HORARIO_ALERTAS)) {
				enviarAlertas();
			}
		} catch (RuntimeException e) {
			log.error("Falha ao executar as rotinas na inicialização", e);
		}
	}
}
