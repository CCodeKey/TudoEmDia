import { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useParams } from 'react-router';
import { Carregando } from '../components/ui/Carregando';
import { useNotificacao } from '../hooks/useNotificacao';
import { empresaService } from '../services/empresaService';
import { salvarUltimaEmpresa } from '../utils/armazenamento';
import { EmpresaContexto } from './contextos';

// Empresa selecionada (vem da URL), com o perfil e as permissões do usuário nela (RN22, RN24)
export function EmpresaProvider({ children }) {
  const { empresaId } = useParams();
  const notificacao = useNotificacao();
  const [estado, setEstado] = useState({ empresaId: null, empresa: null, permissoes: [], empresas: [], semAcesso: false });

  const carregarEmpresas = useCallback(async () => {
    const empresas = await empresaService.listar();
    setEstado((atual) => ({ ...atual, empresas }));
  }, []);

  useEffect(() => {
    let ativo = true;
    Promise.all([empresaService.detalhar(empresaId), empresaService.permissoes(empresaId), empresaService.listar()])
      .then(([empresa, permissoes, empresas]) => {
        if (!ativo) return;
        salvarUltimaEmpresa(empresaId);
        setEstado({ empresaId, empresa, permissoes: permissoes.permissoes, empresas, semAcesso: false });
      })
      .catch((erro) => {
        if (!ativo) return;
        if (erro.status !== 401) notificacao.erro(erro);
        setEstado((atual) => ({ ...atual, empresaId, semAcesso: true }));
      });
    return () => {
      ativo = false;
    };
  }, [empresaId, notificacao]);

  const valor = useMemo(
    () => ({
      empresa: estado.empresa,
      empresas: estado.empresas,
      permissoes: estado.permissoes,
      pode: (permissao) => estado.permissoes.includes(permissao),
      recarregarEmpresas: carregarEmpresas,
    }),
    [estado, carregarEmpresas],
  );

  if (estado.semAcesso && estado.empresaId === empresaId) return <Navigate to="/empresas" replace />;
  if (!estado.empresa || estado.empresaId !== empresaId) return <Carregando tela />;

  return <EmpresaContexto.Provider value={valor}>{children}</EmpresaContexto.Provider>;
}
