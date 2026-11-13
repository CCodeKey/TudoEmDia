import { Navigate } from 'react-router';
import { Carregando } from '../components/ui/Carregando';
import { ErroCarregamento } from '../components/ui/Estados';
import { useCarregar } from '../hooks/useCarregar';
import { empresaService } from '../services/empresaService';
import { lerUltimaEmpresa } from '../utils/armazenamento';

// Abre a última empresa usada; sem nenhuma, leva ao cadastro de empresa
export function Inicio() {
  const { dados: empresas, carregando, erro, recarregar } = useCarregar(() => empresaService.listar(), []);

  if (carregando) return <Carregando tela />;
  if (erro) return <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />;
  if (!empresas.length) return <Navigate to="/empresas" replace />;

  const ultima = lerUltimaEmpresa();
  const destino = empresas.find((empresa) => empresa.id === ultima) ?? empresas[0];
  return <Navigate to={`/empresas/${destino.id}/painel`} replace />;
}
