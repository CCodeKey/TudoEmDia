// Força da senha para o medidor do cadastro.
// O mínimo exigido pelo sistema (RN03) é: 8 caracteres, com pelo menos uma letra e um número.

export const NIVEIS_SENHA = [
  { rotulo: 'Fraca', tom: 'perigo' },
  { rotulo: 'Razoável', tom: 'aviso' },
  { rotulo: 'Boa', tom: 'info' },
  { rotulo: 'Forte', tom: 'sucesso' },
];

export function requisitosSenha(senha) {
  return [
    { texto: 'Pelo menos 8 caracteres', atendido: senha.length >= 8, obrigatorio: true },
    { texto: 'Letras e números', atendido: /[A-Za-z]/.test(senha) && /\d/.test(senha), obrigatorio: true },
    { texto: 'Maiúsculas e minúsculas', atendido: /[a-z]/.test(senha) && /[A-Z]/.test(senha) },
    { texto: 'Um símbolo (ex.: ! @ # $)', atendido: /[^A-Za-z0-9]/.test(senha) },
    { texto: '12 caracteres ou mais', atendido: senha.length >= 12 },
  ];
}

// 0 = Fraca (não cumpre o mínimo), 1 = Razoável, 2 = Boa, 3 = Forte
export function nivelSenha(senha) {
  const requisitos = requisitosSenha(senha);
  if (requisitos.some((requisito) => requisito.obrigatorio && !requisito.atendido)) return 0;
  const reforcos = requisitos.filter((requisito) => !requisito.obrigatorio && requisito.atendido).length;
  return Math.min(3, 1 + reforcos);
}
