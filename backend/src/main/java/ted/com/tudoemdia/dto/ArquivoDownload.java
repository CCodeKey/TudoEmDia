package ted.com.tudoemdia.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record ArquivoDownload(Resource conteudo, String nome, MediaType tipo) {
}
