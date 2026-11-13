# TudoEmDia — Back-end

API REST do TudoEmDia, desenvolvida em Java com Spring Boot, seguindo o padrão MVC descrito em [Arquitetura](../docs/07-arquitetura.md).

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem |
| Spring Boot 4 (Web, Validation, Data JPA, Mail, Actuator) | Base da API |
| Spring Security | Autenticação por sessão, CSRF e senhas com BCrypt |
| PostgreSQL | Banco de dados |
| Flyway | Versionamento do esquema do banco |
| Lombok | Geração de getters, setters e construtores |
| PDFBox | Exportação de relatórios em PDF |
| springdoc-openapi | Documentação da API (Swagger UI) |

## Pré-requisitos

- JDK 21 ou superior;
- Docker Desktop (ou Docker Engine com Compose), para o banco local e para os testes de integração;
- Maven não é necessário: o projeto usa o Maven Wrapper (`mvnw`).

## Configuração

As credenciais do banco não ficam no código. O `application.properties` só referencia as variáveis (`${SPRING_DATASOURCE_URL}` etc.), e os valores vêm de um arquivo `.env` na raiz do repositório, que está no `.gitignore`.

Para configurar, copie o [.env.example](../.env.example) para `.env` na raiz e preencha os valores:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tudoemdia
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=senha
```

O Spring carrega esse arquivo por meio de `spring.config.import`, procurando primeiro no diretório atual e depois no diretório pai. Assim ele funciona tanto ao rodar de dentro de `backend/` quanto da raiz. Se as mesmas variáveis estiverem definidas no sistema ou no Docker (`env_file`), elas têm prioridade sobre o arquivo.

Variáveis opcionais:

| Variável | Padrão | Uso |
|---|---|---|
| `FRONTEND_URL` | `http://localhost:5173` | Origem liberada no CORS e base dos links enviados por e-mail |
| `ARMAZENAMENTO_DIR` | `uploads` | Pasta dos anexos e documentos (fica fora do Git e de qualquer pasta pública) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | — | Servidor SMTP para convites e alertas |
| `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS` | `true` | Use `false` com o Mailpit |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT` | — | Banco criado pelo `docker-compose.yml` |
| `SESSION_COOKIE_SECURE`, `EXIGIR_HTTPS` | `false` | Produção com HTTPS (veja abaixo) |
| `CSRF_HABILITADO` | `false` | Proteção CSRF; ligar antes de produção |

Sem `MAIL_HOST`, os e-mails não são enviados: o texto da mensagem aparece no log, o que permite testar convites em desenvolvimento. Nesse modo os alertas de vencimento não são processados.

## Executando

### Tudo no Docker

Na raiz do repositório:

```bash
docker compose up -d --build   # banco, e-mail (Mailpit) e back-end
docker compose logs -f backend # acompanha o log
docker compose down            # para tudo (os dados ficam nos volumes)
```

| Serviço | Endereço |
|---|---|
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Mailpit (caixa de entrada dos e-mails enviados) | http://localhost:8025 |
| PostgreSQL | `localhost:5432` |

Dentro do compose, o back-end usa o Postgres e o Mailpit dele, ignorando a conexão do `.env`.

### Back-end pela IDE, com banco e e-mail no Docker

```bash
docker compose up -d postgres mailpit
```

No `.env`, aponte `SPRING_DATASOURCE_URL` para `jdbc:postgresql://localhost:5432/tudoemdia` (usuário e senha iguais a `POSTGRES_USER` e `POSTGRES_PASSWORD`) e configure `MAIL_HOST=localhost`, `MAIL_PORT=1025`, `MAIL_SMTP_AUTH=false` e `MAIL_SMTP_STARTTLS=false`. Depois:

```bash
./mvnw spring-boot:run     # Linux / macOS / Git Bash
mvnw.cmd spring-boot:run   # Windows (PowerShell / CMD)
```

## Testes e linter

```bash
./mvnw verify               # linter + todos os testes + .jar (o Docker precisa estar rodando)
./mvnw test "-Dtest=*Test"  # só os testes unitários, sem Docker
./mvnw checkstyle:check     # só o linter
```

- **Unitários** (`*Test`): regras de negócio isoladas, com Mockito.
- **Integração** (`*Tests`): sobem a aplicação com um PostgreSQL descartável (Testcontainers) e percorrem fluxos completos pela API HTTP, com sessão e CSRF. Nunca usam o banco do `.env`.
- **Linter**: Checkstyle, configurado em [checkstyle.xml](checkstyle.xml). Roda em todo build e falha se houver violações (RNF11).

## Estrutura de pacotes

