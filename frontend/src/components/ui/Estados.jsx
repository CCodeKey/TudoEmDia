import { CircleAlert, Inbox } from 'lucide-react';
import { Botao } from './Botao';

export function Vazio({ icone: Icone = Inbox, titulo, descricao, acao }) {
  return (
    <div className="vazio">
      <span className="vazio__icone">
        <Icone size={24} aria-hidden />
      </span>
      <h3>{titulo}</h3>
      {descricao && <p>{descricao}</p>}
      {acao}
    </div>
  );
}

export function ErroCarregamento({ erro, aoTentarNovamente }) {
  return (
    <div className="vazio">
      <span className="vazio__icone" style={{ color: 'var(--cor-perigo)' }}>
        <CircleAlert size={24} aria-hidden />
      </span>
      <h3>Não foi possível carregar</h3>
      <p>{erro?.message ?? 'Tente novamente em instantes.'}</p>
      {aoTentarNovamente && <Botao onClick={aoTentarNovamente}>Tentar novamente</Botao>}
    </div>
  );
}

export function Aviso({ tom = 'info', icone: Icone = CircleAlert, children }) {
  return (
    <div className={`aviso aviso--${tom}`} role={tom === 'perigo' ? 'alert' : undefined}>
      <Icone size={18} aria-hidden />
      <div>{children}</div>
    </div>
  );
}
