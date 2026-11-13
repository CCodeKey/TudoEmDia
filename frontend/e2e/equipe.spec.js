import { expect, test } from '@playwright/test';
import { SENHA, cadastrarPelaTela, criarEmpresaPelaTela, mensagem, preencherCadastro, sufixo, ultimoEmail } from './apoio';

// Convite por e-mail, aceite pelo link e o que cada perfil pode fazer
test('contador convidado por e-mail aceita pelo link e só consulta', async ({ browser, request }) => {
  const id = sufixo();
  const emailContador = `contador.${id}@teste.com`;

  // Proprietário convida
  const dono = await browser.newPage();
  await cadastrarPelaTela(dono, { nome: 'Paulo Dono', email: `paulo.${id}@teste.com` });
  const empresaId = await criarEmpresaPelaTela(dono, 'Mercadinho do Paulo');
  await dono.goto(`/empresas/${empresaId}/equipe`);
  await dono.getByRole('tab', { name: 'Convites' }).click();
  await dono.getByLabel('E-mail da pessoa').fill(emailContador);
  await dono.getByLabel('Perfil').selectOption('CONTADOR');
  await dono.getByRole('button', { name: 'Enviar convite' }).click();
  await mensagem(dono, `Convite enviado para ${emailContador}.`);
  await expect(dono.getByRole('row', { name: new RegExp(emailContador) })).toContainText('Pendente');

  // O e-mail chega com o link do convite
  const email = await ultimoEmail(request, emailContador);
  expect(email.Subject).toContain('Mercadinho do Paulo');
  const caminhoConvite = email.Text.match(/\/convites\/[A-Za-z0-9_-]+/)[0];

  // Contador abre o link, cria a conta com o e-mail já preenchido e aceita
  const contador = await (await browser.newContext()).newPage();
  await contador.goto(caminhoConvite);
  await expect(contador.getByText('Você foi convidado')).toBeVisible();
  await expect(contador.getByText('Mercadinho do Paulo')).toBeVisible();
  await contador.getByRole('button', { name: 'Criar conta' }).click();
  await expect(contador.getByLabel('E-mail')).toHaveValue(emailContador);
  await preencherCadastro(contador, { nome: 'Carlos Contador' });
  await expect(contador).toHaveURL(new RegExp(caminhoConvite));
  await contador.getByRole('button', { name: 'Aceitar convite' }).click();
  await expect(contador).toHaveURL(new RegExp(`/empresas/${empresaId}/painel$`));

  // E-mail de boas-vindas também chegou
  expect((await ultimoEmail(request, emailContador)).Subject).toBeTruthy();

  // Menu e botões conforme o perfil (RN24)
  const menu = contador.getByRole('navigation');
  await expect(menu.getByRole('link', { name: 'Obrigações' })).toBeVisible();
  await expect(menu.getByRole('link', { name: 'Equipe' })).toHaveCount(0);
  await expect(contador.getByRole('button', { name: 'Nova obrigação' })).toHaveCount(0);
  await contador.goto(`/empresas/${empresaId}/obrigacoes`);
  await expect(contador.getByRole('button', { name: 'Exportar PDF' })).toBeVisible();
  await expect(contador.getByRole('button', { name: 'Nova obrigação' })).toHaveCount(0);
  await contador.goto(`/empresas/${empresaId}/equipe`);
  await expect(contador.getByText('Acesso restrito')).toBeVisible();

  // O mesmo link não pode ser usado de novo
  await contador.goto(caminhoConvite);
  await expect(contador.getByText('Este convite já foi aceito.')).toBeVisible();

  // Proprietário vê o contador e desativa o acesso; não consegue desativar a si mesmo (RN19, RN20)
  await dono.goto(`/empresas/${empresaId}/equipe`);
  await expect(dono.getByRole('row', { name: /Carlos Contador/ })).toBeVisible();
  await dono.getByRole('switch', { name: 'Acesso de Carlos Contador' }).uncheck();
  await mensagem(dono, 'Acesso desativado.');
  await dono.getByRole('switch', { name: 'Acesso de Paulo Dono' }).uncheck();
  await mensagem(dono, 'A empresa precisa ter pelo menos um proprietário ativo.');
  await dono.reload();
  await expect(dono.getByRole('switch', { name: 'Acesso de Paulo Dono' })).toBeChecked();

  // Sem acesso, o contador é levado para fora da empresa
  await contador.goto(`/empresas/${empresaId}/painel`);
  await expect(contador).toHaveURL(/\/empresas$/);

  // Contador exclui a própria conta (LGPD) e não consegue mais entrar
  await contador.goto('/conta');
  await contador.getByRole('button', { name: 'Excluir minha conta' }).click();
  await contador.getByRole('dialog').getByLabel('Senha', { exact: true }).fill(SENHA);
  await contador.getByRole('dialog').getByRole('button', { name: 'Excluir conta' }).click();
  await expect(contador).toHaveURL(/\/login$/);
  await contador.getByLabel('E-mail').fill(emailContador);
  await contador.getByLabel('Senha', { exact: true }).fill(SENHA);
  await contador.getByRole('button', { name: 'Entrar' }).click();
  await expect(contador.getByText('E-mail ou senha inválidos.')).toBeVisible();
});

test('convite para outro e-mail não pode ser aceito por quem está logado', async ({ browser, request }) => {
  const id = sufixo();
  const convidado = `convidado.${id}@teste.com`;
  const dono = await browser.newPage();
  await cadastrarPelaTela(dono, { nome: 'Rita Dona', email: `rita.${id}@teste.com` });
  const empresaId = await criarEmpresaPelaTela(dono, 'Loja da Rita');
  await dono.goto(`/empresas/${empresaId}/equipe`);
  await dono.getByRole('tab', { name: 'Convites' }).click();
  await dono.getByLabel('E-mail da pessoa').fill(convidado);
  await dono.getByRole('button', { name: 'Enviar convite' }).click();
  await mensagem(dono, `Convite enviado para ${convidado}.`);
  const caminhoConvite = (await ultimoEmail(request, convidado)).Text.match(/\/convites\/[A-Za-z0-9_-]+/)[0];

  // A própria dona abre o link: o e-mail não confere (RN16)
  await dono.goto(caminhoConvite);
  await expect(dono.getByText(/mas o convite foi enviado para/)).toBeVisible();
  await expect(dono.getByRole('button', { name: 'Aceitar convite' })).toHaveCount(0);

  // Cancelado pela dona, o link deixa de valer
  await dono.goto(`/empresas/${empresaId}/equipe`);
  await dono.getByRole('tab', { name: 'Convites' }).click();
  await dono.getByRole('row', { name: new RegExp(convidado) }).getByRole('button', { name: 'Cancelar' }).click();
  await dono.getByRole('dialog').getByRole('button', { name: 'Cancelar convite' }).click();
  await mensagem(dono, 'Convite cancelado.');
  await dono.goto(caminhoConvite);
  await expect(dono.getByText('Este convite foi cancelado pelo proprietário da empresa.')).toBeVisible();
});
