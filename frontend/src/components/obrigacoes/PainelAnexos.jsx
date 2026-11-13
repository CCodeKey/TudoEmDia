import { useRef, useState } from 'react';
import { Download, Paperclip, Trash2, Upload } from 'lucide-react';
import { Botao } from '../ui/Botao';
import { Confirmacao } from '../ui/Confirmacao';
import { useAcao } from '../../hooks/useAcao';
import { useEmpresa } from '../../hooks/useEmpresa';
import { useNotificacao } from '../../hooks/useNotificacao';
import { obrigacaoService } from '../../services/obrigacaoService';
import { TIPOS_ACEITOS, salvarArquivo, validarArquivo } from '../../utils/arquivos';
import { formatarDataHora } from '../../utils/formato';

// Boletos e guias anexados à obrigação (RN28); download só pela API (RN53)
export function PainelAnexos({ obrigacaoId, anexos, aoAlterar }) {
  const { empresa, pode } = useEmpresa();
  const notificacao = useNotificacao();
  const seletor = useRef(null);
  const [removendo, setRemovendo] = useState(null);
  const { executar, enviando } = useAcao();
  const podeEditar = pode('EDITAR_OBRIGACOES');

  const enviar = async (arquivo) => {
    seletor.current.value = '';
    const problema = validarArquivo(arquivo);
    if (problema) {
      notificacao.erro(problema);
      return;
    }
    const resultado = await executar(() => obrigacaoService.enviarAnexo(empresa.id, obrigacaoId, arquivo), {
      sucesso: 'Anexo enviado.',
    });
    if (resultado.ok) aoAlterar();
  };

  const baixar = (anexo) => executar(async () => salvarArquivo(await obrigacaoService.baixarAnexo(empresa.id, anexo.id)));

  const remover = async () => {
    const resultado = await executar(() => obrigacaoService.removerAnexo(empresa.id, removendo.id), {
      sucesso: 'Anexo removido.',
    });
    if (resultado.ok) {
      setRemovendo(null);
      aoAlterar();
    }
  };

  return (
    <section className="cartao">
      <div className="cartao__cabecalho">
        <h2>Anexos</h2>
        {podeEditar && (
          <>
            <input
              ref={seletor}
              type="file"
              accept={TIPOS_ACEITOS}
              hidden
              onChange={(evento) => evento.target.files[0] && enviar(evento.target.files[0])}
            />
            <Botao pequeno icone={Upload} carregando={enviando} onClick={() => seletor.current.click()}>
              Anexar
            </Botao>
          </>
        )}
      </div>
      <div className="cartao__corpo">
        {anexos.length ? (
          <ul className="lista-arquivos">
            {anexos.map((anexo) => (
              <li key={anexo.id}>
                <Paperclip size={16} color="var(--cor-texto-3)" aria-hidden />
                <span className="lista-arquivos__nome">
                  <strong>{anexo.nomeArquivo}</strong>
                  <span className="texto-fraco">Enviado em {formatarDataHora(anexo.dataEnvio)}</span>
                </span>
                <Botao variante="fantasma" pequeno somenteIcone icone={Download} onClick={() => baixar(anexo)}>
                  Baixar {anexo.nomeArquivo}
                </Botao>
                {podeEditar && (
                  <Botao variante="fantasma" pequeno somenteIcone icone={Trash2} onClick={() => setRemovendo(anexo)}>
                    Remover {anexo.nomeArquivo}
                  </Botao>
                )}
              </li>
            ))}
          </ul>
        ) : (
          <p className="texto-secundario">
            Nenhum anexo.{podeEditar && ' Anexe o boleto ou a guia (PDF, JPG ou PNG, até 10 MB).'}
          </p>
        )}
      </div>

      <Confirmacao
        aberto={Boolean(removendo)}
        aoFechar={() => setRemovendo(null)}
        aoConfirmar={remover}
        titulo="Remover anexo?"
        mensagem={removendo ? `O arquivo "${removendo.nomeArquivo}" será excluído.` : ''}
        textoConfirmar="Remover"
        enviando={enviando}
      />
    </section>
  );
}
