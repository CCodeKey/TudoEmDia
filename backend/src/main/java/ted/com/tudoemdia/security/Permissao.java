package ted.com.tudoemdia.security;

import static ted.com.tudoemdia.model.enuns.Perfil.CONTADOR;
import static ted.com.tudoemdia.model.enuns.Perfil.FINANCEIRO;
import static ted.com.tudoemdia.model.enuns.Perfil.PROPRIETARIO;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import ted.com.tudoemdia.model.enuns.Perfil;

// Matriz de permissões (docs/04-permissoes_de_usuario.md, RN22)
public enum Permissao {

	CONSULTAR_PAINEL(PROPRIETARIO, FINANCEIRO, CONTADOR),
	CONSULTAR_OBRIGACOES(PROPRIETARIO, FINANCEIRO, CONTADOR),
	CADASTRAR_OBRIGACOES(PROPRIETARIO, FINANCEIRO),
	EDITAR_OBRIGACOES(PROPRIETARIO, FINANCEIRO),
	EXCLUIR_OBRIGACOES(PROPRIETARIO, FINANCEIRO),
	CONSULTAR_DOCUMENTOS(PROPRIETARIO, FINANCEIRO, CONTADOR),
	CADASTRAR_DOCUMENTOS(PROPRIETARIO),
	EDITAR_DOCUMENTOS(PROPRIETARIO),
	EXCLUIR_DOCUMENTOS(PROPRIETARIO),
	CONFIGURAR_RECORRENCIA(PROPRIETARIO, FINANCEIRO),
	REGISTRAR_PAGAMENTOS(PROPRIETARIO, FINANCEIRO),
	CONSULTAR_HISTORICO_PAGAMENTOS(PROPRIETARIO, FINANCEIRO, CONTADOR),
	CONFIGURAR_ALERTAS(PROPRIETARIO),
	RECEBER_ALERTAS(PROPRIETARIO, FINANCEIRO, CONTADOR),
	CONVIDAR_USUARIOS(PROPRIETARIO),
	CONSULTAR_USUARIOS(PROPRIETARIO),
	GERENCIAR_ACESSOS(PROPRIETARIO),
	EXPORTAR_RELATORIOS(PROPRIETARIO, FINANCEIRO, CONTADOR),

	// Fora da matriz, definidas nas regras de negócio
	GERENCIAR_CATEGORIAS(PROPRIETARIO, FINANCEIRO), // RN42
	CANCELAR_PAGAMENTOS(PROPRIETARIO); // RN70

	private final Set<Perfil> perfis;

	Permissao(Perfil... perfis) {
		this.perfis = EnumSet.copyOf(Arrays.asList(perfis));
	}

	public boolean permite(Perfil perfil) {
		return perfis.contains(perfil);
	}

	// Usado pelo front para montar o menu conforme o perfil (RN24)
	public static List<Permissao> doPerfil(Perfil perfil) {
		return Arrays.stream(values()).filter(permissao -> permissao.permite(perfil)).toList();
	}
}
