package ted.com.tudoemdia.controller;

import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ted.com.tudoemdia.dto.obrigacao.AnexoResponse;
import ted.com.tudoemdia.service.AnexoService;

@RestController
@RequestMapping("/api/empresas/{empresaId}")
public class AnexoController {

	private final AnexoService anexoService;

	public AnexoController(AnexoService anexoService) {
		this.anexoService = anexoService;
	}

	@GetMapping("/obrigacoes/{obrigacaoId}/anexos")
	public List<AnexoResponse> listar(@PathVariable String empresaId, @PathVariable String obrigacaoId) {
		return anexoService.listar(empresaId, obrigacaoId);
	}

	@PostMapping(path = "/obrigacoes/{obrigacaoId}/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public AnexoResponse adicionar(@PathVariable String empresaId, @PathVariable String obrigacaoId,
			@RequestParam("arquivo") MultipartFile arquivo) {
		return anexoService.adicionar(empresaId, obrigacaoId, arquivo);
	}

	@GetMapping("/anexos/{anexoId}/arquivo")
	public ResponseEntity<Resource> baixar(@PathVariable String empresaId, @PathVariable String anexoId) {
		return Downloads.arquivo(anexoService.baixar(empresaId, anexoId));
	}

	@DeleteMapping("/anexos/{anexoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable String empresaId, @PathVariable String anexoId) {
		anexoService.remover(empresaId, anexoId);
	}
}
