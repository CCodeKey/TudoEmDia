// Cliente HTTP da API: cookie de sessão, token CSRF e erros padronizados

const BASE = import.meta.env.VITE_API_URL ?? '/api';

export class ApiError extends Error {
  constructor(status, mensagem, campos = {}) {
    super(mensagem);
    this.status = status;
    this.campos = campos;
  }
}

let aoExpirarSessao = null;

// O AuthProvider registra aqui o que fazer quando a sessão expira (401)
export function definirAoExpirarSessao(funcao) {
  aoExpirarSessao = funcao;
}

const MENSAGENS_PADRAO = {
  400: 'Confira os dados informados.',
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Você não tem permissão para esta ação.',
  404: 'Não encontrado.',
  409: 'A operação conflita com dados existentes.',
  413: 'O arquivo é maior que o permitido.',
  422: 'A operação não é permitida.',
  429: 'Muitas tentativas. Aguarde alguns minutos.',
};

function lerCookie(nome) {
  const item = document.cookie.split('; ').find((cookie) => cookie.startsWith(`${nome}=`));
  return item ? decodeURIComponent(item.split('=')[1]) : null;
}

function nomeDoArquivo(cabecalho) {
  if (!cabecalho) return 'arquivo';
  const utf8 = cabecalho.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8) return decodeURIComponent(utf8[1]);
  const simples = cabecalho.match(/filename="?([^";]+)"?/i);
  return simples ? simples[1] : 'arquivo';
}

async function requisicao(caminho, { metodo = 'GET', corpo, formulario, tipo = 'json', ignorar401 = false } = {}) {
  const cabecalhos = { Accept: 'application/json' };
  // Usado quando a proteção CSRF do back-end estiver ligada
  const tokenCsrf = lerCookie('XSRF-TOKEN');
  if (tokenCsrf) cabecalhos['X-XSRF-TOKEN'] = tokenCsrf;

  let body;
  if (formulario) {
    body = formulario;
  } else if (corpo !== undefined) {
    cabecalhos['Content-Type'] = 'application/json';
    body = JSON.stringify(corpo);
  }

  let resposta;
  try {
    resposta = await fetch(BASE + caminho, { method: metodo, headers: cabecalhos, body, credentials: 'include' });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique sua conexão.');
  }

  if (!resposta.ok) {
    let dados = null;
    try {
      dados = await resposta.json();
    } catch {
      // Resposta sem corpo JSON
    }
    if (resposta.status === 401 && !ignorar401 && aoExpirarSessao) aoExpirarSessao();
    const mensagem = dados?.detail ?? MENSAGENS_PADRAO[resposta.status] ?? 'Algo deu errado. Tente novamente.';
    throw new ApiError(resposta.status, mensagem, dados?.campos ?? {});
  }

  if (resposta.status === 204) return null;
  if (tipo === 'arquivo') {
    return { blob: await resposta.blob(), nome: nomeDoArquivo(resposta.headers.get('Content-Disposition')) };
  }
  return resposta.json();
}

// Monta a query string ignorando filtros vazios
export function consulta(parametros) {
  const busca = new URLSearchParams();
  Object.entries(parametros ?? {}).forEach(([chave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== '') busca.set(chave, valor);
  });
  const texto = busca.toString();
  return texto ? `?${texto}` : '';
}

export function formularioComArquivo(campos) {
  const formulario = new FormData();
  Object.entries(campos).forEach(([chave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== '') formulario.append(chave, valor);
  });
  return formulario;
}

export const api = {
  get: (caminho, opcoes) => requisicao(caminho, opcoes),
  post: (caminho, corpo, opcoes) => requisicao(caminho, { ...opcoes, metodo: 'POST', corpo }),
  put: (caminho, corpo, opcoes) => requisicao(caminho, { ...opcoes, metodo: 'PUT', corpo }),
  patch: (caminho, corpo, opcoes) => requisicao(caminho, { ...opcoes, metodo: 'PATCH', corpo }),
  delete: (caminho, opcoes) => requisicao(caminho, { ...opcoes, metodo: 'DELETE' }),
  enviar: (caminho, formulario, metodo = 'POST') => requisicao(caminho, { metodo, formulario }),
  baixar: (caminho) => requisicao(caminho, { tipo: 'arquivo' }),
};
