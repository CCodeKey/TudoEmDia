import { useEffect } from 'react';
import { Link } from 'react-router';
import { ArrowLeft } from 'lucide-react';
import { Marca } from '../components/Marca';
import { Rodape } from '../components/Rodape';

const CONTEUDOS = {
  privacidade: {
    titulo: 'Política de privacidade',
    secoes: [
      ['Quais dados coletamos', 'Coletamos apenas o necessário para o funcionamento do serviço: nome, e-mail e senha (guardada somente de forma criptografada), os dados das empresas cadastradas e as informações de obrigações, pagamentos e documentos que você registra.'],
      ['Para que usamos', 'Os dados servem para identificar você, controlar o acesso de cada pessoa às empresas, enviar convites e alertas de vencimento por e-mail e gerar os relatórios solicitados. Não vendemos nem compartilhamos dados com terceiros para fins comerciais.'],
      ['Quem tem acesso', 'Os dados de cada empresa são visíveis apenas para as pessoas vinculadas a ela, conforme o perfil definido pelo proprietário. Os arquivos não têm link público.'],
      ['Seus direitos', 'Conforme a Lei Geral de Proteção de Dados (LGPD), você pode consultar os dados que mantemos sobre você e pedir a exclusão da sua conta a qualquer momento, em "Minha conta". Na exclusão, seus dados pessoais são anonimizados; os registros financeiros das empresas são preservados sem identificar você.'],
      ['Consentimento', 'O aceite desta política é registrado com data no momento do cadastro.'],
    ],
  },
  termos: {
    titulo: 'Termos de uso',
    secoes: [
      ['O serviço', 'O TudoEmDia ajuda pequenas empresas a organizar obrigações financeiras, pagamentos e documentos. O sistema registra as informações informadas pelos usuários e não realiza pagamentos.'],
      ['Sua conta', 'Você é responsável por manter sua senha em segurança e pelas informações que cadastra. Após 5 tentativas de acesso erradas, a conta fica bloqueada por 15 minutos.'],
      ['Acesso da equipe', 'O proprietário da empresa define quem pode acessá-la e com qual perfil (proprietário, financeiro ou contador), e pode desativar acessos quando necessário.'],
      ['Alertas', 'Os alertas por e-mail são um apoio à organização. Confira sempre os prazos das suas obrigações.'],
    ],
  },
};

export function Institucional({ tipo }) {
  const conteudo = CONTEUDOS[tipo];

  useEffect(() => {
    document.title = `${conteudo.titulo} · TudoEmDia`;
  }, [conteudo.titulo]);

  return (
    <div className="layout-simples">
      <header className="layout-simples__topo">
        <Marca />
      </header>
      <main className="layout-simples__conteudo">
        <div className="texto-legal">
          <Link to="/" className="voltar">
            <ArrowLeft size={16} aria-hidden /> Voltar
          </Link>
          <h1>{conteudo.titulo}</h1>
          {conteudo.secoes.map(([titulo, texto]) => (
            <section key={titulo} className="pilha pilha--p">
              <h2>{titulo}</h2>
              <p>{texto}</p>
            </section>
          ))}
        </div>
      </main>
      <Rodape />
    </div>
  );
}
