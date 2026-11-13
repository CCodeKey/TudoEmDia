import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router';
import { FileDown, Plus, ReceiptText, Repeat } from 'lucide-react';
import { FormularioObrigacao } from '../components/obrigacoes/FormularioObrigacao';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { CampoSelecao, CampoTexto } from '../components/ui/Campos';
import { Carregando } from '../components/ui/Carregando';
import { ErroCarregamento, Vazio } from '../components/ui/Estados';
import { SeloObrigacao } from '../components/ui/Selo';
import { useAcao } from '../hooks/useAcao';
import { useCarregar } from '../hooks/useCarregar';
import { useCategorias } from '../hooks/useCategorias';
import { useEmpresa } from '../hooks/useEmpresa';
import { obrigacaoService, painelService } from '../services/obrigacaoService';
import { salvarArquivo } from '../utils/arquivos';
import { formatarData, formatarMoeda, hojeIso, mesAtual, paraIso, plural, primeiroEUltimoDia } from '../utils/formato';
import { PERIODICIDADES } from '../utils/rotulos';

function periodoDosProximosDias(dias) {
  const fim = new Date();
  fim.setDate(fim.getDate() + dias);
  return { inicio: hojeIso(), fim: paraIso(fim) };
}

const ATALHOS = [
  { rotulo: 'Este mês', periodo: () => primeiroEUltimoDia(mesAtual()) },
  { rotulo: 'Próximos 30 dias', periodo: () => periodoDosProximosDias(30) },
  { rotulo: 'Todas', periodo: () => ({ inicio: '', fim: '' }) },
];

