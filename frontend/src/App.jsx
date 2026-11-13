import { BrowserRouter } from 'react-router';
import { AuthProvider } from './contexts/AuthProvider';
import { NotificacaoProvider } from './contexts/NotificacaoProvider';
import { AppRoutes } from './routes/AppRoutes';

export default function App() {
  return (
    <BrowserRouter>
      <NotificacaoProvider>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </NotificacaoProvider>
    </BrowserRouter>
  );
}
