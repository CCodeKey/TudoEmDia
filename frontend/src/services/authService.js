import { api } from './api';

export const authService = {
  prepararCsrf: () => api.get('/auth/csrf'),
  usuarioLogado: () => api.get('/auth/me', { ignorar401: true }),
  entrar: (email, senha) => api.post('/auth/login', { email, senha }, { ignorar401: true }),
  cadastrar: (dados) => api.post('/auth/cadastro', dados),
  sair: () => api.post('/auth/logout', undefined, { ignorar401: true }),
};

export const contaService = {
  meusDados: () => api.get('/conta/dados'),
  excluir: (senha) => api.post('/conta/exclusao', { senha }),
};
