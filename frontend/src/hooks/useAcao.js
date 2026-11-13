import { useCallback, useState } from 'react';
import { useNotificacao } from './useNotificacao';

// Executa uma ação (salvar, excluir...) controlando o "enviando" e mostrando o resultado
export function useAcao() {
  const notificacao = useNotificacao();
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState(null);

  const executar = useCallback(
    async (acao, { sucesso, mostrarErro = true } = {}) => {
      setEnviando(true);
      setErro(null);
      try {
        const resultado = await acao();
        if (sucesso) notificacao.sucesso(sucesso);
        return { ok: true, resultado };
      } catch (falha) {
        setErro(falha);
        if (mostrarErro) notificacao.erro(falha);
        return { ok: false, erro: falha };
      } finally {
        setEnviando(false);
      }
    },
    [notificacao],
  );

  return { executar, enviando, erro, limparErro: () => setErro(null) };
}
