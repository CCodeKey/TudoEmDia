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

As credenciais do banco são lidas de variáveis de ambiente, sem ficar gravadas no código. Use o [.env.example](../.env.example) como referência:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tudoemdia
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=senha
```

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

- [ ] Criar as migrations do Flyway em `src/main/resources/db/migration`, incluindo:
  - as categorias padrão (Impostos, Fornecedores, Folha de pagamento, Aluguel e consumo, Outros);
  - um índice único parcial em `pagamento(id_obrigacao) WHERE cancelado = false`, para garantir no máximo um pagamento vigente por obrigação.
- [ ] Configurar o datasource no `application.properties` e o serviço do banco no `docker-compose.yml`.

## Observação sobre o Lombok

O Lombok está registrado como processador de anotações no `pom.xml`. Com isso ele também funciona em JDKs a partir da versão 23, que não processam anotações por padrão. Na IDE, instale a extensão ou o plugin do Lombok para que os métodos gerados sejam reconhecidos.
