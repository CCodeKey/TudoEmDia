import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';

// Janela sobreposta. Com aoEnviar, o conteúdo vira um formulário e Enter confirma
export function Modal({ aberto, aoFechar, titulo, descricao, largo = false, rodape, aoEnviar, children }) {
  const caixa = useRef(null);
  // Guardado em ref para o efeito rodar só ao abrir, sem roubar o foco a cada digitação
  const fechar = useRef(aoFechar);
  useEffect(() => {
    fechar.current = aoFechar;
  });

  useEffect(() => {
    if (!aberto) return undefined;
    const anterior = document.activeElement;
    const aoTeclar = (evento) => {
      if (evento.key === 'Escape') fechar.current();
    };
    document.addEventListener('keydown', aoTeclar);
    document.body.style.overflow = 'hidden';
    // Foca o primeiro campo; sem campos, o primeiro botão do rodapé
    const primeiro =
      caixa.current?.querySelector('.modal__corpo :is(input:not([type=hidden]), select, textarea)') ??
      caixa.current?.querySelector('.modal__rodape button');
    primeiro?.focus();
    return () => {
      document.removeEventListener('keydown', aoTeclar);
      document.body.style.overflow = '';
      anterior?.focus?.();
    };
  }, [aberto]);

  if (!aberto) return null;

  const Conteudo = aoEnviar ? 'form' : 'div';
  const propsConteudo = aoEnviar
    ? {
        onSubmit: (evento) => {
          evento.preventDefault();
          aoEnviar();
        },
        noValidate: true,
      }
    : {};

  return createPortal(
    <div className="fundo-modal" onMouseDown={(evento) => evento.target === evento.currentTarget && aoFechar()}>
      <div ref={caixa} className={`modal ${largo ? 'modal--largo' : ''}`} role="dialog" aria-modal="true" aria-labelledby="modal-titulo">
        <div className="modal__cabecalho">
          <div>
            <h2 id="modal-titulo">{titulo}</h2>
            {descricao && <p className="texto-secundario">{descricao}</p>}
          </div>
          <button type="button" className="botao botao--fantasma botao--icone botao--pequeno" onClick={aoFechar} aria-label="Fechar">
            <X size={18} />
          </button>
        </div>
        <Conteudo {...propsConteudo} style={{ display: 'contents' }}>
          <div className="modal__corpo">{children}</div>
          {rodape && <div className="modal__rodape">{rodape}</div>}
        </Conteudo>
      </div>
    </div>,
    document.body,
  );
}
