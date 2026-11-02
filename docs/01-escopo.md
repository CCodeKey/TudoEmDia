# ESCOPO

## Visão geral

O TudoEmDia é uma plataforma web para pequenas empresas e MEIs centralizarem obrigações financeiras, documentos empresariais e prazos importantes. O sistema permite acompanhar contas a pagar, registrar pagamentos, armazenar anexos e receber alertas por e-mail antes dos vencimentos.

O MVP será desenvolvido com:

- **Back-end:** Java com Spring;
- **Front-end:** React, Vite e JavaScript;
- **Arquitetura:** MVC no back-end e API REST para comunicação com o front-end;
- **Execução:** Docker e Docker Compose;
- **Persistência:** PostgreSQL.

## Problema

Pequenos empresários frequentemente controlam boletos, impostos, contratos, alvarás e certidões em papéis, planilhas, e-mails ou na memória. Essa dispersão dificulta:

- acompanhar obrigações próximas do vencimento;
- evitar multas, juros e atrasos;
- localizar documentos empresariais;
- saber quem realizou um pagamento;
- compartilhar responsabilidades com a equipe e o contador.

## Solução

O sistema oferecerá um ambiente único para cada empresa, com:

1. cadastro e acompanhamento de obrigações a pagar;
2. categorias para organização das obrigações;
3. recorrência mensal ou anual;
4. anexos associados às obrigações e documentos;
5. central de documentos empresariais com validade e status;
6. registro de pagamentos e histórico do usuário responsável;
7. alertas de vencimento por e-mail;
8. painel com indicadores financeiros das obrigações;
9. controle de acesso por empresa e perfil;
10. exportação de informações em PDF.

## Público-alvo

O produto é destinado a pequenas empresas e MEIs que não possuem uma estrutura financeira complexa.

| Perfil | Usuário | Necessidade |
|---|---|---|
| Proprietário | Dono ou sócio da empresa | Acompanhar prazos, valores e situação financeira |
| Financeiro | Responsável administrativo ou financeiro | Cadastrar obrigações e registrar pagamentos |
| Contador | Profissional responsável pela contabilidade | Consultar obrigações, documentos e histórico |

## Funcionalidades do MVP

### Usuários, empresas e acessos

- Cadastro e autenticação de usuários.
- Cadastro de empresas pelo usuário autenticado.
- Vínculo de usuários a empresas.
- Convite de usuários por e-mail.
- Perfis: proprietário, financeiro e contador.
- Autorização conforme a matriz de permissões.
- Isolamento dos dados entre empresas.

### Obrigações

- Cadastro, consulta, edição e exclusão de obrigações.
- Descrição, valor, categoria e data de vencimento.
- Situação da obrigação: pendente, paga ou vencida.
- Inclusão de anexos, como boletos e guias.
- Configuração de recorrência mensal ou anual.
- Geração de novas ocorrências recorrentes.

### Pagamentos

- Registro da data de pagamento.
- Associação do pagamento à obrigação.
- Identificação do usuário que registrou o pagamento.
- Consulta do histórico de pagamentos.

### Documentos

- Cadastro, consulta, edição e exclusão de documentos empresariais.
- Descrição, categoria, arquivo e caminho de armazenamento.
- Data de validade e status do documento.
- Controle de acesso conforme a empresa e o perfil do usuário.

### Alertas

- Configuração dos dias de antecedência por empresa.
- Alertas por e-mail 7, 3 e 1 dia antes do vencimento e no dia do vencimento.
- Registro dos alertas enviados.
- Prevenção de envio duplicado para o mesmo destinatário, obrigação e prazo.
- Processamento por rotina agendada no back-end Spring.

### Painel e relatórios

- Total de obrigações a pagar no mês selecionado.
- Lista de obrigações vencidas e não pagas.
- Distribuição dos valores por categoria.
- Indicador de obrigações em dia.
- Exportação de obrigações e pagamentos em PDF.

## Fora do MVP

Os seguintes itens não fazem parte da primeira versão:

- alertas por WhatsApp;
- leitura automática de boletos;
- integração com bancos ou instituições financeiras;
- emissão de guias de impostos;
- contas a receber;
- fluxo de caixa projetado com entradas financeiras;
- aplicativo móvel nativo;
- painel multicliente para escritórios de contabilidade;
- cobrança e assinatura por faixa de uso.

Esses itens poderão ser avaliados em versões futuras, após a validação do MVP.

## Premissas e limites

- O sistema controla obrigações e pagamentos informados pelos usuários; não realiza pagamentos.
- O envio de e-mails depende de um serviço de e-mail configurado no ambiente.
- Os arquivos serão armazenados conforme a estratégia definida pelo ambiente de execução, sem expor caminhos diretamente a usuários não autorizados.
- As regras de negócio detalhadas e os critérios de aceitação devem complementar este documento.

## Requisitos não funcionais considerados

- Senhas armazenadas somente com hash seguro.
- Autenticação e autorização por perfil.
- Isolamento dos dados por empresa.
- Comunicação HTTPS em produção.
- Interface responsiva para computadores, tablets e celulares.
- Compatibilidade com versões recentes de Chrome, Firefox e Edge.
- Validação de dados no front-end e no back-end.
- Integridade garantida por transações, relacionamentos e restrições do banco.
- Execução padronizada dos serviços por Docker.
