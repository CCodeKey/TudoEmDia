package ted.com.tudoemdia.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ted.com.tudoemdia.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

	Optional<Usuario> findByEmailIgnoreCase(String email);

	// E-mail único no sistema (RN01)
	boolean existsByEmailIgnoreCase(String email);
}
