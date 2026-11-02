# CRITÉRIOS DE ACEITAÇÃO

| RNF | Categoria (Sommerville) | Critérios de aceitação |
| ----- | ----- | ----- |
| RNF 01 Segurança de acesso | Produto › Segurança | 100% das rotas com dados da empresa exigem login; Usuário sem vínculo recebe erro 403; As 18 funcionalidades da matriz se comportam como previsto nos 3 perfis. |
| RNF02 Proteção de credenciais | Produto › Segurança | Senhas guardadas com bcrypt (custo 10 ou mais); Nenhuma senha em texto puro no banco ou nos logs. |
| RNF03 Privacidade dos dados | Produto › Segurança | Um usuário da empresa A não acessa nenhum dado da empresa B; Dados pessoais nunca vão na URL. |
| RNF04 Desempenho | Produto › Eficiência (desempenho) | 95% das consultas, cadastros e edições respondem em até 3 s, com 50 usuários simultâneos e 10 mil obrigações na base. |
| RNF05 Disponibilidade | Produto › Confiança (disponibilidade) | Disponibilidade mensal de 99% ou mais (até cerca de 7h fora do ar por mês), sem contar manutenções avisadas com 48 h de antecedência. |
| RNF06 Usabilidade | Produto › Usabilidade | Em teste com 5 pessoas sem formação técnica, pelo menos 4 cadastram uma obrigação sem ajuda em até 3 minutos; nota média no questionário SUS de 70 ou mais. |
| RNF07 Responsividade | Produto › Usabilidade | Sem rolagem horizontal e com todas as funções acessíveis nas larguras de 360, 768 e 1366 px. |
| RNF08 Compatibilidade | Produto › Portabilidade | O roteiro de testes passa 100% nas duas últimas versões estáveis do Chrome, Firefox e Edge. |
| RNF09 Integridade dos dados | Produto › Confiança | O banco rejeita registros inválidos (chaves estrangeiras, campos obrigatórios, valor maior que zero); Registrar um pagamento e mudar a situação ocorre numa única transação; 0 registros órfãos ou inconsistentes. |
| RNF10 Confiabilidade das notificações | Produto › Confiança | 0 alertas duplicados; 99% dos alertas enviados até 1 h após o horário programado; Falhas de envio têm até 3 novas tentativas, registradas em log. |
| RNF11 Manutenibilidade | Organizacional › Desenvolvimento | Código versionado em Git, separado em camadas; README com instruções de instalação; Testes unitários cobrem pelo menos 60% das regras de negócio; Linter sem erros. |
| RNF12 Formato dos relatórios | Produto › Usabilidade | O PDF abre sem erro em leitores comuns, em formato A4, com fonte de 10 pt ou maior, cabeçalho (empresa, período, data) e nenhuma tabela cortada entre páginas. |
| RNF13 Proteção na comunicação | Produto › Segurança | Em produção, 100% das requisições usam HTTPS com TLS 1.2 ou superior; acessos por HTTP são redirecionados para HTTPS. |
| RNF14 Conformidade com a legislação | Externo › Legislativo (LGPD) | O consentimento é registrado com data no cadastro; A política de privacidade fica acessível em todas as páginas; Só são coletados dados necessários;  O titular pode pedir acesso ou exclusão dos dados, com resposta em até 15 dias. |
