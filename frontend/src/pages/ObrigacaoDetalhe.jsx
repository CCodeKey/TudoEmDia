import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { Pencil, Repeat, Trash2 } from 'lucide-react';
import { FormularioObrigacao } from '../components/obrigacoes/FormularioObrigacao';
import { PainelAnexos } from '../components/obrigacoes/PainelAnexos';
import { PainelPagamento } from '../components/obrigacoes/PainelPagamento';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { CampoSelecao, CampoTexto } from '../components/ui/Campos';
import { Carregando } from '../components/ui/Carregando';
import { Confirmacao } from '../components/ui/Confirmacao';
import { ErroCarregamento } from '../components/ui/Estados';
import { Modal } from '../components/ui/Modal';
import { SeloObrigacao } from '../components/ui/Selo';
import { useAcao } from '../hooks/useAcao';
import { useCarregar } from '../hooks/useCarregar';
import { useEmpresa } from '../hooks/useEmpresa';
import { obrigacaoService } from '../services/obrigacaoService';
import { formatarData, formatarDataHora, formatarMoeda, plural } from '../utils/formato';
import { PERIODICIDADES } from '../utils/rotulos';

export function ObrigacaoDetalhe() {
  const { obrigacaoId } = useParams();
  const { empresa, pode } = useEmpresa();
  const navegar = useNavigate();
  const lista = `/empresas/${empresa.id}/obrigacoes`;
  const { dados, carregando, erro, recarregar } = useCarregar(
    () => obrigacaoService.detalhar(empresa.id, obrigacaoId),
    [empresa.id, obrigacaoId],
  );
  const [editando, setEditando] = useState(false);
  const [excluindo, setExcluindo] = useState(false);
  const [escopo, setEscopo] = useState('SOMENTE_ESTA');
  const [tornandoRecorrente, setTornandoRecorrente] = useState(false);
  const [recorrencia, setRecorrencia] = useState({ periodicidade: 'MENSAL', dataFim: '' });
  const { executar, enviando } = useAcao();

  if (carregando && !dados) return <Carregando />;
  if (erro) return <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />;

  const { obrigacao, anexos, pagamento } = dados;

  // Obrigação com pagamento não pode ser excluída (RN38)
  const excluir = async () => {
    const resultado = await executar(() => obrigacaoService.excluir(empresa.id, obrigacao.id, escopo), {
      sucesso: 'Obrigação excluída.',
    });
    if (resultado.ok) navegar(lista, { replace: true });
  };

  const salvarRecorrencia = async () => {
    const resultado = await executar(
      () =>
        obrigacaoService.tornarRecorrente(empresa.id, obrigacao.id, {
          periodicidade: recorrencia.periodicidade,
          dataFim: recorrencia.dataFim || null,
        }),
      { sucesso: 'Recorrência configurada. As próximas ocorrências já foram criadas.' },
    );
    if (resultado.ok) {
      setTornandoRecorrente(false);
      recarregar();
    }
  };

  return (
    <div className="pagina">
      <CabecalhoPagina
        titulo={obrigacao.descricao}
        voltar={{ para: lista, texto: 'Obrigações' }}
        acoes={
          <>
            {pode('EDITAR_OBRIGACOES') && (
              <Botao icone={Pencil} onClick={() => setEditando(true)}>
                Editar
              </Botao>
            )}
            {pode('EXCLUIR_OBRIGACOES') && (
              <Botao variante="perigo-suave" icone={Trash2} onClick={() => setExcluindo(true)}>
                Excluir
              </Botao>
            )}
          </>
        }
      />

      <div className="grade-detalhe">
        <section className="cartao">
          <div className="cartao__corpo pilha">
            <div className="linha linha--entre">
              <span className="valor-destaque numero">{formatarMoeda(obrigacao.valor)}</span>
              <SeloObrigacao situacao={obrigacao.status} pagaComAtraso={obrigacao.pagaComAtraso} />
            </div>
            <hr className="divisor" />
            <dl className="lista-dados lista-dados--colunas">
              <div>
                <dt>Vencimento</dt>
                <dd>
                  {formatarData(obrigacao.dataVencimento)}
                  {obrigacao.diasAtraso > 0 && (
                    <span className="texto-perigo"> · {plural(obrigacao.diasAtraso, 'dia', 'dias')} de atraso</span>
                  )}
                </dd>
              </div>
              <div>
                <dt>Categoria</dt>
                <dd>{obrigacao.categoria.nome}</dd>
              </div>
              <div>
                <dt>Recorrência</dt>
                <dd>
                  {obrigacao.recorrenciaId ? (
                    <Link to={`/empresas/${empresa.id}/recorrencias`} className="linha" style={{ display: 'inline-flex', gap: 4 }}>
                      <Repeat size={14} aria-hidden /> {PERIODICIDADES[obrigacao.periodicidade]}
                    </Link>
                  ) : pode('CONFIGURAR_RECORRENCIA') ? (
                    <Botao variante="fantasma" pequeno icone={Repeat} onClick={() => setTornandoRecorrente(true)} style={{ marginLeft: -12 }}>
                      Tornar recorrente
                    </Botao>
                  ) : (
                    'Não se repete'
                  )}
                </dd>
              </div>
              {obrigacao.atualizadoPor && (
                <div>
                  <dt>Última alteração</dt>
                  <dd>
                    {obrigacao.atualizadoPor}
                    <span className="texto-fraco" style={{ display: 'block' }}>
                      {formatarDataHora(obrigacao.dataAtualizacao)}
                    </span>
                  </dd>
                </div>
              )}
            </dl>
          </div>
        </section>

        <div className="pilha">
          <PainelPagamento key={pagamento?.id ?? 'sem-pagamento'} obrigacao={obrigacao} pagamento={pagamento} aoAlterar={recarregar} />
          <PainelAnexos obrigacaoId={obrigacao.id} anexos={anexos} aoAlterar={recarregar} />
        </div>
      </div>

      <FormularioObrigacao
        aberto={editando}
        aoFechar={() => setEditando(false)}
        obrigacao={obrigacao}
        paga={Boolean(pagamento)}
        aoSalvar={recarregar}
      />

      <Confirmacao
        aberto={excluindo}
        aoFechar={() => setExcluindo(false)}
        aoConfirmar={excluir}
        titulo="Excluir obrigação?"
        mensagem="Os anexos também serão excluídos. Esta ação não pode ser desfeita."
        textoConfirmar="Excluir"
        enviando={enviando}
      >
        {obrigacao.recorrenciaId && (
          <fieldset className="pilha pilha--p" style={{ border: 0, padding: 0, margin: 0 }}>
            <legend className="campo__rotulo" style={{ marginBottom: 8 }}>
              Esta obrigação se repete. O que deseja excluir?
            </legend>
            <label className="opcao">
              <input type="radio" name="escopo" checked={escopo === 'SOMENTE_ESTA'} onChange={() => setEscopo('SOMENTE_ESTA')} />
              Somente esta ocorrência
            </label>
            <label className="opcao">
              <input type="radio" name="escopo" checked={escopo === 'ESTA_E_FUTURAS'} onChange={() => setEscopo('ESTA_E_FUTURAS')} />
              Esta e as próximas (encerra a recorrência)
            </label>
          </fieldset>
        )}
      </Confirmacao>

      <Modal
        aberto={tornandoRecorrente}
        aoFechar={() => setTornandoRecorrente(false)}
        titulo="Tornar recorrente"
        descricao={`As próximas ocorrências terão vencimento no mesmo dia de ${formatarData(obrigacao.dataVencimento)}.`}
        aoEnviar={salvarRecorrencia}
        rodape={
          <>
            <Botao onClick={() => setTornandoRecorrente(false)}>Cancelar</Botao>
            <Botao type="submit" variante="primario" carregando={enviando}>
              Salvar
            </Botao>
          </>
        }
      >
        <div className="formulario__linha">
          <CampoSelecao
            rotulo="Periodicidade"
            value={recorrencia.periodicidade}
            onChange={(evento) => setRecorrencia({ ...recorrencia, periodicidade: evento.target.value })}
          >
            <option value="MENSAL">Mensal</option>
            <option value="ANUAL">Anual</option>
          </CampoSelecao>
          <CampoTexto
            rotulo="Repetir até"
            opcional
            type="date"
            min={obrigacao.dataVencimento}
            value={recorrencia.dataFim}
            onChange={(evento) => setRecorrencia({ ...recorrencia, dataFim: evento.target.value })}
          />
        </div>
      </Modal>
    </div>
  );
}
