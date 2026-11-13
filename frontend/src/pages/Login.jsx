import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router';
import { Botao } from '../components/ui/Botao';
import { CampoTexto } from '../components/ui/Campos';
import { Aviso } from '../components/ui/Estados';
import { useAuth } from '../hooks/useAuth';
import { destinoAposLogin } from '../utils/navegacao';

export function Login() {
  const { entrar } = useAuth();
  const navegar = useNavigate();
  const local = useLocation();
  const [email, setEmail] = useState(local.state?.email ?? '');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    document.title = 'Entrar · TudoEmDia';
  }, []);

  async function enviar(evento) {
    evento.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await entrar(email, senha);
      navegar(destinoAposLogin(local), { replace: true });
    } catch (falha) {
      // A mensagem não diz se o erro está no e-mail ou na senha (RN08)
      setErro(falha.message);
      setEnviando(false);
    }
  }

  return (
    <>
      <h2>Entrar</h2>
      <p>Bem-vindo de volta. Acesse com seu e-mail e senha.</p>
      <form className="formulario" onSubmit={enviar}>
        {erro && <Aviso tom="perigo">{erro}</Aviso>}
        <CampoTexto
          rotulo="E-mail"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(evento) => setEmail(evento.target.value)}
          required
        />
        <CampoTexto
          rotulo="Senha"
          type="password"
          autoComplete="current-password"
          value={senha}
          onChange={(evento) => setSenha(evento.target.value)}
          required
        />
        <Botao type="submit" variante="primario" bloco carregando={enviando}>
          Entrar
        </Botao>
      </form>
      <p className="layout-auth__alternativa">
        Ainda não tem conta?{' '}
        <Link to="/cadastro" state={local.state}>
          Criar conta
        </Link>
      </p>
    </>
  );
}
