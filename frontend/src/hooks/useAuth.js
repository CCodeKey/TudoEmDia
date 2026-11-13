import { useContext } from 'react';
import { AuthContexto } from '../contexts/contextos';

export function useAuth() {
  return useContext(AuthContexto);
}
