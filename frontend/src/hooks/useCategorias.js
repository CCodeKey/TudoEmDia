import { categoriaService } from '../services/configuracaoService';
import { useCarregar } from './useCarregar';

export function useCategorias(empresaId) {
  const { dados, recarregar } = useCarregar(() => categoriaService.listar(empresaId), [empresaId]);
  return { categorias: dados ?? [], recarregar };
}
