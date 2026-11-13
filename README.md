# TudoEmDia

Plataforma web para pequenas empresas e MEIs centralizarem obrigações financeiras, documentos empresariais e prazos importantes.

## Autores

<table> 
    <tr>
        <td align="center"> 
            <a href="https://github.com/clarasoaresdev"> 
                <img src="https://avatars.githubusercontent.com/u/137517398?v=4" width="100px;" alt="Clara S."/><br> 
                <sub>
                    <b>Clara Soares</b> 
                </sub> 
            </a> 
        </td> 
        <td align="center"> 
            <a href="https://github.com/CCodekey"> 
                <img src="https://avatars.githubusercontent.com/u/105808889?v=4" width="100px;" alt="Gabriel T."/><br> 
                <sub>
                    <b>Gabriel Tertuliano</b> 
                </sub> 
            </a> 
        </td>
        <td align="center"> 
            <a href="https://github.com/hellenilda/"> 
                <img src="https://avatars.githubusercontent.com/u/109177631?v=4" width="100px;" alt="Hellen L."/><br> 
                <sub> 
                    <b>Hellen Lima</b> 
                </sub> 
            </a> 
        </td> 
    </tr> 
</table>

## MVP

O MVP contempla:

- cadastro e autenticação de usuários;
- cadastro de empresas e controle de acesso;
- perfis de proprietário, financeiro e contador;
- cadastro, consulta, edição e exclusão de obrigações;
- categorias, vencimentos e recorrência mensal ou anual;
- anexos de obrigações e documentos;
- cadastro e controle de validade de documentos empresariais;
- registro e histórico de pagamentos;
- alertas por e-mail nos prazos de 7, 3 e 1 dia antes do vencimento e no dia do vencimento;
- painel com total mensal, obrigações vencidas e distribuição por categoria;
- exportação de obrigações e pagamentos em PDF.

## Estrutura do repositório

```text
.
├── backend/       # API em Java com Spring Boot
├── frontend/      # Interface em React + Vite
├── docs/          # Requisitos, arquitetura e modelo de dados
├── docker-compose.yml
└── README.md
```

## Como executar

Com o Docker Desktop aberto, na raiz do repositório:

```bash
cp .env.example .env          # preencha POSTGRES_PASSWORD
docker compose up -d --build
```

| Serviço | Endereço |
|---|---|
| Sistema (front-end) | http://localhost:5173 |
| API | http://localhost:8080/api |
| Documentação da API (Swagger) | http://localhost:8080/swagger-ui/index.html |
| E-mails enviados (Mailpit) | http://localhost:8025 |

Detalhes de cada parte: [back-end](backend/README.md) e [front-end](frontend/README.md).

## Documentação

- [Escopo do projeto](docs/01-escopo.md)
- [Requisitos funcionais](docs/02-requisitos_funcionais.md)
- [Requisitos não funcionais](docs/03-requisitos_nao_funcionais.md)
- [Permissões de usuário](docs/04-permissoes_de_usuario.md)
- [Regras de negócio](docs/05-regras_de_negocio.md)
- [Modelo de dados](docs/06-modelo_de_dados.drawio)
- [Arquitetura](docs/07-arquitetura.md)
- [Critérios de aceitação](docs/08-criterios_de_aceitacao.md)
- [Documentação da API (Swagger)](http://localhost:8080/swagger-ui/index.html): disponível com o back-end rodando
