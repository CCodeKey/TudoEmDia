# ARQUITETURA DO PROJETO

## Visão geral

O TudoEmDia será uma aplicação web dividida em duas camadas principais:

- **Back-end:** Java com Spring, responsável pela API, regras de negócio, autenticação, autorização, persistência e rotinas agendadas.
- **Front-end:** React com Vite e JavaScript, responsável pela interface, navegação e interação com a API.

Os serviços serão executados em contêineres Docker para padronizar o ambiente de desenvolvimento, testes e produção.

## Tecnologias

| Camada | Tecnologia | Responsabilidade |
|---|---|---|
| Back-end | Java + Spring | API REST, regras de negócio e segurança |
| Arquitetura do back-end | MVC | Separação entre requisições, regras e dados |
| Front-end | React + Vite + JavaScript | Interface web responsiva |
| Comunicação | HTTP/HTTPS + JSON | Comunicação entre front-end e back-end |
| Empacotamento | Docker e Docker Compose | Isolamento e execução dos serviços |
| Persistência | PostgreSQL | Armazenamento relacional e consistente das entidades do sistema |

## Padrão MVC

O back-end utilizará o padrão **Model-View-Controller**, adaptado para uma API REST:

### Model

Representa os dados e as regras de persistência do sistema.

- Entidades JPA correspondentes ao modelo de dados.
- Repositórios responsáveis pelo acesso ao banco.
- DTOs para entrada e saída de dados da API.
- Validações de dados e relacionamentos.

### View

Como o sistema será uma API REST, a view será representada pelas respostas HTTP em JSON. A interface visual ficará no front-end React.

### Controller

Recebe as requisições HTTP, valida os dados de entrada e encaminha a operação para a camada de serviço. Os controllers não devem concentrar regras de negócio.

## Organização do back-end

O back-end deve ser organizado por responsabilidade:

```text
backend/
└── src/main/java/
    └── .../
        ├── controller/
        ├── service/
        ├── repository/
        ├── model/
        ├── dto/
        ├── config/
        ├── security/
        └── exception/
```

- `controller`: endpoints REST.
- `service`: regras de negócio e transações.
- `repository`: persistência e consultas.
- `model`: entidades e enums do domínio.
- `dto`: contratos de entrada e saída da API.
- `config`: configurações do Spring e da aplicação.
- `security`: autenticação, autorização e isolamento por empresa.
- `exception`: tratamento padronizado de erros.

## Organização do front-end

```text
frontend/
└── src/
    ├── components/
    ├── pages/
    ├── layouts/
    ├── services/
    ├── hooks/
    ├── contexts/
    ├── routes/
    └── utils/
```

- `components`: componentes reutilizáveis.
- `pages`: telas da aplicação.
- `layouts`: estruturas comuns das páginas.
- `services`: chamadas para a API.
- `hooks`: lógica reutilizável do React.
- `contexts`: estado compartilhado, como usuário autenticado e empresa selecionada.
- `routes`: rotas públicas e protegidas.
- `utils`: funções auxiliares.

## Comunicação entre as camadas

1. O usuário interage com uma tela React.
2. O front-end envia uma requisição HTTP para a API Spring.
3. O controller recebe a requisição e encaminha a operação ao service.
4. O service aplica as regras de negócio e utiliza o repository quando necessário.
5. A API retorna uma resposta JSON com sucesso ou erro padronizado.
6. O React atualiza a interface com o resultado.

Todas as rotas protegidas devem exigir autenticação e validar se o usuário possui permissão para acessar a empresa e a funcionalidade solicitada.

## Segurança e isolamento de dados

- Senhas armazenadas somente como hash.
- Autenticação baseada em sessão ou token seguro.
- Autorização por perfil: proprietário, financeiro e contador.
- Toda consulta de dados empresariais deve considerar a empresa do usuário autenticado.
- Validação de entrada no front-end e no back-end.
- Comunicação HTTPS em produção.
- Dados sensíveis não devem ser incluídos em logs.
- Arquivos anexados devem ser validados e acessados somente por usuários autorizados.

## Serviços Docker

O ambiente deverá utilizar contêineres independentes para os principais serviços:

```text
docker-compose.yml
├── backend
├── frontend
└── postgres
```

- `backend`: executa a aplicação Java Spring.
- `frontend`: executa o React/Vite em desenvolvimento e disponibiliza os arquivos compilados em produção.
- `postgres`: executa o banco PostgreSQL utilizado pela aplicação.

As credenciais, URLs e demais configurações devem ser fornecidas por variáveis de ambiente, sem serem gravadas diretamente no código-fonte.

## Rotinas agendadas

O back-end Spring será responsável por rotinas agendadas para:

- gerar novas ocorrências de obrigações recorrentes;
- identificar obrigações vencidas;
- enviar alertas nos prazos de 7, 3 e 1 dia antes do vencimento e no dia do vencimento;
- impedir o envio duplicado do mesmo alerta.

Essas rotinas devem ser idempotentes e registrar o resultado do processamento.

## Requisitos não funcionais

Esta arquitetura contribui para os requisitos não funcionais da seguinte forma:

- **Segurança e privacidade:** autenticação, autorização por perfil e isolamento por empresa.
- **Integridade:** validações, transações e restrições no banco de dados.
- **Desempenho:** API REST, consultas específicas e índices nas chaves estrangeiras e datas usadas nos filtros.
- **Disponibilidade:** serviços containerizados e configuração independente por ambiente.
- **Manutenibilidade:** separação por camadas, responsabilidades claras e DTOs.
- **Responsividade e compatibilidade:** interface React adaptável a computadores, tablets e celulares.
- **Proteção na comunicação:** HTTPS no ambiente de produção.
