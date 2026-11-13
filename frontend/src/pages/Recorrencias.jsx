import { useState } from 'react';
import { Pencil, Repeat } from 'lucide-react';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { CampoSelecao, CampoTexto, CampoValor, Interruptor } from '../components/ui/Campos';
import { Carregando } from '../components/ui/Carregando';
import { Aviso, ErroCarregamento, Vazio } from '../components/ui/Estados';
import { Modal } from '../components/ui/Modal';
import { Selo } from '../components/ui/Selo';
import { useAcao } from '../hooks/useAcao';
import { useCarregar } from '../hooks/useCarregar';
import { useCategorias } from '../hooks/useCategorias';
import { useEmpresa } from '../hooks/useEmpresa';
import { recorrenciaService } from '../services/obrigacaoService';
import { formatarData, formatarMoeda } from '../utils/formato';
import { PERIODICIDADES } from '../utils/rotulos';

function FormularioRecorrencia({ recorrencia, aoFechar, aoSalvar }) {
  const { empresa } = useEmpresa();
  const { categorias } = useCategorias(empresa.id);
  const { executar, enviando, erro } = useAcao();
  const [dados, setDados] = useState({
    descricao: recorrencia.descricao,
    valor: recorrencia.valor,
    categoriaId: recorrencia.categoria.id,
    periodicidade: recorrencia.periodicidade,
    dataFim: recorrencia.dataFim ?? '',
    ativa: recorrencia.ativa,
  });
  const alterar = (campo) => (evento) =>
    setDados({ ...dados, [campo]: evento.target.type === 'checkbox' ? evento.target.checked : evento.target.value });

  const salvar = async () => {
    const resultado = await executar(
      () =>
        recorrenciaService.atualizar(empresa.id, recorrencia.id, {
          ...dados,
          valor: Number(dados.valor),
          dataFim: dados.dataFim || null,
        }),
      { sucesso: 'Recorrência atualizada.', mostrarErro: false },
    );
    if (resultado.ok) {
      aoFechar();
      aoSalvar();
    }
  };

  return (
    <Modal
      aberto
      aoFechar={aoFechar}
      titulo="Editar recorrência"
      aoEnviar={salvar}
      rodape={
        <>
          <Botao onClick={aoFechar}>Cancelar</Botao>
          <Botao type="submit" variante="primario" carregando={enviando}>
            Salvar
          </Botao>
        </>
      }
    >
      <div className="formulario">
        <Aviso tom="info">As mudanças valem só para as próximas ocorrências ainda não pagas.</Aviso>
        {erro && !Object.keys(erro.campos ?? {}).length && <Aviso tom="perigo">{erro.message}</Aviso>}
        <CampoTexto rotulo="Descrição" value={dados.descricao} onChange={alterar('descricao')} erro={erro?.campos?.descricao} />
        <div className="formulario__linha">
          <CampoValor rotulo="Valor (R$)" value={dados.valor} onChange={alterar('valor')} erro={erro?.campos?.valor} />
          <CampoSelecao rotulo="Categoria" value={dados.categoriaId} onChange={alterar('categoriaId')}>
            {categorias.map((categoria) => (
              <option key={categoria.id} value={categoria.id}>
                {categoria.nome}
              </option>
            ))}
          </CampoSelecao>
        </div>
        <div className="formulario__linha">
          <CampoSelecao rotulo="Periodicidade" value={dados.periodicidade} onChange={alterar('periodicidade')}>
            <option value="MENSAL">Mensal</option>
            <option value="ANUAL">Anual</option>
          </CampoSelecao>
          <CampoTexto
            rotulo="Repetir até"
            opcional
            type="date"
            min={recorrencia.dataInicio}
            value={dados.dataFim}
            onChange={alterar('dataFim')}
          />
        </div>
        <label className="linha" style={{ cursor: 'pointer' }}>
          <Interruptor rotulo="Recorrência ativa" checked={dados.ativa} onChange={alterar('ativa')} />
          <span>
            <strong>Gerar novas ocorrências</strong>
            <span className="texto-fraco" style={{ display: 'block' }}>
              Desligada, a recorrência para de criar obrigações.
            </span>
          </span>
        </label>
      </div>
    </Modal>
  );
}

// RF16: obrigações que se repetem todo mês ou todo ano
export function Recorrencias() {
  const { empresa, pode } = useEmpresa();
  const { dados, carregando, erro, recarregar } = useCarregar(() => recorrenciaService.listar(empresa.id), [empresa.id]);
  const [editando, setEditando] = useState(null);
  const podeEditar = pode('CONFIGURAR_RECORRENCIA');

  return (
    <div className="pagina">
      <CabecalhoPagina
        titulo="Recorrências"
        descricao="Obrigações que se repetem. A próxima ocorrência é criada automaticamente."
      />
      <section className="cartao tabela-envoltorio">
        {carregando ? (
          <Carregando />
        ) : erro ? (
          <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />
        ) : !dados.length ? (
          <Vazio
            icone={Repeat}
            titulo="Nenhuma recorrência"
            descricao='Ao cadastrar uma obrigação, marque "Repetir esta obrigação" para que ela se repita todo mês ou todo ano.'
          />
        ) : (
          <table className="tabela">
            <thead>
              <tr>
                <th>Descrição</th>
                <th>Periodicidade</th>
                <th>Desde</th>
                <th>Até</th>
                <th>Situação</th>
                <th className="alinhar-direita">Valor</th>
                {podeEditar && <th aria-label="Ações" />}
              </tr>
            </thead>
            <tbody>
              {dados.map((recorrencia) => (
                <tr key={recorrencia.id}>
                  <td className="tabela__principal" data-rotulo="">
                    {recorrencia.descricao}
                    <span className="texto-fraco" style={{ display: 'block', fontWeight: 400 }}>
                      {recorrencia.categoria.nome}
                    </span>
                  </td>
                  <td data-rotulo="Periodicidade">{PERIODICIDADES[recorrencia.periodicidade]}</td>
                  <td data-rotulo="Desde">{formatarData(recorrencia.dataInicio)}</td>
                  <td data-rotulo="Até">{recorrencia.dataFim ? formatarData(recorrencia.dataFim) : 'Sem data final'}</td>
                  <td data-rotulo="Situação">
                    <Selo tom={recorrencia.ativa ? 'sucesso' : 'neutro'}>{recorrencia.ativa ? 'Ativa' : 'Encerrada'}</Selo>
                  </td>
                  <td className="alinhar-direita numero" data-rotulo="Valor">
                    {formatarMoeda(recorrencia.valor)}
                  </td>
                  {podeEditar && (
                    <td data-rotulo="">
                      <div className="tabela__acoes">
                        <Botao variante="fantasma" pequeno icone={Pencil} onClick={() => setEditando(recorrencia)}>
                          Editar
                        </Botao>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {editando && <FormularioRecorrencia recorrencia={editando} aoFechar={() => setEditando(null)} aoSalvar={recarregar} />}
    </div>
  );
}
