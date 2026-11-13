import { formatarMoeda } from '../../utils/formato';

const CORES = ['--grafico-1', '--grafico-2', '--grafico-3', '--grafico-4', '--grafico-5', '--grafico-6'];

// Distribuição dos valores do mês por categoria (RN87, RN88)
export function BarrasCategoria({ itens }) {
  const total = itens.reduce((soma, item) => soma + Number(item.total), 0);
  const maior = Math.max(...itens.map((item) => Number(item.total)), 1);

  return (
    <div className="barras" role="list">
      {itens.map((item, indice) => {
        const valor = Number(item.total);
        const cor = `var(${CORES[indice % CORES.length]})`;
        const percentual = total ? Math.round((valor / total) * 100) : 0;
        return (
          <div key={item.categoria} role="listitem" aria-label={`${item.categoria}: ${formatarMoeda(valor)}, ${percentual}%`}>
            <div className="barra__topo">
              <span className="barra__nome">
                <span className="barra__cor" style={{ background: cor }} aria-hidden />
                <span>{item.categoria}</span>
              </span>
              <span className="numero">
                {formatarMoeda(valor)} <span className="texto-fraco">· {percentual}%</span>
              </span>
            </div>
            <div className="barra__trilho" aria-hidden>
              <div className="barra__preenchimento" style={{ width: `${(valor / maior) * 100}%`, background: cor }} />
            </div>
          </div>
        );
      })}
    </div>
  );
}
