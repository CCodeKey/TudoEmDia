import { Outlet, useNavigate } from 'react-router';
import { LogOut, UserRound } from 'lucide-react';
import { Marca } from '../components/Marca';
import { Rodape } from '../components/Rodape';
import { Botao } from '../components/ui/Botao';
import { useAuth } from '../hooks/useAuth';

// Páginas fora de uma empresa: seleção de empresas e "Minha conta"
export function LayoutSimples() {
  const { sair } = useAuth();
  const navegar = useNavigate();

  return (
    <div className="layout-simples">
      <header className="layout-simples__topo">
        <Marca />
        <div className="linha">
          <Botao variante="fantasma" pequeno icone={UserRound} onClick={() => navegar('/conta')}>
            Minha conta
          </Botao>
          <Botao variante="fantasma" pequeno icone={LogOut} onClick={sair}>
            Sair
          </Botao>
        </div>
      </header>
      <main className="layout-simples__conteudo">
        <Outlet />
      </main>
      <Rodape />
    </div>
  );
}
