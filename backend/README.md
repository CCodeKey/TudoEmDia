# TudoEmDia — Back-end

API REST do TudoEmDia, desenvolvida em Java com Spring Boot, seguindo o padrão MVC descrito em [Arquitetura](../docs/07-arquitetura.md).

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem |
| Spring Boot 4 (Web, Validation, Data JPA, Mail, Actuator) | Base da API |
| PostgreSQL | Banco de dados |
| Flyway | Versionamento do esquema do banco |
| Lombok | Geração de getters, setters e construtores |
| PDFBox | Exportação de relatórios em PDF |
| springdoc-openapi | Documentação da API (Swagger UI) |

## Pré-requisitos

- JDK 21 ou superior;
- PostgreSQL acessível (local ou via Docker);
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

## Executando

```bash
# Linux / macOS / Git Bash
./mvnw spring-boot:run

# Windows (PowerShell / CMD)
mvnw.cmd spring-boot:run
```

Outros comandos úteis:

```bash
./mvnw compile   # compila o projeto
./mvnw test      # executa os testes
./mvnw package   # gera o .jar em target/
```

## Estrutura de pacotes

```text
src/main/java/ted/com/tudoemdia/
├── model/          # Entidades JPA
│   └── enuns/      # Enums do domínio
└── repository/     # Interfaces Spring Data JPA
```

Os próximos pacotes previstos na arquitetura são `controller`, `service`, `dto`, `config`, `security` e `exception`.

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
- [ ] Configurar o serviço do banco no `docker-compose.yml`.

## Migrations

O esquema do banco é criado e versionado pelo Flyway, que executa os scripts de `src/main/resources/db/migration` ao iniciar a aplicação. O Hibernate apenas valida as entidades contra o banco (`ddl-auto=validate`).

Uma migration já aplicada nunca deve ser editada: toda alteração no esquema entra em um novo arquivo, seguindo a sequência `V2__descricao.sql`, `V3__descricao.sql` etc.

## Observação sobre o Lombok

O Lombok está registrado como processador de anotações no `pom.xml`. Com isso ele também funciona em JDKs a partir da versão 23, que não processam anotações por padrão. Na IDE, instale a extensão ou o plugin do Lombok para que os métodos gerados sejam reconhecidos.
