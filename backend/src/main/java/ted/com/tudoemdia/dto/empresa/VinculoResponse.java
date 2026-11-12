package ted.com.tudoemdia.dto.empresa;

import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.Perfil;

public record VinculoResponse(String id, String usuarioId, String nome, String email, Perfil perfil, boolean ativo,
		boolean recebeAlertas) {

	public static VinculoResponse de(UsuarioEmpresa vinculo) {
		Usuario usuario = vinculo.getUsuario();
		return new VinculoResponse(vinculo.getId(), usuario.getId(), usuario.getNome(), usuario.getEmail(),
				vinculo.getPerfil(), vinculo.isAtivo(), vinculo.isRecebeAlertas());
	}
}
