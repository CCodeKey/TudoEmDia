import { api, consulta, formularioComArquivo } from './api';

const base = (empresaId) => `/empresas/${empresaId}`;

export const obrigacaoService = {
  listar: (empresaId, filtros) => api.get(`${base(empresaId)}/obrigacoes${consulta(filtros)}`),
  detalhar: (empresaId, obrigacaoId) => api.get(`${base(empresaId)}/obrigacoes/${obrigacaoId}`),
  criar: (empresaId, dados) => api.post(`${base(empresaId)}/obrigacoes`, dados),
  atualizar: (empresaId, obrigacaoId, dados) => api.put(`${base(empresaId)}/obrigacoes/${obrigacaoId}`, dados),
  excluir: (empresaId, obrigacaoId, escopo) =>
    api.delete(`${base(empresaId)}/obrigacoes/${obrigacaoId}${consulta({ escopo })}`),

  enviarAnexo: (empresaId, obrigacaoId, arquivo) =>
    api.enviar(`${base(empresaId)}/obrigacoes/${obrigacaoId}/anexos`, formularioComArquivo({ arquivo })),
  baixarAnexo: (empresaId, anexoId) => api.baixar(`${base(empresaId)}/anexos/${anexoId}/arquivo`),
  removerAnexo: (empresaId, anexoId) => api.delete(`${base(empresaId)}/anexos/${anexoId}`),

  registrarPagamento: (empresaId, obrigacaoId, dados) =>
    api.post(`${base(empresaId)}/obrigacoes/${obrigacaoId}/pagamento`, dados),
  tornarRecorrente: (empresaId, obrigacaoId, dados) =>
    api.post(`${base(empresaId)}/obrigacoes/${obrigacaoId}/recorrencia`, dados),
};

export const recorrenciaService = {
  listar: (empresaId) => api.get(`${base(empresaId)}/recorrencias`),
  atualizar: (empresaId, recorrenciaId, dados) => api.put(`${base(empresaId)}/recorrencias/${recorrenciaId}`, dados),
};

export const pagamentoService = {
  historico: (empresaId, filtros) => api.get(`${base(empresaId)}/pagamentos${consulta(filtros)}`),
  cancelar: (empresaId, pagamentoId) => api.post(`${base(empresaId)}/pagamentos/${pagamentoId}/cancelamento`),
};

export const painelService = {
  obter: (empresaId, mes) => api.get(`${base(empresaId)}/painel${consulta({ mes })}`),
  relatorio: (empresaId, filtros) => api.baixar(`${base(empresaId)}/relatorios/obrigacoes${consulta(filtros)}`),
};
