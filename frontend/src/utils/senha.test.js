import { NIVEIS_SENHA, nivelSenha, requisitosSenha } from './senha';

const rotulo = (senha) => NIVEIS_SENHA[nivelSenha(senha)].rotulo;

describe('força da senha', () => {
  it('é fraca enquanto não cumpre o mínimo do sistema (RN03)', () => {
    expect(rotulo('abc12')).toBe('Fraca');
    expect(rotulo('abcdefghij')).toBe('Fraca');
    expect(rotulo('1234567890')).toBe('Fraca');
  });

  it('sobe de nível a cada reforço', () => {
    expect(rotulo('padaria2026')).toBe('Razoável');
    expect(rotulo('Padaria2026')).toBe('Boa');
    expect(rotulo('Padaria#2026')).toBe('Forte');
    expect(rotulo('padaria-dourada-2026')).toBe('Forte');
  });

  it('marca os requisitos atendidos', () => {
    const atendidos = requisitosSenha('Padaria2026')
      .filter((requisito) => requisito.atendido)
      .map((requisito) => requisito.texto);
    expect(atendidos).toEqual(['Pelo menos 8 caracteres', 'Letras e números', 'Maiúsculas e minúsculas']);
  });
});
