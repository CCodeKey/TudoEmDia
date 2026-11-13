// Textos e tons visuais dos valores que vêm da API

export const SITUACAO_OBRIGACAO = {
  PENDENTE: { rotulo: 'Pendente', tom: 'aviso' },
  PAGA: { rotulo: 'Paga', tom: 'sucesso' },
  VENCIDA: { rotulo: 'Vencida', tom: 'perigo' },
};

// ATIVO corresponde a "Válido" (RN51)
export const SITUACAO_DOCUMENTO = {
  ATIVO: { rotulo: 'Válido', tom: 'sucesso' },
  VENCIDO: { rotulo: 'Vencido', tom: 'perigo' },
  ARQUIVADO: { rotulo: 'Arquivado', tom: 'neutro' },
};

export const SITUACAO_CONVITE = {
  PENDENTE: { rotulo: 'Pendente', tom: 'aviso' },
  ACEITO: { rotulo: 'Aceito', tom: 'sucesso' },
  EXPIRADO: { rotulo: 'Expirado', tom: 'neutro' },
  CANCELADO: { rotulo: 'Cancelado', tom: 'neutro' },
};

export const PERFIS = {
  PROPRIETARIO: 'Proprietário',
  FINANCEIRO: 'Financeiro',
  CONTADOR: 'Contador',
};

export const PERIODICIDADES = {
  MENSAL: 'Mensal',
  ANUAL: 'Anual',
};

export const PRAZOS_ALERTA = {
  7: '7 dias antes',
  3: '3 dias antes',
  1: '1 dia antes',
  0: 'No dia do vencimento',
};

export const FAIXA_INDICADOR = {
  VERDE: { rotulo: 'Em dia', cor: 'var(--cor-sucesso)', tom: 'sucesso' },
  AMARELO: { rotulo: 'Atenção', cor: 'var(--cor-aviso)', tom: 'aviso' },
  VERMELHO: { rotulo: 'Crítico', cor: 'var(--cor-perigo)', tom: 'perigo' },
};
