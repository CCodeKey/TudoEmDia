import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EmpresaContexto } from '../../contexts/contextos';
import { ComPermissao } from '../../routes/Protecoes';
import { Botao } from './Botao';
import { CampoSenha } from './Campos';
import { ForcaSenha } from './ForcaSenha';
import { Modal } from './Modal';
import { SeloObrigacao } from './Selo';

describe('componentes', () => {
  it('botão carregando fica desabilitado', () => {
    render(<Botao carregando>Salvar</Botao>);
    expect(screen.getByRole('button', { name: 'Salvar' })).toBeDisabled();
  });

  it('olhinho mostra e esconde a senha', async () => {
    const usuario = userEvent.setup();
    render(<CampoSenha rotulo="Senha" defaultValue="Segredo123" />);
    const campo = screen.getByLabelText('Senha', { exact: true });
    expect(campo).toHaveAttribute('type', 'password');

    await usuario.click(screen.getByRole('button', { name: 'Mostrar senha' }));
    expect(campo).toHaveAttribute('type', 'text');

    await usuario.click(screen.getByRole('button', { name: 'Ocultar senha' }));
    expect(campo).toHaveAttribute('type', 'password');
  });

  it('medidor de força aparece só com senha digitada', () => {
    const { rerender } = render(<ForcaSenha senha="" />);
    expect(screen.queryByText(/Força da senha/)).not.toBeInTheDocument();

    rerender(<ForcaSenha senha="Padaria#2026" />);
    expect(screen.getByText('Forte')).toBeInTheDocument();
  });

  it('obrigação paga depois do vencimento aparece como paga com atraso (RN47)', () => {
    render(<SeloObrigacao situacao="PAGA" pagaComAtraso />);
    expect(screen.getByText('Paga com atraso')).toBeInTheDocument();
  });

  it('modal fecha com Esc e foca o primeiro campo', async () => {
    const usuario = userEvent.setup();
    const aoFechar = vi.fn();
    render(
      <Modal aberto aoFechar={aoFechar} titulo="Nova obrigação">
        <input aria-label="Descrição" />
      </Modal>,
    );
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByLabelText('Descrição')).toHaveFocus();

    await usuario.keyboard('{Escape}');
    expect(aoFechar).toHaveBeenCalled();
  });

  it('área sem permissão mostra acesso restrito (RN24)', () => {
    render(
      <EmpresaContexto.Provider value={{ pode: () => false }}>
        <ComPermissao permissao="CONSULTAR_USUARIOS">
          <p>Lista da equipe</p>
        </ComPermissao>
      </EmpresaContexto.Provider>,
    );
    expect(screen.getByText('Acesso restrito')).toBeInTheDocument();
    expect(screen.queryByText('Lista da equipe')).not.toBeInTheDocument();
  });
});
