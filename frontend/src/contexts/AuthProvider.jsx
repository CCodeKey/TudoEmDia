import { useCallback, useEffect, useMemo, useState } from 'react';
import { definirAoExpirarSessao } from '../services/api';
import { authService } from '../services/authService';
import { AuthContexto } from './contextos';

// Usuário logado; a sessão fica no cookie gerenciado pelo back-end
export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    let ativo = true;
    (async () => {
      try {
        await authService.prepararCsrf();
      } catch {
        // Se falhar, a próxima requisição mostra o erro
      }
      try {
        const atual = await authService.usuarioLogado();
        if (ativo) setUsuario(atual);
      } catch {
        if (ativo) setUsuario(null);
      } finally {
        if (ativo) setCarregando(false);
      }
    })();
    return () => {
      ativo = false;
    };
  }, []);

  // Sessão expirada (RN07): volta para o login
  useEffect(() => {
    definirAoExpirarSessao(() => setUsuario(null));
    return () => definirAoExpirarSessao(null);
  }, []);

  const entrar = useCallback(async (email, senha) => {
    const logado = await authService.entrar(email, senha);
    setUsuario(logado);
    return logado;
  }, []);

  const cadastrar = useCallback(
    async (dados) => {
      await authService.cadastrar(dados);
      return entrar(dados.email, dados.senha);
    },
    [entrar],
  );

  const sair = useCallback(async () => {
    try {
      await authService.sair();
    } finally {
      setUsuario(null);
    }
  }, []);

  const valor = useMemo(
    () => ({ usuario, carregando, entrar, cadastrar, sair, encerrarLocalmente: () => setUsuario(null) }),
    [usuario, carregando, entrar, cadastrar, sair],
  );

  return <AuthContexto.Provider value={valor}>{children}</AuthContexto.Provider>;
}
