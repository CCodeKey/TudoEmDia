import { api } from './api';

export const empresaService = {
  listar: () => api.get('/empresas'),
  criar: (dados) => api.post('/empresas', dados),
  detalhar: (empresaId) => api.get(`/empresas/${empresaId}`),
  permissoes: (empresaId) => api.get(`/empresas/${empresaId}/permissoes`),
};

export const equipeService = {
  membros: (empresaId) => api.get(`/empresas/${empresaId}/usuarios`),
  atualizarMembro: (empresaId, vinculoId, dados) => api.patch(`/empresas/${empresaId}/usuarios/${vinculoId}`, dados),
  convites: (empresaId) => api.get(`/empresas/${empresaId}/convites`),
  convidar: (empresaId, dados) => api.post(`/empresas/${empresaId}/convites`, dados),
  cancelarConvite: (empresaId, conviteId) => api.delete(`/empresas/${empresaId}/convites/${conviteId}`),
};

export const conviteService = {
  consultar: (token) => api.get(`/convites/${token}`, { ignorar401: true }),
  aceitar: (token) => api.post(`/convites/${token}/aceite`),
};
