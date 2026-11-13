package ted.com.tudoemdia.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.obrigacao.RecorrenciaAtualizacaoRequest;
import ted.com.tudoemdia.dto.obrigacao.RecorrenciaRequest;
import ted.com.tudoemdia.dto.obrigacao.RecorrenciaResponse;
import ted.com.tudoemdia.exception.ConflitoException;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Categoria;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Recorrencia;
import ted.com.tudoemdia.model.enuns.Periodicidade;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.RecorrenciaRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

// RF16 e RF17: RN57 a RN65
@Service
public class RecorrenciaService {

	// Limite de segurança ao recuperar ocorrências atrasadas de uma vez
	private static final int MAXIMO_OCORRENCIAS_POR_EXECUCAO = 500;

	private final RecorrenciaRepository recorrenciaRepository;
	private final ObrigacaoRepository obrigacaoRepository;
	private final ExclusaoObrigacaoService exclusaoObrigacaoService;
	private final CategoriaService categoriaService;
	private final AlertaService alertaService;
	private final ControleAcesso controleAcesso;
	private final Clock clock;

	public RecorrenciaService(RecorrenciaRepository recorrenciaRepository, ObrigacaoRepository obrigacaoRepository,
			ExclusaoObrigacaoService exclusaoObrigacaoService, CategoriaService categoriaService,
			AlertaService alertaService, ControleAcesso controleAcesso, Clock clock) {
		this.recorrenciaRepository = recorrenciaRepository;
		this.obrigacaoRepository = obrigacaoRepository;
		this.exclusaoObrigacaoService = exclusaoObrigacaoService;
		this.categoriaService = categoriaService;
		this.alertaService = alertaService;
		this.controleAcesso = controleAcesso;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<RecorrenciaResponse> listar(String empresaId) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_OBRIGACOES);
		return recorrenciaRepository.findByEmpresaIdOrderByDescricaoAsc(empresaId).stream()
				.map(RecorrenciaResponse::de).toList();
	}

	// Torna recorrente uma obrigação já cadastrada
	@Transactional
	public RecorrenciaResponse configurar(String empresaId, String obrigacaoId, RecorrenciaRequest dados) {
		controleAcesso.exigir(empresaId, Permissao.CONFIGURAR_RECORRENCIA);
		Obrigacao obrigacao = obrigacaoRepository.findByIdAndEmpresaId(obrigacaoId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Obrigação não encontrada."));
		if (obrigacao.getRecorrencia() != null) {
			throw new ConflitoException("Esta obrigação já é recorrente.");
		}
		Recorrencia recorrencia = criarPara(obrigacao, dados);
		alertaService.verificarAposAlteracao(empresaId);
		return RecorrenciaResponse.de(recorrencia);
	}

	// A obrigação passa a ser a primeira ocorrência; a data dela define o dia dos vencimentos
	public Recorrencia criarPara(Obrigacao obrigacao, RecorrenciaRequest dados) {
		validarDataFim(obrigacao.getDataVencimento(), dados.dataFim());
		Recorrencia recorrencia = new Recorrencia();
		recorrencia.setEmpresa(obrigacao.getEmpresa());
		recorrencia.setCategoria(obrigacao.getCategoria());
		recorrencia.setDescricao(obrigacao.getDescricao());
		recorrencia.setValor(obrigacao.getValor());
		recorrencia.setPeriodicidade(dados.periodicidade());
		recorrencia.setDataInicio(obrigacao.getDataVencimento());
		recorrencia.setDataFim(dados.dataFim());
		recorrenciaRepository.save(recorrencia);
		obrigacao.setRecorrencia(recorrencia);
		gerarOcorrencias(recorrencia, LocalDate.now(clock));
		return recorrencia;
	}

	// Alterações valem só para ocorrências futuras e não pagas (RN61)
	@Transactional
	public RecorrenciaResponse atualizar(String empresaId, String recorrenciaId, RecorrenciaAtualizacaoRequest dados) {
		controleAcesso.exigir(empresaId, Permissao.CONFIGURAR_RECORRENCIA);
		Recorrencia recorrencia = recorrenciaRepository.findByIdAndEmpresaId(recorrenciaId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Recorrência não encontrada."));
		Categoria categoria = categoriaService.buscarDisponivel(empresaId, dados.categoriaId());
		validarDataFim(recorrencia.getDataInicio(), dados.dataFim());
		LocalDate hoje = LocalDate.now(clock);
		boolean mudouPeriodicidade = recorrencia.getPeriodicidade() != dados.periodicidade();

		recorrencia.setDescricao(dados.descricao().trim());
		recorrencia.setValor(dados.valor());
		recorrencia.setCategoria(categoria);
		recorrencia.setPeriodicidade(dados.periodicidade());
		recorrencia.setDataFim(dados.dataFim());
		recorrencia.setAtiva(dados.ativa());

		List<Obrigacao> futuras = obrigacaoRepository.findByRecorrenciaIdAndDataVencimentoAfterAndStatusNot(
				recorrencia.getId(), hoje, StatusObrigacao.PAGA);
		for (Obrigacao futura : futuras) {
			// Ocorrências fora do novo calendário são refeitas pela geração abaixo
			boolean foraDoCalendario = mudouPeriodicidade
					|| (dados.dataFim() != null && futura.getDataVencimento().isAfter(dados.dataFim()));
			if (foraDoCalendario && exclusaoObrigacaoService.podeRemover(futura)) {
				exclusaoObrigacaoService.remover(futura);
			} else {
				futura.setDescricao(recorrencia.getDescricao());
				futura.setValor(recorrencia.getValor());
				futura.setCategoria(categoria);
				futura.setUsuarioAtualizacao(controleAcesso.referenciaUsuarioAtual());
			}
		}
		gerarOcorrencias(recorrencia, hoje);
		alertaService.verificarAposAlteracao(empresaId);
		return RecorrenciaResponse.de(recorrencia);
	}

	// Exclui a ocorrência e as futuras não pagas, encerrando a recorrência (RN39)
	public void excluirAPartirDe(Obrigacao obrigacao) {
		Recorrencia recorrencia = obrigacao.getRecorrencia();
		List<Obrigacao> futuras = obrigacaoRepository.findByRecorrenciaIdAndDataVencimentoAfterAndStatusNot(
				recorrencia.getId(), obrigacao.getDataVencimento(), StatusObrigacao.PAGA);
		exclusaoObrigacaoService.remover(obrigacao);
		futuras.forEach(exclusaoObrigacaoService::remover);

		// Sem nenhuma ocorrência restante, a recorrência deixa de existir e libera a categoria (RN42);
		// com ocorrências anteriores, só é encerrada, preservando o histórico
		if (!obrigacaoRepository.existsByRecorrenciaId(recorrencia.getId())) {
			recorrenciaRepository.delete(recorrencia);
			return;
		}
		recorrencia.setAtiva(false);
		LocalDate ultimoDia = obrigacao.getDataVencimento().minusDays(1);
		if (!ultimoDia.isBefore(recorrencia.getDataInicio())) {
			recorrencia.setDataFim(ultimoDia);
		}
	}

	// Exclui só esta ocorrência sem que a rotina diária a gere de novo
	public void excluirSomente(Obrigacao obrigacao) {
		Recorrencia recorrencia = obrigacao.getRecorrencia();
		LocalDate vencimento = obrigacao.getDataVencimento();
		exclusaoObrigacaoService.remover(obrigacao);
		if (!recorrencia.isAtiva()) {
			return;
		}
		// A geração parte da última ocorrência existente; garante que ela seja posterior à excluída
		boolean existePosterior = obrigacaoRepository
				.findFirstByRecorrenciaIdOrderByDataVencimentoDesc(recorrencia.getId())
				.map(ultima -> ultima.getDataVencimento().isAfter(vencimento)).orElse(false);
		if (existePosterior) {
			return;
		}
		LocalDate proxima = proximaData(recorrencia, vencimento);
		if (recorrencia.getDataFim() != null && proxima.isAfter(recorrencia.getDataFim())) {
			// Era a última ocorrência: a recorrência passa a terminar antes dela
			recorrencia.setDataFim(vencimento.minusDays(1));
			recorrencia.setAtiva(!recorrencia.getDataFim().isBefore(recorrencia.getDataInicio()));
			return;
		}
		obrigacaoRepository.save(novaOcorrencia(recorrencia, proxima, LocalDate.now(clock)));
	}

	// Usado pela rotina diária, uma transação por recorrência
	@Transactional
	public int gerarOcorrencias(String recorrenciaId) {
		return recorrenciaRepository.findById(recorrenciaId)
				.map(recorrencia -> gerarOcorrencias(recorrencia, LocalDate.now(clock)))
				.orElse(0);
	}

	// Garante que a recorrência tenha a próxima ocorrência futura, recuperando as que faltarem (RN62 a RN65)
	public int gerarOcorrencias(Recorrencia recorrencia, LocalDate hoje) {
		if (!recorrencia.isAtiva()) {
			return 0;
		}
		LocalDate ultima = obrigacaoRepository.findFirstByRecorrenciaIdOrderByDataVencimentoDesc(recorrencia.getId())
				.map(Obrigacao::getDataVencimento).orElse(null);
		if (ultima != null && ultima.isAfter(hoje)) {
			return 0;
		}
		int indice = 0;
		LocalDate data = recorrencia.getDataInicio();
		if (ultima != null) {
			indice = indiceApos(recorrencia, ultima);
			data = ocorrencia(recorrencia.getDataInicio(), recorrencia.getPeriodicidade(), indice);
		}
		int geradas = 0;
		while (geradas < MAXIMO_OCORRENCIAS_POR_EXECUCAO) {
			if (recorrencia.getDataFim() != null && data.isAfter(recorrencia.getDataFim())) {
				break;
			}
			if (!obrigacaoRepository.existsByRecorrenciaIdAndDataVencimento(recorrencia.getId(), data)) {
				obrigacaoRepository.save(novaOcorrencia(recorrencia, data, hoje));
				geradas++;
			}
			if (data.isAfter(hoje)) {
				break;
			}
			data = ocorrencia(recorrencia.getDataInicio(), recorrencia.getPeriodicidade(), ++indice);
		}
		return geradas;
	}

	// Calculada sempre a partir da data inicial: o dia 31 vira o último dia nos meses mais curtos
	// e volta a ser 31 nos meses seguintes (RN59)
	static LocalDate ocorrencia(LocalDate inicio, Periodicidade periodicidade, int indice) {
		return periodicidade == Periodicidade.MENSAL ? inicio.plusMonths(indice) : inicio.plusYears(indice);
	}

	// Primeira data do calendário da recorrência posterior a "data"
	private static LocalDate proximaData(Recorrencia recorrencia, LocalDate data) {
		return ocorrencia(recorrencia.getDataInicio(), recorrencia.getPeriodicidade(), indiceApos(recorrencia, data));
	}

	private static int indiceApos(Recorrencia recorrencia, LocalDate data) {
		int indice = 0;
		while (!ocorrencia(recorrencia.getDataInicio(), recorrencia.getPeriodicidade(), indice).isAfter(data)) {
			indice++;
		}
		return indice;
	}

	// Copia descrição, valor e categoria da recorrência (RN63)
	private Obrigacao novaOcorrencia(Recorrencia recorrencia, LocalDate vencimento, LocalDate hoje) {
		Obrigacao obrigacao = new Obrigacao();
		obrigacao.setEmpresa(recorrencia.getEmpresa());
		obrigacao.setCategoria(recorrencia.getCategoria());
		obrigacao.setRecorrencia(recorrencia);
		obrigacao.setDescricao(recorrencia.getDescricao());
		obrigacao.setValor(recorrencia.getValor());
		obrigacao.setDataVencimento(vencimento);
		obrigacao.setStatus(Situacoes.daObrigacao(vencimento, false, hoje));
		return obrigacao;
	}

	private static void validarDataFim(LocalDate inicio, LocalDate fim) {
		if (fim != null && fim.isBefore(inicio)) {
			throw new RegraNegocioException("A data final da recorrência não pode ser anterior ao primeiro vencimento.");
		}
	}
}
