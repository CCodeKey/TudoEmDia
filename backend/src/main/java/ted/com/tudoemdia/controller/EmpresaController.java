package ted.com.tudoemdia.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.empresa.EmpresaRequest;
import ted.com.tudoemdia.dto.empresa.EmpresaResponse;
import ted.com.tudoemdia.dto.empresa.PermissoesResponse;
import ted.com.tudoemdia.service.EmpresaService;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

	private final EmpresaService empresaService;

	public EmpresaController(EmpresaService empresaService) {
		this.empresaService = empresaService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EmpresaResponse criar(@Valid @RequestBody EmpresaRequest dados) {
		return empresaService.criar(dados);
	}

	@GetMapping
	public List<EmpresaResponse> listar() {
		return empresaService.listarDoUsuario();
	}

	@GetMapping("/{empresaId}")
	public EmpresaResponse detalhar(@PathVariable String empresaId) {
		return empresaService.detalhar(empresaId);
	}

	@GetMapping("/{empresaId}/permissoes")
	public PermissoesResponse permissoes(@PathVariable String empresaId) {
		return empresaService.permissoes(empresaId);
	}
}
