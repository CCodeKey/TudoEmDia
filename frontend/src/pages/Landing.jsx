import { useEffect } from 'react';
import { Link } from 'react-router';
import {
  ArrowRight,
  BellRing,
  Calculator,
  CalendarCheck,
  ChartColumn,
  Check,
  FileDown,
  FileLock2,
  Repeat,
  ShieldCheck,
  UserCog,
  UsersRound,
  Wallet,
} from 'lucide-react';
import { Marca } from '../components/Marca';
import { Rodape } from '../components/Rodape';
import { Selo } from '../components/ui/Selo';

// Funcionalidades do MVP (docs/01-escopo.md)
const RECURSOS = [
  {
    icone: CalendarCheck,
    titulo: 'Obrigações sob controle',
    texto: 'Boletos, impostos, aluguel e fornecedores com valor, categoria e vencimento. A situação muda sozinha: pendente, paga ou vencida.',
  },
  {
    icone: Repeat,
    titulo: 'Contas que se repetem',
    texto: 'Cadastre uma vez e o sistema cria as próximas ocorrências todo mês ou todo ano, inclusive nos meses sem dia 31.',
  },
  {
    icone: BellRing,
    titulo: 'Alertas antes do prazo',
    texto: 'E-mails 7, 3 e 1 dia antes e no dia do vencimento, para cada pessoa da equipe que precisa saber.',
  },
  {
    icone: Wallet,
    titulo: 'Histórico de pagamentos',
    texto: 'Registre quando e quanto foi pago, com juros ou multa, e saiba sempre quem registrou cada pagamento.',
  },
  {
    icone: FileLock2,
    titulo: 'Documentos com validade',
    texto: 'Alvarás, contratos e certidões guardados em um só lugar, com aviso de vencido. Sem link público.',
  },
  {
    icone: ChartColumn,
    titulo: 'Painel do mês',
    texto: 'Total a pagar, o que já foi pago, as contas em atraso e quanto cada categoria pesa no mês.',
  },
];

const PASSOS = [
  { titulo: 'Crie sua conta', texto: 'Cadastro rápido, só com nome, e-mail e senha.' },
  { titulo: 'Cadastre a empresa', texto: 'Informe o CNPJ e você passa a ser o proprietário.' },
  { titulo: 'Lance as obrigações', texto: 'Anexe boletos e guias, e deixe os alertas trabalharem por você.' },
];

const PERFIS = [
  {
    icone: UserCog,
    nome: 'Proprietário',
    texto: 'Acompanha tudo, cadastra documentos, convida a equipe e define quem recebe os alertas.',
  },
  {
    icone: Wallet,
    nome: 'Financeiro',
    texto: 'Cadastra obrigações, configura recorrências e registra os pagamentos do dia a dia.',
  },
  {
    icone: Calculator,
    nome: 'Contador',
    texto: 'Consulta obrigações, documentos e histórico, e exporta relatórios em PDF.',
  },
];

