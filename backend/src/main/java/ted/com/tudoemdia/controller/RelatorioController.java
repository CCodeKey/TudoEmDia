package ted.com.tudoemdia.controller;

import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.obrigacao.FiltroObrigacoes;
import ted.com.tudoemdia.model.enuns.StatusObrigacao;
import ted.com.tudoemdia.service.RelatorioService;

@RestController
@RequestMapping("/api/empresas/{empresaId}/relatorios")
public class RelatorioController {

	private final RelatorioService relatorioService;

	public RelatorioController(RelatorioService relatorioService) {
		this.relatorioService = relatorioService;
	}

	// PDF com os mesmos filtros da consulta de obrigações (RN92, RNF12)
	@GetMapping("/obrigacoes")
	public ResponseEntity<byte[]> obrigacoes(@PathVariable String empresaId,
			@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim,
			@RequestParam(required = false) String categoriaId,
			@RequestParam(required = false) StatusObrigacao status) {
		byte[] pdf = relatorioService.obrigacoesEPagamentos(empresaId,
				new FiltroObrigacoes(inicio, fim, categoriaId, status));
		return Downloads.pdf(pdf, "relatorio-obrigacoes-" + LocalDate.now() + ".pdf");
	}
}
