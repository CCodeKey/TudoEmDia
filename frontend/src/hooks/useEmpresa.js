import { useContext } from 'react';
import { EmpresaContexto } from '../contexts/contextos';

export function useEmpresa() {
  return useContext(EmpresaContexto);
}
