package ted.com.tudoemdia.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.empresa.ConvitePublicoResponse;
import ted.com.tudoemdia.dto.empresa.ConviteRequest;
import ted.com.tudoemdia.dto.empresa.ConviteResponse;
import ted.com.tudoemdia.dto.empresa.EmpresaResponse;
import ted.com.tudoemdia.service.ConviteService;

@RestController
@RequestMapping("/api")
public class ConviteController {

	private final ConviteService conviteService;

	public ConviteController(ConviteService conviteService) {
		this.conviteService = conviteService;
	}

	@PostMapping("/empresas/{empresaId}/convites")
	@ResponseStatus(HttpStatus.CREATED)
	public ConviteResponse convidar(@PathVariable String empresaId, @Valid @RequestBody ConviteRequest dados) {
		return conviteService.convidar(empresaId, dados);
	}

	@GetMapping("/empresas/{empresaId}/convites")
	public List<ConviteResponse> listar(@PathVariable String empresaId) {
		return conviteService.listar(empresaId);
	}

	@DeleteMapping("/empresas/{empresaId}/convites/{conviteId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancelar(@PathVariable String empresaId, @PathVariable String conviteId) {
		conviteService.cancelar(empresaId, conviteId);
	}

	// Público: quem recebeu o link vê a empresa e o e-mail antes de entrar ou se cadastrar
	@GetMapping("/convites/{token}")
	public ConvitePublicoResponse consultar(@PathVariable String token) {
		return conviteService.consultar(token);
	}

	@PostMapping("/convites/{token}/aceite")
	public EmpresaResponse aceitar(@PathVariable String token) {
		return conviteService.aceitar(token);
	}
}
