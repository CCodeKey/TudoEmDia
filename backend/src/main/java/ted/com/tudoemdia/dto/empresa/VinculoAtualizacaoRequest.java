package ted.com.tudoemdia.dto.empresa;

import ted.com.tudoemdia.model.enuns.Perfil;

// Campos nulos não são alterados (RN19, RN75)
public record VinculoAtualizacaoRequest(Perfil perfil, Boolean ativo, Boolean recebeAlertas) {
}
