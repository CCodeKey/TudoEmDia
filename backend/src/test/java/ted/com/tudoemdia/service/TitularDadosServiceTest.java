package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import ted.com.tudoemdia.dto.auth.ExclusaoContaRequest;
import ted.com.tudoemdia.exception.AcessoNegadoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Convite;
import ted.com.tudoemdia.model.Empresa;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.model.enuns.StatusConvite;
import ted.com.tudoemdia.repository.ConviteRepository;
import ted.com.tudoemdia.repository.PagamentoRepository;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.repository.UsuarioRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.UsuarioAutenticado;

class TitularDadosServiceTest {

	private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
	private final UsuarioEmpresaRepository usuarioEmpresaRepository = mock(UsuarioEmpresaRepository.class);
	private final ConviteRepository conviteRepository = mock(ConviteRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final ControleAcesso controleAcesso = mock(ControleAcesso.class);
	private final TitularDadosService titularDadosService = new TitularDadosService(usuarioRepository,
			usuarioEmpresaRepository, conviteRepository, mock(PagamentoRepository.class), passwordEncoder,
			controleAcesso);
	private final Usuario usuario = new Usuario();
	private final UsuarioEmpresa vinculo = new UsuarioEmpresa();

	@BeforeEach
	void preparar() {
		usuario.setId("u1");
		usuario.setNome("Bruno");
		usuario.setEmail("bruno@teste.com");
		usuario.setSenha("hash");
		when(controleAcesso.usuarioAtual()).thenReturn(new UsuarioAutenticado("u1", "Bruno", "bruno@teste.com"));
		when(usuarioRepository.findById("u1")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("Senha123", "hash")).thenReturn(true);
		when(passwordEncoder.encode(any())).thenReturn("novo-hash");

		Empresa empresa = new Empresa();
		empresa.setId("e1");
		empresa.setNome("Padaria");
		vinculo.setEmpresa(empresa);
		vinculo.setUsuario(usuario);
		vinculo.setPerfil(Perfil.FINANCEIRO);
		when(usuarioEmpresaRepository.findByUsuarioId("u1")).thenReturn(List.of(vinculo));
	}

	@Test
	void exclusaoAnonimizaOsDadosEPreservaOHistorico() {
		Convite recebido = new Convite();
		recebido.setEmailConvidado("bruno@teste.com");
		when(conviteRepository.findByEmailConvidadoIgnoreCase("bruno@teste.com")).thenReturn(List.of(recebido));

		titularDadosService.excluirConta(new ExclusaoContaRequest("Senha123"));

		assertThat(usuario.getNome()).isEqualTo(TitularDadosService.NOME_ANONIMO);
		assertThat(usuario.getEmail()).isEqualTo("removido-u1@tudoemdia.invalid");
		assertThat(usuario.getSenha()).isEqualTo("novo-hash");
		assertThat(vinculo.isAtivo()).isFalse();
		assertThat(recebido.getEmailConvidado()).isEqualTo("removido-u1@tudoemdia.invalid");
		assertThat(recebido.getStatus()).isEqualTo(StatusConvite.CANCELADO);
	}

	@Test
	void exigeASenhaAtual() {
		assertThatThrownBy(() -> titularDadosService.excluirConta(new ExclusaoContaRequest("errada")))
				.isInstanceOf(AcessoNegadoException.class);
		assertThat(usuario.getNome()).isEqualTo("Bruno");
	}

	@Test
	void unicoProprietarioNaoPodeExcluirAConta() {
		vinculo.setPerfil(Perfil.PROPRIETARIO);
		when(usuarioEmpresaRepository.countByEmpresaIdAndPerfilAndAtivoTrue("e1", Perfil.PROPRIETARIO)).thenReturn(1L);

		assertThatThrownBy(() -> titularDadosService.excluirConta(new ExclusaoContaRequest("Senha123")))
				.isInstanceOf(RegraNegocioException.class);
		assertThat(vinculo.isAtivo()).isTrue();
	}
}
