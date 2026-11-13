import { expect, test } from '@playwright/test';
import { api, cadastrarPelaTela, criarEmpresaPelaTela, dataIso, semRolagemHorizontal, sufixo } from './apoio';

// RNF07: sem rolagem horizontal e com todas as funções acessíveis em 360, 768 e 1366 px
for (const largura of [360, 768, 1366]) {
  test(`telas principais funcionam com ${largura} px de largura`, async ({ page }) => {
    await page.setViewportSize({ width: largura, height: 800 });
    await page.goto('/');
    await semRolagemHorizontal(page);

    await cadastrarPelaTela(page, { nome: 'Sara Responsiva', email: `sara.${largura}.${sufixo()}@teste.com` });
    const empresaId = await criarEmpresaPelaTela(page, 'Empresa da Sara');
    const categoria = (await api(page, 'GET', `/empresas/${empresaId}/categorias`))[0].id;
    await api(page, 'POST', `/empresas/${empresaId}/obrigacoes`, {
      descricao: 'Fornecedor com um nome bem comprido para testar a quebra de linha',
      valor: 1234.56,
      categoriaId: categoria,
      dataVencimento: dataIso(-3),
    });

    for (const caminho of ['painel', 'obrigacoes', 'recorrencias', 'pagamentos', 'documentos', 'equipe', 'configuracoes']) {
      await page.goto(`/empresas/${empresaId}/${caminho}`);
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
      await semRolagemHorizontal(page);
    }

    // No celular, o menu abre como gaveta e leva às outras telas
    if (largura <= 768) {
      await page.getByRole('button', { name: 'Abrir menu' }).click();
      await page.getByRole('navigation').getByRole('link', { name: 'Documentos' }).click();
      await expect(page).toHaveURL(/\/documentos$/);
      await expect(page.getByRole('heading', { name: 'Documentos' })).toBeVisible();
    }
  });
}
