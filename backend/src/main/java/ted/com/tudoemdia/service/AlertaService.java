package ted.com.tudoemdia.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ted.com.tudoemdia.dto.alerta.ConfiguracaoAlertasRequest;
import ted.com.tudoemdia.dto.alerta.PrazoAlerta;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.AlertaEnviado;
import ted.com.tudoemdia.model.ConfiguracaoAlerta;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.repository.AlertaEnviadoRepository;
import ted.com.tudoemdia.repository.ConfiguracaoAlertaRepository;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.repository.UsuarioRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

// RF20 e RF21: RN74 a RN81
@Service
public class AlertaService {

	// 7, 3 e 1 dia antes e no dia do vencimento (RF21)
	public static final List<Integer> PRAZOS = List.of(7, 3, 1, 0);

	private static final Logger log = LoggerFactory.getLogger(AlertaService.class);
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final ConfiguracaoAlertaRepository configuracaoRepository;
	private final AlertaEnviadoRepository alertaEnviadoRepository;
	private final ObrigacaoRepository obrigacaoRepository;
	private final UsuarioEmpresaRepository usuarioEmpresaRepository;
	private final UsuarioRepository usuarioRepository;
	private final ControleAcesso controleAcesso;
	private final EmailService emailService;
	private final TransactionTemplate transacao;
	private final Clock clock;
	private final String frontendUrl;

	public AlertaService(ConfiguracaoAlertaRepository configuracaoRepository,
			AlertaEnviadoRepository alertaEnviadoRepository, ObrigacaoRepository obrigacaoRepository,
			UsuarioEmpresaRepository usuarioEmpresaRepository, UsuarioRepository usuarioRepository,
			ControleAcesso controleAcesso, EmailService emailService, TransactionTemplate transacao, Clock clock,
			@Value("${app.frontend-url}") String frontendUrl) {
		this.configuracaoRepository = configuracaoRepository;
		this.alertaEnviadoRepository = alertaEnviadoRepository;
		this.obrigacaoRepository = obrigacaoRepository;
		this.usuarioEmpresaRepository = usuarioEmpresaRepository;
		this.usuarioRepository = usuarioRepository;
		this.controleAcesso = controleAcesso;
		this.emailService = emailService;
		this.transacao = transacao;
		this.clock = clock;
		this.frontendUrl = frontendUrl;
	}

	// Os destinatários são definidos em cada vínculo (recebeAlertas), na gestão de acessos (RN75)
	@Transactional(readOnly = true)
	public List<PrazoAlerta> consultar(String empresaId) {
		controleAcesso.exigir(empresaId, Permissao.CONFIGURAR_ALERTAS);
		return configuracaoRepository.findByEmpresaIdOrderByDiasAntecedenciaDesc(empresaId).stream()
				.map(configuracao -> new PrazoAlerta(configuracao.getDiasAntecedencia(), configuracao.isAtivo()))
				.toList();
	}

	// RN74, RN76
	@Transactional
	public List<PrazoAlerta> configurar(String empresaId, ConfiguracaoAlertasRequest dados) {
		UsuarioEmpresa vinculo = controleAcesso.exigir(empresaId, Permissao.CONFIGURAR_ALERTAS);
		Map<Integer, Boolean> novos = new HashMap<>();
		for (PrazoAlerta prazo : dados.prazos()) {
			if (!PRAZOS.contains(prazo.diasAntecedencia())) {
				throw new RegraNegocioException("Prazo inválido: use 7, 3, 1 ou 0 dias de antecedência.");
			}
			novos.put(prazo.diasAntecedencia(), prazo.ativo());
		}
		List<ConfiguracaoAlerta> configuracoes = new ArrayList<>();
		for (int dias : PRAZOS) {
			ConfiguracaoAlerta configuracao = configuracaoRepository.findByEmpresaIdAndDiasAntecedencia(empresaId, dias)
					.orElseGet(() -> {
						ConfiguracaoAlerta nova = new ConfiguracaoAlerta();
						nova.setEmpresa(vinculo.getEmpresa());
						nova.setDiasAntecedencia(dias);
						return nova;
					});
			if (novos.containsKey(dias)) {
				configuracao.setAtivo(novos.get(dias));
			}
			configuracoes.add(configuracao);
		}
		if (configuracoes.stream().noneMatch(ConfiguracaoAlerta::isAtivo)) {
			throw new RegraNegocioException("Mantenha pelo menos um prazo de alerta ativo.");
		}
		configuracaoRepository.saveAll(configuracoes);
		return configuracoes.stream()
				.map(configuracao -> new PrazoAlerta(configuracao.getDiasAntecedencia(), configuracao.isAtivo()))
				.toList();
	}

