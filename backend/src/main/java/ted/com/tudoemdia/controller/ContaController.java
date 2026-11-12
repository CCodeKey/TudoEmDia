package ted.com.tudoemdia.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.auth.DadosTitularResponse;
import ted.com.tudoemdia.dto.auth.ExclusaoContaRequest;
import ted.com.tudoemdia.service.TitularDadosService;

// Direitos do titular dos dados (RNF14)
@RestController
@RequestMapping("/api/conta")
public class ContaController {

	private final TitularDadosService titularDadosService;

	public ContaController(TitularDadosService titularDadosService) {
		this.titularDadosService = titularDadosService;
	}

	@GetMapping("/dados")
	public DadosTitularResponse dados() {
		return titularDadosService.consultar();
	}

	@PostMapping("/exclusao")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@Valid @RequestBody ExclusaoContaRequest dados, HttpServletRequest request) {
		titularDadosService.excluirConta(dados);
		// Encerra a sessão da conta removida
		HttpSession sessao = request.getSession(false);
		if (sessao != null) {
			sessao.invalidate();
		}
		SecurityContextHolder.clearContext();
	}
}
