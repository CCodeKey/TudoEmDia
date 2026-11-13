import { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { Building2, MailX } from 'lucide-react';
import { Botao } from '../components/ui/Botao';
import { Carregando } from '../components/ui/Carregando';
import { Aviso, Vazio } from '../components/ui/Estados';
import { useAcao } from '../hooks/useAcao';
import { useAuth } from '../hooks/useAuth';
import { useCarregar } from '../hooks/useCarregar';
import { conviteService } from '../services/empresaService';
import { formatarDataHora } from '../utils/formato';
import { PERFIS } from '../utils/rotulos';

const INDISPONIVEL = {
  ACEITO: 'Este convite já foi aceito.',
  EXPIRADO: 'Este convite expirou. Peça um novo ao proprietário da empresa.',
  CANCELADO: 'Este convite foi cancelado pelo proprietário da empresa.',
};

// Página aberta pelo link do e-mail de convite (RF04, RN16)
export function Convite() {
  const { token } = useParams();
  const { usuario, sair } = useAuth();
  const navegar = useNavigate();
  const { dados: convite, carregando, erro } = useCarregar(() => conviteService.consultar(token), [token]);
  const { executar, enviando } = useAcao();

  useEffect(() => {
    document.title = 'Convite · TudoEmDia';
  }, []);

  if (carregando) return <Carregando />;
  if (erro) {
    return (
      <Vazio
        icone={MailX}
        titulo="Convite não encontrado"
        descricao="Confira se o link está completo ou peça um novo convite ao proprietário da empresa."
        acao={<Link to="/">Ir para o início</Link>}
      />
    );
  }

  const aceitar = async () => {
    const resultado = await executar(() => conviteService.aceitar(token), { sucesso: 'Convite aceito. Bem-vindo!' });
    if (resultado.ok) navegar(`/empresas/${resultado.resultado.id}/painel`, { replace: true });
  };

  const estadoDestaPagina = { de: { pathname: `/convites/${token}` }, email: convite.email };
  const emailCorreto = usuario?.email?.toLowerCase() === convite.email.toLowerCase();

  return (
    <div className="pilha">
      <div>
        <h2>Você foi convidado</h2>
        <p className="texto-secundario" style={{ marginTop: 6 }}>
          Para acessar uma empresa no TudoEmDia.
        </p>
      </div>

      <div className="cartao">
        <div className="cartao__corpo linha" style={{ flexWrap: 'nowrap', gap: 14 }}>
          <span className="cartao-empresa__icone">
            <Building2 size={20} aria-hidden />
          </span>
          <div className="preenche">
            <strong>{convite.empresa}</strong>
            <p className="texto-fraco">
              Perfil {PERFIS[convite.perfil]} · para {convite.email}
            </p>
          </div>
        </div>
      </div>

      {convite.status !== 'PENDENTE' ? (
        <Aviso tom="aviso">{INDISPONIVEL[convite.status]}</Aviso>
      ) : !usuario ? (
        <>
          <p className="texto-secundario">
            Entre com sua conta ou crie uma usando o e-mail <strong>{convite.email}</strong>. O convite vale até{' '}
            {formatarDataHora(convite.dataExpiracao)}.
          </p>
          <div className="pilha pilha--p">
            <Botao variante="primario" bloco onClick={() => navegar('/cadastro', { state: estadoDestaPagina })}>
              Criar conta
            </Botao>
            <Botao bloco onClick={() => navegar('/login', { state: estadoDestaPagina })}>
              Já tenho conta
            </Botao>
          </div>
        </>
      ) : emailCorreto ? (
        <Botao variante="primario" bloco carregando={enviando} onClick={aceitar}>
          Aceitar convite
        </Botao>
      ) : (
        <>
          <Aviso tom="aviso">
            Você está conectado como <strong>{usuario.email}</strong>, mas o convite foi enviado para{' '}
            <strong>{convite.email}</strong>.
          </Aviso>
          <Botao bloco onClick={sair}>
            Sair e entrar com outro e-mail
          </Botao>
        </>
      )}
    </div>
  );
}