```text
src/main/java/ted/com/tudoemdia/
├── config/         # Segurança, CORS, agendamento e relógio no horário de Brasília
├── controller/     # Endpoints REST, sem regras de negócio
├── dto/            # Contratos de entrada e saída da API (records)
├── exception/      # Exceções de negócio e respostas de erro padronizadas
├── model/          # Entidades JPA
│   └── enuns/      # Enums do domínio
├── repository/     # Interfaces Spring Data JPA
├── security/       # Matriz de permissões e controle de acesso por empresa
└── service/        # Regras de negócio, transações e rotinas agendadas
```

## Autenticação e permissões

A API usa **sessão em cookie** (`JSESSIONID`), que expira após 30 minutos sem atividade (RN07). O Postman e o navegador guardam e reenviam esse cookie sozinhos.

> **Por enquanto, a proteção CSRF está desligada** (`CSRF_HABILITADO=false`, o padrão), para facilitar os testes manuais: basta fazer login e chamar os endpoints. **Antes de produção, defina `CSRF_HABILITADO=true`.** Os testes de integração já rodam com ela ligada.

Com o CSRF ligado, as requisições que alteram dados (`POST`, `PUT`, `PATCH`, `DELETE`) exigem o **token CSRF**:

1. `GET /api/auth/csrf` grava o cookie `XSRF-TOKEN`;
2. o front envia o valor desse cookie no cabeçalho `X-XSRF-TOKEN`;
3. as requisições usam `credentials: "include"` (fetch) ou `withCredentials: true` (Axios).

O Axios faz o passo 2 sozinho (`withXSRFToken: true`). No Postman ou Insomnia, chame `/api/auth/csrf` primeiro e copie o valor do cookie para o cabeçalho.

Os dados de cada empresa ficam sob `/api/empresas/{empresaId}/...`. A cada requisição o servidor confere se o usuário tem vínculo ativo com a empresa e se o perfil dele permite a operação (RN23). Sem vínculo, a resposta é `403`. A matriz de [permissões](../docs/04-permissoes_de_usuario.md) está no enum `security.Permissao`, e `GET /api/empresas/{empresaId}/permissoes` devolve a lista do usuário logado para o front montar o menu (RN24).

Os erros seguem o formato `application/problem+json`, com a mensagem em `detail`. Erros de validação trazem também `campos`, com a mensagem de cada campo inválido.

## Endpoints

Todas as rotas começam com `/api`. Exigem login, exceto as marcadas como públicas.

| Método | Rota | Descrição |
|---|---|---|
| GET | `/auth/csrf` | Gera o token CSRF (público) |
| POST | `/auth/cadastro` | Cria uma conta e envia o e-mail de boas-vindas (público) |
| POST | `/auth/login` | Inicia a sessão (público) |
| POST | `/auth/logout` | Encerra a sessão |
| GET | `/auth/me` | Usuário logado |
| GET | `/conta/dados` | Dados pessoais mantidos sobre o usuário (LGPD) |
| POST | `/conta/exclusao` | Exclui a conta, confirmando a senha (LGPD) |
| GET, POST | `/empresas` | Empresas do usuário; cadastra uma empresa (quem cadastra vira proprietário) |
| GET | `/empresas/{e}` | Dados da empresa e perfil do usuário nela |
| GET | `/empresas/{e}/permissoes` | Permissões do usuário na empresa |
| GET | `/empresas/{e}/usuarios` | Usuários vinculados |
| PATCH | `/empresas/{e}/usuarios/{vinculoId}` | Altera perfil, ativa/desativa acesso ou recebimento de alertas |
| GET, POST | `/empresas/{e}/convites` | Lista e envia convites |
| DELETE | `/empresas/{e}/convites/{conviteId}` | Cancela um convite pendente |
| GET | `/convites/{token}` | Dados do convite a partir do link do e-mail (público) |
| POST | `/convites/{token}/aceite` | Aceita o convite com a conta logada |
| GET, POST | `/empresas/{e}/categorias` | Lista e cria categorias |
| DELETE | `/empresas/{e}/categorias/{categoriaId}` | Exclui categoria da empresa que não esteja em uso |
| GET, POST | `/empresas/{e}/obrigacoes` | Lista (filtros `inicio`, `fim`, `categoriaId`, `status`) e cadastra, com recorrência opcional |
| GET, PUT, DELETE | `/empresas/{e}/obrigacoes/{obrigacaoId}` | Detalha, edita e exclui (`escopo=SOMENTE_ESTA` ou `ESTA_E_FUTURAS`) |
| GET, POST | `/empresas/{e}/obrigacoes/{obrigacaoId}/anexos` | Lista e envia anexos (`multipart/form-data`, campo `arquivo`) |
| GET | `/empresas/{e}/anexos/{anexoId}/arquivo` | Baixa um anexo |
| DELETE | `/empresas/{e}/anexos/{anexoId}` | Remove um anexo |
| POST | `/empresas/{e}/obrigacoes/{obrigacaoId}/recorrencia` | Torna recorrente uma obrigação existente |
| GET | `/empresas/{e}/recorrencias` | Lista recorrências |
| PUT | `/empresas/{e}/recorrencias/{recorrenciaId}` | Altera a recorrência (afeta só ocorrências futuras não pagas) |
| POST | `/empresas/{e}/obrigacoes/{obrigacaoId}/pagamento` | Registra o pagamento |
| GET | `/empresas/{e}/pagamentos` | Histórico (filtros `inicio` e `fim` pela data do pagamento) |
| POST | `/empresas/{e}/pagamentos/{pagamentoId}/cancelamento` | Cancela um pagamento registrado por engano |
| GET, POST | `/empresas/{e}/documentos` | Lista (filtros `categoriaId`, `status`) e cadastra (`multipart/form-data`) |
| GET, PUT, DELETE | `/empresas/{e}/documentos/{documentoId}` | Detalha, edita e exclui |
| GET, PUT | `/empresas/{e}/documentos/{documentoId}/arquivo` | Baixa ou substitui o arquivo |
| GET, PUT | `/empresas/{e}/alertas` | Consulta e configura os prazos de alerta |
| GET | `/empresas/{e}/painel?mes=AAAA-MM` | Indicadores do mês (sem `mes`, o mês atual) |
| GET | `/empresas/{e}/relatorios/obrigacoes` | PDF de obrigações e pagamentos, com os mesmos filtros da listagem |

