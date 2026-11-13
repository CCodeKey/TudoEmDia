import { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { ChevronLeft, ChevronRight, CircleCheck, Clock, PieChart, Plus, TriangleAlert, Wallet } from 'lucide-react';
import { AnelIndicador } from '../components/graficos/AnelIndicador';
import { BarrasCategoria } from '../components/graficos/BarrasCategoria';
import { FormularioObrigacao } from '../components/obrigacoes/FormularioObrigacao';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { Esqueleto } from '../components/ui/Carregando';
import { ErroCarregamento, Vazio } from '../components/ui/Estados';
import { Selo } from '../components/ui/Selo';
import { useCarregar } from '../hooks/useCarregar';
import { useEmpresa } from '../hooks/useEmpresa';
import { painelService } from '../services/obrigacaoService';
import { capitalizar, formatarData, formatarMoeda, mesAtual, nomeDoMes, plural, primeiroEUltimoDia, somarMeses } from '../utils/formato';
import { FAIXA_INDICADOR } from '../utils/rotulos';

function Indicador({ titulo, icone: Icone, valor, detalhe, tom }) {
  return (
    <div className="cartao indicador">
      <div className="indicador__topo">
        <span>{titulo}</span>
        <span
          className="indicador__icone"
          style={tom ? { background: `var(--cor-${tom}-suave)`, color: `var(--cor-${tom})` } : undefined}
        >
          <Icone size={17} aria-hidden />
        </span>
      </div>
      <div className="indicador__valor numero">{valor}</div>
      {detalhe && <div className="indicador__detalhe">{detalhe}</div>}
    </div>
  );
}

// RF22 a RF26: abre no mês atual e mostra só a empresa selecionada (RN83)
export function Painel() {
  const { empresa, pode } = useEmpresa();
  const navegar = useNavigate();
  const [mes, setMes] = useState(mesAtual());
  const [criando, setCriando] = useState(false);
  const { dados, carregando, erro, recarregar } = useCarregar(() => painelService.obter(empresa.id, mes), [empresa.id, mes]);

  const totalVencido = dados?.vencidas.reduce((soma, item) => soma + Number(item.valor), 0) ?? 0;
  const faixa = dados?.indicador.faixa ? FAIXA_INDICADOR[dados.indicador.faixa] : null;
  const { inicio, fim } = primeiroEUltimoDia(mes);

  return (
    <div className="pagina">
      <CabecalhoPagina
        titulo="Painel"
        descricao={`Visão geral das obrigações de ${empresa.nome}.`}
        acoes={
          <>
            <div className="seletor-mes" role="group" aria-label="Mês do painel">
              <Botao variante="fantasma" pequeno somenteIcone icone={ChevronLeft} onClick={() => setMes(somarMeses(mes, -1))}>
                Mês anterior
              </Botao>
              <span aria-live="polite">{capitalizar(nomeDoMes(mes))}</span>
              <Botao variante="fantasma" pequeno somenteIcone icone={ChevronRight} onClick={() => setMes(somarMeses(mes, 1))}>
                Próximo mês
              </Botao>
            </div>
            {pode('CADASTRAR_OBRIGACOES') && (
              <Botao variante="primario" icone={Plus} onClick={() => setCriando(true)}>
                Nova obrigação
              </Botao>
            )}
          </>
        }
      />

      {erro ? (
        <div className="cartao">
          <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />
        </div>
      ) : (
        <>
          <section className="grade-indicadores" aria-label="Indicadores do mês">
            {carregando || !dados ? (
              [1, 2, 3, 4].map((chave) => (
                <div key={chave} className="cartao indicador">
                  <Esqueleto altura={14} largura="50%" />
                  <Esqueleto altura={28} largura="70%" />
                </div>
              ))
            ) : (
              <>
                <Indicador
                  titulo="A pagar no mês"
                  icone={Clock}
                  valor={formatarMoeda(dados.totalAPagar)}
                  detalhe="Pendentes e vencidas do mês"
                  tom="aviso"
                />
                <Indicador
                  titulo="Já pago no mês"
                  icone={Wallet}
                  valor={formatarMoeda(dados.totalPago)}
                  detalhe="Obrigações com vencimento no mês"
                  tom="sucesso"
                />
                <Indicador
                  titulo="Vencidas"
                  icone={TriangleAlert}
                  valor={formatarMoeda(totalVencido)}
                  detalhe={dados.vencidas.length ? plural(dados.vencidas.length, 'obrigação em atraso', 'obrigações em atraso') : 'Nenhuma em atraso'}
                  tom="perigo"
                />
                <div className="cartao indicador">
                  <div className="indicador__topo">
                    <span>Obrigações em dia</span>
                    {faixa && <Selo tom={faixa.tom}>{faixa.rotulo}</Selo>}
                  </div>
                  <div className="linha" style={{ flexWrap: 'nowrap', gap: 14 }}>
                    <AnelIndicador percentual={dados.indicador.percentual} cor={faixa?.cor} />
                    <span className="indicador__detalhe">
                      {dados.indicador.total
                        ? `${dados.indicador.pagasEmDia} de ${dados.indicador.total} pagas até o vencimento`
                        : 'Nenhuma obrigação venceu ainda neste mês'}
                    </span>
                  </div>
                </div>
              </>
            )}
          </section>

          <div className="grade-painel">
            <section className="cartao">
              <div className="cartao__cabecalho">
                <div>
                  <h2>Por categoria</h2>
                  <p className="texto-fraco">Valores com vencimento em {nomeDoMes(mes)}</p>
                </div>
              </div>
              <div className="cartao__corpo">
                {carregando || !dados ? (
                  <div className="pilha">
                    <Esqueleto />
                    <Esqueleto />
                    <Esqueleto />
                  </div>
                ) : dados.porCategoria.length ? (
                  <BarrasCategoria itens={dados.porCategoria} />
                ) : (
                  <Vazio icone={PieChart} titulo="Sem obrigações neste mês" descricao="Quando houver obrigações com vencimento no mês, a distribuição aparece aqui." />
                )}
              </div>
              {dados?.porCategoria.length > 0 && (
                <div className="cartao__rodape">
                  <Link to={`../obrigacoes?inicio=${inicio}&fim=${fim}`}>Ver obrigações do mês</Link>
                </div>
              )}
            </section>

            <section className="cartao">
              <div className="cartao__cabecalho">
                <div>
                  <h2>Vencidas e não pagas</h2>
                  <p className="texto-fraco">Da mais antiga para a mais recente</p>
                </div>
              </div>
              <div style={{ paddingTop: 10 }}>
                {carregando || !dados ? (
                  <div className="cartao__corpo pilha">
                    <Esqueleto />
                    <Esqueleto />
                  </div>
                ) : dados.vencidas.length ? (
                  <ul className="lista-vencidas">
                    {dados.vencidas.slice(0, 8).map((item) => (
                      <li key={item.id}>
                        <Link to={`../obrigacoes/${item.id}`}>
                          <span className="lista-vencidas__info">
                            <strong>{item.descricao}</strong>
                            <span className="texto-fraco">
                              {item.categoria} · venceu em {formatarData(item.dataVencimento)}
                            </span>
                          </span>
                          <span className="lista-vencidas__valor">
                            <strong className="numero">{formatarMoeda(item.valor)}</strong>
                            <Selo tom="perigo" semPonto>
                              {plural(item.diasAtraso, 'dia', 'dias')} de atraso
                            </Selo>
                          </span>
                        </Link>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <Vazio icone={CircleCheck} titulo="Tudo em dia" descricao="Nenhuma obrigação vencida e não paga." />
                )}
              </div>
              {dados?.vencidas.length > 8 && (
                <div className="cartao__rodape">
                  <Link to="../obrigacoes?status=VENCIDA">Ver todas as {dados.vencidas.length} vencidas</Link>
                </div>
              )}
            </section>
          </div>
        </>
      )}

      <FormularioObrigacao
        aberto={criando}
        aoFechar={() => setCriando(false)}
        aoSalvar={(obrigacao) => navegar(`../obrigacoes/${obrigacao.id}`)}
      />
    </div>
  );
}
