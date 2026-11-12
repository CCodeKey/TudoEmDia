package ted.com.tudoemdia.security;

import java.io.Serializable;

// Dados do usuário guardados na sessão; nunca contém a senha
public record UsuarioAutenticado(String id, String nome, String email) implements Serializable {
}
