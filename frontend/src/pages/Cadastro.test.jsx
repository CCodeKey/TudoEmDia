import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { AuthContexto } from '../contexts/contextos';
import { ApiError } from '../services/api';
import { Cadastro } from './Cadastro';

function renderizar(cadastrar) {
  return render(
    <AuthContexto.Provider value={{ cadastrar }}>
      <MemoryRouter initialEntries={['/cadastro']}>
        <Routes>
          <Route path="/cadastro" element={<Cadastro />} />
          <Route path="/" element={<p>Página inicial</p>} />
        </Routes>
      </MemoryRouter>
    </AuthContexto.Provider>,
  );
}

async function preencher(usuario, { nome = 'Ana', email = 'ana@teste.com', senha = 'Padaria2026', aceitar = true } = {}) {
  if (nome) await usuario.type(screen.getByLabelText('Nome'), nome);
  if (email) await usuario.type(screen.getByLabelText('E-mail'), email);
  if (senha) await usuario.type(screen.getByLabelText('Senha', { exact: true }), senha);
  if (aceitar) await usuario.click(screen.getByRole('checkbox'));
  await usuario.click(screen.getByRole('button', { name: 'Criar conta' }));
}

describe('cadastro', () => {
  it('valida os campos sem chamar a API (RN02 a RN04)', async () => {
    const cadastrar = vi.fn();
    const usuario = userEvent.setup();
    renderizar(cadastrar);

    await preencher(usuario, { nome: '', senha: 'abc', aceitar: false });

    expect(screen.getByText('Informe o nome.')).toBeInTheDocument();
    expect(screen.getByText('A senha deve ter pelo menos 8 caracteres.')).toBeInTheDocument();
    expect(screen.getByText('É necessário aceitar os termos para continuar.')).toBeInTheDocument();
    expect(cadastrar).not.toHaveBeenCalled();
  });

  it('exige letra e número na senha', async () => {
    const cadastrar = vi.fn();
    const usuario = userEvent.setup();
    renderizar(cadastrar);

    await preencher(usuario, { senha: 'somenteletras' });

    expect(screen.getByText('A senha deve ter pelo menos uma letra e um número.')).toBeInTheDocument();
    expect(cadastrar).not.toHaveBeenCalled();
  });

  it('cadastra e entra no sistema', async () => {
    const cadastrar = vi.fn().mockResolvedValue({});
    const usuario = userEvent.setup();
    renderizar(cadastrar);

    await preencher(usuario);

    expect(cadastrar).toHaveBeenCalledWith({ nome: 'Ana', email: 'ana@teste.com', senha: 'Padaria2026', aceiteTermos: true });
    expect(await screen.findByText('Página inicial')).toBeInTheDocument();
  });

  it('mostra o erro do servidor, como e-mail já cadastrado (RN01)', async () => {
    const cadastrar = vi.fn().mockRejectedValue(new ApiError(409, 'Já existe uma conta com este e-mail.'));
    const usuario = userEvent.setup();
    renderizar(cadastrar);

    await preencher(usuario);

    expect(await screen.findByText('Já existe uma conta com este e-mail.')).toBeInTheDocument();
  });
});
