import { useState } from 'react';
import { Download, FilePlus2, FileText, Pencil, Trash2 } from 'lucide-react';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { CaixaSelecao, CampoArquivo, CampoSelecao, CampoTexto } from '../components/ui/Campos';
import { Carregando } from '../components/ui/Carregando';
import { Confirmacao } from '../components/ui/Confirmacao';
import { Aviso, ErroCarregamento, Vazio } from '../components/ui/Estados';
import { Modal } from '../components/ui/Modal';
import { SeloDocumento } from '../components/ui/Selo';
import { useAcao } from '../hooks/useAcao';
import { useCarregar } from '../hooks/useCarregar';
import { useCategorias } from '../hooks/useCategorias';
import { useEmpresa } from '../hooks/useEmpresa';
import { documentoService } from '../services/documentoService';
import { salvarArquivo, validarArquivo } from '../utils/arquivos';
import { formatarData, formatarDataHora } from '../utils/formato';

// Cadastro (sem documento) e edição (com documento) — RN49 a RN51, RN56
function FormularioDocumento({ documento, aoFechar, aoSalvar }) {
  const { empresa } = useEmpresa();
  const { categorias } = useCategorias(empresa.id);
  const { executar, enviando, erro } = useAcao();
  const editando = Boolean(documento);
  const [dados, setDados] = useState({
    descricao: documento?.descricao ?? '',
    categoriaId: documento?.categoria.id ?? '',
    dataValidade: documento?.dataValidade ?? '',
    arquivado: documento?.status === 'ARQUIVADO',
  });
  const [arquivo, setArquivo] = useState(null);
  const [errosLocais, setErrosLocais] = useState({});
  const erros = { ...(erro?.campos ?? {}), ...errosLocais };

  const alterar = (campo) => (evento) =>
    setDados({ ...dados, [campo]: evento.target.type === 'checkbox' ? evento.target.checked : evento.target.value });

  const salvar = async () => {
    const locais = {};
    if (!dados.descricao.trim()) locais.descricao = 'Informe a descrição.';
    if (!dados.categoriaId) locais.categoriaId = 'Escolha uma categoria.';
    if (!editando || arquivo) {
      const problema = validarArquivo(arquivo);
      if (problema) locais.arquivo = problema;
    }
    setErrosLocais(locais);
    if (Object.keys(locais).length) return;

    const corpo = { descricao: dados.descricao.trim(), categoriaId: dados.categoriaId, dataValidade: dados.dataValidade || null };
    const resultado = await executar(
      async () => {
        if (!editando) return documentoService.criar(empresa.id, corpo, arquivo);
        await documentoService.atualizar(empresa.id, documento.id, { ...corpo, arquivado: dados.arquivado });
        if (arquivo) await documentoService.substituirArquivo(empresa.id, documento.id, arquivo);
        return null;
      },
      { sucesso: editando ? 'Documento atualizado.' : 'Documento cadastrado.', mostrarErro: false },
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
      titulo={editando ? 'Editar documento' : 'Novo documento'}
      descricao={editando ? undefined : 'Contratos, alvarás, certidões e outros documentos da empresa.'}
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
          placeholder="Ex.: Alvará de funcionamento 2026"
          maxLength={500}
        />
        <div className="formulario__linha">
          <CampoSelecao rotulo="Categoria" value={dados.categoriaId} onChange={alterar('categoriaId')} erro={erros.categoriaId}>
            <option value="">Selecione…</option>
            {categorias.map((categoria) => (
              <option key={categoria.id} value={categoria.id}>
                {categoria.nome}
              </option>
            ))}
          </CampoSelecao>
          <CampoTexto
            rotulo="Validade"
            opcional
            type="date"
            value={dados.dataValidade}
            onChange={alterar('dataValidade')}
            ajuda="Com validade, o documento aparece como válido ou vencido."
          />
        </div>
        <CampoArquivo
          rotulo={editando ? 'Substituir arquivo (opcional)' : 'Arquivo'}
          arquivo={arquivo}
          aoSelecionar={(selecionado) => {
            setArquivo(selecionado);
            setErrosLocais((atual) => ({ ...atual, arquivo: undefined }));
          }}
          erro={erros.arquivo}
          ajuda={editando ? `Atual: ${documento.nomeArquivo}.` : undefined}
        />
        {editando && (
          <CaixaSelecao checked={dados.arquivado} onChange={alterar('arquivado')}>
            <strong>Arquivar documento</strong>
            <span className="texto-fraco" style={{ display: 'block' }}>
              Mantém o documento guardado, fora dos controles de validade.
            </span>
          </CaixaSelecao>
        )}
      </div>
    </Modal>
  );
}

// RF13 a RF15: central de documentos da empresa
export function Documentos() {
  const { empresa, pode } = useEmpresa();
  const { categorias } = useCategorias(empresa.id);
  const [filtros, setFiltros] = useState({ categoriaId: '', status: '' });
  const { dados, carregando, erro, recarregar } = useCarregar(
    () => documentoService.listar(empresa.id, filtros),
    [empresa.id, filtros.categoriaId, filtros.status],
  );
  const [formulario, setFormulario] = useState(null);
  const [excluindo, setExcluindo] = useState(null);
  const { executar, enviando } = useAcao();
  const temFiltro = Boolean(filtros.categoriaId || filtros.status);

  const baixar = (documento) =>
    executar(async () => salvarArquivo(await documentoService.baixar(empresa.id, documento.id)));

  const excluir = async () => {
    const resultado = await executar(() => documentoService.excluir(empresa.id, excluindo.id), {
      sucesso: 'Documento excluído.',
    });
    if (resultado.ok) {
      setExcluindo(null);
      recarregar();
    }
  };

  return (
    <div className="pagina">
      <CabecalhoPagina
        titulo="Documentos"
        descricao="Documentos da empresa, com controle de validade."
        acoes={
          pode('CADASTRAR_DOCUMENTOS') && (
            <Botao variante="primario" icone={FilePlus2} onClick={() => setFormulario({})}>
              Novo documento
            </Botao>
          )
        }
      />

      <section className="cartao tabela-envoltorio">
        <div className="barra-filtros" style={{ borderBottom: '1px solid var(--cor-borda)' }}>
          <CampoSelecao
            rotulo="Categoria"
            pequeno
            value={filtros.categoriaId}
            onChange={(evento) => setFiltros({ ...filtros, categoriaId: evento.target.value })}
          >
            <option value="">Todas</option>
            {categorias.map((categoria) => (
              <option key={categoria.id} value={categoria.id}>
                {categoria.nome}
              </option>
            ))}
          </CampoSelecao>
          <CampoSelecao
            rotulo="Situação"
            pequeno
            value={filtros.status}
            onChange={(evento) => setFiltros({ ...filtros, status: evento.target.value })}
          >
            <option value="">Todas</option>
            <option value="ATIVO">Válido</option>
            <option value="VENCIDO">Vencido</option>
            <option value="ARQUIVADO">Arquivado</option>
          </CampoSelecao>
        </div>

        {carregando ? (
          <Carregando />
        ) : erro ? (
          <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />
        ) : !dados.length ? (
          <Vazio
            icone={FileText}
            titulo={temFiltro ? 'Nenhum documento encontrado' : 'Nenhum documento cadastrado'}
            descricao={
              temFiltro
                ? 'Ajuste os filtros para ver outros documentos.'
                : 'Guarde aqui alvarás, contratos e certidões, e acompanhe as datas de validade.'
            }
            acao={
              !temFiltro &&
              pode('CADASTRAR_DOCUMENTOS') && (
                <Botao variante="primario" icone={FilePlus2} onClick={() => setFormulario({})}>
                  Novo documento
                </Botao>
              )
            }
          />
        ) : (
          <table className="tabela">
            <thead>
              <tr>
                <th>Documento</th>
                <th>Categoria</th>
                <th>Validade</th>
                <th>Situação</th>
                <th aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {dados.map((documento) => (
                <tr key={documento.id}>
                  <td className="tabela__principal" data-rotulo="">
                    {documento.descricao}
                    <span className="texto-fraco" style={{ display: 'block', fontWeight: 400 }}>
                      {documento.nomeArquivo}
                      {documento.atualizadoPor &&
                        ` · alterado por ${documento.atualizadoPor} em ${formatarDataHora(documento.dataAtualizacao)}`}
                    </span>
                  </td>
                  <td data-rotulo="Categoria">{documento.categoria.nome}</td>
                  <td data-rotulo="Validade">{documento.dataValidade ? formatarData(documento.dataValidade) : 'Sem validade'}</td>
                  <td data-rotulo="Situação">
                    <SeloDocumento situacao={documento.status} />
                  </td>
                  <td data-rotulo="">
                    <div className="tabela__acoes">
                      <Botao variante="fantasma" pequeno somenteIcone icone={Download} onClick={() => baixar(documento)}>
                        Baixar {documento.descricao}
                      </Botao>
                      {pode('EDITAR_DOCUMENTOS') && (
                        <Botao variante="fantasma" pequeno somenteIcone icone={Pencil} onClick={() => setFormulario({ documento })}>
                          Editar {documento.descricao}
                        </Botao>
                      )}
                      {pode('EXCLUIR_DOCUMENTOS') && (
                        <Botao variante="fantasma" pequeno somenteIcone icone={Trash2} onClick={() => setExcluindo(documento)}>
                          Excluir {documento.descricao}
                        </Botao>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {formulario && (
        <FormularioDocumento documento={formulario.documento} aoFechar={() => setFormulario(null)} aoSalvar={recarregar} />
      )}

      <Confirmacao
        aberto={Boolean(excluindo)}
        aoFechar={() => setExcluindo(null)}
        aoConfirmar={excluir}
        titulo="Excluir documento?"
        mensagem={excluindo ? `"${excluindo.descricao}" e o arquivo serão excluídos. Esta ação não pode ser desfeita.` : ''}
        textoConfirmar="Excluir"
        enviando={enviando}
      />
    </div>
  );
}
