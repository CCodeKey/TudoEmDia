package ted.com.tudoemdia.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ted.com.tudoemdia.dto.ArquivoDownload;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;

// Guarda os arquivos em disco, fora de qualquer pasta pública; só a API os entrega (RN53)
@Service
public class ArmazenamentoService {

	private static final Logger log = LoggerFactory.getLogger(ArmazenamentoService.class);

	private final Path raiz;

	public ArmazenamentoService(@Value("${app.armazenamento.diretorio}") String diretorio) {
		this.raiz = Path.of(diretorio).toAbsolutePath().normalize();
	}

	// Valida o arquivo e devolve o caminho relativo a ser gravado no banco
	public String salvar(MultipartFile arquivo, String pasta) {
		TipoArquivo tipo = TipoArquivo.identificar(arquivo);
		Path destino = resolver(pasta + "/" + UUID.randomUUID() + tipo.extensao());
		try (InputStream entrada = arquivo.getInputStream()) {
			Files.createDirectories(destino.getParent());
			Files.copy(entrada, destino);
		} catch (IOException e) {
			throw new UncheckedIOException("Falha ao armazenar o arquivo", e);
		}
		removerSeDesfeito(destino);
		return raiz.relativize(destino).toString().replace('\\', '/');
	}

	public ArquivoDownload carregar(String caminho, String nomeArquivo) {
		Path arquivo = resolver(caminho);
		if (!Files.isReadable(arquivo)) {
			throw new RecursoNaoEncontradoException("Arquivo não encontrado.");
		}
		return new ArquivoDownload(new FileSystemResource(arquivo), nomeArquivo,
				TipoArquivo.porNome(caminho).mediaType());
	}

	// O arquivo só sai do disco se a exclusão no banco for confirmada
	public void removerAposCommit(String caminho) {
		Path arquivo = resolver(caminho);
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			apagar(arquivo);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				apagar(arquivo);
			}
		});
	}

	// Nome exibido ao usuário, sem pastas
	public String nomeOriginal(MultipartFile arquivo) {
		String nome = StringUtils.getFilename(StringUtils.cleanPath(String.valueOf(arquivo.getOriginalFilename())));
		return StringUtils.hasText(nome) ? nome : "arquivo";
	}

	// Evita arquivo órfão quando a transação que o registraria é desfeita
	private void removerSeDesfeito(Path arquivo) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status != STATUS_COMMITTED) {
					apagar(arquivo);
				}
			}
		});
	}

	private Path resolver(String caminho) {
		Path arquivo = raiz.resolve(caminho).normalize();
		if (!arquivo.startsWith(raiz)) {
			throw new IllegalArgumentException("Caminho de arquivo inválido");
		}
		return arquivo;
	}

	private void apagar(Path arquivo) {
		try {
			Files.deleteIfExists(arquivo);
		} catch (IOException e) {
			log.warn("Não foi possível remover o arquivo {}", raiz.relativize(arquivo), e);
		}
	}
}
