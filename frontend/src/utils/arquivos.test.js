import { tamanhoLegivel, validarArquivo } from './arquivos';
import { destinoAposLogin } from './navegacao';

const arquivo = (nome, tamanho = 1000) => ({ name: nome, size: tamanho });

describe('arquivos', () => {
  it('aceita PDF, JPG e PNG de até 10 MB (RN28, RN50)', () => {
    expect(validarArquivo(arquivo('boleto.pdf'))).toBeNull();
    expect(validarArquivo(arquivo('FOTO.JPEG'))).toBeNull();
    expect(validarArquivo(arquivo('guia.png', 10 * 1024 * 1024))).toBeNull();
  });

  it('recusa outros tipos, arquivos grandes e ausência de arquivo', () => {
    expect(validarArquivo(arquivo('planilha.xlsx'))).toMatch(/PDF, JPG ou PNG/);
    expect(validarArquivo(arquivo('grande.pdf', 10 * 1024 * 1024 + 1))).toMatch(/10 MB/);
    expect(validarArquivo(null)).toMatch(/Selecione/);
  });

  it('mostra o tamanho de forma legível', () => {
    expect(tamanhoLegivel(300)).toBe('1 KB');
    expect(tamanhoLegivel(2.5 * 1024 * 1024)).toBe('2,5 MB');
  });
});

describe('navegação após login', () => {
  it('volta para a página que a pessoa tentava abrir', () => {
    expect(destinoAposLogin({ state: { de: { pathname: '/convites/abc', search: '?x=1' } } })).toBe('/convites/abc?x=1');
    expect(destinoAposLogin({ state: null })).toBe('/');
  });
});