	// Executado pela rotina das 8h; cada alerta é registrado e enviado na própria transação (RN77 a RN81, RNF10)
	public void enviarAlertasDoDia() {
		if (!emailService.habilitado()) {
			log.warn("Envio de alertas ignorado: e-mail não configurado (MAIL_HOST).");
			return;
		}
		LocalDate hoje = LocalDate.now(clock);
		List<AlertaPendente> pendentes = transacao.execute(status -> buscarPendentes(hoje));
		int enviados = 0;
		int falhas = 0;
		for (AlertaPendente alerta : pendentes) {
			try {
				if (Boolean.TRUE.equals(transacao.execute(status -> enviar(alerta)))) {
					enviados++;
				}
			} catch (RuntimeException e) {
				falhas++;
			}
		}
		log.info("Alertas de vencimento: {} enviados, {} com falha", enviados, falhas);
	}

	private List<AlertaPendente> buscarPendentes(LocalDate hoje) {
		List<AlertaPendente> pendentes = new ArrayList<>();
		for (ConfiguracaoAlerta configuracao : configuracaoRepository.findByAtivoTrue()) {
			String empresaId = configuracao.getEmpresa().getId();
			int dias = configuracao.getDiasAntecedencia();
			// Só obrigações pendentes geram alerta (RN77)
			List<Obrigacao> obrigacoes = obrigacaoRepository.findByEmpresaIdAndStatusAndDataVencimento(empresaId,
					StatusObrigacao.PENDENTE, hoje.plusDays(dias));
			if (obrigacoes.isEmpty()) {
				continue;
			}
			// Usuários desativados não recebem alertas (RN81)
			List<UsuarioEmpresa> destinatarios = usuarioEmpresaRepository
					.findByEmpresaIdAndAtivoTrueAndRecebeAlertasTrue(empresaId);
			for (Obrigacao obrigacao : obrigacoes) {
				for (UsuarioEmpresa destinatario : destinatarios) {
					String usuarioId = destinatario.getUsuario().getId();
					if (!alertaEnviadoRepository.existsByObrigacaoIdAndUsuarioIdAndDiasAntecedencia(obrigacao.getId(),
							usuarioId, dias)) {
						pendentes.add(new AlertaPendente(obrigacao.getId(), usuarioId,
								destinatario.getUsuario().getEmail(), dias, empresaId,
								configuracao.getEmpresa().getNome(), obrigacao.getDescricao(), obrigacao.getValor(),
								obrigacao.getDataVencimento()));
					}
				}
			}
		}
		return pendentes;
	}

	// Grava o registro antes de enviar: se o envio falhar, a transação é desfeita e o alerta segue pendente
	private boolean enviar(AlertaPendente alerta) {
		// Um único envio por obrigação, prazo e destinatário (RN78)
		if (alertaEnviadoRepository.existsByObrigacaoIdAndUsuarioIdAndDiasAntecedencia(alerta.obrigacaoId(),
				alerta.usuarioId(), alerta.dias())) {
			return false;
		}
		AlertaEnviado registro = new AlertaEnviado();
		registro.setObrigacao(obrigacaoRepository.getReferenceById(alerta.obrigacaoId()));
		registro.setUsuario(usuarioRepository.getReferenceById(alerta.usuarioId()));
		registro.setDiasAntecedencia(alerta.dias());
		alertaEnviadoRepository.saveAndFlush(registro);
		emailService.enviar(alerta.email(), assunto(alerta), texto(alerta));
		return true;
	}

	private static String assunto(AlertaPendente alerta) {
		String prazo = switch (alerta.dias()) {
			case 0 -> "vence hoje";
			case 1 -> "vence amanhã";
			default -> "vence em " + alerta.dias() + " dias";
		};
		return "[" + alerta.empresa() + "] " + alerta.descricao() + " " + prazo;
	}

	// Descrição, valor, vencimento e link para a obrigação (RN80)
	private String texto(AlertaPendente alerta) {
		NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
		return """
				Olá!

				Uma obrigação da empresa %s está próxima do vencimento:

				Descrição: %s
				Valor: %s
				Vencimento: %s

				Acesse a obrigação: %s/empresas/%s/obrigacoes/%s
				""".formatted(alerta.empresa(), alerta.descricao(), moeda.format(alerta.valor()),
				alerta.vencimento().format(FORMATO_DATA), frontendUrl, alerta.empresaId(), alerta.obrigacaoId());
	}

	private record AlertaPendente(String obrigacaoId, String usuarioId, String email, int dias, String empresaId,
			String empresa, String descricao, BigDecimal valor, LocalDate vencimento) {
	}
}
