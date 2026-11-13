import { expect, test } from '@playwright/test';
import { PDF, PNG, api, cadastrarPelaTela, criarEmpresaPelaTela, dataBr, dataIso, mensagem, sufixo } from './apoio';

// Jornada da proprietária, do cadastro da empresa ao relatório, numa única sessão
test.describe.configure({ mode: 'serial' });

let pagina;
let empresaId;
let categorias;

const rota = (caminho) => `/empresas/${empresaId}/${caminho}`;
const dialogo = () => pagina.getByRole('dialog');

test.beforeAll(async ({ browser }) => {
  pagina = await browser.newPage();
  await cadastrarPelaTela(pagina, { nome: 'Olívia Dona', email: `olivia.${sufixo()}@teste.com` });
  empresaId = await criarEmpresaPelaTela(pagina, 'Padaria da Olívia');
  categorias = Object.fromEntries(
    (await api(pagina, 'GET', `/empresas/${empresaId}/categorias`)).map((categoria) => [categoria.nome, categoria.id]),
  );
});

test.afterAll(async () => {
  await pagina.close();
});

test('cadastra obrigação recorrente pela tela; vencida desde o início', async () => {
  await pagina.goto(rota('obrigacoes'));
  await pagina.getByRole('button', { name: 'Nova obrigação' }).first().click();
  await dialogo().getByLabel('Descrição').fill('Aluguel da loja');
  await dialogo().getByLabel('Valor (R$)').fill('3200');
  await dialogo().getByLabel('Vencimento').fill(dataIso(-40));
  await dialogo().getByLabel('Categoria').selectOption({ label: 'Aluguel e consumo' });
  await dialogo().getByText('Repetir esta obrigação').click();
  await dialogo().getByLabel('Periodicidade').selectOption('MENSAL');
  await dialogo().getByRole('button', { name: 'Cadastrar' }).click();

  await mensagem(pagina, 'Obrigação cadastrada.');
  await expect(pagina.getByRole('heading', { name: 'Aluguel da loja' })).toBeVisible();
  await expect(pagina.getByText('Vencida', { exact: true })).toBeVisible();

  await pagina.goto(rota('obrigacoes'));
  await expect(pagina.getByRole('row', { name: /Aluguel da loja/ }).first()).toContainText('Mensal');
  expect(await pagina.getByRole('row', { name: /Aluguel da loja/ }).count()).toBeGreaterThanOrEqual(2);
});

test('valida o formulário de obrigação', async () => {
  await pagina.goto(rota('obrigacoes'));
  await pagina.getByRole('button', { name: 'Nova obrigação' }).first().click();
  await dialogo().getByRole('button', { name: 'Cadastrar' }).click();
  await expect(dialogo().getByText('Informe a descrição.')).toBeVisible();
  await expect(dialogo().getByText('Informe um valor maior que zero.')).toBeVisible();
  await expect(dialogo().getByText('Escolha uma categoria.')).toBeVisible();
  await pagina.keyboard.press('Escape');
  await expect(dialogo()).toBeHidden();
});

test('recorrência sem data de vencimento usa hoje e avisa isso', async () => {
  await pagina.goto(rota('obrigacoes'));
  await pagina.getByRole('button', { name: 'Nova obrigação' }).first().click();
  await dialogo().getByLabel('Descrição').fill('Contador mensal');
  await dialogo().getByLabel('Valor (R$)').fill('450');
  await dialogo().getByLabel('Categoria').selectOption({ label: 'Outros' });
  await dialogo().getByText('Repetir esta obrigação').click();
  await expect(
    dialogo().getByText('Se não adicionar uma data, hoje será usado como data da recorrência.'),
  ).toBeVisible();
  await dialogo().getByLabel(/^Vencimento/).fill('');
  await dialogo().getByRole('button', { name: 'Cadastrar' }).click();

  await mensagem(pagina, 'Obrigação cadastrada.');
  await expect(pagina.getByRole('heading', { name: 'Contador mensal' })).toBeVisible();
  await expect(pagina.locator('dl div', { hasText: 'Vencimento' })).toContainText(dataBr(dataIso(0)));
  await expect(pagina.locator('dl div', { hasText: 'Recorrência' })).toContainText('Mensal');
});

