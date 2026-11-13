package ted.com.tudoemdia.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.alerta.ConfiguracaoAlertasRequest;
import ted.com.tudoemdia.dto.alerta.PrazoAlerta;
import ted.com.tudoemdia.service.AlertaService;

// Prazos dos alertas; os destinatários ficam em PATCH /usuarios/{vinculoId} (recebeAlertas)
@RestController
@RequestMapping("/api/empresas/{empresaId}/alertas")
public class AlertaController {

	private final AlertaService alertaService;

	public AlertaController(AlertaService alertaService) {
		this.alertaService = alertaService;
	}

	@GetMapping
	public List<PrazoAlerta> consultar(@PathVariable String empresaId) {
		return alertaService.consultar(empresaId);
	}

	@PutMapping
	public List<PrazoAlerta> configurar(@PathVariable String empresaId,
			@Valid @RequestBody ConfiguracaoAlertasRequest dados) {
		return alertaService.configurar(empresaId, dados);
	}
}
