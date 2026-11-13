package ted.com.tudoemdia.service;

import jakarta.persistence.criteria.Predicate;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.obrigacao.AnexoResponse;
import ted.com.tudoemdia.dto.obrigacao.EscopoExclusao;
import ted.com.tudoemdia.dto.obrigacao.FiltroObrigacoes;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoAtualizacaoRequest;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoDetalheResponse;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoRequest;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoResponse;
import ted.com.tudoemdia.dto.pagamento.PagamentoResponse;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Categoria;
import ted.com.tudoemdia.model.Obrigacao;
import ted.com.tudoemdia.model.Pagamento;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.repository.AnexoObrigacaoRepository;
import ted.com.tudoemdia.repository.ObrigacaoRepository;
import ted.com.tudoemdia.repository.PagamentoRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

// RF07 a RF12
@Service
public class ObrigacaoService {

	private static final Sort ORDEM_VENCIMENTO = Sort.by("dataVencimento", "descricao");

	private final ObrigacaoRepository obrigacaoRepository;
	private final PagamentoRepository pagamentoRepository;
	private final AnexoObrigacaoRepository anexoRepository;
	private final CategoriaService categoriaService;
	private final RecorrenciaService recorrenciaService;
	private final ExclusaoObrigacaoService exclusaoObrigacaoService;
	private final AlertaService alertaService;
	private final ControleAcesso controleAcesso;
	private final Clock clock;

	public ObrigacaoService(ObrigacaoRepository obrigacaoRepository, PagamentoRepository pagamentoRepository,
			AnexoObrigacaoRepository anexoRepository, CategoriaService categoriaService,
			RecorrenciaService recorrenciaService, ExclusaoObrigacaoService exclusaoObrigacaoService,
			AlertaService alertaService, ControleAcesso controleAcesso, Clock clock) {
		this.obrigacaoRepository = obrigacaoRepository;
		this.pagamentoRepository = pagamentoRepository;
		this.anexoRepository = anexoRepository;
		this.categoriaService = categoriaService;
		this.recorrenciaService = recorrenciaService;
		this.exclusaoObrigacaoService = exclusaoObrigacaoService;
		this.alertaService = alertaService;
		this.controleAcesso = controleAcesso;
		this.clock = clock;
	}

