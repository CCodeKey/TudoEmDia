import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Bot, FileText, LayoutDashboard, PartyPopper, ReceiptText, Repeat, Sparkles, Users, Wallet, X } from 'lucide-react';
import { Botao } from './ui/Botao';
import { registrarQueViuTutorial, viuTutorial } from '../utils/armazenamento';

// Roteiro do assistente; cada passo só aparece se o perfil tiver acesso à tela
const PASSOS = [
  {
    titulo: 'Boas-vindas ao TudoEmDia!',
    texto: 'Sou seu assistente. Em menos de um minuto, mostro onde fica cada coisa para você manter as contas da empresa em dia.',
    icone: Sparkles,
  },
  {
    titulo: 'Painel',
    texto: 'Sua visão geral: total do mês, obrigações vencidas e gastos por categoria, tudo num só lugar.',
    icone: LayoutDashboard,
    para: 'painel',
    permissao: 'CONSULTAR_PAINEL',
  },
  {
    titulo: 'Obrigações',
    texto: 'Cadastre impostos, boletos e contas com vencimento. Avisamos por e-mail 7, 3 e 1 dia antes e no próprio dia.',
    icone: ReceiptText,
    para: 'obrigacoes',
    permissao: 'CONSULTAR_OBRIGACOES',
  },
  {
    titulo: 'Recorrências',
    texto: 'Contas que se repetem todo mês ou todo ano são geradas automaticamente. Cadastre uma vez e esqueça.',
    icone: Repeat,
    para: 'recorrencias',
    permissao: 'CONSULTAR_OBRIGACOES',
  },
  {
    titulo: 'Pagamentos',
    texto: 'Registre o que foi pago e consulte o histórico completo. Precisa enviar ao contador? Exporte em PDF.',
    icone: Wallet,
    para: 'pagamentos',
    permissao: 'CONSULTAR_HISTORICO_PAGAMENTOS',
  },
  {
    titulo: 'Documentos',
    texto: 'Guarde alvarás, certidões e licenças e saiba com antecedência quando cada um vai vencer.',
    icone: FileText,
    para: 'documentos',
    permissao: 'CONSULTAR_DOCUMENTOS',
  },
  {
    titulo: 'Equipe',
    texto: 'Convide o financeiro ou o contador da empresa. Cada perfil enxerga só o que precisa.',
    icone: Users,
    para: 'equipe',
    permissao: 'CONSULTAR_USUARIOS',
  },
  {
    titulo: 'Tudo pronto!',
    texto: 'Uma boa forma de começar é cadastrar sua primeira obrigação. Se bater alguma dúvida, é só me chamar no botão de ajuda.',
    icone: PartyPopper,
  },
];

const DESTAQUE = 'navegacao__item--tutorial';

export function Tutorial({ usuarioId, pode }) {
  const navegar = useNavigate();
  const [aberto, setAberto] = useState(() => !viuTutorial(usuarioId));
  const [indice, setIndice] = useState(0);

  const passos = PASSOS.filter((passo) => !passo.permissao || pode(passo.permissao));
  const passo = passos[indice];
  const ultimo = indice === passos.length - 1;
  const Icone = passo.icone;

  // Destaca no menu lateral o item que está sendo explicado
  useEffect(() => {
    if (!aberto || !passo.para) return undefined;
    const item = document.querySelector(`.navegacao a[href$="/${passo.para}"]`);
    item?.classList.add(DESTAQUE);
    return () => item?.classList.remove(DESTAQUE);
  }, [aberto, passo.para]);

  const irPara = (novo) => {
    setIndice(novo);
    if (passos[novo].para) navegar(passos[novo].para);
  };

  const fechar = () => {
    registrarQueViuTutorial(usuarioId);
    setAberto(false);
    setIndice(0);
  };

  const concluir = () => {
    fechar();
    if (pode('CONSULTAR_OBRIGACOES')) navegar('obrigacoes');
  };

  if (!aberto) {
    return (
      <button type="button" className="tutorial-botao" onClick={() => setAberto(true)} aria-label="Abrir tutorial do sistema">
        <Bot size={20} aria-hidden />
        <span className="tutorial-botao__texto">Ajuda</span>
      </button>
    );
  }

  return (
    <section className="tutorial" role="dialog" aria-labelledby="tutorial-titulo" aria-describedby="tutorial-texto">
      <div className="tutorial__topo">
        <span className="tutorial__avatar" aria-hidden>
          <Bot size={18} />
        </span>
        <span className="tutorial__assistente">Assistente TudoEmDia</span>
        <button type="button" className="botao botao--fantasma botao--icone botao--pequeno" onClick={fechar} aria-label="Fechar tutorial">
          <X size={16} />
        </button>
      </div>

      {/* A chave reinicia a animação a cada passo */}
      <div key={indice} className="tutorial__conteudo" aria-live="polite">
        <span className="tutorial__icone" aria-hidden>
          <Icone size={22} />
        </span>
        <div>
          <h2 id="tutorial-titulo">{passo.titulo}</h2>
          <p id="tutorial-texto">{passo.texto}</p>
        </div>
      </div>

      <div className="tutorial__progresso" aria-label={`Passo ${indice + 1} de ${passos.length}`}>
        {passos.map((item, i) => (
          <span key={item.titulo} className={i <= indice ? 'tutorial__ponto tutorial__ponto--ativo' : 'tutorial__ponto'} />
        ))}
      </div>

      <div className="tutorial__rodape">
        {indice === 0 ? (
          <Botao pequeno variante="fantasma" onClick={fechar}>
            Pular tutorial
          </Botao>
        ) : (
          <Botao pequeno variante="fantasma" onClick={() => irPara(indice - 1)}>
            Voltar
          </Botao>
        )}
        <Botao pequeno variante="primario" onClick={() => (ultimo ? concluir() : irPara(indice + 1))}>
          {indice === 0 ? 'Vamos lá' : ultimo ? 'Começar a usar' : 'Próximo'}
        </Botao>
      </div>
    </section>
  );
}
