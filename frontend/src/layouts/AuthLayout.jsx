import { Outlet } from 'react-router';
import { BellRing, CalendarCheck, FileLock2, UsersRound } from 'lucide-react';
import { Marca } from '../components/Marca';
import { Rodape } from '../components/Rodape';

const BENEFICIOS = [
  { icone: CalendarCheck, texto: 'Contas, impostos e boletos com vencimento sob controle' },
  { icone: BellRing, texto: 'Alertas por e-mail antes de cada vencimento' },
  { icone: FileLock2, texto: 'Alvarás, contratos e certidões em um só lugar' },
  { icone: UsersRound, texto: 'Equipe e contador com o acesso certo' },
];

export function AuthLayout() {
  return (
    <div className="layout-auth">
      <aside className="layout-auth__vitrine">
        <Marca clara />
        <div>
          <h1>As obrigações da sua empresa, todas em dia.</h1>
          <p>Organize contas a pagar, pagamentos e documentos, e receba lembretes antes de cada prazo.</p>
        </div>
        <ul className="beneficios">
          {BENEFICIOS.map(({ icone: Icone, texto }) => (
            <li key={texto}>
              <span>
                <Icone size={18} aria-hidden />
              </span>
              {texto}
            </li>
          ))}
        </ul>
      </aside>
      <main className="layout-auth__formulario">
        <div className="layout-auth__miolo">
          <div className="marca-movel">
            <Marca />
          </div>
          <Outlet />
        </div>
        <Rodape />
      </main>
    </div>
  );
}
