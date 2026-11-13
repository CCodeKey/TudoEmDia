import { api } from './api';

export const categoriaService = {
  listar: (empresaId) => api.get(`/empresas/${empresaId}/categorias`),
  criar: (empresaId, nome) => api.post(`/empresas/${empresaId}/categorias`, { nome }),
  excluir: (empresaId, categoriaId) => api.delete(`/empresas/${empresaId}/categorias/${categoriaId}`),
};

export const alertaService = {
  consultar: (empresaId) => api.get(`/empresas/${empresaId}/alertas`),
  configurar: (empresaId, prazos) => api.put(`/empresas/${empresaId}/alertas`, { prazos }),
};
