import { useState } from 'react';
import { CircleCheck, Undo2 } from 'lucide-react';
import { Botao } from '../ui/Botao';
import { CampoTexto, CampoValor } from '../ui/Campos';
import { Confirmacao } from '../ui/Confirmacao';
import { Aviso } from '../ui/Estados';
import { Selo } from '../ui/Selo';
import { useAcao } from '../../hooks/useAcao';
import { useEmpresa } from '../../hooks/useEmpresa';
import { obrigacaoService, pagamentoService } from '../../services/obrigacaoService';
import { formatarData, formatarDataHora, formatarMoeda, hojeIso } from '../../utils/formato';

// Registro (RF18, RN66 a RN69) e cancelamento (RN70) do pagamento de uma obrigação
export function PainelPagamento({ obrigacao, pagamento, aoAlterar }) {
  const { empresa, pode } = useEmpresa();
  const [dados, setDados] = useState({ dataPagamento: hojeIso(), valorPago: obrigacao.valor });
  const [cancelando, setCancelando] = useState(false);
  const { executar, enviando, erro } = useAcao();

  const registrar = async (evento) => {
    evento.preventDefault();
    const resultado = await executar(
      () =>
        obrigacaoService.registrarPagamento(empresa.id, obrigacao.id, {
          dataPagamento: dados.dataPagamento,
          valorPago: dados.valorPago ? Number(dados.valorPago) : null,
        }),
      { sucesso: 'Pagamento registrado.', mostrarErro: false },
    );
    if (resultado.ok) aoAlterar();
  };

  const cancelar = async () => {
    const resultado = await executar(() => pagamentoService.cancelar(empresa.id, pagamento.id), {
      sucesso: 'Pagamento cancelado. O registro continua no histórico.',
    });
    if (resultado.ok) {
      setCancelando(false);
      aoAlterar();
    }
  };

  return (
    <section className="cartao">
      <div className="cartao__cabecalho">
        <h2>Pagamento</h2>
        {pagamento && (
          <Selo tom="sucesso">{pagamento.pagoComAtraso ? 'Pago com atraso' : 'Pago em dia'}</Selo>
        )}
      </div>
      <div className="cartao__corpo">
        {pagamento ? (
          <div className="pilha">
            <dl className="lista-dados lista-dados--colunas">
              <div>
                <dt>Data do pagamento</dt>
                <dd>{formatarData(pagamento.dataPagamento)}</dd>
              </div>
              <div>
                <dt>Valor pago</dt>
                <dd className="numero">{formatarMoeda(pagamento.valorPago)}</dd>
              </div>
              <div>
                <dt>Registrado por</dt>
                <dd>{pagamento.registradoPor}</dd>
              </div>
              <div>
                <dt>Registrado em</dt>
                <dd>{formatarDataHora(pagamento.dataRegistro)}</dd>
              </div>
            </dl>
            {pode('CANCELAR_PAGAMENTOS') && (
              <div>
                <Botao variante="perigo-suave" pequeno icone={Undo2} onClick={() => setCancelando(true)}>
                  Cancelar pagamento
                </Botao>
              </div>
            )}
          </div>
        ) : pode('REGISTRAR_PAGAMENTOS') ? (
          <form className="formulario" onSubmit={registrar} noValidate>
            {erro && <Aviso tom="perigo">{erro.message}</Aviso>}
            <div className="formulario__linha">
              <CampoTexto
                rotulo="Data do pagamento"
                type="date"
                max={hojeIso()}
                value={dados.dataPagamento}
                onChange={(evento) => setDados({ ...dados, dataPagamento: evento.target.value })}
                erro={erro?.campos?.dataPagamento}
              />
              <CampoValor
                rotulo="Valor pago (R$)"
                value={dados.valorPago}
                onChange={(evento) => setDados({ ...dados, valorPago: evento.target.value })}
                erro={erro?.campos?.valorPago}
                ajuda="Altere se houve juros ou multa."
              />
            </div>
            <div>
              <Botao type="submit" variante="primario" icone={CircleCheck} carregando={enviando}>
                Registrar pagamento
              </Botao>
            </div>
          </form>
        ) : (
          <p className="texto-secundario">Ainda não há pagamento registrado.</p>
        )}
      </div>

      <Confirmacao
        aberto={cancelando}
        aoFechar={() => setCancelando(false)}
        aoConfirmar={cancelar}
        titulo="Cancelar pagamento?"
        mensagem="Use quando o pagamento foi registrado por engano. A obrigação volta a ficar pendente ou vencida, e o cancelamento fica registrado no histórico."
        textoConfirmar="Cancelar pagamento"
        enviando={enviando}
      />
    </section>
  );
}
