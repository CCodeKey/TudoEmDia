import { defineConfig, devices } from '@playwright/test';

// Testes de ponta a ponta: usam o sistema pelo navegador, com back-end, banco e Mailpit rodando.
// Rode contra um ambiente de teste, pois eles criam contas e empresas.
//   E2E_BASE_URL  endereço do front   (padrão http://localhost:5173)
//   MAILPIT_URL   endereço do Mailpit (padrão http://localhost:8025)
//   PW_CHANNEL    navegador instalado (ex.: msedge, chrome); sem ele, use "npx playwright install chromium"
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: [['list']],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:5173',
    locale: 'pt-BR',
    timezoneId: 'America/Sao_Paulo',
    colorScheme: 'light',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'navegador',
      use: { ...devices['Desktop Chrome'], channel: process.env.PW_CHANNEL || undefined },
    },
  ],
});
