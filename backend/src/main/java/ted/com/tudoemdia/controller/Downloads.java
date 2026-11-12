package ted.com.tudoemdia.controller;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ted.com.tudoemdia.dto.ArquivoDownload;

// Respostas de arquivo para download, com o nome original preservado
final class Downloads {

	private Downloads() {
	}

	static ResponseEntity<Resource> arquivo(ArquivoDownload arquivo) {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, anexo(arquivo.nome()))
				.contentType(arquivo.tipo())
				.body(arquivo.conteudo());
	}

	static ResponseEntity<byte[]> pdf(byte[] conteudo, String nome) {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, anexo(nome))
				.contentType(MediaType.APPLICATION_PDF)
				.body(conteudo);
	}

	private static String anexo(String nome) {
		return ContentDisposition.attachment().filename(nome, StandardCharsets.UTF_8).build().toString();
	}
}
