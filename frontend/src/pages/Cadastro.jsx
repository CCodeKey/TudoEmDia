import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router';
import { Botao } from '../components/ui/Botao';
import { CaixaSelecao, CampoTexto } from '../components/ui/Campos';
import { Aviso } from '../components/ui/Estados';
import { useAuth } from '../hooks/useAuth';
import { destinoAposLogin } from '../utils/navegacao';

// Mesma regra do servidor (RN03): pelo menos 8 caracteres, com letra e número
function validarSenha(senha) {
  if (senha.length < 8) return 'A senha deve ter pelo menos 8 caracteres.';
  if (!/[A-Za-z]/.test(senha) || !/\d/.test(senha)) return 'A senha deve ter pelo menos uma letra e um número.';
  return null;
}

export function Cadastro() {
  const { cadastrar } = useAuth();
  const navegar = useNavigate();
  const local = useLocation();
  const [dados, setDados] = useState({ nome: '', email: local.state?.email ?? '', senha: '', aceiteTermos: false });
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    document.title = 'Criar conta · TudoEmDia';
  }, []);

  const alterar = (campo) => (evento) => {
    const valor = evento.target.type === 'checkbox' ? evento.target.checked : evento.target.value;
    setDados((atual) => ({ ...atual, [campo]: valor }));
    setErros((atual) => ({ ...atual, [campo]: undefined }));
  };

  async function enviar(evento) {
    evento.preventDefault();
    const locais = {};
    if (!dados.nome.trim()) locais.nome = 'Informe o nome.';
    if (!dados.email.trim()) locais.email = 'Informe o e-mail.';
    const erroSenha = validarSenha(dados.senha);
    if (erroSenha) locais.senha = erroSenha;
    if (!dados.aceiteTermos) locais.aceiteTermos = 'É necessário aceitar os termos para continuar.';
    setErros(locais);
    setErroGeral(null);
    if (Object.keys(locais).length) return;

    setEnviando(true);
    try {
      await cadastrar(dados);
      navegar(destinoAposLogin(local), { replace: true });
    } catch (falha) {
      setErros(falha.campos ?? {});
      setErroGeral(falha.message);
      setEnviando(false);
    }
  }

  return (
    <>
      <h2>Criar conta</h2>
      <p>Comece a organizar as obrigações da sua empresa.</p>
      <form className="formulario" onSubmit={enviar} noValidate>
        {erroGeral && <Aviso tom="perigo">{erroGeral}</Aviso>}
        <CampoTexto rotulo="Nome" autoComplete="name" value={dados.nome} onChange={alterar('nome')} erro={erros.nome} />
        <CampoTexto
          rotulo="E-mail"
          type="email"
          autoComplete="email"
          value={dados.email}
          onChange={alterar('email')}
          erro={erros.email}
        />
        <CampoTexto
          rotulo="Senha"
          type="password"
          autoComplete="new-password"
          value={dados.senha}
          onChange={alterar('senha')}
          erro={erros.senha}
          ajuda="Pelo menos 8 caracteres, com letras e números."
        />
        <div className="pilha pilha--p">
          <CaixaSelecao checked={dados.aceiteTermos} onChange={alterar('aceiteTermos')}>
            Li e aceito os{' '}
            <Link to="/termos" target="_blank">
              termos de uso
            </Link>{' '}
            e a{' '}
            <Link to="/privacidade" target="_blank">
              política de privacidade
            </Link>
            .
          </CaixaSelecao>
          {erros.aceiteTermos && <span className="campo__erro">{erros.aceiteTermos}</span>}
        </div>
        <Botao type="submit" variante="primario" bloco carregando={enviando}>
          Criar conta
        </Botao>
      </form>
      <p className="layout-auth__alternativa">
        Já tem conta?{' '}
        <Link to="/login" state={local.state}>
          Entrar
        </Link>
      </p>
    </>
  );
}
