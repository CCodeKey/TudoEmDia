package ted.com.tudoemdia.controller;

import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.painel.PainelResponse;
import ted.com.tudoemdia.service.PainelService;

@RestController
@RequestMapping("/api/empresas/{empresaId}/painel")
public class PainelController {

	private final PainelService painelService;

	public PainelController(PainelService painelService) {
		this.painelService = painelService;
	}

	// mes no formato AAAA-MM; sem ele, o mês atual
	@GetMapping
	public PainelResponse gerar(@PathVariable String empresaId, @RequestParam(required = false) String mes) {
		return painelService.gerar(empresaId, mes != null ? YearMonth.parse(mes) : null);
	}
}
