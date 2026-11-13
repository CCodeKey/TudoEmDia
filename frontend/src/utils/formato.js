const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export function formatarMoeda(valor) {
  return moeda.format(Number(valor ?? 0));
}

// Datas da API chegam como "AAAA-MM-DD"; não passam por Date para não sofrer com fuso
export function formatarData(iso) {
  if (!iso) return '—';
  const [ano, mes, dia] = iso.slice(0, 10).split('-');
  return `${dia}/${mes}/${ano}`;
}

export function formatarDataHora(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

export function hojeIso() {
  const agora = new Date();
  return paraIso(agora);
}

export function paraIso(data) {
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${data.getFullYear()}-${mes}-${dia}`;
}

export function mesAtual() {
  return hojeIso().slice(0, 7);
}

// "2026-11" -> "novembro de 2026"
export function nomeDoMes(mes) {
  const [ano, numero] = mes.split('-').map(Number);
  return new Date(ano, numero - 1, 1).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
}

export function capitalizar(texto) {
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

export function somarMeses(mes, quantidade) {
  const [ano, numero] = mes.split('-').map(Number);
  const data = new Date(ano, numero - 1 + quantidade, 1);
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`;
}

export function primeiroEUltimoDia(mes) {
  const [ano, numero] = mes.split('-').map(Number);
  return { inicio: `${mes}-01`, fim: paraIso(new Date(ano, numero, 0)) };
}

export function formatarCnpj(cnpj) {
  if (!cnpj || cnpj.length !== 14) return cnpj ?? '';
  return `${cnpj.slice(0, 2)}.${cnpj.slice(2, 5)}.${cnpj.slice(5, 8)}/${cnpj.slice(8, 12)}-${cnpj.slice(12)}`;
}

export function iniciais(nome) {
  return (nome ?? '')
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0].toUpperCase())
    .join('');
}

export function plural(quantidade, singular, pluralTexto) {
  return `${quantidade} ${quantidade === 1 ? singular : pluralTexto}`;
}
