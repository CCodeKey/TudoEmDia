package ted.com.tudoemdia.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.empresa.VinculoAtualizacaoRequest;
import ted.com.tudoemdia.dto.empresa.VinculoResponse;
import ted.com.tudoemdia.service.AcessoService;

@RestController
@RequestMapping("/api/empresas/{empresaId}/usuarios")
public class AcessoController {

	private final AcessoService acessoService;

	public AcessoController(AcessoService acessoService) {
		this.acessoService = acessoService;
	}

	@GetMapping
	public List<VinculoResponse> listar(@PathVariable String empresaId) {
		return acessoService.listar(empresaId);
	}

	@PatchMapping("/{vinculoId}")
	public VinculoResponse atualizar(@PathVariable String empresaId, @PathVariable String vinculoId,
			@RequestBody VinculoAtualizacaoRequest dados) {
		return acessoService.atualizar(empresaId, vinculoId, dados);
	}
}
