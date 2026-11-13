import { useContext } from 'react';
import { NotificacaoContexto } from '../contexts/contextos';

export function useNotificacao() {
  return useContext(NotificacaoContexto);
}
