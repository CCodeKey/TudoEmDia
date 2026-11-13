import { expect, test } from '@playwright/test';
import { SENHA, cadastrarPelaTela, criarEmpresaPelaTela, sufixo } from './apoio';

test.describe('landing page', () => {
  test('apresenta o produto e leva ao cadastro e ao login', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { level: 1 })).toContainText('Nenhuma conta');
    await expect(page.getByRole('heading', { name: 'Quem faz o TudoEmDia' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Política de privacidade' })).toBeVisible();

    await page.getByRole('link', { name: 'Começar agora' }).click();
    await expect(page).toHaveURL(/\/cadastro$/);
    await page.goBack();
    await page.getByRole('banner').getByRole('link', { name: 'Entrar' }).click();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('política de privacidade e termos abrem sem login', async ({ page }) => {
    await page.goto('/privacidade');
    await expect(page.getByRole('heading', { name: 'Política de privacidade' })).toBeVisible();
    await page.goto('/termos');
    await expect(page.getByRole('heading', { name: 'Termos de uso' })).toBeVisible();
  });

  test('página inexistente mostra aviso', async ({ page }) => {
    await page.goto('/pagina-que-nao-existe');
    await expect(page.getByText('Página não encontrada')).toBeVisible();
  });
});

test.describe('cadastro e login', () => {
  test('cadastro valida campos, mede a força da senha e mostra a senha no olhinho', async ({ page }) => {
    await page.goto('/cadastro');
    await page.getByRole('button', { name: 'Criar conta' }).click();
    await expect(page.getByText('Informe o nome.')).toBeVisible();
    await expect(page.getByText('É necessário aceitar os termos para continuar.')).toBeVisible();

    const senha = page.getByLabel('Senha', { exact: true });
    for (const [valor, nivel] of [['abc12', 'Fraca'], ['padaria2026', 'Razoável'], ['Padaria2026', 'Boa'], [SENHA, 'Forte']]) {
      await senha.fill(valor);
      await expect(page.getByText(`Força da senha: ${nivel}`)).toBeVisible();
    }

    await expect(senha).toHaveAttribute('type', 'password');
    await page.getByRole('button', { name: 'Mostrar senha' }).click();
    await expect(senha).toHaveAttribute('type', 'text');
    await page.getByRole('button', { name: 'Ocultar senha' }).click();
    await expect(senha).toHaveAttribute('type', 'password');
  });

  test('cria a conta, recusa CNPJ inválido e cadastra a empresa', async ({ page }) => {
    const id = sufixo();
    await cadastrarPelaTela(page, { nome: 'Lia Teste', email: `lia.${id}@teste.com` });
    await expect(page.getByRole('heading', { name: 'Boas-vindas, Lia!' })).toBeVisible();

    await page.getByLabel('Nome da empresa').fill('Empresa da Lia');
    await page.getByLabel('CNPJ').fill('11.111.111/1111-11');
    await page.getByRole('button', { name: 'Cadastrar empresa' }).click();
    await expect(page.getByText('CNPJ inválido.')).toBeVisible();

    await page.goto('/empresas');
    await criarEmpresaPelaTela(page, 'Empresa da Lia');
    await expect(page.getByRole('heading', { name: 'Painel' })).toBeVisible();
  });

  test('e-mail repetido é recusado no cadastro', async ({ page }) => {
    const email = `repetido.${sufixo()}@teste.com`;
    await cadastrarPelaTela(page, { nome: 'Primeira', email });
    await expect(page).toHaveURL(/\/empresas$/);
    await page.getByRole('button', { name: 'Sair' }).click();

    await cadastrarPelaTela(page, { nome: 'Segunda', email });
    await expect(page.getByText('Já existe uma conta com este e-mail.')).toBeVisible();
  });

  test('login com senha errada não diz se o erro é no e-mail ou na senha', async ({ page }) => {
    const email = `login.${sufixo()}@teste.com`;
    await cadastrarPelaTela(page, { nome: 'Mário', email });
    await page.getByRole('button', { name: 'Sair' }).click();
    await expect(page).toHaveURL(/\/login$/);

    await page.getByLabel('E-mail').fill(email);
    await page.getByLabel('Senha', { exact: true }).fill('SenhaErrada1');
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page.getByText('E-mail ou senha inválidos.')).toBeVisible();

    await page.getByLabel('Senha', { exact: true }).fill(SENHA);
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page).toHaveURL(/\/empresas$/);
  });

  test('rota protegida leva ao login e volta para ela depois', async ({ page }) => {
    const email = `volta.${sufixo()}@teste.com`;
    await cadastrarPelaTela(page, { nome: 'Nina', email });
    await page.getByRole('button', { name: 'Sair' }).click();

    await page.goto('/conta');
    await expect(page).toHaveURL(/\/login$/);
    await page.getByLabel('E-mail').fill(email);
    await page.getByLabel('Senha', { exact: true }).fill(SENHA);
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page).toHaveURL(/\/conta$/);
    await expect(page.getByRole('heading', { name: 'Minha conta' })).toBeVisible();
  });
});
