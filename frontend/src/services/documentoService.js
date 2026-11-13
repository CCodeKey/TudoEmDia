import { api, consulta, formularioComArquivo } from './api';

const base = (empresaId) => `/empresas/${empresaId}/documentos`;

export const documentoService = {
  listar: (empresaId, filtros) => api.get(`${base(empresaId)}${consulta(filtros)}`),
  criar: (empresaId, dados, arquivo) => api.enviar(base(empresaId), formularioComArquivo({ ...dados, arquivo })),
  atualizar: (empresaId, documentoId, dados) => api.put(`${base(empresaId)}/${documentoId}`, dados),
  substituirArquivo: (empresaId, documentoId, arquivo) =>
    api.enviar(`${base(empresaId)}/${documentoId}/arquivo`, formularioComArquivo({ arquivo }), 'PUT'),
  baixar: (empresaId, documentoId) => api.baixar(`${base(empresaId)}/${documentoId}/arquivo`),
  excluir: (empresaId, documentoId) => api.delete(`${base(empresaId)}/${documentoId}`),
};