// Prévia ilustrativa do painel, montada com os próprios componentes do sistema
function PreviaPainel() {
  const linhas = [
    { descricao: 'DAS Simples Nacional', data: '20/11', valor: 'R$ 812,45', tom: 'aviso', situacao: 'Pendente' },
    { descricao: 'Aluguel da loja', data: '10/11', valor: 'R$ 3.200,00', tom: 'sucesso', situacao: 'Paga' },
    { descricao: 'Conta de luz', data: '05/11', valor: 'R$ 487,90', tom: 'perigo', situacao: 'Vencida' },
  ];
  return (
    <div className="previa" aria-hidden>
      <div className="previa__barra">
        <span />
        <span />
        <span />
      </div>
      <div className="previa__conteudo">
        <div className="previa__indicadores">
          <div>
            <small>A pagar no mês</small>
            <strong>R$ 13.002,45</strong>
          </div>
          <div>
            <small>Já pago</small>
            <strong>R$ 3.651,50</strong>
          </div>
          <div>
            <small>Em dia</small>
            <strong className="previa__verde">92%</strong>
          </div>
        </div>
        <div className="previa__barras">
          {[
            ['Folha de pagamento', 82],
            ['Aluguel e consumo', 46],
            ['Impostos', 24],
          ].map(([nome, largura], indice) => (
            <div key={nome}>
              <small>{nome}</small>
              <div className="barra__trilho">
                <div className="barra__preenchimento" style={{ width: `${largura}%`, background: `var(--grafico-${indice + 1})` }} />
              </div>
            </div>
          ))}
        </div>
        <ul className="previa__lista">
          {linhas.map((linha) => (
            <li key={linha.descricao}>
              <span>
                <strong>{linha.descricao}</strong>
                <small>vence {linha.data}</small>
              </span>
              <Selo tom={linha.tom}>{linha.situacao}</Selo>
              <strong className="numero">{linha.valor}</strong>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

// Página inicial para quem ainda não entrou no sistema
export function Landing() {
  useEffect(() => {
    document.title = 'TudoEmDia · Obrigações, pagamentos e documentos da sua empresa';
  }, []);

  return (
    <div className="landing">
      <header className="landing__topo">
        <div className="landing__largura landing__topo-interno">
          <Marca />
          <nav className="linha" aria-label="Acesso">
            <Link to="/login" className="botao botao--fantasma">
              Entrar
            </Link>
            <Link to="/cadastro" className="botao botao--primario">
              Criar conta
            </Link>
          </nav>
        </div>
      </header>

      <main>
        <section className="landing__heroi">
          <div className="landing__largura landing__heroi-grade">
            <div className="landing__heroi-texto">
              <span className="landing__sobretitulo">Para pequenas empresas e MEIs</span>
              <h1>
                Nenhuma conta esquecida.
                <br />
                <span>Tudo em dia.</span>
              </h1>
              <p>
                Organize boletos, impostos e documentos da empresa em um só lugar, receba alertas antes de cada
                vencimento e divida o trabalho com sua equipe e seu contador.
              </p>
              <div className="linha landing__acoes">
                <Link to="/cadastro" className="botao botao--primario landing__botao-grande">
                  Começar agora
                  <ArrowRight size={18} aria-hidden />
                </Link>
                <Link to="/login" className="botao botao--secundario landing__botao-grande">
                  Já tenho conta
                </Link>
              </div>
              <ul className="landing__garantias">
                {['Sem planilhas', 'Alertas por e-mail', 'Relatórios em PDF'].map((item) => (
                  <li key={item}>
                    <Check size={16} aria-hidden />
                    {item}
                  </li>
                ))}
              </ul>
            </div>
            <PreviaPainel />
          </div>
        </section>

        <section className="landing__secao" aria-labelledby="problema">
          <div className="landing__largura landing__problema">
            <h2 id="problema">Papel, planilha, e-mail, memória…</h2>
            <p>
              Quando as obrigações estão espalhadas, é fácil perder um prazo, pagar multa e juros, ou não saber quem
              pagou o quê. O TudoEmDia reúne tudo e avisa antes que vire problema.
            </p>
          </div>
        </section>

        <section className="landing__secao landing__secao--clara" aria-labelledby="recursos">
          <div className="landing__largura">
            <div className="landing__cabecalho-secao">
              <h2 id="recursos">Tudo o que a rotina financeira precisa</h2>
              <p>Do lançamento da conta ao relatório para o contador.</p>
            </div>
            <div className="landing__recursos">
              {RECURSOS.map(({ icone: Icone, titulo, texto }) => (
                <article key={titulo} className="cartao landing__recurso">
                  <span className="landing__icone">
                    <Icone size={20} aria-hidden />
                  </span>
                  <h3>{titulo}</h3>
                  <p>{texto}</p>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className="landing__secao" aria-labelledby="como-funciona">
          <div className="landing__largura">
            <div className="landing__cabecalho-secao">
              <h2 id="como-funciona">Comece em poucos minutos</h2>
            </div>
            <ol className="landing__passos">
              {PASSOS.map((passo, indice) => (
                <li key={passo.titulo}>
                  <span className="landing__numero">{indice + 1}</span>
                  <h3>{passo.titulo}</h3>
                  <p>{passo.texto}</p>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section className="landing__secao landing__secao--clara" aria-labelledby="equipe">
          <div className="landing__largura">
            <div className="landing__cabecalho-secao">
              <h2 id="equipe">Cada pessoa com o acesso certo</h2>
              <p>
                Convide por e-mail. Cada perfil vê e faz só o que precisa, e o histórico registra quem fez cada
                alteração.
              </p>
            </div>
            <div className="landing__perfis">
              {PERFIS.map(({ icone: Icone, nome, texto }) => (
                <article key={nome} className="cartao landing__perfil">
                  <span className="landing__icone">
                    <Icone size={20} aria-hidden />
                  </span>
                  <div>
                    <h3>{nome}</h3>
                    <p>{texto}</p>
                  </div>
                </article>
              ))}
            </div>
            <ul className="landing__selos">
              <li>
                <ShieldCheck size={18} aria-hidden /> Dados isolados por empresa
              </li>
              <li>
                <UsersRound size={18} aria-hidden /> Acesso desativado sem perder o histórico
              </li>
              <li>
                <FileDown size={18} aria-hidden /> Exportação em PDF
              </li>
            </ul>
          </div>
        </section>

        <section className="landing__secao">
          <div className="landing__largura">
            <div className="landing__chamada">
              <div>
                <h2>Deixe os prazos com a gente</h2>
                <p>Crie sua conta e cadastre a primeira obrigação hoje.</p>
              </div>
              <Link to="/cadastro" className="botao landing__botao-grande landing__botao-claro">
                Criar minha conta
                <ArrowRight size={18} aria-hidden />
              </Link>
            </div>
          </div>
        </section>
      </main>

      <Rodape />
    </div>
  );
}
