package ted.com.tudoemdia.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.service.UsuarioService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController 
@RequestMapping("user")
public class UsuarioController {
    private UsuarioService userService;

    public UsuarioController(UsuarioService usuarioService){
        this.userService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return userService.listar();
    }

    @PostMapping("save")
    public void salvarUsuario(@RequestBody Usuario user) {
        userService.salvar(user);
    }
    
}
