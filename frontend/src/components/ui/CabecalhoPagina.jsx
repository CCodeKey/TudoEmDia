import { useEffect } from 'react';
import { Link } from 'react-router';
import { ArrowLeft } from 'lucide-react';

export function CabecalhoPagina({ titulo, descricao, acoes, voltar }) {
  useEffect(() => {
    document.title = `${titulo} · TudoEmDia`;
  }, [titulo]);

  return (
    <header className="pagina__cabecalho">
      <div className="pagina__titulo">
        {voltar && (
          <Link to={voltar.para} className="voltar">
            <ArrowLeft size={16} aria-hidden />
            {voltar.texto}
          </Link>
        )}
        <h1>{titulo}</h1>
        {descricao && <p>{descricao}</p>}
      </div>
      {acoes && <div className="pagina__acoes">{acoes}</div>}
    </header>
  );
}
