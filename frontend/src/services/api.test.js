import { ApiError, api, consulta, definirAoExpirarSessao } from './api';

function respostaJson(status, corpo, cabecalhos = {}) {
  return new Response(corpo === undefined ? null : JSON.stringify(corpo), {
    status,
    headers: { 'Content-Type': 'application/json', ...cabecalhos },
  });
}

afterEach(() => {
  definirAoExpirarSessao(null);
  document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT';
});

describe('consulta', () => {
  it('ignora filtros vazios', () => {
    expect(consulta({ inicio: '2026-11-01', fim: '', status: null, categoriaId: undefined })).toBe('?inicio=2026-11-01');
    expect(consulta({})).toBe('');
  });
});

describe('cliente da API', () => {
  // Precisa rodar primeiro: depois dele o cliente já sabe que o CSRF está desligado
  it('sem token CSRF, busca um antes de alterar dados (ex.: login depois do logout)', async () => {
    const buscar = vi.spyOn(globalThis, 'fetch').mockImplementation(async (url) => {
      if (url === '/api/auth/csrf') {
        document.cookie = 'XSRF-TOKEN=novo-token';
        return new Response(null, { status: 204 });
      }
      return respostaJson(200, {});
    });

    await api.post('/auth/login', { email: 'a@b.com', senha: 'x' });

    expect(buscar.mock.calls.map(([url]) => url)).toEqual(['/api/auth/csrf', '/api/auth/login']);
    expect(buscar.mock.calls[1][1].headers['X-XSRF-TOKEN']).toBe('novo-token');
  });

  it('com o CSRF desligado no servidor, para de buscar o token', async () => {
    const buscar = vi.spyOn(globalThis, 'fetch').mockImplementation(async (url) =>
      url === '/api/auth/csrf' ? new Response(null, { status: 204 }) : respostaJson(200, {}),
    );

    await api.post('/empresas', {});
    await api.post('/empresas', {});

    expect(buscar.mock.calls.map(([url]) => url)).toEqual(['/api/auth/csrf', '/api/empresas', '/api/empresas']);
  });

  it('envia JSON com cookie de sessão e devolve o corpo', async () => {
    const buscar = vi.spyOn(globalThis, 'fetch').mockResolvedValue(respostaJson(200, { id: '1' }));

    const resultado = await api.post('/empresas', { nome: 'Padaria' });

    expect(resultado).toEqual({ id: '1' });
    const [url, opcoes] = buscar.mock.calls.at(-1);
    expect(url).toBe('/api/empresas');
    expect(opcoes.credentials).toBe('include');
    expect(opcoes.headers['Content-Type']).toBe('application/json');
    expect(opcoes.body).toBe('{"nome":"Padaria"}');
  });

  it('envia o token CSRF quando o cookie existe', async () => {
    document.cookie = 'XSRF-TOKEN=abc123';
    const buscar = vi.spyOn(globalThis, 'fetch').mockResolvedValue(respostaJson(204));

    expect(await api.delete('/empresas/1/anexos/2')).toBeNull();
    expect(buscar.mock.calls[0][1].headers['X-XSRF-TOKEN']).toBe('abc123');
  });

  it('transforma o erro da API em mensagem e campos', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      respostaJson(400, { detail: 'Dados inválidos.', campos: { valor: 'O valor deve ser maior que zero.' } }),
    );

    const erro = await api.post('/x', {}).catch((falha) => falha);

    expect(erro).toBeInstanceOf(ApiError);
    expect(erro.status).toBe(400);
    expect(erro.message).toBe('Dados inválidos.');
    expect(erro.campos.valor).toBe('O valor deve ser maior que zero.');
  });

  it('usa uma mensagem padrão quando a resposta não tem corpo', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 403 }));
    await expect(api.get('/x')).rejects.toThrow('Você não tem permissão para esta ação.');
  });

  it('avisa quando a sessão expira, exceto se pedido para ignorar', async () => {
    const aoExpirar = vi.fn();
    definirAoExpirarSessao(aoExpirar);
    vi.spyOn(globalThis, 'fetch').mockImplementation(async () => respostaJson(401, { detail: 'Faça login.' }));

    await expect(api.get('/empresas')).rejects.toThrow('Faça login.');
    expect(aoExpirar).toHaveBeenCalledTimes(1);

    await expect(api.get('/auth/me', { ignorar401: true })).rejects.toThrow();
    expect(aoExpirar).toHaveBeenCalledTimes(1);
  });

  it('informa falha de conexão', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'));
    const erro = await api.get('/x').catch((falha) => falha);
    expect(erro.status).toBe(0);
    expect(erro.message).toMatch(/conectar ao servidor/);
  });

  it('baixa arquivos com o nome original, inclusive com acentos', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response('%PDF', {
        status: 200,
        headers: { 'Content-Disposition': "attachment; filename*=UTF-8''Certid%C3%A3o.pdf" },
      }),
    );
    const arquivo = await api.baixar('/x');
    expect(arquivo.nome).toBe('Certidão.pdf');
    expect(await arquivo.blob.text()).toBe('%PDF');
  });
});