## Rotinas agendadas

| Horário (Brasília) | Rotina |
|---|---|
| 0h | Marca obrigações e documentos vencidos, expira convites e gera as próximas ocorrências recorrentes |
| 8h | Envia os alertas de vencimento (7, 3 e 1 dia antes e no dia), sem repetir envios |

As duas também rodam ao iniciar a aplicação (os alertas, só depois das 8h), para recuperar o que não foi processado enquanto ela estava parada. São idempotentes: rodar de novo não duplica nada.

Obrigações cadastradas ou alteradas **depois das 8h** (inclusive por recorrência ou cancelamento de pagamento) não esperam o dia seguinte: se caírem num prazo de hoje (por exemplo, vencem hoje ou daqui a 7 dias), o alerta é enviado logo após a gravação, em segundo plano. Sem isso, esses alertas se perderiam.

> **E-mails no Docker:** por padrão, tudo vai para o Mailpit (http://localhost:8025) e **não chega** às caixas de e-mail reais. Para enviar de verdade, configure `SMTP_HOST`, `SMTP_PORT`, `SMTP_AUTH`, `SMTP_STARTTLS`, `MAIL_USERNAME`, `MAIL_PASSWORD` e `MAIL_FROM` no `.env` (veja o `.env.example`).

## LGPD (RNF14)

- O consentimento com os termos é registrado com data no cadastro.
- `GET /api/conta/dados` mostra ao titular os dados pessoais mantidos: nome, e-mail, datas, empresas, convites recebidos e quantidade de pagamentos registrados.
- `POST /api/conta/exclusao` **anonimiza** a conta em vez de apagá-la. Nome e e-mail são substituídos, a senha é descartada, os acessos são desativados e os convites pendentes, cancelados. Pagamentos e edições continuam no histórico (RN21, RN73), mas deixam de identificar a pessoa. O único proprietário de uma empresa precisa definir outro antes de excluir a conta.
- A política de privacidade visível em todas as páginas fica a cargo do front-end.

## Produção (HTTPS, RNF13)

A aplicação deve ficar atrás de um proxy reverso com HTTPS (Nginx, Traefik ou o balanceador do provedor) que repasse os cabeçalhos `X-Forwarded-*`. Nesse ambiente, defina:

- `SESSION_COOKIE_SECURE=true`: o cookie de sessão só trafega por HTTPS;
- `EXIGIR_HTTPS=true`: acessos por HTTP são redirecionados para HTTPS;
- `CSRF_HABILITADO=true`: religa a proteção CSRF;
- `FRONTEND_URL` com o endereço público do front;
- um servidor SMTP real em `MAIL_*`.

A versão mínima do TLS (1.2 ou superior) é configurada no proxy.

## Modelo de domínio

As entidades seguem o [modelo de dados](../docs/06-modelo_de_dados.drawio), complementado pelos campos exigidos nas [regras de negócio](../docs/05-regras_de_negocio.md). Os comentários no código indicam a regra (RN) ou o requisito (RNF) de origem de cada campo ou consulta.

### Entidades (`model`)

| Entidade | Tabela | Descrição |
|---|---|---|
| `Usuario` | `usuario` | Conta de acesso, com senha em hash BCrypt, aceite dos termos e controle de bloqueio de login |
| `Empresa` | `empresa` | Empresa cadastrada, identificada por CNPJ único |
| `UsuarioEmpresa` | `usuario_empresa` | Vínculo entre usuário e empresa, com perfil, situação do acesso e recebimento de alertas |
| `Convite` | `convite` | Convite por e-mail para um usuário acessar a empresa |
| `Categoria` | `categoria` | Categoria de obrigações e documentos; sem empresa, é uma categoria padrão do sistema |
| `Recorrencia` | `recorrencia` | Modelo de obrigação recorrente, mensal ou anual |
| `Obrigacao` | `obrigacao` | Conta ou obrigação a pagar, com vencimento e situação |
| `AnexoObrigacao` | `anexo_obrigacao` | Arquivo anexado a uma obrigação (boleto, guia etc.) |
| `Documento` | `documento` | Documento empresarial com validade opcional |
| `Pagamento` | `pagamento` | Registro de pagamento de uma obrigação; cancelamentos ficam no histórico |
| `ConfiguracaoAlerta` | `configuracao_alerta` | Prazos de alerta (7, 3, 1 e 0 dias) ligados ou desligados por empresa |
| `AlertaEnviado` | `alerta_enviado` | Registro de alertas enviados, usado para evitar duplicidade |

Todas as chaves primárias são UUID armazenados como `VARCHAR(36)`, e os relacionamentos usam carregamento sob demanda (`FetchType.LAZY`).

### Enums (`model.enuns`)

| Enum | Valores |
|---|---|
| `Perfil` | `PROPRIETARIO`, `FINANCEIRO`, `CONTADOR` |
| `StatusConvite` | `PENDENTE`, `ACEITO`, `EXPIRADO`, `CANCELADO` |
| `StatusObrigacao` | `PENDENTE`, `PAGA`, `VENCIDA` |
| `StatusDocumento` | `ATIVO`, `VENCIDO`, `ARQUIVADO` |
| `Periodicidade` | `MENSAL`, `ANUAL` |

### Repositórios (`repository`)

Há um repositório por entidade, todos estendendo `JpaRepository`. Além do CRUD padrão, eles oferecem consultas para as regras de negócio, por exemplo:

- busca sempre filtrada pela empresa (`findByIdAndEmpresaId`), garantindo o isolamento dos dados;
- verificação de e-mail e CNPJ já cadastrados;
- contagem de proprietários ativos da empresa;
- marcação diária de obrigações vencidas, documentos vencidos e convites expirados;
- total a pagar no mês e soma dos valores por categoria para o painel;
- verificação de alerta já enviado para a mesma obrigação, usuário e prazo.

O `ObrigacaoRepository` também estende `JpaSpecificationExecutor`, para os filtros dinâmicos por período, categoria e situação.

## Pendências

- [x] Criar a migration inicial do Flyway ([V1__criar_tabelas.sql](src/main/resources/db/migration/V1__criar_tabelas.sql)), com as tabelas, as categorias padrão e o índice único parcial que garante no máximo um pagamento vigente por obrigação.
- [x] Configurar o datasource e o JPA no `application.properties`.
- [x] Ler as credenciais do banco a partir do `.env`, em vez de valores fixos.
- [x] Services e controllers com as regras de negócio e a matriz de permissões.
- [x] `docker-compose.yml` com banco, e-mail e back-end; `Dockerfile` do back-end.
- [x] Testes de integração com banco descartável e linter.
- [x] Atendimento ao titular dos dados (acesso e exclusão, RNF14).
- [ ] Serviço `frontend` no `docker-compose.yml`, quando a aplicação React existir.

## Migrations

O esquema do banco é criado e versionado pelo Flyway, que executa os scripts de `src/main/resources/db/migration` ao iniciar a aplicação. O Hibernate apenas valida as entidades contra o banco (`ddl-auto=validate`).

Uma migration já aplicada nunca deve ser editada: toda alteração no esquema entra em um novo arquivo, seguindo a sequência `V2__descricao.sql`, `V3__descricao.sql` etc.

## Observação sobre o Lombok

O Lombok está registrado como processador de anotações no `pom.xml`. Com isso ele também funciona em JDKs a partir da versão 23, que não processam anotações por padrão. Na IDE, instale a extensão ou o plugin do Lombok para que os métodos gerados sejam reconhecidos.
