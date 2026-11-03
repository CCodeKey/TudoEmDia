package ted.com.tudoemdia.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.repository.UsuarioRepository;

@Service 
public class UsuarioService {
    private UsuarioRepository userRepository;

    public UsuarioService(UsuarioRepository usuarioRepository){
        this.userRepository = usuarioRepository;
    }

    public List<Usuario> listar(){
        return userRepository.findAll();
    }

    public void salvar(Usuario user){
        user.setDataAceiteTermos(LocalDateTime.now()); // TODO: Remover essa parte!
        userRepository.save(user);
    }
}
