package ted.com.tudoemdia.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

// Tabela em A4 paisagem, fonte de 10 pt, cabeçalho repetido a cada página e
// nenhuma linha dividida entre páginas (RNF12)
final class RelatorioPdf {

	record Coluna(String titulo, float largura, boolean alinhadaADireita) {
	}

	private static final PDRectangle A4_PAISAGEM = new PDRectangle(PDRectangle.A4.getHeight(),
			PDRectangle.A4.getWidth());
	private static final float MARGEM = 40;
	private static final float FONTE = 10;
	private static final float FONTE_TITULO = 14;
	private static final float ALTURA_LINHA = 18;
	private static final float RESPIRO = 4;
	private static final float ESPACO_RODAPE = 30;
	private static final Color CINZA_CABECALHO = new Color(230, 230, 230);
	private static final Color CINZA_LINHA = new Color(200, 200, 200);

	private final PDDocument documento = new PDDocument();
	private final PDFont normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
	private final PDFont negrito = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
	private final List<Coluna> colunas;
	private PDPageContentStream conteudo;
	private float y;

	private RelatorioPdf(List<Coluna> colunas) {
		this.colunas = colunas;
	}

	static byte[] gerar(String titulo, List<String> informacoes, List<Coluna> colunas, List<List<String>> linhas,
			List<String> totais) {
		RelatorioPdf pdf = new RelatorioPdf(colunas);
		try {
			return pdf.montar(titulo, informacoes, linhas, totais);
		} catch (IOException e) {
			throw new IllegalStateException("Falha ao gerar o PDF", e);
		}
	}

	private byte[] montar(String titulo, List<String> informacoes, List<List<String>> linhas, List<String> totais)
			throws IOException {
		try (documento) {
			novaPagina();
			escrever(negrito, FONTE_TITULO, MARGEM, y, titulo);
			y -= 24;
			for (String informacao : informacoes) {
				escrever(normal, FONTE, MARGEM, y, informacao);
				y -= 15;
			}
			y -= 10;
			cabecalhoDaTabela();
			if (linhas.isEmpty()) {
				linha(List.of("Nenhuma obrigação encontrada para os filtros informados."), normal, false);
			}
			for (List<String> valores : linhas) {
				linha(valores, normal, false);
			}
			if (totais != null) {
				linha(totais, negrito, true);
			}
			conteudo.close();
			numerarPaginas();
			ByteArrayOutputStream saida = new ByteArrayOutputStream();
			documento.save(saida);
			return saida.toByteArray();
		}
	}

	private void novaPagina() throws IOException {
		PDPage pagina = new PDPage(A4_PAISAGEM);
		documento.addPage(pagina);
		conteudo = new PDPageContentStream(documento, pagina);
		y = A4_PAISAGEM.getHeight() - MARGEM;
	}

	private void cabecalhoDaTabela() throws IOException {
		conteudo.setNonStrokingColor(CINZA_CABECALHO);
		conteudo.addRect(MARGEM, y - ALTURA_LINHA, larguraTotal(), ALTURA_LINHA);
		conteudo.fill();
		conteudo.setNonStrokingColor(Color.BLACK);
		escreverCelulas(colunas.stream().map(Coluna::titulo).toList(), negrito);
		y -= ALTURA_LINHA;
	}

	// Se a linha não couber, vai inteira para a próxima página, com o cabeçalho repetido
	private void linha(List<String> valores, PDFont fonte, boolean destacada) throws IOException {
		if (y - ALTURA_LINHA < MARGEM + ESPACO_RODAPE) {
			conteudo.close();
			novaPagina();
			cabecalhoDaTabela();
		}
		if (valores.size() == 1) {
			escrever(fonte, FONTE, MARGEM + RESPIRO, y - 13, valores.get(0));
		} else {
			escreverCelulas(valores, fonte);
		}
		conteudo.setStrokingColor(destacada ? Color.BLACK : CINZA_LINHA);
		conteudo.moveTo(MARGEM, y - ALTURA_LINHA);
		conteudo.lineTo(MARGEM + larguraTotal(), y - ALTURA_LINHA);
		conteudo.stroke();
		y -= ALTURA_LINHA;
	}

	private void escreverCelulas(List<String> valores, PDFont fonte) throws IOException {
		float x = MARGEM;
		for (int i = 0; i < colunas.size(); i++) {
			Coluna coluna = colunas.get(i);
			String texto = ajustar(valores.get(i), fonte, coluna.largura() - 2 * RESPIRO);
			float posicao = coluna.alinhadaADireita()
					? x + coluna.largura() - RESPIRO - largura(texto, fonte, FONTE)
					: x + RESPIRO;
			escrever(fonte, FONTE, posicao, y - 13, texto);
			x += coluna.largura();
		}
	}

	private void numerarPaginas() throws IOException {
		int total = documento.getNumberOfPages();
		for (int i = 0; i < total; i++) {
			PDPage pagina = documento.getPage(i);
			try (PDPageContentStream rodape = new PDPageContentStream(documento, pagina,
					PDPageContentStream.AppendMode.APPEND, true, true)) {
				String texto = "Página " + (i + 1) + " de " + total;
				rodape.beginText();
				rodape.setFont(normal, FONTE);
				rodape.newLineAtOffset(A4_PAISAGEM.getWidth() - MARGEM - largura(texto, normal, FONTE), MARGEM - 15);
				rodape.showText(texto);
				rodape.endText();
			}
		}
	}

	private void escrever(PDFont fonte, float tamanho, float x, float yTexto, String texto) throws IOException {
		conteudo.beginText();
		conteudo.setFont(fonte, tamanho);
		conteudo.newLineAtOffset(x, yTexto);
		conteudo.showText(compativel(texto, fonte));
		conteudo.endText();
	}

	// Corta textos longos com reticências para não invadir a coluna vizinha
	private String ajustar(String texto, PDFont fonte, float larguraMaxima) throws IOException {
		String ajustado = compativel(texto, fonte);
		if (largura(ajustado, fonte, FONTE) <= larguraMaxima) {
			return ajustado;
		}
		while (!ajustado.isEmpty() && largura(ajustado + "…", fonte, FONTE) > larguraMaxima) {
			ajustado = ajustado.substring(0, ajustado.length() - 1);
		}
		return ajustado + "…";
	}

	private float largura(String texto, PDFont fonte, float tamanho) throws IOException {
		return fonte.getStringWidth(compativel(texto, fonte)) / 1000 * tamanho;
	}

	// As fontes padrão do PDF não têm todos os caracteres (emojis, por exemplo); troca os ausentes por "?"
	private static String compativel(String texto, PDFont fonte) {
		if (texto == null) {
			return "";
		}
		StringBuilder resultado = new StringBuilder();
		texto.replaceAll("\\s+", " ").codePoints().forEach(codigo -> {
			String caractere = new String(Character.toChars(codigo));
			try {
				fonte.encode(caractere);
				resultado.append(caractere);
			} catch (IllegalArgumentException | IOException e) {
				resultado.append('?');
			}
		});
		return resultado.toString();
	}

	private float larguraTotal() {
		return (float) colunas.stream().mapToDouble(Coluna::largura).sum();
	}
}
