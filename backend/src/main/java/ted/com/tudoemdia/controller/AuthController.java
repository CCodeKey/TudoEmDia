package ted.com.tudoemdia.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ted.com.tudoemdia.dto.auth.CadastroRequest;
import ted.com.tudoemdia.dto.auth.LoginRequest;
import ted.com.tudoemdia.dto.auth.UsuarioResponse;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.UsuarioAutenticado;
import ted.com.tudoemdia.service.AuthService;
import ted.com.tudoemdia.service.UsuarioService;

// RF01 e RF02; o logout é tratado pelo Spring Security em POST /api/auth/logout
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UsuarioService usuarioService;
	private final AuthService authService;
	private final ControleAcesso controleAcesso;
	private final SecurityContextRepository securityContextRepository;

	public AuthController(UsuarioService usuarioService, AuthService authService, ControleAcesso controleAcesso,
			SecurityContextRepository securityContextRepository) {
		this.usuarioService = usuarioService;
		this.authService = authService;
		this.controleAcesso = controleAcesso;
		this.securityContextRepository = securityContextRepository;
	}

	// Gera o cookie XSRF-TOKEN; o front o reenvia no cabeçalho X-XSRF-TOKEN.
	// Com o CSRF desligado, só responde 204, para o front funcionar nos dois modos
	@GetMapping("/csrf")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void csrf(CsrfToken token) {
		if (token != null) {
			token.getToken();
		}
	}

	@PostMapping("/cadastro")
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest dados) {
		return usuarioService.cadastrar(dados);
	}

	@PostMapping("/login")
	public UsuarioResponse login(@Valid @RequestBody LoginRequest dados, HttpServletRequest request,
			HttpServletResponse response) {
		UsuarioAutenticado usuario = authService.autenticar(dados);

		// Nova sessão a cada login, evitando reaproveitar um identificador anterior
		HttpSession sessaoAnterior = request.getSession(false);
		if (sessaoAnterior != null) {
			sessaoAnterior.invalidate();
		}
		SecurityContext contexto = SecurityContextHolder.createEmptyContext();
		contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(usuario, null, List.of()));
		SecurityContextHolder.setContext(contexto);
		securityContextRepository.saveContext(contexto, request, response);
		return UsuarioResponse.de(usuario);
	}

	@GetMapping("/me")
	public UsuarioResponse usuarioLogado() {
		return UsuarioResponse.de(controleAcesso.usuarioAtual());
	}
}
