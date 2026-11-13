import { Link } from 'react-router';

export function Marca({ clara = false, para = '/' }) {
  return (
    <Link to={para} className={`marca ${clara ? 'marca--clara' : ''}`} aria-label="TudoEmDia, início">
      <svg width="30" height="30" viewBox="0 0 32 32" aria-hidden>
        <rect width="32" height="32" rx="8" fill={clara ? 'rgba(255,255,255,0.16)' : 'var(--cor-primaria)'} />
        <rect x="7" y="9" width="18" height="16" rx="3" fill="none" stroke="#fff" strokeWidth="2" />
        <path d="M7 14h18M12 6.5v4M20 6.5v4" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
        <path d="m12.5 19.5 2.5 2.5 4.5-4.5" fill="none" stroke="#fff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
      <span>
        Tudo<b>Em</b>Dia
      </span>
    </Link>
  );
}
