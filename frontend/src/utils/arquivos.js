// Salva no computador um arquivo recebido da API (anexos, documentos e relatórios)
export function salvarArquivo({ blob, nome }) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = nome;
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export const TIPOS_ACEITOS = '.pdf,.jpg,.jpeg,.png';
export const TAMANHO_MAXIMO = 10 * 1024 * 1024;

// Mesma regra do back-end (RN28, RN50); evita enviar o que será recusado
export function validarArquivo(arquivo) {
  if (!arquivo) return 'Selecione um arquivo.';
  if (!/\.(pdf|jpe?g|png)$/i.test(arquivo.name)) return 'Envie um arquivo PDF, JPG ou PNG.';
  if (arquivo.size > TAMANHO_MAXIMO) return 'O arquivo deve ter até 10 MB.';
  return null;
}

export function tamanhoLegivel(bytes) {
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1).replace('.', ',')} MB`;
}
