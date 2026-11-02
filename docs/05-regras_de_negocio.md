# REGRAS DE NEGÓCIO

## **Usuários, empresa e acesso (RF01–RF06)**

| RF | Nome | Regra de negócio |
| ----- | ----- | ----- |
| RF01 Cadastrar usuário | RN01 | O e-mail é único no sistema; Não pode haver duas contas com o mesmo e-mail. |
|  | RN02 | Nome, e-mail e senha são obrigatórios. |
|  | RN03 | A senha tem no mínimo 8 caracteres, com pelo menos uma letra e um número. |
|  | RN04 | O cadastro só é concluído com o aceite dos termos de uso e da política de privacidade. |
| RF02 Autenticar usuário | RN05 | O acesso é feito com e-mail e senha cadastrados. |
|  | RN06 | Após 5 tentativas erradas seguidas, o login fica bloqueado por 15 minutos. |
|  | RN07 | A sessão expira após 30 minutos sem atividade. |
|  | RN08 | A mensagem de erro não informa se o problema está no e-mail ou na senha. |
| RF03 Cadastrar empresa | RN09 | O CNPJ é obrigatório, deve ser válido e não pode estar cadastrado em outra empresa. |
|  | RN10 | Quem cadastra a empresa passa a ser o seu proprietário. |
|  | RN11 | Um usuário pode estar vinculado a várias empresas, com um perfil diferente em cada uma. |
|  | RN12 | Toda empresa tem pelo menos um proprietário ativo. |
| RF04 Convidar usuários | RN13 | Só o proprietário pode enviar convites. |
|  | RN14 | O convite informa o e-mail do convidado e o perfil: financeiro ou contador. |
|  | RN15 | O convite expira em 7 dias se não for aceito. |
|  | RN16 | Quem ainda não tem conta precisa se cadastrar (RF01) com o mesmo e-mail do convite. |
|  | RN17 | Não é possível convidar um e-mail que já está vinculado à empresa. |
| RF05 Gerenciamento de acesso | RN18 | Só o proprietário consulta a lista de usuários vinculados e gerencia seus acessos. |
|  | RN19 | O proprietário pode alterar o perfil de um usuário ou desativar seu acesso. |
|  | RN20 | O único proprietário da empresa não pode remover o próprio acesso. |
|  | RN21 | Desativar um usuário não apaga os registros que ele fez. O histórico é preservado. |
| RF06 Controle de permissões | RN22 | Cada perfil só executa as funcionalidades marcadas como "Sim" na matriz de permissões. |
|  | RN23 | A permissão é verificada no servidor a cada requisição, e não apenas escondendo botões na tela. |
|  | RN24 | Funcionalidades não permitidas ao perfil não aparecem no menu. |

## 

## **Obrigações (RF07–RF12)**

| RF | RN | Regra de negócio |
| ----- | ----- | ----- |
| RF07 Cadastro de obrigações | RN25 | Só proprietário e financeiro cadastram obrigações. |
|  | RN26 | Descrição, valor, categoria e data de vencimento são obrigatórios. |
|  | RN27 | O valor é em reais, maior que zero e com duas casas decimais. |
|  | RN28 | Anexos aceitos: PDF, JPG ou PNG, com até 10 MB cada. |
|  | RN29 | Pode-se cadastrar uma obrigação com vencimento passado. Ela já nasce com situação vencida. |
| RF08 Consulta de obrigações | RN30 | Os três perfis consultam obrigações. |
|  | RN31 | O usuário só vê as obrigações da empresa selecionada, entre as que ele tem vínculo. |
|  | RN32 | A lista pode ser filtrada por período, categoria e situação, e vem ordenada pelo vencimento mais próximo. |
| RF09 Edição de obrigações | RN33 | Só proprietário e financeiro editam obrigações. |
|  | RN34 | Uma obrigação paga não pode ter valor nem vencimento alterados enquanto o pagamento estiver registrado. |
|  | RN35 | Toda edição registra o usuário que alterou e a data da alteração. |
| RF10 Exclusão de obrigações | RN36 | Só proprietário e financeiro excluem obrigações. |
|  | RN37 | A exclusão exige confirmação do usuário. |
|  | RN38 | Uma obrigação com pagamento registrado não pode ser excluída, para preservar o histórico. |
|  | RN39 | Ao excluir uma obrigação recorrente, o usuário escolhe entre excluir só essa ocorrência ou ela e as futuras. |
| RF11 Classificação de obrigações | RN40 | Toda obrigação pertence a exatamente uma categoria. |
|  | RN41 | O sistema já vem com as categorias Impostos, Fornecedores, Folha de pagamento, Aluguel e consumo e Outros. |
|  | RN42 | Proprietário e financeiro podem criar categorias; Uma categoria em uso não pode ser excluída. |
| RF12 Controle de situação | RN43 | Pendente: sem pagamento registrado e com vencimento hoje ou no futuro. |
|  | RN44 | Vencida: sem pagamento registrado e com vencimento anterior a hoje. |
|  | RN45 | Paga: com pagamento registrado (RF18), independentemente da data de vencimento. |
|  | RN46 | A situação é recalculada todos os dias à 0h (horário de Brasília) e sempre que um pagamento é registrado ou cancelado. |
|  | RN47 | Uma obrigação paga depois do vencimento fica como Paga, com a indicação "paga com atraso". |

## 

## **Documentos (RF13–RF15)**

