package ted.com.tudoemdia.dto.empresa;

import java.util.List;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.security.Permissao;

public record PermissoesResponse(Perfil perfil, List<Permissao> permissoes) {
}
