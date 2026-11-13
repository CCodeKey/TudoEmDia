import { useId, useState } from 'react';
import { Eye, EyeOff, Upload } from 'lucide-react';
import { TIPOS_ACEITOS, tamanhoLegivel } from '../../utils/arquivos';

function Campo({ id, rotulo, opcional, ajuda, erro, children }) {
  return (
    <div className="campo">
      {rotulo && (
        <label className="campo__rotulo" htmlFor={id}>
          {rotulo}
          {opcional && <span className="campo__opcional"> (opcional)</span>}
        </label>
      )}
      {children}
      {erro ? (
        <span className="campo__erro" id={`${id}-erro`}>
          {erro}
        </span>
      ) : (
        ajuda && (
          <span className="campo__ajuda" id={`${id}-ajuda`}>
            {ajuda}
          </span>
        )
      )}
    </div>
  );
}

function atributosAcessiveis(id, erro, ajuda) {
  return {
    id,
    'aria-invalid': erro ? true : undefined,
    'aria-describedby': erro ? `${id}-erro` : ajuda ? `${id}-ajuda` : undefined,
  };
}

export function CampoTexto({ rotulo, opcional, ajuda, erro, pequeno, className = '', ...props }) {
  const id = useId();
  return (
    <Campo id={id} rotulo={rotulo} opcional={opcional} ajuda={ajuda} erro={erro}>
      <input
        className={`entrada ${pequeno ? 'entrada--pequena' : ''} ${className}`}
        {...atributosAcessiveis(id, erro, ajuda)}
        {...props}
      />
    </Campo>
  );
}

// Campo de senha com botão para mostrar ou ocultar o que foi digitado
export function CampoSenha({ rotulo, opcional, ajuda, erro, className = '', ...props }) {
  const id = useId();
  const [visivel, setVisivel] = useState(false);
  return (
    <Campo id={id} rotulo={rotulo} opcional={opcional} ajuda={ajuda} erro={erro}>
      <div className="campo-senha">
        <input
          className={`entrada ${className}`}
          type={visivel ? 'text' : 'password'}
          autoCapitalize="none"
          autoCorrect="off"
          spellCheck={false}
          {...atributosAcessiveis(id, erro, ajuda)}
          {...props}
        />
        <button
          type="button"
          className="campo-senha__botao"
          onClick={() => setVisivel((atual) => !atual)}
          aria-label={visivel ? 'Ocultar senha' : 'Mostrar senha'}
          aria-pressed={visivel}
          aria-controls={id}
          title={visivel ? 'Ocultar senha' : 'Mostrar senha'}
        >
          {visivel ? <EyeOff size={18} aria-hidden /> : <Eye size={18} aria-hidden />}
        </button>
      </div>
    </Campo>
  );
}

export function CampoSelecao({ rotulo, opcional, ajuda, erro, pequeno, children, ...props }) {
  const id = useId();
  return (
    <Campo id={id} rotulo={rotulo} opcional={opcional} ajuda={ajuda} erro={erro}>
      <select className={`entrada ${pequeno ? 'entrada--pequena' : ''}`} {...atributosAcessiveis(id, erro, ajuda)} {...props}>
        {children}
      </select>
    </Campo>
  );
}

export function CampoValor(props) {
  return <CampoTexto type="number" inputMode="decimal" step="0.01" min="0.01" placeholder="0,00" {...props} />;
}

export function CampoArquivo({ rotulo, erro, ajuda = 'PDF, JPG ou PNG, até 10 MB.', aoSelecionar, arquivo }) {
  const id = useId();
  const [arrastando, setArrastando] = useState(false);
  return (
    <Campo id={id} rotulo={rotulo} erro={erro}>
      <label
        className="envio-arquivo"
        style={arrastando ? { borderColor: 'var(--cor-primaria)' } : undefined}
        onDragOver={(evento) => {
          evento.preventDefault();
          setArrastando(true);
        }}
        onDragLeave={() => setArrastando(false)}
        onDrop={(evento) => {
          evento.preventDefault();
          setArrastando(false);
          if (evento.dataTransfer.files[0]) aoSelecionar(evento.dataTransfer.files[0]);
        }}
      >
        <input
          id={id}
          type="file"
          accept={TIPOS_ACEITOS}
          onChange={(evento) => aoSelecionar(evento.target.files[0] ?? null)}
          {...atributosAcessiveis(id, erro, null)}
        />
        <span className="envio-arquivo__icone">
          <Upload size={18} aria-hidden />
        </span>
        <span className="envio-arquivo__texto">
          {arquivo ? (
            <>
              <strong>{arquivo.name}</strong>
              <span className="texto-fraco">{tamanhoLegivel(arquivo.size)} · clique para trocar</span>
            </>
          ) : (
            <>
              <strong>Escolher arquivo</strong>
              <span className="texto-fraco">{ajuda} Você também pode arrastar até aqui.</span>
            </>
          )}
        </span>
      </label>
    </Campo>
  );
}

export function CaixaSelecao({ children, ...props }) {
  return (
    <label className="caixa-selecao">
      <input type="checkbox" {...props} />
      <span>{children}</span>
    </label>
  );
}

export function Interruptor({ rotulo, ...props }) {
  return (
    <span className="interruptor">
      <input type="checkbox" role="switch" aria-label={rotulo} {...props} />
      <span aria-hidden />
    </span>
  );
}