test('torna recorrente uma obrigação ao editá-la', async () => {
  const seguro = await api(pagina, 'POST', `/empresas/${empresaId}/obrigacoes`, {
    descricao: 'Seguro da loja',
    valor: 380,
    categoriaId: categorias.Outros,
    dataVencimento: dataIso(12),
  });
  await pagina.goto(rota(`obrigacoes/${seguro.id}`));
  await pagina.getByRole('button', { name: 'Editar' }).click();
  await dialogo().getByText('Tornar esta obrigação recorrente').click();
  await dialogo().getByLabel('Periodicidade').selectOption('ANUAL');
  await dialogo().getByRole('button', { name: 'Salvar alterações' }).click();

  await mensagem(pagina, 'Obrigação atualizada e agora se repete.');
  await expect(pagina.locator('dl div', { hasText: 'Recorrência' })).toContainText('Anual');

  // Já recorrente: a edição mostra o aviso em vez da opção
  await pagina.getByRole('button', { name: 'Editar' }).click();
  await expect(dialogo().getByText('Esta obrigação se repete (anual).')).toBeVisible();
  await expect(dialogo().getByText('Tornar esta obrigação recorrente')).toHaveCount(0);
  await pagina.keyboard.press('Escape');
});

test('registra e cancela pagamento; obrigação paga não muda valor', async () => {
  const das = await api(pagina, 'POST', `/empresas/${empresaId}/obrigacoes`, {
    descricao: 'DAS Simples Nacional',
    valor: 812.45,
    categoriaId: categorias.Impostos,
    dataVencimento: dataIso(-2),
  });
  await pagina.goto(rota(`obrigacoes/${das.id}`));
  await pagina.getByLabel('Valor pago (R$)').fill('830.10');
  await pagina.getByRole('button', { name: 'Registrar pagamento' }).click();
  await mensagem(pagina, 'Pagamento registrado.');
  await expect(pagina.getByText('Paga com atraso')).toBeVisible();
  await expect(pagina.getByText('R$ 830,10')).toBeVisible();

  // Valor e vencimento bloqueados enquanto houver pagamento (RN34)
  await pagina.getByRole('button', { name: 'Editar' }).click();
  await expect(dialogo().getByLabel('Valor (R$)')).toBeDisabled();
  await expect(dialogo().getByLabel('Vencimento')).toBeDisabled();
  await dialogo().getByLabel('Descrição').fill('DAS de outubro');
  await dialogo().getByRole('button', { name: 'Salvar alterações' }).click();
  await mensagem(pagina, 'Obrigação atualizada.');
  await expect(pagina.getByRole('heading', { name: 'DAS de outubro' })).toBeVisible();
  // Registro de quem alterou e quando (RN35)
  await expect(pagina.locator('dl div', { hasText: 'Última alteração' })).toContainText('Olívia Dona');

  // Cancelamento volta a obrigação para vencida, e o histórico guarda os dois estados (RN70)
  await pagina.getByRole('button', { name: 'Cancelar pagamento' }).click();
  await dialogo().getByRole('button', { name: 'Cancelar pagamento' }).click();
  await mensagem(pagina, 'Pagamento cancelado');
  await expect(pagina.getByText('Vencida', { exact: true })).toBeVisible();

  await pagina.goto(rota('pagamentos'));
  await expect(pagina.getByRole('row', { name: /DAS de outubro/ })).toContainText('Cancelado');
});

test('anexa, baixa e remove boleto; recusa arquivo falso', async () => {
  const luz = await api(pagina, 'POST', `/empresas/${empresaId}/obrigacoes`, {
    descricao: 'Conta de luz',
    valor: 487.9,
    categoriaId: categorias['Aluguel e consumo'],
    dataVencimento: dataIso(5),
  });
  await pagina.goto(rota(`obrigacoes/${luz.id}`));
  const seletor = pagina.locator('input[type=file]');

  await seletor.setInputFiles({ name: 'boleto.pdf', mimeType: 'application/pdf', buffer: PDF });
  await mensagem(pagina, 'Anexo enviado.');
  await expect(pagina.getByText('boleto.pdf', { exact: true })).toBeVisible();

  const [download] = await Promise.all([
    pagina.waitForEvent('download'),
    pagina.getByRole('button', { name: 'Baixar boleto.pdf' }).click(),
  ]);
  expect(download.suggestedFilename()).toBe('boleto.pdf');

  // Extensão de PDF com conteúdo de imagem: recusado pelo servidor (RN28)
  await seletor.setInputFiles({ name: 'falso.pdf', mimeType: 'application/pdf', buffer: PNG });
  await mensagem(pagina, 'Arquivo inválido');

  // Tipo não aceito: recusado antes de enviar
  await seletor.setInputFiles({ name: 'planilha.xlsx', mimeType: 'application/octet-stream', buffer: PDF });
  await mensagem(pagina, 'Envie um arquivo PDF, JPG ou PNG.');

  await pagina.getByRole('button', { name: 'Remover boleto.pdf' }).click();
  await dialogo().getByRole('button', { name: 'Remover' }).click();
  await mensagem(pagina, 'Anexo removido.');
  await expect(pagina.getByText('Nenhum anexo.')).toBeVisible();
});

