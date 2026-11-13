import { useCallback, useMemo, useRef, useState } from 'react';
import { CircleAlert, CircleCheck, X } from 'lucide-react';
import { NotificacaoContexto } from './contextos';

const DURACAO_MS = 4500;

// Mensagens rápidas de sucesso e erro no canto da tela
export function NotificacaoProvider({ children }) {
  const [itens, setItens] = useState([]);
  const sequencia = useRef(0);

  const fechar = useCallback((id) => setItens((atuais) => atuais.filter((item) => item.id !== id)), []);

  const mostrar = useCallback(
    (tipo, mensagem) => {
      const id = ++sequencia.current;
      setItens((atuais) => [...atuais.slice(-2), { id, tipo, mensagem }]);
      setTimeout(() => fechar(id), DURACAO_MS);
    },
    [fechar],
  );

  const valor = useMemo(
    () => ({
      sucesso: (mensagem) => mostrar('sucesso', mensagem),
      erro: (erroOuMensagem) =>
        mostrar('erro', typeof erroOuMensagem === 'string' ? erroOuMensagem : erroOuMensagem?.message),
    }),
    [mostrar],
  );

  return (
    <NotificacaoContexto.Provider value={valor}>
      {children}
      <div className="notificacoes" role="status" aria-live="polite">
        {itens.map((item) => (
          <div key={item.id} className={`notificacao ${item.tipo === 'erro' ? 'notificacao--erro' : ''}`}>
            {item.tipo === 'erro' ? (
              <CircleAlert size={18} color="var(--cor-perigo)" aria-hidden />
            ) : (
              <CircleCheck size={18} color="var(--cor-primaria)" aria-hidden />
            )}
            <p>{item.mensagem}</p>
            <button type="button" className="botao botao--fantasma botao--icone botao--pequeno" onClick={() => fechar(item.id)} aria-label="Fechar">
              <X size={16} />
            </button>
          </div>
        ))}
      </div>
    </NotificacaoContexto.Provider>
  );
}
