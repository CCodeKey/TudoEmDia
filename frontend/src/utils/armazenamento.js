// Preferências locais do navegador; podem não estar disponíveis (modo privado, bloqueio)
const ULTIMA_EMPRESA = 'tudoemdia:ultima-empresa';

export function lerUltimaEmpresa() {
  try {
    return localStorage.getItem(ULTIMA_EMPRESA);
  } catch {
    return null;
  }
}

export function salvarUltimaEmpresa(empresaId) {
  try {
    localStorage.setItem(ULTIMA_EMPRESA, empresaId);
  } catch {
    // Sem armazenamento local, o sistema só não lembra a última empresa
  }
}