test('exclui obrigação recorrente escolhendo "esta e as próximas"', async () => {
  const internet = await api(pagina, 'POST', `/empresas/${empresaId}/obrigacoes`, {
    descricao: 'Internet',
    valor: 120,
    categoriaId: categorias['Aluguel e consumo'],
    dataVencimento: dataIso(3),
    recorrencia: { periodicidade: 'MENSAL' },
  });
  await pagina.goto(rota(`obrigacoes/${internet.id}`));
  await pagina.getByRole('button', { name: 'Excluir' }).click();
  await expect(dialogo().getByText('Esta obrigação se repete')).toBeVisible();
  await dialogo().getByLabel('Esta e as próximas (encerra a recorrência)').check();
  await dialogo().getByRole('button', { name: 'Excluir' }).click();
  await mensagem(pagina, 'Obrigação excluída.');
  await expect(pagina).toHaveURL(new RegExp(`${rota('obrigacoes')}$`));
  await expect(pagina.getByRole('row', { name: /Internet/ })).toHaveCount(0);
});

test('filtra a lista e exporta o PDF', async () => {
  await pagina.goto(rota('obrigacoes'));
  await pagina.getByLabel('Situação').selectOption('VENCIDA');
  await expect(pagina).toHaveURL(/status=VENCIDA/);
  const linhas = pagina.locator('tbody tr');
  await expect(linhas.first()).toBeVisible();
  for (const linha of await linhas.all()) {
    await expect(linha).toContainText('Vencida');
  }

  const [relatorio] = await Promise.all([
    pagina.waitForEvent('download'),
    pagina.getByRole('button', { name: 'Exportar PDF' }).click(),
  ]);
  expect(relatorio.suggestedFilename()).toMatch(/^relatorio-obrigacoes-.*\.pdf$/);

  await pagina.getByRole('button', { name: 'Todas' }).click();
  await pagina.getByLabel('Situação').selectOption('');
  await expect(pagina.getByText(/^\d+ obrigaç(ão|ões)$/)).toBeVisible();
});

test('painel mostra indicadores e navega entre meses', async () => {
  await pagina.goto(rota('painel'));
  await expect(pagina.getByText('A pagar no mês')).toBeVisible();
  await expect(pagina.getByText('Vencidas e não pagas')).toBeVisible();
  await expect(pagina.getByRole('link', { name: /Aluguel da loja/ }).first()).toBeVisible();

  const mes = pagina.getByRole('group', { name: 'Mês do painel' }).locator('span[aria-live]');
  const atual = await mes.innerText();
  await pagina.getByRole('button', { name: 'Mês anterior' }).click();
  await expect(mes).not.toHaveText(atual);
  await pagina.getByRole('button', { name: 'Próximo mês' }).click();
  await expect(mes).toHaveText(atual);
});

