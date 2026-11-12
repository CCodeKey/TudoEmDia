package ted.com.tudoemdia.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.obrigacao.EscopoExclusao;
import ted.com.tudoemdia.dto.obrigacao.FiltroObrigacoes;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoAtualizacaoRequest;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoDetalheResponse;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoRequest;
import ted.com.tudoemdia.dto.obrigacao.ObrigacaoResponse;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.service.ObrigacaoService;

@RestController
@RequestMapping("/api/empresas/{empresaId}/obrigacoes")
public class ObrigacaoController {

	private final ObrigacaoService obrigacaoService;

	public ObrigacaoController(ObrigacaoService obrigacaoService) {
		this.obrigacaoService = obrigacaoService;
	}

	// Filtros por período de vencimento, categoria e situação (RN32)
	@GetMapping
	public List<ObrigacaoResponse> listar(@PathVariable String empresaId,
			@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim,
			@RequestParam(required = false) String categoriaId,
			@RequestParam(required = false) StatusObrigacao status) {
		return obrigacaoService.listar(empresaId, new FiltroObrigacoes(inicio, fim, categoriaId, status));
	}

	@GetMapping("/{obrigacaoId}")
	public ObrigacaoDetalheResponse detalhar(@PathVariable String empresaId, @PathVariable String obrigacaoId) {
		return obrigacaoService.detalhar(empresaId, obrigacaoId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ObrigacaoResponse criar(@PathVariable String empresaId, @Valid @RequestBody ObrigacaoRequest dados) {
		return obrigacaoService.criar(empresaId, dados);
	}

	@PutMapping("/{obrigacaoId}")
	public ObrigacaoResponse atualizar(@PathVariable String empresaId, @PathVariable String obrigacaoId,
			@Valid @RequestBody ObrigacaoAtualizacaoRequest dados) {
		return obrigacaoService.atualizar(empresaId, obrigacaoId, dados);
	}

	// Em obrigação recorrente, escopo define se exclui só esta ou também as futuras (RN39)
	@DeleteMapping("/{obrigacaoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable String empresaId, @PathVariable String obrigacaoId,
			@RequestParam(defaultValue = "SOMENTE_ESTA") EscopoExclusao escopo) {
		obrigacaoService.excluir(empresaId, obrigacaoId, escopo);
	}
}
