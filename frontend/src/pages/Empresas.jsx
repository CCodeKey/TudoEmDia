import { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { Building2, ChevronRight, Plus } from 'lucide-react';
import { Botao } from '../components/ui/Botao';
import { CabecalhoPagina } from '../components/ui/CabecalhoPagina';
import { CampoTexto } from '../components/ui/Campos';
import { Carregando } from '../components/ui/Carregando';
import { Aviso, ErroCarregamento } from '../components/ui/Estados';
import { Modal } from '../components/ui/Modal';
import { useAcao } from '../hooks/useAcao';
import { useAuth } from '../hooks/useAuth';
import { useCarregar } from '../hooks/useCarregar';
import { empresaService } from '../services/empresaService';
import { formatarCnpj } from '../utils/formato';
import { PERFIS } from '../utils/rotulos';

function FormularioEmpresa({ aoCriar, rodape }) {
  const [dados, setDados] = useState({ nome: '', cnpj: '' });
  const { executar, enviando, erro } = useAcao();

  async function enviar(evento) {
    evento?.preventDefault();
    const resultado = await executar(() => empresaService.criar(dados), {
      sucesso: 'Empresa cadastrada.',
      mostrarErro: false,
    });
    if (resultado.ok) aoCriar(resultado.resultado);
  }

  return (
    <form className="formulario" onSubmit={enviar} noValidate>
      {erro && !Object.keys(erro.campos ?? {}).length && <Aviso tom="perigo">{erro.message}</Aviso>}
      <CampoTexto
        rotulo="Nome da empresa"
        value={dados.nome}
        onChange={(evento) => setDados({ ...dados, nome: evento.target.value })}
        erro={erro?.campos?.nome}
        autoComplete="organization"
      />
      <CampoTexto
        rotulo="CNPJ"
        value={dados.cnpj}
        onChange={(evento) => setDados({ ...dados, cnpj: evento.target.value })}
        erro={erro?.campos?.cnpj}
        placeholder="00.000.000/0000-00"
        ajuda="Aceita o formato numérico e o novo CNPJ alfanumérico."
      />
      {rodape(enviando)}
    </form>
  );
}

// Empresas do usuário (RN11) e cadastro de nova empresa (RF03)
export function Empresas() {
  const { usuario } = useAuth();
  const navegar = useNavigate();
  const { dados: empresas, carregando, erro, recarregar } = useCarregar(() => empresaService.listar(), []);
  const [modalAberto, setModalAberto] = useState(false);

  const abrirEmpresa = (empresa) => navegar(`/empresas/${empresa.id}/painel`);

  if (carregando) return <Carregando />;
  if (erro) return <ErroCarregamento erro={erro} aoTentarNovamente={recarregar} />;

  if (!empresas.length) {
    return (
      <div className="pagina" style={{ maxWidth: 520, margin: '0 auto' }}>
        <CabecalhoPagina
          titulo={`Boas-vindas, ${usuario.nome.split(' ')[0]}!`}
          descricao="Cadastre sua empresa para começar. Você será o proprietário e poderá convidar sua equipe e seu contador."
        />
        <div className="cartao">
          <div className="cartao__corpo">
            <FormularioEmpresa
              aoCriar={abrirEmpresa}
              rodape={(enviando) => (
                <Botao type="submit" variante="primario" bloco carregando={enviando}>
                  Cadastrar empresa
                </Botao>
              )}
            />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="pagina">
      <CabecalhoPagina
        titulo="Suas empresas"
        descricao="Escolha a empresa que deseja acompanhar."
        acoes={
          <Botao variante="primario" icone={Plus} onClick={() => setModalAberto(true)}>
            Nova empresa
          </Botao>
        }
      />
      <div className="grade-empresas">
        {empresas.map((empresa) => (
          <Link key={empresa.id} to={`/empresas/${empresa.id}/painel`} className="cartao cartao-empresa">
            <span className="cartao-empresa__icone">
              <Building2 size={20} aria-hidden />
            </span>
            <span className="cartao-empresa__texto">
              <strong>{empresa.nome}</strong>
              <span className="texto-fraco">
                {PERFIS[empresa.perfil]} · {formatarCnpj(empresa.cnpj)}
              </span>
            </span>
            <ChevronRight size={18} color="var(--cor-texto-3)" aria-hidden />
          </Link>
        ))}
      </div>

      <Modal
        aberto={modalAberto}
        aoFechar={() => setModalAberto(false)}
        titulo="Nova empresa"
        descricao="Você será o proprietário desta empresa."
      >
        <FormularioEmpresa
          aoCriar={abrirEmpresa}
          rodape={(enviando) => (
            <div className="linha" style={{ justifyContent: 'flex-end' }}>
              <Botao onClick={() => setModalAberto(false)}>Cancelar</Botao>
              <Botao type="submit" variante="primario" carregando={enviando}>
                Cadastrar
              </Botao>
            </div>
          )}
        />
      </Modal>
    </div>
  );
}
