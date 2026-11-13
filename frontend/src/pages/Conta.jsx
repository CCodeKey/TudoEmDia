import { useState } from 'react';
import { ShieldCheck, Trash2 } from 'lucide-react';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { CampoSenha } from '../components/ui/Campos';
import { Carregando } from '../components/ui/Carregando';
import { Confirmacao } from '../components/ui/Confirmacao';
import { Aviso, ErroCarregamento } from '../components/ui/Estados';
import { Selo, SeloConvite } from '../components/ui/Selo';
import { useAcao } from '../hooks/useAcao';
import { useAuth } from '../hooks/useAuth';
import { useCarregar } from '../hooks/useCarregar';
import { contaService } from '../services/authService';
import { formatarDataHora } from '../utils/formato';
import { PERFIS } from '../utils/rotulos';

// Direitos do titular dos dados: consulta e exclusão (RNF14)
export function Conta() {
  const { encerrarLocalmente } = useAuth();
  const { dados, carregando, erro, recarregar } = useCarregar(() => contaService.meusDados(), []);
  const [confirmando, setConfirmando] = useState(false);
  const [senha, setSenha] = useState('');
  const { executar, enviando, erro: erroExclusao, limparErro } = useAcao();

  if (carregando) return <Carregando />;
  if (erro) return <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />;

  const excluir = async () => {
    const resultado = await executar(() => contaService.excluir(senha), {
      sucesso: 'Sua conta foi excluída.',
      mostrarErro: false,
    });
    if (resultado.ok) encerrarLocalmente();
  };

  return (
    <div className="pagina">
      <CabecalhoPagina
        titulo="Minha conta"
        descricao="Seus dados pessoais guardados pelo TudoEmDia."
        voltar={{ para: '/', texto: 'Voltar ao sistema' }}
      />

      <section className="cartao">
        <div className="cartao__cabecalho">
          <h2>Dados pessoais</h2>
          <Selo tom="marca" semPonto>
            <ShieldCheck size={14} aria-hidden /> LGPD
          </Selo>
        </div>
        <div className="cartao__corpo">
          <dl className="lista-dados lista-dados--colunas">
            <div>
              <dt>Nome</dt>
              <dd>{dados.nome}</dd>
            </div>
            <div>
              <dt>E-mail</dt>
              <dd>{dados.email}</dd>
            </div>
            <div>
              <dt>Conta criada em</dt>
              <dd>{formatarDataHora(dados.dataCadastro)}</dd>
            </div>
            <div>
              <dt>Aceite dos termos e da privacidade</dt>
              <dd>{formatarDataHora(dados.dataAceiteTermos)}</dd>
            </div>
            <div>
              <dt>Pagamentos registrados por você</dt>
              <dd>{dados.pagamentosRegistrados}</dd>
            </div>
          </dl>
        </div>
      </section>

      <section className="cartao tabela-envoltorio">
        <div className="cartao__cabecalho" style={{ paddingBottom: 14 }}>
          <h2>Empresas e convites</h2>
        </div>
        <table className="tabela">
          <thead>
            <tr>
              <th>Empresa</th>
              <th>Perfil</th>
              <th>Situação</th>
            </tr>
          </thead>
          <tbody>
            {dados.vinculos.map((vinculo) => (
              <tr key={`v-${vinculo.empresa}`}>
                <td className="tabela__principal" data-rotulo="">
                  {vinculo.empresa}
                </td>
                <td data-rotulo="Perfil">{PERFIS[vinculo.perfil]}</td>
                <td data-rotulo="Situação">
                  <Selo tom={vinculo.ativo ? 'sucesso' : 'neutro'}>{vinculo.ativo ? 'Acesso ativo' : 'Acesso desativado'}</Selo>
                </td>
              </tr>
            ))}
            {dados.convitesRecebidos.map((convite) => (
              <tr key={`c-${convite.empresa}-${convite.dataEnvio}`}>
                <td className="tabela__principal" data-rotulo="">
                  {convite.empresa} <span className="texto-fraco">(convite)</span>
                </td>
                <td data-rotulo="Perfil">{PERFIS[convite.perfil]}</td>
                <td data-rotulo="Situação">
                  <SeloConvite situacao={convite.status} />
                </td>
              </tr>
            ))}
            {!dados.vinculos.length && !dados.convitesRecebidos.length && (
              <tr>
                <td colSpan={3} className="texto-secundario" data-rotulo="">
                  Você ainda não participa de nenhuma empresa.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>

      <section className="cartao cartao--perigo">
        <div className="cartao__cabecalho">
          <h2>Excluir conta</h2>
        </div>
        <div className="cartao__corpo pilha">
          <p className="texto-secundario">
            Seu nome e e-mail serão removidos e você perderá o acesso a todas as empresas. Pagamentos e alterações que
            você registrou continuam no histórico das empresas, sem identificar você.
          </p>
          <div>
            <Botao variante="perigo-suave" icone={Trash2} onClick={() => setConfirmando(true)}>
              Excluir minha conta
            </Botao>
          </div>
        </div>
      </section>

      <Confirmacao
        aberto={confirmando}
        aoFechar={() => {
          setConfirmando(false);
          setSenha('');
          limparErro();
        }}
        aoConfirmar={excluir}
        titulo="Excluir sua conta?"
        mensagem="Esta ação não pode ser desfeita. Digite sua senha para confirmar."
        textoConfirmar="Excluir conta"
        enviando={enviando}
      >
        {erroExclusao && <Aviso tom="perigo">{erroExclusao.message}</Aviso>}
        <CampoSenha
          rotulo="Senha"
          autoComplete="current-password"
          value={senha}
          onChange={(evento) => setSenha(evento.target.value)}
        />
      </Confirmacao>
    </div>
  );
}
