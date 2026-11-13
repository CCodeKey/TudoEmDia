# TudoEmDia — Front-end

Interface web do TudoEmDia, em React + Vite + JavaScript, seguindo a [arquitetura](../docs/07-arquitetura.md) do projeto. Consome a API REST do [back-end](../backend/README.md).

## Tecnologias

| Tecnologia | Uso |
|---|---|
| React 19 | Interface |
| React Router 8 | Rotas públicas e protegidas |
| Vite 8 | Servidor de desenvolvimento e build |
| Lucide | Ícones |
| CSS próprio | Design system com tokens, tema claro e escuro automático |
| ESLint | Linter |

Gráficos, modais e notificações são componentes próprios, sem bibliotecas extras.

## Executando

### Junto com o resto, no Docker

Na raiz do repositório:

```bash
docker compose up -d --build
```

Acesse **http://localhost:5173**. O Nginx do container serve o front e repassa `/api` ao back-end.

### Em modo de desenvolvimento (recarrega a cada alteração)

Com o back-end rodando em `localhost:8080` (pelo Docker ou pela IDE):

```bash
cd frontend
npm install
npm run dev
```

Acesse **http://localhost:5173**. Se o front do Docker estiver rodando, pare-o antes (`docker compose stop frontend`), pois os dois usam a mesma porta.

O Vite repassa as chamadas `/api` para o back-end. Para usar outro endereço:

```bash
VITE_API_PROXY=http://localhost:8081 npm run dev
```

### Outros comandos

```bash
npm run lint      # linter
npm run build     # gera a versão de produção em dist/
npm run preview   # serve a versão de produção localmente
```

## Estrutura

```text
src/
├── components/     # Componentes reutilizáveis
│   ├── ui/         # Botão, campos, modal, selos, estados vazios
│   ├── graficos/   # Barras por categoria e anel do indicador
│   └── obrigacoes/ # Formulário, pagamento e anexos de obrigações
├── contexts/       # Usuário logado, empresa selecionada e notificações
├── hooks/          # useAuth, useEmpresa, useCarregar, useAcao...
├── layouts/        # Login/cadastro, área logada e páginas simples
├── pages/          # Telas
├── routes/         # Rotas e proteções (login e permissões)
├── services/       # Chamadas à API
├── styles/         # Design system (global.css)
└── utils/          # Formatação, rótulos e arquivos
```

## Como funciona

- **Login por sessão:** o back-end guarda a sessão num cookie, e todas as chamadas usam `credentials: "include"`. Quando a sessão expira (RN07), a resposta 401 leva de volta ao login.
- **CSRF:** se a proteção CSRF do back-end for ligada (`CSRF_HABILITADO=true`), o front já envia o cabeçalho `X-XSRF-TOKEN`. Não é preciso mudar nada.
- **Empresa na URL:** as telas ficam em `/empresas/{id}/...`. O link dos e-mails de alerta (`/empresas/{id}/obrigacoes/{id}`) abre direto a obrigação, e o link de convite (`/convites/{token}`) abre a tela de aceite.
- **Permissões:** o menu e os botões aparecem conforme `GET /api/empresas/{id}/permissoes` (RN24). A verificação de verdade é sempre feita pelo back-end (RN23).
- **Responsivo:** em telas estreitas o menu vira gaveta e as tabelas viram cartões, sem rolagem horizontal até 360 px (RNF07).
- **LGPD:** a política de privacidade e os termos ficam no rodapé de todas as páginas, e "Minha conta" permite consultar os dados e excluir a conta (RNF14).

## Telas

| Tela | Rota | Quem acessa |
|---|---|---|
| Página inicial (landing) | `/` | Público; quem já entrou segue direto para a empresa |
| Entrar / Criar conta | `/login`, `/cadastro` | Público |
| Convite | `/convites/{token}` | Público (o aceite exige login) |
| Empresas | `/empresas` | Logado |
| Painel | `/empresas/{id}/painel` | Todos os perfis |
| Obrigações e detalhe | `/empresas/{id}/obrigacoes` | Todos (cadastro e edição: proprietário e financeiro) |
| Recorrências | `/empresas/{id}/recorrencias` | Todos (edição: proprietário e financeiro) |
| Pagamentos | `/empresas/{id}/pagamentos` | Todos (cancelamento: proprietário) |
| Documentos | `/empresas/{id}/documentos` | Todos (cadastro e edição: proprietário) |
| Equipe | `/empresas/{id}/equipe` | Proprietário |
| Configurações | `/empresas/{id}/configuracoes` | Todos (alertas: proprietário) |
| Minha conta | `/conta` | Logado |
