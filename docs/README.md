# GUIA DA DOCUMENTAÇÃO

Este guia apresenta todos os documentos da pasta `docs/` do TudoEmDia: o que cada um contém, quando consultá-lo e como os documentos se relacionam. Também explica os diagramas UML de classes, de casos de uso e de componentes.

## Mapa dos documentos

| Nº | Documento | Tipo | Conteúdo | Quando consultar |
|---|---|---|---|---|
| 01 | [Escopo](01-escopo.md) | Texto | Visão geral, problema, solução, público-alvo, funcionalidades do MVP, o que fica fora do MVP e premissas. | Para entender o que é o produto e até onde vai a primeira versão. |
| 02 | [Requisitos funcionais](02-requisitos_funcionais.md) | Tabela | RF01 a RF27: o que o sistema deve fazer. | Para saber se uma funcionalidade foi prevista. |
| 03 | [Requisitos não funcionais](03-requisitos_nao_funcionais.md) | Tabela | RNF01 a RNF14: segurança, desempenho, usabilidade, LGPD e outras qualidades. | Para decisões técnicas e de qualidade. |
| 04 | [Permissões de usuário](04-permissoes_de_usuario.md) | Matriz | O que cada perfil (Proprietário, Financeiro, Contador) pode fazer. | Para saber quem pode executar cada ação. |
| 05 | [Regras de negócio](05-regras_de_negocio.md) | Lista | RN01 a RN94, agrupadas pelos RFs. Detalham validações, limites e comportamentos. | Para implementar ou testar uma funcionalidade em detalhe. |
| 06 | [Modelo de dados](06-modelo_de_dados.drawio) | Diagrama (draw.io) | Tabelas, colunas, chaves e relacionamentos do PostgreSQL. | Para trabalhar no banco de dados ou nas migrações Flyway. |
| 07 | [Arquitetura](07-arquitetura.md) | Texto | Tecnologias, padrão MVC, organização das pastas, segurança, serviços Docker e rotinas agendadas. | Para entender como o código está organizado. |
| 08 | [Critérios de aceitação](08-criterios_de_aceitacao.md) | Tabela | Critérios mensuráveis para cada RNF. | Para validar se um requisito não funcional foi atendido. |
| 09 | [Diagrama de classes](09-diagrama_de_classes.puml) | Diagrama UML (PlantUML) | Entidades do domínio, atributos, enumerações e associações. | Para entender o modelo orientado a objetos do back-end. |
| 10 | [Diagrama de casos de uso](10-diagrama_de_casos_de_uso.puml) | Diagrama UML (PlantUML) | Atores, casos de uso e relações com os RFs. | Para ver quem usa o sistema e para quê. |
| 11 | [Diagrama de componentes](11-diagrama_de_componentes.puml) | Diagrama UML (PlantUML) | Front-end, back-end, banco, e-mail e armazenamento, e como se comunicam. | Para entender a arquitetura física e lógica em execução. |

## Como os documentos se relacionam

```text
01 Escopo
 ├── 02 Requisitos funcionais (RF) ─────┬── 05 Regras de negócio (RN detalham os RF)
 │                                      ├── 04 Permissões (quem executa cada RF)
 │                                      └── 10 Casos de uso (RF vistos pelos atores)
 └── 03 Requisitos não funcionais (RNF) ─── 08 Critérios de aceitação (como medir os RNF)

07 Arquitetura ──┬── 11 Diagrama de componentes (a arquitetura em execução)
                 ├── 09 Diagrama de classes (o domínio em Java)
                 └── 06 Modelo de dados (o domínio no banco)
```

A rastreabilidade segue este caminho: **RF → RN → caso de uso → classes e componentes → código**. Cada caso de uso do diagrama 10 traz o número do RF que atende, e as notas do diagrama 09 citam as RNs aplicadas nas entidades. O código-fonte também cita as regras nos comentários, por exemplo `// RN69`.

## Como visualizar e editar os diagramas

