import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router';
import {
  FileText,
  LayoutDashboard,
  LogOut,
  Menu,
  ReceiptText,
  Repeat,
  Settings,
  UserRound,
  Users,
  Wallet,
} from 'lucide-react';
import { Marca } from '../components/Marca';
import { Rodape } from '../components/Rodape';
import { useAuth } from '../hooks/useAuth';
import { useEmpresa } from '../hooks/useEmpresa';
import { formatarCnpj, iniciais } from '../utils/formato';
import { PERFIS } from '../utils/rotulos';

// Só aparecem no menu as funcionalidades permitidas ao perfil (RN24)
const ITENS_MENU = [
  { para: 'painel', rotulo: 'Painel', icone: LayoutDashboard, permissao: 'CONSULTAR_PAINEL' },
  { para: 'obrigacoes', rotulo: 'Obrigações', icone: ReceiptText, permissao: 'CONSULTAR_OBRIGACOES' },
  { para: 'recorrencias', rotulo: 'Recorrências', icone: Repeat, permissao: 'CONSULTAR_OBRIGACOES' },
  { para: 'pagamentos', rotulo: 'Pagamentos', icone: Wallet, permissao: 'CONSULTAR_HISTORICO_PAGAMENTOS' },
  { para: 'documentos', rotulo: 'Documentos', icone: FileText, permissao: 'CONSULTAR_DOCUMENTOS' },
  { para: 'equipe', rotulo: 'Equipe', icone: Users, permissao: 'CONSULTAR_USUARIOS' },
  { para: 'configuracoes', rotulo: 'Configurações', icone: Settings },
];

export function AppLayout() {
  const { usuario, sair } = useAuth();
  const { empresa, empresas, pode } = useEmpresa();
  const navegar = useNavigate();
  const local = useLocation();
  const [menuAberto, setMenuAberto] = useState(false);

  // Fecha o menu do celular ao trocar de página
  useEffect(() => {
    setMenuAberto(false);
  }, [local.pathname]);

  const trocarEmpresa = (evento) => {
    const valor = evento.target.value;
    navegar(valor === 'gerenciar' ? '/empresas' : `/empresas/${valor}/painel`);
  };

  return (
    <div className="layout-app">
      <div className={`fundo-menu ${menuAberto ? 'fundo-menu--visivel' : ''}`} onClick={() => setMenuAberto(false)} aria-hidden />

      <aside className={`barra-lateral ${menuAberto ? 'barra-lateral--aberta' : ''}`} aria-label="Menu principal">
        <div className="barra-lateral__marca">
          <Marca />
        </div>

        <div className="seletor-empresa">
          <label className="seletor-empresa__rotulo" htmlFor="seletor-empresa">
            Empresa
          </label>
          <select id="seletor-empresa" className="entrada" value={empresa.id} onChange={trocarEmpresa}>
            {empresas.map((item) => (
              <option key={item.id} value={item.id}>
                {item.nome}
              </option>
            ))}
            <option value="gerenciar">+ Outra empresa…</option>
          </select>
          <p className="texto-fraco" style={{ margin: '6px 6px 0' }}>
            {PERFIS[empresa.perfil]} · {formatarCnpj(empresa.cnpj)}
          </p>
        </div>

        <nav className="navegacao">
          {ITENS_MENU.filter((item) => !item.permissao || pode(item.permissao)).map(({ para, rotulo, icone: Icone }) => (
            <NavLink key={para} to={para} className="navegacao__item">
              <Icone size={18} aria-hidden />
              {rotulo}
            </NavLink>
          ))}
        </nav>

        <div className="barra-lateral__rodape">
          <div className="usuario-resumo">
            <span className="avatar" aria-hidden>
              {iniciais(usuario.nome)}
            </span>
            <div className="usuario-resumo__texto">
              <strong>{usuario.nome}</strong>
              <span>{usuario.email}</span>
            </div>
          </div>
          <NavLink to="/conta" className="navegacao__item">
            <UserRound size={18} aria-hidden />
            Minha conta
          </NavLink>
          <button type="button" className="navegacao__item" style={{ background: 'none', border: 0, cursor: 'pointer' }} onClick={sair}>
            <LogOut size={18} aria-hidden />
            Sair
          </button>
        </div>
      </aside>

      <div>
        <header className="topo-movel">
          <button
            type="button"
            className="botao botao--fantasma botao--icone"
            onClick={() => setMenuAberto(true)}
            aria-label="Abrir menu"
            aria-expanded={menuAberto}
          >
            <Menu size={20} />
          </button>
          <Marca />
        </header>
        <main className="conteudo">
          <div className="conteudo__interno">
            <Outlet />
          </div>
        </main>
        <Rodape />
      </div>
    </div>
  );
}
