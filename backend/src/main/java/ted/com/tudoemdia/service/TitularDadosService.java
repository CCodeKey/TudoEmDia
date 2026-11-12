package ted.com.tudoemdia.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.auth.DadosTitularResponse;
import ted.com.tudoemdia.dto.auth.ExclusaoContaRequest;
import ted.com.tudoemdia.exception.AcessoNegadoException;
import ted.com.tudoemdia.exception.RecursoNaoEncontradoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.Convite;
import ted.com.tudoemdia.model.Usuario;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.model.enuns.StatusConvite;
import ted.com.tudoemdia.repository.ConviteRepository;
import ted.com.tudoemdia.repository.PagamentoRepository;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.repository.UsuarioRepository;
import ted.com.tudoemdia.security.ControleAcesso;

// Direitos do titular dos dados pessoais: acesso e exclusão (RNF14, LGPD)
@Service
public class TitularDadosService {

	static final String NOME_ANONIMO = "Usuário removido";

	private final SecureRandom aleatorio = new SecureRandom();
	private final UsuarioRepository usuarioRepository;
	private final UsuarioEmpresaRepository usuarioEmpresaRepository;
	private final ConviteRepository conviteRepository;
	private final PagamentoRepository pagamentoRepository;
	private final PasswordEncoder passwordEncoder;
	private final ControleAcesso controleAcesso;

	public TitularDadosService(UsuarioRepository usuarioRepository, UsuarioEmpresaRepository usuarioEmpresaRepository,
			ConviteRepository conviteRepository, PagamentoRepository pagamentoRepository,
			PasswordEncoder passwordEncoder, ControleAcesso controleAcesso) {
		this.usuarioRepository = usuarioRepository;
		this.usuarioEmpresaRepository = usuarioEmpresaRepository;
		this.conviteRepository = conviteRepository;
		this.pagamentoRepository = pagamentoRepository;
		this.passwordEncoder = passwordEncoder;
		this.controleAcesso = controleAcesso;
	}

	@Transactional(readOnly = true)
	public DadosTitularResponse consultar() {
		Usuario usuario = usuarioLogado();
		List<DadosTitularResponse.Vinculo> vinculos = usuarioEmpresaRepository.findByUsuarioId(usuario.getId()).stream()
				.map(vinculo -> new DadosTitularResponse.Vinculo(vinculo.getEmpresa().getNome(), vinculo.getPerfil(),
						vinculo.isAtivo(), vinculo.isRecebeAlertas()))
				.toList();
		List<DadosTitularResponse.ConviteRecebido> convites = conviteRepository
				.findByEmailConvidadoIgnoreCase(usuario.getEmail()).stream()
				.map(convite -> new DadosTitularResponse.ConviteRecebido(convite.getEmpresa().getNome(),
						convite.getPerfilConvidado(), convite.getStatus(), convite.getDataEnvio()))
				.toList();
		return new DadosTitularResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
				usuario.getDataCriacao(), usuario.getDataAceiteTermos(), vinculos, convites,
				pagamentoRepository.countByUsuarioId(usuario.getId()));
	}

	// Anonimiza em vez de apagar: o histórico de pagamentos e edições é preservado (RN21, RN73),
	// mas deixa de identificar a pessoa
	@Transactional
	public void excluirConta(ExclusaoContaRequest dados) {
		Usuario usuario = usuarioLogado();
		if (!passwordEncoder.matches(dados.senha(), usuario.getSenha())) {
			throw new AcessoNegadoException("Senha incorreta.");
		}
		List<UsuarioEmpresa> vinculos = usuarioEmpresaRepository.findByUsuarioId(usuario.getId());
		for (UsuarioEmpresa vinculo : vinculos) {
			// Toda empresa mantém ao menos um proprietário ativo (RN12)
			boolean unicoProprietario = vinculo.isAtivo() && vinculo.getPerfil() == Perfil.PROPRIETARIO
					&& usuarioEmpresaRepository.countByEmpresaIdAndPerfilAndAtivoTrue(vinculo.getEmpresa().getId(),
							Perfil.PROPRIETARIO) <= 1;
			if (unicoProprietario) {
				throw new RegraNegocioException("Você é o único proprietário de " + vinculo.getEmpresa().getNome()
						+ ". Defina outro proprietário antes de excluir sua conta.");
			}
		}

		String emailAnonimo = "removido-" + usuario.getId() + "@tudoemdia.invalid";
		for (Convite convite : conviteRepository.findByEmailConvidadoIgnoreCase(usuario.getEmail())) {
			convite.setEmailConvidado(emailAnonimo);
			if (convite.getStatus() == StatusConvite.PENDENTE) {
				convite.setStatus(StatusConvite.CANCELADO);
			}
		}
		conviteRepository.findByConvidanteIdAndStatus(usuario.getId(), StatusConvite.PENDENTE)
				.forEach(convite -> convite.setStatus(StatusConvite.CANCELADO));
		for (UsuarioEmpresa vinculo : vinculos) {
			vinculo.setAtivo(false);
			vinculo.setRecebeAlertas(false);
		}

		usuario.setNome(NOME_ANONIMO);
		usuario.setEmail(emailAnonimo);
		// Senha aleatória descartada: a conta não pode mais ser acessada
		usuario.setSenha(passwordEncoder.encode(senhaAleatoria()));
		usuario.setTentativasLoginFalhas(0);
		usuario.setBloqueadoAte(null);
	}

	private Usuario usuarioLogado() {
		return usuarioRepository.findById(controleAcesso.usuarioAtual().id())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
	}

	private String senhaAleatoria() {
		byte[] bytes = new byte[32];
		aleatorio.nextBytes(bytes);
		return Base64.getEncoder().encodeToString(bytes);
	}
}
