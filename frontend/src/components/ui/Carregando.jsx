import { LoaderCircle } from 'lucide-react';

export function Carregando({ tela = false, texto = 'Carregando…' }) {
  return (
    <div className={`carregando ${tela ? 'carregando--tela' : ''}`} role="status">
      <LoaderCircle size={20} className="girando" aria-hidden />
      <span>{texto}</span>
    </div>
  );
}

export function Esqueleto({ altura = 16, largura = '100%' }) {
  return <div className="esqueleto" style={{ height: altura, width: largura }} aria-hidden />;
}