Os diagramas 09, 10 e 11 são arquivos de texto no formato [PlantUML](https://plantuml.com). Por serem texto, podem ser versionados e revisados no Git como o código. Para visualizar:

- **VS Code:** instale a extensão *PlantUML* (jebbs.plantuml), abra o arquivo `.puml` e pressione `Alt+D`. A extensão também exporta PNG, SVG ou PDF.
- **Docker:** com o Docker em execução, gere imagens SVG de todos os diagramas a partir da raiz do projeto:

  ```bash
  docker run --rm -v "$(pwd)/docs:/data" plantuml/plantuml -tsvg /data
  ```

- **Astah:** use o plugin de PlantUML do Astah para importar o arquivo `.puml` como diagrama editável.

O diagrama 06 é editado no [draw.io](https://app.diagrams.net) ou na extensão *Draw.io Integration* do VS Code.

## 09 — Diagrama de classes

Representa as entidades JPA do pacote `model`, as enumerações de `model/enuns` e a enumeração `Permissao` do pacote `security`. Os getters e setters, gerados pelo Lombok, foram omitidos.

### Pacotes

| Pacote | Classes | Responsabilidade |
|---|---|---|
| Acesso | `Usuario`, `Empresa`, `UsuarioEmpresa`, `Convite` | Contas, empresas, vínculos com perfil e convites para a equipe. |
| Obrigações e pagamentos | `Categoria`, `Obrigacao`, `Recorrencia`, `AnexoObrigacao`, `Pagamento` | Contas a pagar, recorrência, anexos e registro de pagamentos. |
| Documentos | `Documento` | Central de documentos empresariais com validade e status. |
| Alertas | `ConfiguracaoAlerta`, `AlertaEnviado` | Prazos de alerta ativos por empresa e registro dos envios. |
| Enumerações | `Perfil`, `StatusConvite`, `StatusObrigacao`, `StatusDocumento`, `Periodicidade`, `Permissao` | Valores fixos do domínio. |

### Pontos principais

- **`UsuarioEmpresa` é a classe central do controle de acesso.** Um usuário pode participar de várias empresas, com um perfil diferente em cada uma. O vínculo pode ser desativado sem apagar o histórico (`ativo`) e define se o usuário recebe alertas (`recebeAlertas`).
- **Isolamento por empresa:** `Obrigacao`, `Recorrencia`, `Documento`, `Convite`, `ConfiguracaoAlerta` e `UsuarioEmpresa` pertencem a uma `Empresa`. Toda consulta é filtrada pela empresa do vínculo do usuário.
- **Categorias padrão:** quando `Categoria.empresa` é nula, a categoria é do sistema e aparece para todas as empresas (RN41).
- **Recorrência:** uma `Recorrencia` guarda o modelo (descrição, valor, categoria, periodicidade, início e fim) e gera várias ocorrências de `Obrigacao`. Cada recorrência tem no máximo uma obrigação por data de vencimento (RN64).
- **Pagamento como histórico:** um pagamento não é editado nem apagado (RN73). Um registro feito por engano é *cancelado*, e o sistema guarda quem cancelou e quando. Cada obrigação tem no máximo um pagamento não cancelado (RN69).
- **Composições** (losango preenchido): os anexos e os alertas enviados não existem sem a obrigação e são removidos com ela. As configurações de alerta pertencem à empresa, com até quatro prazos (7, 3, 1 e 0 dias).
- **Auditoria:** `Obrigacao` e `Documento` registram o último usuário que os editou (`usuarioAtualizacao`) e a data da edição.
- **`Permissao`** relaciona cada funcionalidade aos perfis autorizados. Ela implementa a matriz do documento 04 e acrescenta `GERENCIAR_CATEGORIAS` (RN42) e `CANCELAR_PAGAMENTOS` (RN70).

## 10 — Diagrama de casos de uso

Mostra quem interage com o sistema e o que cada ator pode fazer. Os casos de uso são agrupados por área funcional e trazem o número do RF correspondente.

### Atores

| Ator | Descrição |
|---|---|
| Visitante | Pessoa sem sessão. Cria conta, entra no sistema e abre o link de um convite. |
| Usuário autenticado | Pessoa com sessão, mesmo sem empresa. Cadastra empresas, aceita convites, consulta e exclui os próprios dados (LGPD). |
| Membro da empresa | Ator abstrato: qualquer usuário com vínculo ativo na empresa. Reúne as consultas comuns aos três perfis. |
| Contador | Membro somente de consulta. |
| Operador financeiro | Ator abstrato que reúne o que Financeiro e Proprietário têm em comum: cadastrar, editar e excluir obrigações, categorias, recorrências e pagamentos. |
| Financeiro | Operador financeiro sem acesso à gestão da equipe e dos documentos. |
| Proprietário | Quem cadastrou a empresa. Além do que o Operador financeiro faz, gerencia a equipe, os documentos e os alertas e cancela pagamentos. |
| Agendador | Ator de sistema: rotina diária do Spring (`RotinaDiariaService`). À meia-noite atualiza situações e gera ocorrências; às 8h envia alertas. |
| Servidor de e-mail | Ator de sistema secundário que entrega os convites e os alertas. |

As setas de generalização (triângulo vazado) indicam herança: um ator herda todos os casos de uso do ator mais geral. Assim, o Proprietário faz tudo o que o Operador financeiro e o Membro da empresa fazem. O resultado é o mesmo da matriz do documento 04.

### Relações entre casos de uso

- **«include»:** o caso incluído sempre acontece. *Aceitar convite* inclui *Consultar convite*, e *Gerenciar acesso e perfil* inclui *Consultar usuários vinculados*.
- **«extend»:** o caso estendido é opcional. Ao *Cadastrar obrigação*, o usuário pode *Anexar arquivo* e *Configurar recorrência*. Ao *Consultar documentos*, pode *Baixar arquivo*.

## 11 — Diagrama de componentes

Mostra as partes do sistema em execução, os contêineres do `docker-compose.yml` e as interfaces de comunicação.

### Nós e componentes

| Nó | Componentes | Papel |
|---|---|---|
| Navegador | SPA React (rotas, páginas, contextos, cliente da API) | Interface do usuário. As rotas protegidas verificam a sessão e as permissões do perfil antes de exibir uma página. |
| Contêiner `frontend` | Nginx e build estático | Entrega os arquivos do React e repassa as chamadas `/api` ao back-end. |
| Contêiner `backend` | Spring Security, controllers, controle de acesso, serviços, rotinas, alertas, relatórios PDF, e-mail, armazenamento, repositórios, Flyway, Swagger e Actuator | API REST com as regras de negócio. |
| Contêiner `postgres` | PostgreSQL | Persistência. O esquema é criado e versionado pelo Flyway (`db/migration`). |
| Contêiner `mailpit` / SMTP | Servidor SMTP | Em desenvolvimento, o Mailpit captura os e-mails (http://localhost:8025). Em produção, é usado um SMTP real. |
| Volume `uploads` | Arquivos | Anexos e documentos enviados. O caminho interno nunca é exposto ao usuário (RN53). |

### Fluxo de uma requisição

1. O usuário acessa a aplicação pelo navegador, e o Nginx entrega a SPA.
2. A SPA chama a API REST com `fetch`, enviando o cookie de sessão e o token CSRF.
3. O Spring Security valida a sessão, o CSRF e o CORS e então repassa a requisição ao controller.
4. O controller chama o serviço, que usa o **Controle de acesso** para exigir o vínculo com a empresa e a `Permissao` necessária.
5. O serviço aplica as regras de negócio e usa os repositórios (JPA/JDBC), o armazenamento de arquivos e o serviço de e-mail. Os e-mails só são enviados depois que a transação é confirmada.
6. Os erros são convertidos em respostas padronizadas pelo `TratadorExcecoes`.

As **rotinas agendadas** não passam pela API: são executadas no próprio back-end e usam os mesmos serviços.

## Manutenção da documentação

- Ao criar ou alterar uma entidade em `model/`, atualize o diagrama 09 e o modelo de dados 06.
- Ao criar um endpoint ou uma funcionalidade, verifique se o RF existe (02), se as permissões estão na matriz (04) e se o caso de uso aparece no diagrama 10.
- Ao adicionar um serviço de infraestrutura no `docker-compose.yml`, atualize o diagrama 11 e a seção "Serviços Docker" do documento 07.
- Novos documentos devem seguir a numeração sequencial (`12-...`) e ser incluídos no mapa deste guia.