test('cadastra, edita, arquiva, baixa e exclui documento', async () => {
  await pagina.goto(rota('documentos'));
  await pagina.getByRole('button', { name: 'Novo documento' }).first().click();
  await dialogo().getByLabel('Descrição').fill('Alvará de funcionamento');
  await dialogo().getByLabel('Categoria').selectOption({ label: 'Outros' });
  await dialogo().getByLabel(/^Validade/).fill(dataIso(-1));
  await dialogo().locator('input[type=file]').setInputFiles({ name: 'alvará.pdf', mimeType: 'application/pdf', buffer: PDF });
  await dialogo().getByRole('button', { name: 'Cadastrar' }).click();
  await mensagem(pagina, 'Documento cadastrado.');

  const linha = pagina.getByRole('row', { name: /Alvará de funcionamento/ });
  await expect(linha).toContainText('Vencido');
  await expect(linha).toContainText(dataBr(dataIso(-1)));

  const [arquivo] = await Promise.all([
    pagina.waitForEvent('download'),
    linha.getByRole('button', { name: 'Baixar Alvará de funcionamento' }).click(),
  ]);
  expect(arquivo.suggestedFilename()).toBe('alvará.pdf');

  await linha.getByRole('button', { name: 'Editar Alvará de funcionamento' }).click();
  await dialogo().getByLabel(/^Validade/).fill(dataIso(365));
  await dialogo().getByRole('button', { name: 'Salvar alterações' }).click();
  await mensagem(pagina, 'Documento atualizado.');
  await expect(linha).toContainText('Válido');

  await linha.getByRole('button', { name: 'Editar Alvará de funcionamento' }).click();
  await dialogo().getByText('Arquivar documento').click();
  await dialogo().getByRole('button', { name: 'Salvar alterações' }).click();
  await expect(linha).toContainText('Arquivado');

  await linha.getByRole('button', { name: 'Excluir Alvará de funcionamento' }).click();
  await dialogo().getByRole('button', { name: 'Excluir' }).click();
  await mensagem(pagina, 'Documento excluído.');
  await expect(pagina.getByText('Nenhum documento cadastrado')).toBeVisible();
});

test('configura categorias e alertas', async () => {
  await pagina.goto(rota('configuracoes'));
  await pagina.getByLabel('Nova categoria').fill('Marketing');
  await pagina.getByRole('button', { name: 'Adicionar' }).click();
  await mensagem(pagina, 'Categoria criada.');

  // Categoria em uso não pode ser excluída (RN42)
  const marketing = (await api(pagina, 'GET', `/empresas/${empresaId}/categorias`)).find((c) => c.nome === 'Marketing');
  const anuncio = await api(pagina, 'POST', `/empresas/${empresaId}/obrigacoes`, {
    descricao: 'Anúncio',
    valor: 200,
    categoriaId: marketing.id,
    dataVencimento: dataIso(20),
  });
  await pagina.getByRole('button', { name: 'Excluir Marketing' }).click();
  await dialogo().getByRole('button', { name: 'Excluir' }).click();
  await mensagem(pagina, 'Uma categoria em uso não pode ser excluída.');

  await api(pagina, 'DELETE', `/empresas/${empresaId}/obrigacoes/${anuncio.id}`);
  await pagina.getByRole('button', { name: 'Excluir Marketing' }).click();
  await dialogo().getByRole('button', { name: 'Excluir' }).click();
  await mensagem(pagina, 'Categoria excluída.');

  // Alertas: desligar um prazo e salvar; desligar todos não é permitido (RN76)
  await pagina.getByRole('switch', { name: '7 dias antes' }).uncheck();
  await pagina.getByRole('button', { name: 'Salvar alertas' }).click();
  await mensagem(pagina, 'Alertas salvos.');
  for (const prazo of ['3 dias antes', '1 dia antes', 'No dia do vencimento']) {
    await pagina.getByRole('switch', { name: prazo }).uncheck();
  }
  await expect(pagina.getByText('Mantenha pelo menos um prazo ativo.')).toBeVisible();
  await expect(pagina.getByRole('button', { name: 'Salvar alertas' })).toBeDisabled();
});

test('edita a recorrência', async () => {
  await pagina.goto(rota('recorrencias'));
  const linha = pagina.getByRole('row', { name: /Aluguel da loja/ });
  await linha.getByRole('button', { name: 'Editar' }).click();
  await dialogo().getByLabel('Valor (R$)').fill('3350');
  await dialogo().getByRole('button', { name: 'Salvar' }).click();
  await mensagem(pagina, 'Recorrência atualizada.');
  await expect(linha).toContainText('R$ 3.350,00');
});

test('minha conta mostra os dados e impede a única proprietária de excluir a conta', async () => {
  await pagina.goto('/conta');
  await expect(pagina.getByText('Padaria da Olívia')).toBeVisible();
  await pagina.getByRole('button', { name: 'Excluir minha conta' }).click();
  await dialogo().getByLabel('Senha', { exact: true }).fill('SenhaErrada1');
  await dialogo().getByRole('button', { name: 'Excluir conta' }).click();
  await expect(dialogo().getByText('Senha incorreta.')).toBeVisible();
  await dialogo().getByLabel('Senha', { exact: true }).fill('Padaria#2026');
  await dialogo().getByRole('button', { name: 'Excluir conta' }).click();
  await expect(dialogo().getByText(/único proprietário/)).toBeVisible();
});
