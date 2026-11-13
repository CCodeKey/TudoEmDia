// Preferências locais do navegador; podem não estar disponíveis (modo privado, bloqueio)
const ULTIMA_EMPRESA = 'tudoemdia:ultima-empresa';
const PULOU_EMPRESA = 'tudoemdia:pulou-cadastro-empresa:';

function ler(chave) {
  try {
    return localStorage.getItem(chave);
  } catch {
    return null;
  }
}

function gravar(chave, valor) {
  try {
    localStorage.setItem(chave, valor);
  } catch {
    // Sem armazenamento local, o sistema só não lembra a preferência
  }
}

export function lerUltimaEmpresa() {
  return ler(ULTIMA_EMPRESA);
}

export function salvarUltimaEmpresa(empresaId) {
  gravar(ULTIMA_EMPRESA, empresaId);
}

// Quem pulou o cadastro da empresa não vê de novo a tela de boas-vindas
export function pulouCadastroEmpresa(usuarioId) {
  return ler(PULOU_EMPRESA + usuarioId) === 'sim';
}

export function registrarQuePulouCadastroEmpresa(usuarioId) {
  gravar(PULOU_EMPRESA + usuarioId, 'sim');
}
