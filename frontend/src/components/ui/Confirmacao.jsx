import { Botao } from './Botao';
import { Modal } from './Modal';

// Pede confirmação antes de ações que não podem ser desfeitas (RN37, RN55)
export function Confirmacao({
  aberto,
  aoFechar,
  aoConfirmar,
  titulo,
  mensagem,
  textoConfirmar = 'Confirmar',
  perigo = true,
  enviando = false,
  children,
}) {
  return (
    <Modal
      aberto={aberto}
      aoFechar={aoFechar}
      titulo={titulo}
      aoEnviar={aoConfirmar}
      rodape={
        <>
          <Botao onClick={aoFechar} disabled={enviando}>
            Cancelar
          </Botao>
          <Botao type="submit" variante={perigo ? 'perigo' : 'primario'} carregando={enviando}>
            {textoConfirmar}
          </Botao>
        </>
      }
    >
      <div className="pilha">
        {mensagem && <p className="texto-secundario">{mensagem}</p>}
        {children}
      </div>
    </Modal>
  );
}
