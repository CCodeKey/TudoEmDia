import { useState } from 'react';
import { Link } from 'react-router';
import { Botao } from '../ui/Botao';
import { CaixaSelecao, CampoSelecao, CampoTexto, CampoValor } from '../ui/Campos';
import { Aviso } from '../ui/Estados';
import { Modal } from '../ui/Modal';
import { useAcao } from '../../hooks/useAcao';
import { useCategorias } from '../../hooks/useCategorias';
import { useEmpresa } from '../../hooks/useEmpresa';
import { obrigacaoService } from '../../services/obrigacaoService';
import { hojeIso } from '../../utils/formato';
import { PERIODICIDADES } from '../../utils/rotulos';

// Cadastro (RF07) e edição (RF09) de obrigações; nos dois, a recorrência é opcional (RF16)
export function FormularioObrigacao(props) {
  // Montado só quando aberto, para começar sempre com os dados atuais
  return props.aberto ? <Conteudo {...props} /> : null;
}

function Conteudo({ aoFechar, obrigacao, paga = false, aoSalvar }) {
  const { empresa, pode } = useEmpresa();
  const { categorias } = useCategorias(empresa.id);
  const { executar, enviando, erro } = useAcao();
  const editando = Boolean(obrigacao);
  const jaRecorrente = Boolean(obrigacao?.recorrenciaId);
  const podeRepetir = pode('CONFIGURAR_RECORRENCIA') && !jaRecorrente;
  const [dados, setDados] = useState({
    descricao: obrigacao?.descricao ?? '',
    valor: obrigacao?.valor ?? '',
    categoriaId: obrigacao?.categoria.id ?? '',
    dataVencimento: obrigacao?.dataVencimento ?? hojeIso(),
    recorrente: false,
    periodicidade: 'MENSAL',
    dataFim: '',
  });
  const [errosLocais, setErrosLocais] = useState({});
  const erros = { ...(erro?.campos ?? {}), ...errosLocais };

  const alterar = (campo) => (evento) => {
    const valor = evento.target.type === 'checkbox' ? evento.target.checked : evento.target.value;
    setDados((atual) => ({ ...atual, [campo]: valor }));
    setErrosLocais((atual) => ({ ...atual, [campo]: undefined }));
  };

  async function salvar() {
    // Recorrente sem data: o primeiro vencimento é hoje
    const vencimento = dados.dataVencimento || (dados.recorrente ? hojeIso() : '');
    const locais = {};
    if (!dados.descricao.trim()) locais.descricao = 'Informe a descrição.';
    if (!(Number(dados.valor) > 0)) locais.valor = 'Informe um valor maior que zero.';
    if (!dados.categoriaId) locais.categoriaId = 'Escolha uma categoria.';
    if (!vencimento) locais.dataVencimento = 'Informe o vencimento.';
    if (dados.recorrente && dados.dataFim && dados.dataFim < vencimento) {
      locais.dataFim = 'A data final não pode ser anterior ao primeiro vencimento.';
    }
    setErrosLocais(locais);
    if (Object.keys(locais).length) return;

    const corpo = {
      descricao: dados.descricao.trim(),
      valor: Number(dados.valor),
      categoriaId: dados.categoriaId,
      dataVencimento: vencimento,
    };
    const recorrencia = dados.recorrente ? { periodicidade: dados.periodicidade, dataFim: dados.dataFim || null } : null;

    const resultado = await executar(
      async () => {
        if (!editando) {
          return obrigacaoService.criar(empresa.id, recorrencia ? { ...corpo, recorrencia } : corpo);
        }
        const atualizada = await obrigacaoService.atualizar(empresa.id, obrigacao.id, corpo);
        if (recorrencia) await obrigacaoService.tornarRecorrente(empresa.id, obrigacao.id, recorrencia);
        return atualizada;
      },
      {
        sucesso: !editando
          ? 'Obrigação cadastrada.'
          : recorrencia
            ? 'Obrigação atualizada e agora se repete.'
            : 'Obrigação atualizada.',
        mostrarErro: false,
      },
    );
    if (resultado.ok) {
      aoFechar();
      aoSalvar?.(resultado.resultado);
    }
  }

  return (
    <Modal
      aberto
      aoFechar={aoFechar}
      titulo={editando ? 'Editar obrigação' : 'Nova obrigação'}
      descricao={editando ? undefined : 'Conta, imposto, boleto ou qualquer valor a pagar.'}
      aoEnviar={salvar}
      rodape={
        <>
          <Botao onClick={aoFechar} disabled={enviando}>
            Cancelar
          </Botao>
          <Botao type="submit" variante="primario" carregando={enviando}>
            {editando ? 'Salvar alterações' : 'Cadastrar'}
          </Botao>
        </>
      }
    >
      <div className="formulario">
        {erro && !Object.keys(erro.campos ?? {}).length && <Aviso tom="perigo">{erro.message}</Aviso>}
        <CampoTexto
          rotulo="Descrição"
          value={dados.descricao}
          onChange={alterar('descricao')}
          erro={erros.descricao}
          placeholder="Ex.: DAS de novembro, aluguel da loja"
          maxLength={500}
        />
        <div className="formulario__linha">
          <CampoValor
            rotulo="Valor (R$)"
            value={dados.valor}
            onChange={alterar('valor')}
            erro={erros.valor}
            disabled={paga}
          />
          <CampoTexto
            rotulo="Vencimento"
            opcional={dados.recorrente}
            type="date"
            value={dados.dataVencimento}
            onChange={alterar('dataVencimento')}
            erro={erros.dataVencimento}
            ajuda={
              dados.recorrente
                ? 'Se não adicionar uma data, hoje será usado como data da recorrência.'
                : undefined
            }
            disabled={paga}
          />
        </div>
        {paga && (
          <Aviso tom="info">Valor e vencimento não podem ser alterados enquanto houver pagamento registrado.</Aviso>
        )}
        <CampoSelecao rotulo="Categoria" value={dados.categoriaId} onChange={alterar('categoriaId')} erro={erros.categoriaId}>
          <option value="">Selecione…</option>
          {categorias.map((categoria) => (
            <option key={categoria.id} value={categoria.id}>
              {categoria.nome}
            </option>
          ))}
        </CampoSelecao>

        {jaRecorrente && (
          <Aviso tom="info">
            Esta obrigação se repete ({PERIODICIDADES[obrigacao.periodicidade].toLowerCase()}). Para mudar a
            recorrência, use a tela <Link to={`/empresas/${empresa.id}/recorrencias`}>Recorrências</Link>.
          </Aviso>
        )}

        {podeRepetir && (
          <div className="pilha pilha--p">
            <CaixaSelecao checked={dados.recorrente} onChange={alterar('recorrente')}>
              <strong>{editando ? 'Tornar esta obrigação recorrente' : 'Repetir esta obrigação'}</strong>
              <span className="campo__opcional"> (opcional)</span>
              <span className="texto-fraco" style={{ display: 'block' }}>
                Novas ocorrências são criadas automaticamente a cada mês ou ano.
              </span>
            </CaixaSelecao>
            {dados.recorrente && (
              <div className="formulario__linha">
                <CampoSelecao rotulo="Periodicidade" value={dados.periodicidade} onChange={alterar('periodicidade')}>
                  <option value="MENSAL">Mensal</option>
                  <option value="ANUAL">Anual</option>
                </CampoSelecao>
                <CampoTexto
                  rotulo="Repetir até"
                  opcional
                  type="date"
                  value={dados.dataFim}
                  onChange={alterar('dataFim')}
                  erro={erros.dataFim}
                  ajuda="Sem data, repete por tempo indeterminado."
                />
              </div>
            )}
          </div>
        )}
      </div>
    </Modal>
  );
}
