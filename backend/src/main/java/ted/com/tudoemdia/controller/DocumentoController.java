package ted.com.tudoemdia.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ted.com.tudoemdia.dto.documento.DocumentoAtualizacaoRequest;
import ted.com.tudoemdia.dto.documento.DocumentoRequest;
import ted.com.tudoemdia.dto.documento.DocumentoResponse;
import ted.com.tudoemdia.model.enuns.StatusDocumento;
import ted.com.tudoemdia.service.DocumentoService;

@RestController
@RequestMapping("/api/empresas/{empresaId}/documentos")
public class DocumentoController {

	private final DocumentoService documentoService;

	public DocumentoController(DocumentoService documentoService) {
		this.documentoService = documentoService;
	}

	@GetMapping
	public List<DocumentoResponse> listar(@PathVariable String empresaId,
			@RequestParam(required = false) String categoriaId,
			@RequestParam(required = false) StatusDocumento status) {
		return documentoService.listar(empresaId, categoriaId, status);
	}

	@GetMapping("/{documentoId}")
	public DocumentoResponse detalhar(@PathVariable String empresaId, @PathVariable String documentoId) {
		return documentoService.detalhar(empresaId, documentoId);
	}

	// multipart/form-data: campos descricao, categoriaId, dataValidade e o arquivo
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentoResponse criar(@PathVariable String empresaId, @Valid @ModelAttribute DocumentoRequest dados,
			@RequestParam("arquivo") MultipartFile arquivo) {
		return documentoService.criar(empresaId, dados, arquivo);
	}

	@PutMapping("/{documentoId}")
	public DocumentoResponse atualizar(@PathVariable String empresaId, @PathVariable String documentoId,
			@Valid @RequestBody DocumentoAtualizacaoRequest dados) {
		return documentoService.atualizar(empresaId, documentoId, dados);
	}

	@PutMapping(path = "/{documentoId}/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DocumentoResponse substituirArquivo(@PathVariable String empresaId, @PathVariable String documentoId,
			@RequestParam("arquivo") MultipartFile arquivo) {
		return documentoService.substituirArquivo(empresaId, documentoId, arquivo);
	}

	@GetMapping("/{documentoId}/arquivo")
	public ResponseEntity<Resource> baixar(@PathVariable String empresaId, @PathVariable String documentoId) {
		return Downloads.arquivo(documentoService.baixar(empresaId, documentoId));
	}

	@DeleteMapping("/{documentoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable String empresaId, @PathVariable String documentoId) {
		documentoService.excluir(empresaId, documentoId);
	}
}
