import { Link } from 'react-router';
import { Compass } from 'lucide-react';
import { Vazio } from '../components/ui/Estados';

export function NaoEncontrada() {
  return (
    <div className="carregando--tela" style={{ display: 'grid', placeItems: 'center' }}>
      <Vazio
        icone={Compass}
        titulo="Página não encontrada"
        descricao="O endereço pode estar incorreto ou a página não existe mais."
        acao={
          <Link to="/" className="botao botao--primario">
            Ir para o início
          </Link>
        }
      />
    </div>
  );
}
