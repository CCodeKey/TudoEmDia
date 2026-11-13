import { Check, Circle } from 'lucide-react';
import { NIVEIS_SENHA, nivelSenha, requisitosSenha } from '../../utils/senha';

// Barra colorida que mostra a força da senha enquanto a pessoa digita
export function ForcaSenha({ senha }) {
  if (!senha) return null;

  const nivel = nivelSenha(senha);
  const { rotulo, tom } = NIVEIS_SENHA[nivel];

  return (
    <div className={`forca-senha forca-senha--${tom}`}>
      <div className="forca-senha__topo">
        <div className="forca-senha__barra" aria-hidden>
          {NIVEIS_SENHA.map((item, indice) => (
            <span key={item.rotulo} className={indice <= nivel ? 'forca-senha__segmento--ativo' : ''} />
          ))}
        </div>
        <span className="forca-senha__rotulo" aria-live="polite">
          <span className="visualmente-oculto">Força da senha: </span>
          {rotulo}
        </span>
      </div>
      <ul className="forca-senha__requisitos">
        {requisitosSenha(senha).map((requisito) => (
          <li key={requisito.texto} className={requisito.atendido ? 'forca-senha__ok' : ''}>
            {requisito.atendido ? <Check size={14} aria-hidden /> : <Circle size={14} aria-hidden />}
            {requisito.texto}
            {requisito.obrigatorio && !requisito.atendido && <span className="visualmente-oculto"> (obrigatório)</span>}
          </li>
        ))}
      </ul>
    </div>
  );
}
