import { Link } from 'react-router';

// Política de privacidade acessível em todas as páginas (RNF14)
export function Rodape() {
  return (
    <footer className="rodape">
      <span>© {new Date().getFullYear()} TudoEmDia</span>
      <Link to="/privacidade">Política de privacidade</Link>
      <Link to="/termos">Termos de uso</Link>
    </footer>
  );
}
