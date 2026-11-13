package ted.com.tudoemdia.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ted.com.tudoemdia.dto.empresa.EmpresaRequest;
import ted.com.tudoemdia.dto.empresa.EmpresaResponse;
import ted.com.tudoemdia.dto.empresa.PermissoesResponse;
import ted.com.tudoemdia.exception.ConflitoException;
import ted.com.tudoemdia.exception.RegraNegocioException;
import ted.com.tudoemdia.model.ConfiguracaoAlerta;
import ted.com.tudoemdia.model.Empresa;
import ted.com.tudoemdia.model.UsuarioEmpresa;
import ted.com.tudoemdia.model.enuns.Perfil;
import ted.com.tudoemdia.repository.ConfiguracaoAlertaRepository;
import ted.com.tudoemdia.repository.EmpresaRepository;
import ted.com.tudoemdia.repository.UsuarioEmpresaRepository;
import ted.com.tudoemdia.security.ControleAcesso;
import ted.com.tudoemdia.security.Permissao;

@Service
public class EmpresaService {

	private final EmpresaRepository empresaRepository;
	private final UsuarioEmpresaRepository usuarioEmpresaRepository;
	private final ConfiguracaoAlertaRepository configuracaoAlertaRepository;
	private final ControleAcesso controleAcesso;

	public EmpresaService(EmpresaRepository empresaRepository, UsuarioEmpresaRepository usuarioEmpresaRepository,
			ConfiguracaoAlertaRepository configuracaoAlertaRepository, ControleAcesso controleAcesso) {
		this.empresaRepository = empresaRepository;
		this.usuarioEmpresaRepository = usuarioEmpresaRepository;
		this.configuracaoAlertaRepository = configuracaoAlertaRepository;
		this.controleAcesso = controleAcesso;
	}

	// RF03: RN09, RN10
	@Transactional
	public EmpresaResponse criar(EmpresaRequest dados) {
		String cnpj = Cnpj.normalizar(dados.cnpj());
		if (!Cnpj.valido(cnpj)) {
			throw new RegraNegocioException("CNPJ inválido.");
		}
		if (empresaRepository.existsByCnpj(cnpj)) {
			throw new ConflitoException("Este CNPJ já está cadastrado.");
		}
		Empresa empresa = new Empresa();
		empresa.setNome(dados.nome().trim());
		empresa.setCnpj(cnpj);
		empresaRepository.save(empresa);

		UsuarioEmpresa vinculo = new UsuarioEmpresa();
		vinculo.setUsuario(controleAcesso.referenciaUsuarioAtual());
		vinculo.setEmpresa(empresa);
		vinculo.setPerfil(Perfil.PROPRIETARIO);
		usuarioEmpresaRepository.save(vinculo);

		// Todos os prazos de alerta começam ligados (RN76)
		for (int dias : AlertaService.PRAZOS) {
			ConfiguracaoAlerta configuracao = new ConfiguracaoAlerta();
			configuracao.setEmpresa(empresa);
			configuracao.setDiasAntecedencia(dias);
			configuracaoAlertaRepository.save(configuracao);
		}
		return EmpresaResponse.de(vinculo);
	}

	// Empresas que o usuário pode selecionar, com o perfil em cada uma (RN11)
	@Transactional(readOnly = true)
	public List<EmpresaResponse> listarDoUsuario() {
		return usuarioEmpresaRepository.findByUsuarioIdAndAtivoTrue(controleAcesso.usuarioAtual().id()).stream()
				.map(EmpresaResponse::de)
				.sorted(Comparator.comparing(EmpresaResponse::nome, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	@Transactional(readOnly = true)
	public EmpresaResponse detalhar(String empresaId) {
		return EmpresaResponse.de(controleAcesso.exigirVinculo(empresaId));
	}

	// Permite ao front esconder o que o perfil não pode usar (RN24)
	@Transactional(readOnly = true)
	public PermissoesResponse permissoes(String empresaId) {
		Perfil perfil = controleAcesso.exigirVinculo(empresaId).getPerfil();
		return new PermissoesResponse(perfil, Permissao.doPerfil(perfil));
	}
}
