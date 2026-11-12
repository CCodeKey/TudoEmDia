package ted.com.tudoemdia.dto.documento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import ted.com.tudoemdia.dto.categoria.CategoriaResponse;
import ted.com.tudoemdia.model.Documento;
import ted.com.tudoemdia.model.enuns.StatusDocumento;

// O caminho interno do arquivo nunca é exposto (RN53)
public record DocumentoResponse(
		String id,
		String descricao,
		CategoriaResponse categoria,
		LocalDate dataValidade,
		StatusDocumento status,
		String nomeArquivo,
		LocalDateTime dataCriacao,
		LocalDateTime dataAtualizacao,
		String atualizadoPor) { // RN56

	public static DocumentoResponse de(Documento documento) {
		return new DocumentoResponse(
				documento.getId(),
				documento.getDescricao(),
				CategoriaResponse.de(documento.getCategoria()),
				documento.getDataValidade(),
				documento.getStatus(),
				documento.getNomeArquivo(),
				documento.getDataCriacao(),
				documento.getDataAtualizacao(),
				documento.getUsuarioAtualizacao() != null ? documento.getUsuarioAtualizacao().getNome() : null);
	}
}
