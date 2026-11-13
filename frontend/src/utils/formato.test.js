import {
  capitalizar,
  formatarCnpj,
  formatarData,
  formatarMoeda,
  iniciais,
  nomeDoMes,
  plural,
  primeiroEUltimoDia,
  somarMeses,
} from './formato';

// O Intl usa espaço não separável entre "R$" e o valor
const semEspacosEspeciais = (texto) => texto.replace(/\s/g, ' ');

describe('formato', () => {
  it('formata valores em reais', () => {
    expect(semEspacosEspeciais(formatarMoeda(1234.5))).toBe('R$ 1.234,50');
    expect(semEspacosEspeciais(formatarMoeda('812.45'))).toBe('R$ 812,45');
    expect(semEspacosEspeciais(formatarMoeda(null))).toBe('R$ 0,00');
  });

  it('formata datas da API sem sofrer com fuso horário', () => {
    expect(formatarData('2026-11-01')).toBe('01/11/2026');
    expect(formatarData('2026-11-01T23:59:00')).toBe('01/11/2026');
    expect(formatarData(null)).toBe('—');
  });

  it('navega entre meses, inclusive na virada do ano', () => {
    expect(somarMeses('2026-12', 1)).toBe('2027-01');
    expect(somarMeses('2026-01', -1)).toBe('2025-12');
    expect(nomeDoMes('2026-11')).toBe('novembro de 2026');
    expect(capitalizar(nomeDoMes('2026-11'))).toBe('Novembro de 2026');
  });

  it('calcula o último dia do mês, inclusive em ano bissexto', () => {
    expect(primeiroEUltimoDia('2028-02')).toEqual({ inicio: '2028-02-01', fim: '2028-02-29' });
    expect(primeiroEUltimoDia('2026-02')).toEqual({ inicio: '2026-02-01', fim: '2026-02-28' });
    expect(primeiroEUltimoDia('2026-12').fim).toBe('2026-12-31');
  });

  it('formata CNPJ, iniciais e plural', () => {
    expect(formatarCnpj('11222333000181')).toBe('11.222.333/0001-81');
    expect(formatarCnpj('12ABC34501DE35')).toBe('12.ABC.345/01DE-35');
    expect(iniciais('Hellen Lima Araújo')).toBe('HL');
    expect(iniciais('  ana ')).toBe('A');
    expect(plural(1, 'dia', 'dias')).toBe('1 dia');
    expect(plural(3, 'dia', 'dias')).toBe('3 dias');
  });
});
