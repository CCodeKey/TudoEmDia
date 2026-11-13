import { Navigate, Route, Routes } from 'react-router';
import { EmpresaProvider } from '../contexts/EmpresaProvider';
import { AppLayout } from '../layouts/AppLayout';
import { AuthLayout } from '../layouts/AuthLayout';
import { LayoutSimples } from '../layouts/LayoutSimples';
import { Cadastro } from '../pages/Cadastro';
import { Configuracoes } from '../pages/Configuracoes';
import { Conta } from '../pages/Conta';
import { Convite } from '../pages/Convite';
import { Documentos } from '../pages/Documentos';
import { Empresas } from '../pages/Empresas';
import { Equipe } from '../pages/Equipe';
import { Institucional } from '../pages/Institucional';
import { Login } from '../pages/Login';
import { NaoEncontrada } from '../pages/NaoEncontrada';
import { ObrigacaoDetalhe } from '../pages/ObrigacaoDetalhe';
import { Obrigacoes } from '../pages/Obrigacoes';
import { Pagamentos } from '../pages/Pagamentos';
import { Painel } from '../pages/Painel';
import { Recorrencias } from '../pages/Recorrencias';
import { ComPermissao, PaginaInicial, RotaPrivada, SoVisitante } from './Protecoes';

export function AppRoutes() {
  return (
    <Routes>
      {/* Públicas */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<SoVisitante><Login /></SoVisitante>} />
        <Route path="/cadastro" element={<SoVisitante><Cadastro /></SoVisitante>} />
        <Route path="/convites/:token" element={<Convite />} />
      </Route>
      <Route path="/" element={<PaginaInicial />} />
      <Route path="/privacidade" element={<Institucional tipo="privacidade" />} />
      <Route path="/termos" element={<Institucional tipo="termos" />} />

      {/* Exigem login */}
      <Route element={<RotaPrivada />}>
        <Route element={<LayoutSimples />}>
          <Route path="/empresas" element={<Empresas />} />
          <Route path="/conta" element={<Conta />} />
        </Route>

        {/* Dados de uma empresa: a URL define a empresa selecionada */}
        <Route
          path="/empresas/:empresaId"
          element={
            <EmpresaProvider>
              <AppLayout />
            </EmpresaProvider>
          }
        >
          <Route index element={<Navigate to="painel" replace />} />
          <Route path="painel" element={<ComPermissao permissao="CONSULTAR_PAINEL"><Painel /></ComPermissao>} />
          <Route path="obrigacoes" element={<ComPermissao permissao="CONSULTAR_OBRIGACOES"><Obrigacoes /></ComPermissao>} />
          <Route
            path="obrigacoes/:obrigacaoId"
            element={<ComPermissao permissao="CONSULTAR_OBRIGACOES"><ObrigacaoDetalhe /></ComPermissao>}
          />
          <Route path="recorrencias" element={<ComPermissao permissao="CONSULTAR_OBRIGACOES"><Recorrencias /></ComPermissao>} />
          <Route
            path="pagamentos"
            element={<ComPermissao permissao="CONSULTAR_HISTORICO_PAGAMENTOS"><Pagamentos /></ComPermissao>}
          />
          <Route path="documentos" element={<ComPermissao permissao="CONSULTAR_DOCUMENTOS"><Documentos /></ComPermissao>} />
          <Route path="equipe" element={<ComPermissao permissao="CONSULTAR_USUARIOS"><Equipe /></ComPermissao>} />
          <Route path="configuracoes" element={<Configuracoes />} />
        </Route>
      </Route>

      <Route path="*" element={<NaoEncontrada />} />
    </Routes>
  );
}
