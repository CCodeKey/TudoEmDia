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
import ted.com.tudoemdia.dto.categoria.CategoriaRequest;
import ted.com.tudoemdia.dto.categoria.CategoriaResponse;
import ted.com.tudoemdia.service.CategoriaService;

@RestController
@RequestMapping("/api/empresas/{empresaId}/categorias")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@GetMapping
	public List<CategoriaResponse> listar(@PathVariable String empresaId) {
		return categoriaService.listar(empresaId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CategoriaResponse criar(@PathVariable String empresaId, @Valid @RequestBody CategoriaRequest dados) {
		return categoriaService.criar(empresaId, dados);
	}

	@DeleteMapping("/{categoriaId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable String empresaId, @PathVariable String categoriaId) {
		categoriaService.excluir(empresaId, categoriaId);
	}
}