| RF | RN | Regra de negócio |
| ----- | ----- | ----- |
| RF13 Cadastro de documentos | RN48 | Só o proprietário cadastra documentos. |
|  | RN49 | Descrição, categoria e arquivo são obrigatórios. |
|  | RN50 | Arquivos aceitos: PDF, JPG ou PNG, com até 10 MB. |
|  | RN51 | A data de validade é opcional. Quando informada, o documento aparece como Válido ou Vencido. |
| RF14 Consulta de documentos | RN52 | Os três perfis consultam e baixam documentos da empresa selecionada. |
|  | RN53 | O arquivo só é aberto por usuário autenticado e vinculado à empresa; não existe link público. |
| RF15 Edição e exclusão de documentos | RN54 | Só o proprietário edita ou exclui documentos. |
|  | RN55 | A exclusão exige confirmação do usuário. |
|  | RN56 | Toda edição registra o usuário que alterou e a data da alteração. |

## 

## **Recorrência e pagamentos (RF16–RF19)**

| RF | Nome | Regra de negócio |
| ----- | ----- | ----- |
| RF16 Configuração de recorrência | RN57 | Só proprietário e financeiro configuram recorrência. |
|  | RN58 | A periodicidade é mensal ou anual. |
|  | RN59 | Na recorrência mensal com vencimento nos dias 29, 30 ou 31, nos meses sem esse dia a ocorrência vence no último dia do mês. |
|  | RN60 | A recorrência pode ter uma data final; sem ela, continua por tempo indeterminado. |
|  | RN61 | Alterar a recorrência afeta só as ocorrências futuras, nunca as já pagas. |
| RF17 Geração de obrigações recorrentes | RN62 | Uma rotina diária garante que cada recorrência tenha sempre a próxima ocorrência futura gerada. |
|  | RN63 | A nova ocorrência copia descrição, valor e categoria da recorrência e nasce como Pendente. |
|  | RN64 | Não se gera mais de uma ocorrência da mesma recorrência para a mesma data. |
|  | RN65 | A geração para quando a data final da recorrência é atingida. |
| RF18 Registro de pagamentos | RN66 | Só proprietário e financeiro registram pagamentos. |
|  | RN67 | A data de pagamento é obrigatória e não pode ser futura. |
|  | RN68 | O valor pago vem preenchido com o valor da obrigação e pode ser alterado (juros ou multa). |
|  | RN69 | Cada obrigação tem no máximo um pagamento registrado; não há pagamento parcial no MVP. |
|  | RN70 | O proprietário pode cancelar um registro de pagamento feito por engano; o cancelamento fica no histórico. |
| RF19 Histórico de pagamentos | RN71 | Os três perfis consultam o histórico. |
|  | RN72 | Cada registro mostra a obrigação, o valor pago, a data do pagamento e o usuário que registrou. |
|  | RN73 | Registros do histórico não podem ser editados nem apagados. |

## 

## **Alertas (RF20–RF21)**

| RF | Nome | Regra de negócio |
| ----- | ----- | ----- |
| RF20 Configuração de alertas | RN74 | Só o proprietário configura os alertas da empresa. |
|  | RN75 | O proprietário define quais usuários recebem alertas; por padrão, todos os vinculados recebem. |
|  | RN76 | Cada prazo (7, 3 e 1 dia antes, e no dia) pode ser ligado ou desligado, mas pelo menos um fica ativo. |
| RF21 Envio de alertas de vencimento | RN77 | Só obrigações Pendentes geram alerta; obrigações pagas não geram. |
|  | RN78 | Cada alerta é enviado uma única vez por obrigação, prazo e destinatário. |
|  | RN79 | Os alertas do dia são enviados às 8h (horário de Brasília). |
|  | RN80 | O e-mail traz a descrição, o valor, o vencimento e um link para a obrigação. |
|  | RN81 | Usuários com acesso desativado não recebem alertas. |

## 

## **Painel e relatórios (RF22–RF27)**

| RF | Nome | Regra de negócio |
| ----- | ----- | ----- |
| RF22 Visualização do painel | RN82 | Os três perfis acessam o painel. |
|  | RN83 | O painel mostra só dados da empresa selecionada e abre no mês atual. |
| RF23 Total a pagar | RN84 | Total a pagar \= soma das obrigações Pendentes e Vencidas com vencimento no mês selecionado. |
|  | RN85 | O valor já pago no mês aparece separado do total a pagar. |
| RF24 Obrigações vencidas | RN86 | A lista mostra todas as obrigações Vencidas, da mais antiga para a mais recente, com os dias de atraso. |
| RF25 Gráficos por categoria | RN87 | O gráfico soma os valores das obrigações do mês selecionado por categoria. |
|  | RN88 | Categorias sem obrigações no mês não aparecem no gráfico. |
| RF26 Indicador de obrigações em dia | RN89 | Indicador \= obrigações pagas até o vencimento ÷ obrigações com vencimento até hoje no mês × 100\. |
|  | RN90 | Faixas do indicador: 90% ou mais é verde, de 70% a 89% é amarelo e abaixo de 70% é vermelho. |
| RF27 Exportação de relatórios | RN91 | Os três perfis exportam relatórios. |
|  | RN92 | O relatório aceita os mesmos filtros da consulta: período, categoria e situação. |
|  | RN93 | O relatório traz a empresa, o período, a data de geração e o usuário que gerou. |
|  | RN94 | Só entram dados da empresa selecionada. |

