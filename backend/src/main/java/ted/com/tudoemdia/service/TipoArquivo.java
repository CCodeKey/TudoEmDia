package ted.com.tudoemdia.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import ted.com.tudoemdia.exception.RegraNegocioException;

// Tipos aceitos em anexos e documentos: PDF, JPG ou PNG com até 10 MB (RN28, RN50)
enum TipoArquivo {

	PDF(".pdf", MediaType.APPLICATION_PDF, new byte[] { '%', 'P', 'D', 'F' }, "pdf"),
	JPG(".jpg", MediaType.IMAGE_JPEG, new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }, "jpg", "jpeg"),
	PNG(".png", MediaType.IMAGE_PNG, new byte[] { (byte) 0x89, 'P', 'N', 'G' }, "png");

	static final long TAMANHO_MAXIMO = 10L * 1024 * 1024;
	private static final String MENSAGEM = "Arquivo inválido: são aceitos PDF, JPG ou PNG com até 10 MB.";

	private final String extensao;
	private final MediaType mediaType;
	private final byte[] assinatura;
	private final List<String> extensoesAceitas;

	TipoArquivo(String extensao, MediaType mediaType, byte[] assinatura, String... extensoesAceitas) {
		this.extensao = extensao;
		this.mediaType = mediaType;
		this.assinatura = assinatura;
		this.extensoesAceitas = List.of(extensoesAceitas);
	}

	String extensao() {
		return extensao;
	}

	MediaType mediaType() {
		return mediaType;
	}

	// Confere extensão e conteúdo, para que um arquivo renomeado não seja aceito
	static TipoArquivo identificar(MultipartFile arquivo) {
		if (arquivo == null || arquivo.isEmpty()) {
			throw new RegraNegocioException("Selecione um arquivo.");
		}
		if (arquivo.getSize() > TAMANHO_MAXIMO) {
			throw new RegraNegocioException(MENSAGEM);
		}
		TipoArquivo tipo = porNome(arquivo.getOriginalFilename());
		if (tipo == null || !tipo.confere(arquivo)) {
			throw new RegraNegocioException(MENSAGEM);
		}
		return tipo;
	}

	static TipoArquivo porNome(String nome) {
		if (nome == null || nome.lastIndexOf('.') < 0) {
			return null;
		}
		String extensaoDoNome = nome.substring(nome.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
		return Arrays.stream(values()).filter(tipo -> tipo.extensoesAceitas.contains(extensaoDoNome)).findFirst()
				.orElse(null);
	}

	private boolean confere(MultipartFile arquivo) {
		try (InputStream entrada = arquivo.getInputStream()) {
			return Arrays.equals(entrada.readNBytes(assinatura.length), assinatura);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
