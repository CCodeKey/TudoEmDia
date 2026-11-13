import { SITUACAO_CONVITE, SITUACAO_DOCUMENTO, SITUACAO_OBRIGACAO } from '../../utils/rotulos';

export function Selo({ tom = 'neutro', semPonto = false, children }) {
  return <span className={`selo selo--${tom} ${semPonto ? 'selo--sem-ponto' : ''}`}>{children}</span>;
}

function SeloDe({ mapa, valor }) {
  const item = mapa[valor] ?? { rotulo: valor, tom: 'neutro' };
  return <Selo tom={item.tom}>{item.rotulo}</Selo>;
}

export function SeloObrigacao({ situacao, pagaComAtraso }) {
  if (situacao === 'PAGA' && pagaComAtraso) return <Selo tom="sucesso">Paga com atraso</Selo>;
  return <SeloDe mapa={SITUACAO_OBRIGACAO} valor={situacao} />;
}

export function SeloDocumento({ situacao }) {
  return <SeloDe mapa={SITUACAO_DOCUMENTO} valor={situacao} />;
}

export function SeloConvite({ situacao }) {
  return <SeloDe mapa={SITUACAO_CONVITE} valor={situacao} />;
}
