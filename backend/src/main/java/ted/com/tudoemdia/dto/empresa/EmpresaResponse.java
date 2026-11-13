package ted.com.tudoemdia.dto.empresa;

import ted.com.tudoemdia.model.Empresa;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.Perfil;

// Empresa sob o ponto de vista do usuário logado, com o perfil dele nela
public record EmpresaResponse(String id, String nome, String cnpj, Perfil perfil) {

	public static EmpresaResponse de(UsuarioEmpresa vinculo) {
		Empresa empresa = vinculo.getEmpresa();
		return new EmpresaResponse(empresa.getId(), empresa.getNome(), empresa.getCnpj(), vinculo.getPerfil());
	}
}
