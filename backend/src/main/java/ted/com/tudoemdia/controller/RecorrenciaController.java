package ted.com.tudoemdia.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.obrigacao.RecorrenciaAtualizacaoRequest;
import ted.com.tudoemdia.dto.obrigacao.RecorrenciaRequest;
import ted.com.tudoemdia.dto.obrigacao.RecorrenciaResponse;
import ted.com.tudoemdia.service.RecorrenciaService;

@RestController
@RequestMapping("/api/empresas/{empresaId}")
public class RecorrenciaController {

	private final RecorrenciaService recorrenciaService;

	public RecorrenciaController(RecorrenciaService recorrenciaService) {
		this.recorrenciaService = recorrenciaService;
	}

	@GetMapping("/recorrencias")
	public List<RecorrenciaResponse> listar(@PathVariable String empresaId) {
		return recorrenciaService.listar(empresaId);
	}

	// Torna recorrente uma obrigação já cadastrada
	@PostMapping("/obrigacoes/{obrigacaoId}/recorrencia")
	@ResponseStatus(HttpStatus.CREATED)
	public RecorrenciaResponse configurar(@PathVariable String empresaId, @PathVariable String obrigacaoId,
			@Valid @RequestBody RecorrenciaRequest dados) {
		return recorrenciaService.configurar(empresaId, obrigacaoId, dados);
	}

	@PutMapping("/recorrencias/{recorrenciaId}")
	public RecorrenciaResponse atualizar(@PathVariable String empresaId, @PathVariable String recorrenciaId,
			@Valid @RequestBody RecorrenciaAtualizacaoRequest dados) {
		return recorrenciaService.atualizar(empresaId, recorrenciaId, dados);
	}
}
