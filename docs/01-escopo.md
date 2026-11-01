# ESCOPO

### Visão geral

O projeto é uma plataforma web que centraliza contas, documentos e obrigações de pequenas empresas e avisa o dono antes de cada vencimento. O controle de prazos se junta a um painel consolidado de saúde financeira, no estilo do Investidor 10\.

### Problema principal

Pequenos empresários perdem prazos porque contas, impostos, certidões e contratos estão espalhados em e-mails, papéis, planilhas e na memória de quem toca o negócio.

Multas e juros por atraso em boletos, DAS, FGTS e outras guias.

Documentos vencidos, como alvarás, certidões e contratos, que podem travar licitações, crédito ou a operação.

O dono não sabe quanto vai pagar e receber nas próximas semanas.

Tudo depende de uma pessoa: se ela sai ou esquece, ninguém mais sabe o que está pendente.

ERPs são caros e complexos para esse porte, e planilhas não avisam ninguém.

### Solução proposta

Um painel único onde a empresa cadastra o que precisa pagar, receber e manter em dia, e recebe alertas antes de cada vencimento. Ele tem seis módulos.

1\. Cadastro de contas e obrigações, com descrição, valor, categoria, vencimento, recorrência (mensal ou anual) e anexo do boleto ou documento.

2\. Central de documentos para alvarás, certidões, contratos e licenças, com data de validade e status.

3\. Alertas 7, 3 e 1 dia antes do vencimento e no próprio dia, por e-mail (WhatsApp depois do MVP).

4\. Dashboard de saúde financeira, com total a pagar no mês, fluxo de caixa projetado, gráficos por categoria e um indicador de obrigações em dia.

5\. Perfis de acesso para dono, financeiro e contador, cada um com suas permissões. 6\. Histórico de quem pagou e quando, com exportação em PDF ou planilha.

### Público-alvo

O foco são pequenas empresas e MEIs que não têm um setor financeiro estruturado e sofrem com multas por esquecimento.

| Perfil     | Quem é                                                                                     | Por que usaria                                                                |
| ---------- | :----------------------------------------------------------------------------------------- | :---------------------------------------------------------------------------- |
| Principal  | Dono ou sócio de pequena empresa (padaria, clínica, oficina, loja) com 1 a 20 funcionários | Quer evitar multas e saber quanto vai pagar no mês                            |
| Secundário | Responsável financeiro ou administrativo da empresa                                        | Precisa de um lugar único para registrar e acompanhar pagamentos              |
| Parceiro   | Contador que atende várias pequenas empresas                                               | Ganha visibilidade das obrigações dos clientes e reduz cobranças por WhatsApp |

Exemplo de persona: Marcos, 42 anos, dono de uma padaria com 8 funcionários. Ele controla boletos num caderno, já pagou multa de DAS atrasado e não sabe quando vence o alvará sanitário.

### Diferenciais

A diferença está em juntar prazos e visão financeira numa ferramenta pensada para quem não tem equipe de finanças.

| Alternativa                      | Limitação                                   | Como o projeto resolve                     |
| :------------------------------- | ------------------------------------------- | :----------------------------------------- |
| Planilhas                        | Não avisam ninguém e dependem de disciplina | Alertas automáticos e recorrência          |
| ERPs completos                   | Caros e complexos para o porte              | Interface enxuta, focada em vencimentos    |
| Apps de finanças pessoais        | Não tratam documentos nem perfis de equipe  | Central de documentos e multiusuário       |
| Controle manual (papel, memória) | Depende de uma só pessoa                    | Histórico compartilhado e perfis de acesso |

Dashboard estilo Investidor 10, com indicadores e gráficos consolidados numa tela em vez de listas soltas.

Contas, impostos, certidões e contratos no mesmo lugar.

Alertas por WhatsApp, previstos para depois do MVP.

Um mesmo contador acompanhando vários clientes, também previsto para depois do MVP.

Assinatura mensal por faixa de uso, sem taxa de implantação.

### Escopo do MVP

A primeira versão permite cadastrar contas, acompanhá-las e receber o aviso, sem integrações externas complexas.

#### O que entra

Cadastro e login de usuários, com perfis (dono, financeiro, contador) CRUD de contas e documentos, com categoria, valor, vencimento e anexo Recorrência de contas (mensal e anual)

Alertas por e-mail antes do vencimento, gerados por rotina agendada Dashboard com total a pagar no mês, itens vencidos e gráfico por categoria Histórico de pagamentos e exportação em PDF ou planilha

#### O que fica para as próximas versões

Alertas por WhatsApp

Leitura automática de boletos e integração com bancos

Emissão de guias de imposto

Aplicativo móvel nativo

Painel multicliente para escritórios de contabilidade

Requisitos não funcionais: senhas protegidas por hash, acesso restrito por perfil, dados isolados por empresa e interface responsiva.
