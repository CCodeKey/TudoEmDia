import { Navigate, Outlet, useLocation } from 'react-router';
import { Lock } from 'lucide-react';
import { Carregando } from '../components/ui/Carregando';
import { Vazio } from '../components/ui/Estados';
import { useAuth } from '../hooks/useAuth';
import { useEmpresa } from '../hooks/useEmpresa';
import { Inicio } from '../pages/Inicio';
import { Landing } from '../pages/Landing';
import { destinoAposLogin } from '../utils/navegacao';

// Página inicial: landing para visitantes; quem está logado segue para a empresa
export function PaginaInicial() {
  const { usuario, carregando } = useAuth();
  if (carregando) return <Carregando tela />;
  return usuario ? <Inicio /> : <Landing />;
}

// Exige login; quem não está logado vai para o login e volta depois
export function RotaPrivada() {
  const { usuario, carregando } = useAuth();
  const local = useLocation();
  if (carregando) return <Carregando tela />;
  if (!usuario) return <Navigate to="/login" replace state={{ de: local }} />;
  return <Outlet />;
}

// Login e cadastro: quem já está logado vai direto para o sistema
export function SoVisitante({ children }) {
  const { usuario, carregando } = useAuth();
  const local = useLocation();
  if (carregando) return <Carregando tela />;
  if (usuario) return <Navigate to={destinoAposLogin(local)} replace />;
  return children;
}

// A verificação real é feita no servidor (RN23); aqui só evita uma tela sem uso
export function ComPermissao({ permissao, children }) {
  const { pode } = useEmpresa();
  if (!pode(permissao)) {
    return (
      <Vazio
        icone={Lock}
        titulo="Acesso restrito"
        descricao="Seu perfil nesta empresa não tem acesso a esta área. Fale com o proprietário se precisar."
      />
    );
  }
  return children;
}