// RF08: só obrigações da empresa selecionada, filtradas e pelo vencimento mais próximo (RN31, RN32)
export function Obrigacoes() {
  const { empresa, pode } = useEmpresa();
  const navegar = useNavigate();
  const [busca, setBusca] = useSearchParams();
  const [criando, setCriando] = useState(false);
  const { categorias } = useCategorias(empresa.id);
  const { executar, enviando: exportando } = useAcao();

  const filtros = {
    inicio: busca.get('inicio') ?? '',
    fim: busca.get('fim') ?? '',
    categoriaId: busca.get('categoriaId') ?? '',
    status: busca.get('status') ?? '',
  };
  const { dados, carregando, erro, recarregar } = useCarregar(
    () => obrigacaoService.listar(empresa.id, filtros),
    [empresa.id, busca.toString()],
  );

  const alterarFiltros = (novos) => {
    const proxima = new URLSearchParams(busca);
    Object.entries(novos).forEach(([campo, valor]) => (valor ? proxima.set(campo, valor) : proxima.delete(campo)));
    setBusca(proxima, { replace: true });
  };

  const exportar = () =>
    executar(async () => salvarArquivo(await painelService.relatorio(empresa.id, filtros)), {
      sucesso: 'Relatório gerado.',
    });

  const total = (dados ?? []).reduce((soma, item) => soma + Number(item.valor), 0);
  const temFiltro = Object.values(filtros).some(Boolean);

  return (
    <div className="pagina">
      <CabecalhoPagina
        titulo="Obrigações"
        descricao="Contas, impostos e boletos da empresa."
        acoes={
          <>
            {pode('EXPORTAR_RELATORIOS') && (
              <Botao icone={FileDown} carregando={exportando} onClick={exportar}>
                Exportar PDF
              </Botao>
            )}
            {pode('CADASTRAR_OBRIGACOES') && (
              <Botao variante="primario" icone={Plus} onClick={() => setCriando(true)}>
                Nova obrigação
              </Botao>
            )}
          </>
        }
      />

      <section className="cartao tabela-envoltorio">
        <div className="barra-filtros" style={{ borderBottom: '1px solid var(--cor-borda)' }}>
          <CampoTexto
            rotulo="Vencimento de"
            type="date"
            pequeno
            value={filtros.inicio}
            onChange={(evento) => alterarFiltros({ inicio: evento.target.value })}
          />
          <CampoTexto
            rotulo="até"
            type="date"
            pequeno
            value={filtros.fim}
            onChange={(evento) => alterarFiltros({ fim: evento.target.value })}
          />
          <CampoSelecao
            rotulo="Categoria"
            pequeno
            value={filtros.categoriaId}
            onChange={(evento) => alterarFiltros({ categoriaId: evento.target.value })}
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
            onChange={(evento) => alterarFiltros({ status: evento.target.value })}
          >
            <option value="">Todas</option>
            <option value="PENDENTE">Pendente</option>
            <option value="VENCIDA">Vencida</option>
            <option value="PAGA">Paga</option>
          </CampoSelecao>
          <div className="linha">
            {ATALHOS.map((atalho) => (
              <Botao key={atalho.rotulo} variante="fantasma" pequeno onClick={() => alterarFiltros(atalho.periodo())}>
                {atalho.rotulo}
              </Botao>
            ))}
          </div>
        </div>

        {carregando ? (
          <Carregando />
        ) : erro ? (
          <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />
        ) : !dados.length ? (
          <Vazio
            icone={ReceiptText}
            titulo={temFiltro ? 'Nenhuma obrigação encontrada' : 'Nenhuma obrigação cadastrada'}
            descricao={
              temFiltro
                ? 'Ajuste os filtros para ver outras obrigações.'
                : 'Cadastre as contas e impostos da empresa para acompanhar os vencimentos.'
            }
            acao={
              !temFiltro &&
              pode('CADASTRAR_OBRIGACOES') && (
                <Botao variante="primario" icone={Plus} onClick={() => setCriando(true)}>
                  Nova obrigação
                </Botao>
              )
            }
          />
        ) : (
          <>
            <table className="tabela">
              <thead>
                <tr>
                  <th>Descrição</th>
                  <th>Vencimento</th>
                  <th>Categoria</th>
                  <th>Situação</th>
                  <th className="alinhar-direita">Valor</th>
                </tr>
              </thead>
              <tbody>
                {dados.map((obrigacao) => (
                  <tr
                    key={obrigacao.id}
                    className="tabela__linha-clicavel"
                    onClick={() => navegar(obrigacao.id)}
                  >
                    <td className="tabela__principal" data-rotulo="">
                      <Link to={obrigacao.id} onClick={(evento) => evento.stopPropagation()} style={{ color: 'inherit' }}>
                        {obrigacao.descricao}
                      </Link>
                      {obrigacao.recorrenciaId && (
                        <span className="marcador-recorrente" title={PERIODICIDADES[obrigacao.periodicidade]}>
                          <Repeat size={13} aria-hidden />
                          {PERIODICIDADES[obrigacao.periodicidade]}
                        </span>
                      )}
                    </td>
                    <td data-rotulo="Vencimento">
                      {formatarData(obrigacao.dataVencimento)}
                      {obrigacao.diasAtraso > 0 && (
                        <span className="texto-perigo" style={{ display: 'block', fontSize: '0.8rem' }}>
                          {plural(obrigacao.diasAtraso, 'dia', 'dias')} de atraso
                        </span>
                      )}
                    </td>
                    <td data-rotulo="Categoria">{obrigacao.categoria.nome}</td>
                    <td data-rotulo="Situação">
                      <SeloObrigacao situacao={obrigacao.status} pagaComAtraso={obrigacao.pagaComAtraso} />
                    </td>
                    <td className="alinhar-direita numero" data-rotulo="Valor">
                      {formatarMoeda(obrigacao.valor)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="resumo-lista">
              <span>{plural(dados.length, 'obrigação', 'obrigações')}</span>
              <span>
                Total: <strong className="numero">{formatarMoeda(total)}</strong>
              </span>
            </div>
          </>
        )}
      </section>

      <FormularioObrigacao aberto={criando} aoFechar={() => setCriando(false)} aoSalvar={(obrigacao) => navegar(obrigacao.id)} />
    </div>
  );
}
