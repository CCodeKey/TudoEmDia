import { useState } from 'react';
import { Link } from 'react-router';
import { Bell, Plus, Tag, Trash2 } from 'lucide-react';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { CampoTexto, Interruptor } from '../components/ui/Campos';
import { Carregando } from '../components/ui/Carregando';
import { Confirmacao } from '../components/ui/Confirmacao';
import { Aviso, ErroCarregamento } from '../components/ui/Estados';
import { Selo } from '../components/ui/Selo';
import { useAcao } from '../hooks/useAcao';
import { useCarregar } from '../hooks/useCarregar';
import { useCategorias } from '../hooks/useCategorias';
import { useEmpresa } from '../hooks/useEmpresa';
import { alertaService, categoriaService } from '../services/configuracaoService';
import { PRAZOS_ALERTA } from '../utils/rotulos';

// RF11: categorias padrão e da empresa; categoria em uso não pode ser excluída (RN41, RN42)
function Categorias() {
  const { empresa, pode } = useEmpresa();
  const { categorias, recarregar } = useCategorias(empresa.id);
  const [nome, setNome] = useState('');
  const [excluindo, setExcluindo] = useState(null);
  const criacao = useAcao();
  const exclusao = useAcao();
  const podeGerenciar = pode('GERENCIAR_CATEGORIAS');

  const criar = async (evento) => {
    evento.preventDefault();
    if (!nome.trim()) return;
    const resultado = await criacao.executar(() => categoriaService.criar(empresa.id, nome.trim()), {
      sucesso: 'Categoria criada.',
    });
    if (resultado.ok) {
      setNome('');
      recarregar();
    }
  };

  const excluir = async () => {
    const resultado = await exclusao.executar(() => categoriaService.excluir(empresa.id, excluindo.id), {
      sucesso: 'Categoria excluída.',
    });
    setExcluindo(null);
    if (resultado.ok) recarregar();
  };

  return (
    <section className="cartao">
      <div className="cartao__cabecalho">
        <div>
          <h2 className="linha">
            <Tag size={18} aria-hidden /> Categorias
          </h2>
          <p className="texto-fraco">Organizam obrigações e documentos. As categorias padrão não podem ser removidas.</p>
        </div>
      </div>
      <div className="cartao__corpo pilha">
        {podeGerenciar && (
          <form className="linha" style={{ alignItems: 'flex-end' }} onSubmit={criar}>
            <div className="preenche">
              <CampoTexto
                rotulo="Nova categoria"
                pequeno
                value={nome}
                onChange={(evento) => setNome(evento.target.value)}
                placeholder="Ex.: Marketing"
                maxLength={100}
              />
            </div>
            <Botao type="submit" icone={Plus} carregando={criacao.enviando} disabled={!nome.trim()} style={{ height: 34 }}>
              Adicionar
            </Botao>
          </form>
        )}
        <ul className="lista-arquivos">
          {categorias.map((categoria) => (
            <li key={categoria.id}>
              <span className="lista-arquivos__nome">
                <strong>{categoria.nome}</strong>
              </span>
              {categoria.padrao ? (
                <Selo tom="neutro" semPonto>
                  Padrão
                </Selo>
              ) : (
                podeGerenciar && (
                  <Botao variante="fantasma" pequeno somenteIcone icone={Trash2} onClick={() => setExcluindo(categoria)}>
                    Excluir {categoria.nome}
                  </Botao>
                )
              )}
            </li>
          ))}
        </ul>
      </div>

      <Confirmacao
        aberto={Boolean(excluindo)}
        aoFechar={() => setExcluindo(null)}
        aoConfirmar={excluir}
        titulo="Excluir categoria?"
        mensagem={excluindo ? `A categoria "${excluindo.nome}" será removida. Só é possível excluir categorias sem uso.` : ''}
        textoConfirmar="Excluir"
        enviando={exclusao.enviando}
      />
    </section>
  );
}

// RF20: prazos dos alertas por e-mail; pelo menos um ativo (RN74, RN76)
function Alertas() {
  const { empresa } = useEmpresa();
  const { dados, carregando, erro, recarregar } = useCarregar(() => alertaService.consultar(empresa.id), [empresa.id]);
  const [alterados, setAlterados] = useState({});
  const { executar, enviando } = useAcao();

  if (carregando) return <Carregando />;
  if (erro) return <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />;

  const prazos = dados.map((prazo) => ({ ...prazo, ativo: alterados[prazo.diasAntecedencia] ?? prazo.ativo }));
  const nenhumAtivo = prazos.every((prazo) => !prazo.ativo);
  const houveMudanca = Object.keys(alterados).length > 0;

  const salvar = async () => {
    const resultado = await executar(() => alertaService.configurar(empresa.id, prazos), { sucesso: 'Alertas salvos.' });
    if (resultado.ok) {
      setAlterados({});
      recarregar();
    }
  };

  return (
    <section className="cartao">
      <div className="cartao__cabecalho">
        <div>
          <h2 className="linha">
            <Bell size={18} aria-hidden /> Alertas por e-mail
          </h2>
          <p className="texto-fraco">Enviados às 8h para obrigações pendentes. Cada alerta é enviado uma única vez.</p>
        </div>
      </div>
      <div className="cartao__corpo pilha">
        <ul className="lista-arquivos">
          {prazos.map((prazo) => (
            <li key={prazo.diasAntecedencia}>
              <label className="lista-arquivos__nome" htmlFor={`prazo-${prazo.diasAntecedencia}`} style={{ cursor: 'pointer' }}>
                <strong>{PRAZOS_ALERTA[prazo.diasAntecedencia]}</strong>
              </label>
              <Interruptor
                id={`prazo-${prazo.diasAntecedencia}`}
                rotulo={PRAZOS_ALERTA[prazo.diasAntecedencia]}
                checked={prazo.ativo}
                onChange={(evento) => setAlterados({ ...alterados, [prazo.diasAntecedencia]: evento.target.checked })}
              />
            </li>
          ))}
        </ul>
        {nenhumAtivo && <Aviso tom="aviso">Mantenha pelo menos um prazo ativo.</Aviso>}
        <p className="texto-fraco">
          Quem recebe os alertas é definido em <Link to={`/empresas/${empresa.id}/equipe`}>Equipe</Link>.
        </p>
      </div>
      <div className="cartao__rodape">
        <Botao variante="primario" onClick={salvar} carregando={enviando} disabled={!houveMudanca || nenhumAtivo}>
          Salvar alertas
        </Botao>
      </div>
    </section>
  );
}

export function Configuracoes() {
  const { pode } = useEmpresa();
  return (
    <div className="pagina">
      <CabecalhoPagina titulo="Configurações" descricao="Categorias e alertas desta empresa." />
      <div className="grade-painel" style={{ alignItems: 'start' }}>
        <Categorias />
        {pode('CONFIGURAR_ALERTAS') && <Alertas />}
      </div>
    </div>
  );
}
