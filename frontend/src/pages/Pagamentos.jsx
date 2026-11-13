import { useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { Undo2, Wallet } from 'lucide-react';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { CampoTexto } from '../components/ui/Campos';
import { Carregando } from '../components/ui/Carregando';
import { Confirmacao } from '../components/ui/Confirmacao';
import { ErroCarregamento, Vazio } from '../components/ui/Estados';
import { Selo } from '../components/ui/Selo';
import { useAcao } from '../hooks/useAcao';
import { useCarregar } from '../hooks/useCarregar';
import { useEmpresa } from '../hooks/useEmpresa';
import { pagamentoService } from '../services/obrigacaoService';
import { formatarData, formatarDataHora, formatarMoeda, plural } from '../utils/formato';

// RF19: histórico com obrigação, valor, data e quem registrou (RN72); registros não são editados (RN73)
export function Pagamentos() {
  const { empresa, pode } = useEmpresa();
  const [busca, setBusca] = useSearchParams();
  const filtros = { inicio: busca.get('inicio') ?? '', fim: busca.get('fim') ?? '' };
  const { dados, carregando, erro, recarregar } = useCarregar(
    () => pagamentoService.historico(empresa.id, filtros),
    [empresa.id, busca.toString()],
  );
  const [cancelando, setCancelando] = useState(null);
  const { executar, enviando } = useAcao();

  const alterarFiltro = (campo) => (evento) => {
    const proxima = new URLSearchParams(busca);
    if (evento.target.value) proxima.set(campo, evento.target.value);
    else proxima.delete(campo);
    setBusca(proxima, { replace: true });
  };

  const cancelar = async () => {
    const resultado = await executar(() => pagamentoService.cancelar(empresa.id, cancelando.id), {
      sucesso: 'Pagamento cancelado.',
    });
    if (resultado.ok) {
      setCancelando(null);
      recarregar();
    }
  };

  const validos = (dados ?? []).filter((pagamento) => !pagamento.cancelado);
  const totalPago = validos.reduce((soma, pagamento) => soma + Number(pagamento.valorPago), 0);

  return (
    <div className="pagina">
      <CabecalhoPagina titulo="Pagamentos" descricao="Histórico de pagamentos registrados, inclusive os cancelados." />

      <section className="cartao tabela-envoltorio">
        <div className="barra-filtros" style={{ borderBottom: '1px solid var(--cor-borda)' }}>
          <CampoTexto rotulo="Pagos de" type="date" pequeno value={filtros.inicio} onChange={alterarFiltro('inicio')} />
          <CampoTexto rotulo="até" type="date" pequeno value={filtros.fim} onChange={alterarFiltro('fim')} />
        </div>

        {carregando ? (
          <Carregando />
        ) : erro ? (
          <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />
        ) : !dados.length ? (
          <Vazio
            icone={Wallet}
            titulo="Nenhum pagamento"
            descricao="Os pagamentos registrados nas obrigações aparecem aqui."
          />
        ) : (
          <>
            <table className="tabela">
              <thead>
                <tr>
                  <th>Obrigação</th>
                  <th>Pago em</th>
                  <th>Vencimento</th>
                  <th>Registrado por</th>
                  <th>Situação</th>
                  <th className="alinhar-direita">Valor pago</th>
                  {pode('CANCELAR_PAGAMENTOS') && <th aria-label="Ações" />}
                </tr>
              </thead>
              <tbody>
                {dados.map((pagamento) => (
                  <tr key={pagamento.id}>
                    <td className="tabela__principal" data-rotulo="">
                      <Link to={`/empresas/${empresa.id}/obrigacoes/${pagamento.obrigacaoId}`} style={{ color: 'inherit' }}>
                        {pagamento.obrigacaoDescricao}
                      </Link>
                    </td>
                    <td data-rotulo="Pago em">
                      {formatarData(pagamento.dataPagamento)}
                      {pagamento.pagoComAtraso && (
                        <span className="texto-fraco" style={{ display: 'block' }}>
                          com atraso
                        </span>
                      )}
                    </td>
                    <td data-rotulo="Vencimento">{formatarData(pagamento.dataVencimento)}</td>
                    <td data-rotulo="Registrado por">
                      {pagamento.registradoPor}
                      <span className="texto-fraco" style={{ display: 'block' }}>
                        {formatarDataHora(pagamento.dataRegistro)}
                      </span>
                    </td>
                    <td data-rotulo="Situação">
                      {pagamento.cancelado ? (
                        <span title={`Cancelado por ${pagamento.canceladoPor} em ${formatarDataHora(pagamento.dataCancelamento)}`}>
                          <Selo tom="neutro">Cancelado</Selo>
                        </span>
                      ) : (
                        <Selo tom="sucesso">Válido</Selo>
                      )}
                    </td>
                    <td
                      className="alinhar-direita numero"
                      data-rotulo="Valor pago"
                      style={pagamento.cancelado ? { textDecoration: 'line-through', color: 'var(--cor-texto-3)' } : undefined}
                    >
                      {formatarMoeda(pagamento.valorPago)}
                    </td>
                    {pode('CANCELAR_PAGAMENTOS') && (
                      <td data-rotulo="">
                        <div className="tabela__acoes">
                          {!pagamento.cancelado && (
                            <Botao variante="fantasma" pequeno icone={Undo2} onClick={() => setCancelando(pagamento)}>
                              Cancelar
                            </Botao>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="resumo-lista">
              <span>{plural(dados.length, 'registro', 'registros')}</span>
              <span>
                Total pago (sem cancelados): <strong className="numero">{formatarMoeda(totalPago)}</strong>
              </span>
            </div>
          </>
        )}
      </section>

      <Confirmacao
        aberto={Boolean(cancelando)}
        aoFechar={() => setCancelando(null)}
        aoConfirmar={cancelar}
        titulo="Cancelar pagamento?"
        mensagem={
          cancelando
            ? `O pagamento de "${cancelando.obrigacaoDescricao}" será cancelado e a obrigação volta a ficar em aberto. O registro continua no histórico.`
            : ''
        }
        textoConfirmar="Cancelar pagamento"
        enviando={enviando}
      />
    </div>
  );
}
