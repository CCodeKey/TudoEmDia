import { LoaderCircle } from 'lucide-react';

const VARIANTES = {
  primario: 'botao--primario',
  secundario: 'botao--secundario',
  fantasma: 'botao--fantasma',
  perigo: 'botao--perigo',
  'perigo-suave': 'botao--perigo-suave',
};

export function Botao({
  variante = 'secundario',
  pequeno = false,
  icone: Icone,
  somenteIcone = false,
  bloco = false,
  carregando = false,
  className = '',
  children,
  type = 'button',
  disabled,
  ...props
}) {
  const classes = [
    'botao',
    VARIANTES[variante],
    pequeno && 'botao--pequeno',
    somenteIcone && 'botao--icone',
    bloco && 'botao--bloco',
    className,
  ]
    .filter(Boolean)
    .join(' ');
  const tamanhoIcone = pequeno ? 16 : 18;

  return (
    <button type={type} className={classes} disabled={disabled || carregando} aria-busy={carregando || undefined} {...props}>
      {carregando ? (
        <LoaderCircle size={tamanhoIcone} className="girando" aria-hidden />
      ) : (
        Icone && <Icone size={tamanhoIcone} aria-hidden />
      )}
      {somenteIcone ? <span className="visualmente-oculto">{children}</span> : children}
    </button>
  );
}
