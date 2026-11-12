package ted.com.tudoemdia.dto.auth;

import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.security.UsuarioAutenticado;

public record UsuarioResponse(String id, String nome, String email) {

	public static UsuarioResponse de(Usuario usuario) {
		return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
	}

	public static UsuarioResponse de(UsuarioAutenticado usuario) {
		return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email());
	}
}
