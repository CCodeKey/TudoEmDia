import { createContext } from 'react';

// Contextos compartilhados; os provedores ficam nos arquivos .jsx e os hooks em src/hooks
export const AuthContexto = createContext(null);
export const EmpresaContexto = createContext(null);
export const NotificacaoContexto = createContext(null);
