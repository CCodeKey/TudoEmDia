package ted.com.tudoemdia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ted.com.tudoemdia.dto.empresa.VinculoAtualizacaoRequest;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.security.ControleAcesso;

class AcessoServiceTest {

	private final UsuarioEmpresaRepository repository = mock(UsuarioEmpresaRepository.class);
	private final AcessoService acessoService = new AcessoService(repository, mock(ControleAcesso.class));
	private final UsuarioEmpresa proprietario = new UsuarioEmpresa();

	@BeforeEach
	void prepararVinculo() {
		Usuario usuario = new Usuario();
		usuario.setNome("Ana");
		proprietario.setId("v1");
		proprietario.setUsuario(usuario);
		proprietario.setPerfil(Perfil.PROPRIETARIO);
		when(repository.findByIdAndEmpresaId("v1", "e1")).thenReturn(Optional.of(proprietario));
	}

	@Test
	void unicoProprietarioNaoPodeSerDesativadoNemRebaixado() {
		when(repository.countByEmpresaIdAndPerfilAndAtivoTrue("e1", Perfil.PROPRIETARIO)).thenReturn(1L);

		assertThatThrownBy(() -> acessoService.atualizar("e1", "v1", new VinculoAtualizacaoRequest(null, false, null)))
				.isInstanceOf(RegraNegocioException.class);
		assertThatThrownBy(() -> acessoService.atualizar("e1", "v1",
				new VinculoAtualizacaoRequest(Perfil.CONTADOR, null, null)))
				.isInstanceOf(RegraNegocioException.class);
		assertThat(proprietario.isAtivo()).isTrue();
	}

	@Test
	void proprietarioPodeSerDesativadoSeHouverOutro() {
		when(repository.countByEmpresaIdAndPerfilAndAtivoTrue("e1", Perfil.PROPRIETARIO)).thenReturn(2L);

		acessoService.atualizar("e1", "v1", new VinculoAtualizacaoRequest(null, false, null));

		assertThat(proprietario.isAtivo()).isFalse();
	}

	@Test
	void alterarSoOsAlertasNaoAfetaAcesso() {
		acessoService.atualizar("e1", "v1", new VinculoAtualizacaoRequest(null, null, false));

		assertThat(proprietario.isRecebeAlertas()).isFalse();
		assertThat(proprietario.isAtivo()).isTrue();
		assertThat(proprietario.getPerfil()).isEqualTo(Perfil.PROPRIETARIO);
	}
}
