package ted.com.tudoemdia.dto.categoria;

import ted.com.tudoemdia.model.Categoria;

// padrao = categoria do sistema, que não pode ser excluída (RN41)
public record CategoriaResponse(String id, String nome, boolean padrao) {

	public static CategoriaResponse de(Categoria categoria) {
		return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getEmpresa() == null);
	}
}
