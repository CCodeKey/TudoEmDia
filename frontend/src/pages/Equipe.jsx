import { useState } from 'react';
import { Mail, Send, Users, X } from 'lucide-react';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { CampoSelecao, CampoTexto, Interruptor } from '../components/ui/Campos';
import { Carregando } from '../components/ui/Carregando';
import { Confirmacao } from '../components/ui/Confirmacao';
import { ErroCarregamento, Vazio } from '../components/ui/Estados';
import { SeloConvite } from '../components/ui/Selo';
import { useAcao } from '../hooks/useAcao';
import { useAuth } from '../hooks/useAuth';
import { useCarregar } from '../hooks/useCarregar';
import { useEmpresa } from '../hooks/useEmpresa';
import { equipeService } from '../services/empresaService';
import { formatarData, iniciais } from '../utils/formato';
import { PERFIS } from '../utils/rotulos';

// RF05: usuários vinculados, perfil, acesso e recebimento de alertas (RN18 a RN21, RN75)
function Membros() {
  const { empresa, pode } = useEmpresa();
  const { usuario } = useAuth();
  const { dados, carregando, erro, recarregar, alterarDados } = useCarregar(
    () => equipeService.membros(empresa.id),
    [empresa.id],
  );
  const { executar } = useAcao();
  const podeGerenciar = pode('GERENCIAR_ACESSOS');
  const podeAlertas = pode('CONFIGURAR_ALERTAS');

  // A mudança aparece na hora; se o servidor recusar (ex.: único proprietário), volta ao estado anterior
  const atualizar = async (membro, alteracao, sucesso) => {
    const substituir = (novo) => (lista) => lista.map((item) => (item.id === membro.id ? novo : item));
    alterarDados(substituir({ ...membro, ...alteracao }));
    const resultado = await executar(() => equipeService.atualizarMembro(empresa.id, membro.id, alteracao), { sucesso });
    alterarDados(substituir(resultado.ok ? resultado.resultado : membro));
  };

  if (carregando && !dados) return <Carregando />;
  if (erro) return <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />;

  return (
    <table className="tabela">
      <thead>
        <tr>
          <th>Pessoa</th>
          <th>Perfil</th>
          <th>Acesso</th>
          <th>Recebe alertas</th>
        </tr>
      </thead>
      <tbody>
        {dados.map((membro) => (
          <tr key={membro.id}>
            <td className="tabela__principal" data-rotulo="">
              <span className="linha" style={{ flexWrap: 'nowrap', gap: 10 }}>
                <span className="avatar" aria-hidden>
                  {iniciais(membro.nome)}
                </span>
                <span style={{ minWidth: 0, textAlign: 'left' }}>
                  {membro.nome}
                  {membro.usuarioId === usuario.id && <span className="texto-fraco"> (você)</span>}
                  <span className="texto-fraco" style={{ display: 'block', fontWeight: 400 }}>
                    {membro.email}
                  </span>
                </span>
              </span>
            </td>
            <td data-rotulo="Perfil">
              {podeGerenciar ? (
                <select
                  className="entrada entrada--pequena"
                  style={{ width: 'auto' }}
                  value={membro.perfil}
                  aria-label={`Perfil de ${membro.nome}`}
                  onChange={(evento) => atualizar(membro, { perfil: evento.target.value }, 'Perfil atualizado.')}
                >
                  {Object.entries(PERFIS).map(([valor, rotulo]) => (
                    <option key={valor} value={valor}>
                      {rotulo}
                    </option>
                  ))}
                </select>
              ) : (
                PERFIS[membro.perfil]
              )}
            </td>
            <td data-rotulo="Acesso">
              <span className="linha" style={{ flexWrap: 'nowrap' }}>
                <Interruptor
                  rotulo={`Acesso de ${membro.nome}`}
                  checked={membro.ativo}
                  disabled={!podeGerenciar}
                  onChange={(evento) =>
                    atualizar(membro, { ativo: evento.target.checked }, evento.target.checked ? 'Acesso reativado.' : 'Acesso desativado.')
                  }
                />
                <span className="texto-fraco">{membro.ativo ? 'Ativo' : 'Desativado'}</span>
              </span>
            </td>
            <td data-rotulo="Recebe alertas">
              <Interruptor
                rotulo={`Alertas por e-mail para ${membro.nome}`}
                checked={membro.recebeAlertas}
                disabled={!podeAlertas || !membro.ativo}
                onChange={(evento) => atualizar(membro, { recebeAlertas: evento.target.checked }, 'Alertas atualizados.')}
              />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

// RF04: convites por e-mail para os perfis financeiro e contador (RN13 a RN17)
function Convites() {
  const { empresa } = useEmpresa();
  const { dados, carregando, erro, recarregar } = useCarregar(() => equipeService.convites(empresa.id), [empresa.id]);
  const [novo, setNovo] = useState({ email: '', perfil: 'FINANCEIRO' });
  const [cancelando, setCancelando] = useState(null);
  const envio = useAcao();
  const cancelamento = useAcao();

  const convidar = async (evento) => {
    evento.preventDefault();
    const resultado = await envio.executar(() => equipeService.convidar(empresa.id, novo), {
      sucesso: `Convite enviado para ${novo.email}.`,
    });
    if (resultado.ok) {
      setNovo({ ...novo, email: '' });
      recarregar();
    }
  };

  const cancelar = async () => {
    const resultado = await cancelamento.executar(() => equipeService.cancelarConvite(empresa.id, cancelando.id), {
      sucesso: 'Convite cancelado.',
    });
    if (resultado.ok) {
      setCancelando(null);
      recarregar();
    }
  };

  return (
    <>
      <form className="barra-filtros" style={{ borderBottom: '1px solid var(--cor-borda)' }} onSubmit={convidar}>
        <CampoTexto
          rotulo="E-mail da pessoa"
          type="email"
          pequeno
          value={novo.email}
          onChange={(evento) => setNovo({ ...novo, email: evento.target.value })}
          erro={envio.erro?.campos?.email}
          required
        />
        <CampoSelecao rotulo="Perfil" pequeno value={novo.perfil} onChange={(evento) => setNovo({ ...novo, perfil: evento.target.value })}>
          <option value="FINANCEIRO">Financeiro: cadastra obrigações e pagamentos</option>
          <option value="CONTADOR">Contador: consulta e exporta</option>
        </CampoSelecao>
        <Botao type="submit" variante="primario" icone={Send} carregando={envio.enviando} style={{ height: 34 }}>
          Enviar convite
        </Botao>
      </form>

      {carregando ? (
        <Carregando />
      ) : erro ? (
        <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />
      ) : !dados.length ? (
        <Vazio icone={Mail} titulo="Nenhum convite enviado" descricao="Convide sua equipe e seu contador. O convite vale por 7 dias." />
      ) : (
        <table className="tabela">
          <thead>
            <tr>
              <th>E-mail</th>
              <th>Perfil</th>
              <th>Situação</th>
              <th>Enviado</th>
              <th>Válido até</th>
              <th aria-label="Ações" />
            </tr>
          </thead>
          <tbody>
            {dados.map((convite) => (
              <tr key={convite.id}>
                <td className="tabela__principal" data-rotulo="">
                  {convite.email}
                </td>
                <td data-rotulo="Perfil">{PERFIS[convite.perfil]}</td>
                <td data-rotulo="Situação">
                  <SeloConvite situacao={convite.status} />
                </td>
                <td data-rotulo="Enviado">
                  {formatarData(convite.dataEnvio)}
                  <span className="texto-fraco" style={{ display: 'block' }}>
                    por {convite.convidadoPor}
                  </span>
                </td>
                <td data-rotulo="Válido até">{formatarData(convite.dataExpiracao)}</td>
                <td data-rotulo="">
                  <div className="tabela__acoes">
                    {convite.status === 'PENDENTE' && (
                      <Botao variante="fantasma" pequeno icone={X} onClick={() => setCancelando(convite)}>
                        Cancelar
                      </Botao>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Confirmacao
        aberto={Boolean(cancelando)}
        aoFechar={() => setCancelando(null)}
        aoConfirmar={cancelar}
        titulo="Cancelar convite?"
        mensagem={cancelando ? `O link enviado para ${cancelando.email} deixará de funcionar.` : ''}
        textoConfirmar="Cancelar convite"
        enviando={cancelamento.enviando}
      />
    </>
  );
}

export function Equipe() {
  const { pode } = useEmpresa();
  const [aba, setAba] = useState('membros');
  const podeConvidar = pode('CONVIDAR_USUARIOS');

  return (
    <div className="pagina">
      <CabecalhoPagina titulo="Equipe" descricao="Quem acessa esta empresa e com qual perfil." />
      <section className="cartao tabela-envoltorio">
        {podeConvidar && (
          <div className="abas" role="tablist" style={{ padding: '0 16px' }}>
            <button type="button" role="tab" className="aba" aria-selected={aba === 'membros'} onClick={() => setAba('membros')}>
              <Users size={15} aria-hidden style={{ verticalAlign: '-2px', marginRight: 6 }} />
              Pessoas
            </button>
            <button type="button" role="tab" className="aba" aria-selected={aba === 'convites'} onClick={() => setAba('convites')}>
              <Mail size={15} aria-hidden style={{ verticalAlign: '-2px', marginRight: 6 }} />
              Convites
            </button>
          </div>
        )}
        <div role="tabpanel">{aba === 'membros' ? <Membros /> : <Convites />}</div>
      </section>
    </div>
  );
}
