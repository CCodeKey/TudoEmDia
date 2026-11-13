import { expect } from '@playwright/test';

export const SENHA = 'Padaria#2026';
export const PDF = Buffer.from('%PDF-1.4\n% teste\n');
export const PNG = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3]);

const MAILPIT = process.env.MAILPIT_URL ?? 'http://localhost:8025';

// Identificador único por execução, para os testes poderem rodar mais de uma vez no mesmo banco
export const sufixo = () => `${Date.now().toString(36)}${Math.floor(Math.random() * 1000)}`;

// Data "AAAA-MM-DD" no horário de Brasília, deslocada em dias
export function dataIso(dias = 0) {
  const data = new Date(Date.now() + dias * 86_400_000);
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Sao_Paulo' }).format(data);
}

export function dataBr(iso) {
  const [ano, mes, dia] = iso.split('-');
  return `${dia}/${mes}/${ano}`;
}

export function cnpjValido() {
  const digitos = Array.from({ length: 8 }, () => Math.floor(Math.random() * 10)).concat([0, 0, 0, 1]);
  const dv = (pesos) => {
    const resto = pesos.reduce((soma, peso, i) => soma + digitos[i] * peso, 0) % 11;
    return resto < 2 ? 0 : 11 - resto;
  };
  digitos.push(dv([5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]));
  digitos.push(dv([6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]));
  return digitos.join('');
}

export async function cadastrarPelaTela(pagina, { nome, email, senha = SENHA }) {
  await pagina.goto('/cadastro');
  await preencherCadastro(pagina, { nome, email, senha });
}

export async function preencherCadastro(pagina, { nome, email, senha = SENHA }) {
  await pagina.getByLabel('Nome').fill(nome);
  if (email) await pagina.getByLabel('E-mail').fill(email);
  await pagina.getByLabel('Senha', { exact: true }).fill(senha);
  await pagina.getByRole('checkbox').check();
  await pagina.getByRole('button', { name: 'Criar conta' }).click();
}

export async function criarEmpresaPelaTela(pagina, nome) {
  await expect(pagina.getByRole('heading', { name: /Boas-vindas/ })).toBeVisible();
  await pagina.getByLabel('Nome da empresa').fill(nome);
  await pagina.getByLabel('CNPJ').fill(cnpjValido());
  await pagina.getByRole('button', { name: 'Cadastrar empresa' }).click();
  await expect(pagina).toHaveURL(/\/empresas\/[^/]+\/painel$/);
  return pagina.url().split('/empresas/')[1].split('/')[0];
}

// Chamadas diretas à API com a sessão do navegador, para preparar dados rapidamente
export async function api(pagina, metodo, caminho, dados) {
  // Envia o token CSRF como o front faz, para funcionar também com a proteção ligada
  const token = (await pagina.context().cookies()).find((cookie) => cookie.name === 'XSRF-TOKEN')?.value;
  const resposta = await pagina.request.fetch(`/api${caminho}`, {
    method: metodo,
    data: dados,
    headers: token ? { 'X-XSRF-TOKEN': decodeURIComponent(token) } : {},
  });
  expect(resposta.ok(), `${metodo} ${caminho} → ${resposta.status()}`).toBeTruthy();
  return resposta.status() === 204 ? null : resposta.json();
}

export async function mensagem(pagina, texto) {
  await expect(pagina.getByRole('status').getByText(texto)).toBeVisible();
}

// Lê no Mailpit o último e-mail enviado para o endereço
export async function ultimoEmail(request, para) {
  let encontrado = null;
  await expect
    .poll(
      async () => {
        const busca = await (await request.get(`${MAILPIT}/api/v1/search?query=to:"${para}"`)).json();
        encontrado = busca.messages?.[0] ?? null;
        return Boolean(encontrado);
      },
      { timeout: 15_000, message: `e-mail para ${para} não chegou ao Mailpit` },
    )
    .toBe(true);
  return (await request.get(`${MAILPIT}/api/v1/message/${encontrado.ID}`)).json();
}

export async function semRolagemHorizontal(pagina) {
  const { largura, janela } = await pagina.evaluate(() => ({
    largura: document.documentElement.scrollWidth,
    janela: window.innerWidth,
  }));
  expect(largura, 'a página não pode rolar na horizontal').toBeLessThanOrEqual(janela);
}
