package ted.com.tudoemdia.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import ted.com.tudoemdia.model.enuns.Perfil;

// Confere a matriz de docs/04-permissoes_de_usuario.md
class PermissaoTest {

	@Test
	void contadorSoConsultaRecebeAlertasEExporta() {
		assertThat(Permissao.doPerfil(Perfil.CONTADOR)).containsExactlyInAnyOrder(
				Permissao.CONSULTAR_PAINEL,
				Permissao.CONSULTAR_OBRIGACOES,
				Permissao.CONSULTAR_DOCUMENTOS,
				Permissao.CONSULTAR_HISTORICO_PAGAMENTOS,
				Permissao.RECEBER_ALERTAS,
				Permissao.EXPORTAR_RELATORIOS);
	}

	@Test
	void financeiroGerenciaObrigacoesMasNaoDocumentosNemAcessos() {
		assertThat(Permissao.CADASTRAR_OBRIGACOES.permite(Perfil.FINANCEIRO)).isTrue();
		assertThat(Permissao.REGISTRAR_PAGAMENTOS.permite(Perfil.FINANCEIRO)).isTrue();
		assertThat(Permissao.CONFIGURAR_RECORRENCIA.permite(Perfil.FINANCEIRO)).isTrue();
		assertThat(Permissao.CADASTRAR_DOCUMENTOS.permite(Perfil.FINANCEIRO)).isFalse();
		assertThat(Permissao.CONVIDAR_USUARIOS.permite(Perfil.FINANCEIRO)).isFalse();
		assertThat(Permissao.CONFIGURAR_ALERTAS.permite(Perfil.FINANCEIRO)).isFalse();
		assertThat(Permissao.CANCELAR_PAGAMENTOS.permite(Perfil.FINANCEIRO)).isFalse();
	}

	@Test
	void proprietarioPodeTudo() {
		assertThat(Permissao.doPerfil(Perfil.PROPRIETARIO)).containsExactlyInAnyOrder(Permissao.values());
	}
}