	// Somente obrigações da empresa selecionada, pelo vencimento mais próximo (RN30 a RN32)
	@Transactional(readOnly = true)
	public List<ObrigacaoResponse> listar(String empresaId, FiltroObrigacoes filtro) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_OBRIGACOES);
		List<Obrigacao> obrigacoes = buscarFiltradas(empresaId, filtro);
		Map<String, Pagamento> pagamentos = pagamentosVigentes(obrigacoes);
		LocalDate hoje = LocalDate.now(clock);
		return obrigacoes.stream()
				.map(obrigacao -> ObrigacaoResponse.de(obrigacao, pagamentos.get(obrigacao.getId()), hoje))
				.toList();
	}

	@Transactional(readOnly = true)
	public ObrigacaoDetalheResponse detalhar(String empresaId, String obrigacaoId) {
		controleAcesso.exigir(empresaId, Permissao.CONSULTAR_OBRIGACOES);
		Obrigacao obrigacao = buscar(empresaId, obrigacaoId);
		Pagamento pagamento = pagamentoRepository.findByObrigacaoIdAndCanceladoFalse(obrigacaoId).orElse(null);
		List<AnexoResponse> anexos = anexoRepository.findByObrigacaoId(obrigacaoId).stream()
				.map(AnexoResponse::de).toList();
		return new ObrigacaoDetalheResponse(ObrigacaoResponse.de(obrigacao, pagamento, LocalDate.now(clock)), anexos,
				pagamento != null ? PagamentoResponse.de(pagamento) : null);
	}

	// RN25 a RN29; com recorrência, também RN57
	@Transactional
	public ObrigacaoResponse criar(String empresaId, ObrigacaoRequest dados) {
		UsuarioEmpresa vinculo = controleAcesso.exigir(empresaId, Permissao.CADASTRAR_OBRIGACOES);
		if (dados.recorrencia() != null) {
			controleAcesso.exigir(empresaId, Permissao.CONFIGURAR_RECORRENCIA);
		}
		Categoria categoria = categoriaService.buscarDisponivel(empresaId, dados.categoriaId());
		LocalDate hoje = LocalDate.now(clock);

		Obrigacao obrigacao = new Obrigacao();
		obrigacao.setEmpresa(vinculo.getEmpresa());
		obrigacao.setCategoria(categoria);
		obrigacao.setDescricao(dados.descricao().trim());
		obrigacao.setValor(dados.valor());
		obrigacao.setDataVencimento(dados.dataVencimento());
		// Vencimento no passado já nasce vencida (RN29)
		obrigacao.setStatus(Situacoes.daObrigacao(dados.dataVencimento(), false, hoje));
		obrigacaoRepository.save(obrigacao);

		if (dados.recorrencia() != null) {
			recorrenciaService.criarPara(obrigacao, dados.recorrencia());
		}
		alertaService.verificarAposAlteracao(empresaId);
		return ObrigacaoResponse.de(obrigacao, null, hoje);
	}

	// RN33 a RN35
	@Transactional
	public ObrigacaoResponse atualizar(String empresaId, String obrigacaoId, ObrigacaoAtualizacaoRequest dados) {
		controleAcesso.exigir(empresaId, Permissao.EDITAR_OBRIGACOES);
		Obrigacao obrigacao = buscar(empresaId, obrigacaoId);
		Categoria categoria = categoriaService.buscarDisponivel(empresaId, dados.categoriaId());
		Pagamento pagamento = pagamentoRepository.findByObrigacaoIdAndCanceladoFalse(obrigacaoId).orElse(null);
		boolean alteraValorOuVencimento = obrigacao.getValor().compareTo(dados.valor()) != 0
				|| !obrigacao.getDataVencimento().equals(dados.dataVencimento());
		if (pagamento != null && alteraValorOuVencimento) {
			throw new RegraNegocioException(
					"Uma obrigação paga não pode ter valor nem vencimento alterados enquanto o pagamento estiver registrado.");
		}
		LocalDate hoje = LocalDate.now(clock);
		obrigacao.setDescricao(dados.descricao().trim());
		obrigacao.setValor(dados.valor());
		obrigacao.setCategoria(categoria);
		obrigacao.setDataVencimento(dados.dataVencimento());
		obrigacao.setStatus(Situacoes.daObrigacao(dados.dataVencimento(), pagamento != null, hoje));
		obrigacao.setUsuarioAtualizacao(controleAcesso.referenciaUsuarioAtual());
		obrigacaoRepository.saveAndFlush(obrigacao);
		alertaService.verificarAposAlteracao(empresaId);
		return ObrigacaoResponse.de(obrigacao, pagamento, hoje);
	}

	// RN36 a RN39; a confirmação (RN37) é pedida pelo front antes da chamada
	@Transactional
	public void excluir(String empresaId, String obrigacaoId, EscopoExclusao escopo) {
		controleAcesso.exigir(empresaId, Permissao.EXCLUIR_OBRIGACOES);
		Obrigacao obrigacao = buscar(empresaId, obrigacaoId);
		if (obrigacao.getRecorrencia() == null) {
			exclusaoObrigacaoService.remover(obrigacao);
		} else if (escopo == EscopoExclusao.ESTA_E_FUTURAS) {
			recorrenciaService.excluirAPartirDe(obrigacao);
		} else {
			recorrenciaService.excluirSomente(obrigacao);
		}
	}

	// Sem checagem de acesso: quem chama já verificou a permissão
	public List<Obrigacao> buscarFiltradas(String empresaId, FiltroObrigacoes filtro) {
		if (filtro.inicio() != null && filtro.fim() != null && filtro.inicio().isAfter(filtro.fim())) {
			throw new RegraNegocioException("A data inicial do período não pode ser posterior à data final.");
		}
		return obrigacaoRepository.findAll(especificacao(empresaId, filtro), ORDEM_VENCIMENTO);
	}

	public Map<String, Pagamento> pagamentosVigentes(Collection<Obrigacao> obrigacoes) {
		if (obrigacoes.isEmpty()) {
			return Map.of();
		}
		List<String> ids = obrigacoes.stream().map(Obrigacao::getId).toList();
		return pagamentoRepository.findByObrigacaoIdInAndCanceladoFalse(ids).stream()
				.collect(Collectors.toMap(pagamento -> pagamento.getObrigacao().getId(), Function.identity()));
	}

	private Obrigacao buscar(String empresaId, String obrigacaoId) {
		return obrigacaoRepository.findByIdAndEmpresaId(obrigacaoId, empresaId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Obrigação não encontrada."));
	}

	private static Specification<Obrigacao> especificacao(String empresaId, FiltroObrigacoes filtro) {
		return (raiz, consulta, criterios) -> {
			List<Predicate> condicoes = new ArrayList<>();
			condicoes.add(criterios.equal(raiz.get("empresa").get("id"), empresaId));
			if (filtro.inicio() != null) {
				condicoes.add(criterios.greaterThanOrEqualTo(raiz.<LocalDate>get("dataVencimento"), filtro.inicio()));
			}
			if (filtro.fim() != null) {
				condicoes.add(criterios.lessThanOrEqualTo(raiz.<LocalDate>get("dataVencimento"), filtro.fim()));
			}
			if (filtro.categoriaId() != null) {
				condicoes.add(criterios.equal(raiz.get("categoria").get("id"), filtro.categoriaId()));
			}
			if (filtro.status() != null) {
				condicoes.add(criterios.equal(raiz.get("status"), filtro.status()));
			}
			return criterios.and(condicoes.toArray(Predicate[]::new));
		};
	}
}
