import { useCallback, useEffect, useState } from 'react';

// Carrega dados da API e refaz a busca quando as dependências mudam
export function useCarregar(buscar, dependencias) {
  const [estado, setEstado] = useState({ dados: null, carregando: true, erro: null });
  const [versao, setVersao] = useState(0);

  useEffect(() => {
    let ativo = true;
    setEstado((atual) => ({ ...atual, carregando: true, erro: null }));
    buscar()
      .then((dados) => ativo && setEstado({ dados, carregando: false, erro: null }))
      .catch((erro) => ativo && setEstado({ dados: null, carregando: false, erro }));
    return () => {
      ativo = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependencias, versao]);

  const recarregar = useCallback(() => setVersao((atual) => atual + 1), []);
  return { ...estado, recarregar };
}
