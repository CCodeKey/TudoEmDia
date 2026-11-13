package ted.com.tudoemdia.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.pagamento.PagamentoRequest;
import ted.com.tudoemdia.dto.pagamento.PagamentoResponse;
import ted.com.tudoemdia.service.PagamentoService;

@RestController
@RequestMapping("/api/empresas/{empresaId}")
public class PagamentoController {

	private final PagamentoService pagamentoService;

	public PagamentoController(PagamentoService pagamentoService) {
		this.pagamentoService = pagamentoService;
	}

	@PostMapping("/obrigacoes/{obrigacaoId}/pagamento")
	@ResponseStatus(HttpStatus.CREATED)
	public PagamentoResponse registrar(@PathVariable String empresaId, @PathVariable String obrigacaoId,
			@Valid @RequestBody PagamentoRequest dados) {
		return pagamentoService.registrar(empresaId, obrigacaoId, dados);
	}

	// Histórico, com período opcional pela data do pagamento
	@GetMapping("/pagamentos")
	public List<PagamentoResponse> historico(@PathVariable String empresaId,
			@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim) {
		return pagamentoService.historico(empresaId, inicio, fim);
	}

	@PostMapping("/pagamentos/{pagamentoId}/cancelamento")
	public PagamentoResponse cancelar(@PathVariable String empresaId, @PathVariable String pagamentoId) {
		return pagamentoService.cancelar(empresaId, pagamentoId);
	}
}
