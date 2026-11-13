// Página que a pessoa tentava abrir antes de entrar, ou o início
export function destinoAposLogin(local) {
  const de = local.state?.de;
  return de?.pathname ? `${de.pathname}${de.search ?? ''}` : '/';
}
