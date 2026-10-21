package ui;

import modelo.Carta;
import modelo.CartaEnergia;
import modelo.CartaPokemon;
import modelo.CartaTreinador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.Normalizer;

/**
 * Botão desenhado em formato de carta.
 * Como herda de JButton, continua aceitando addActionListener, setEnabled
 * e setToolTipText, igual ao botão de antes.
 */
public class CartaVisual extends JButton {

    public static final int LARGURA_PADRAO = 130;
    public static final int ALTURA_PADRAO = 182;

    private final Carta carta;
    private Image sprite;
    private boolean mouseEmCima = false;
    private boolean selecionada = false;

    public CartaVisual(Carta carta) {
        this(carta, null, LARGURA_PADRAO, ALTURA_PADRAO);
    }

    public CartaVisual(Carta carta, Image sprite) {
        this(carta, sprite, LARGURA_PADRAO, ALTURA_PADRAO);
    }

    public CartaVisual(Carta carta, Image sprite, int largura, int altura) {
        this.carta = carta;
        this.sprite = sprite;

        setPreferredSize(new Dimension(largura, altura));
        setMinimumSize(new Dimension(largura, altura));
        setMaximumSize(new Dimension(largura, altura));

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(carta.toString());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseEmCima = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseEmCima = false;
                repaint();
            }
        });
    }

    /** Espaço vazio do campo (banco ou ativo sem Pokémon), com borda tracejada. */
    public static JComponent espacoVazio(String texto, int largura, int altura) {
        JComponent espaco = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int cw = getWidth() - 7;
                int ch = getHeight() - 9;
                int raio = Math.max(10, cw / 9);
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(3, 4, cw, ch, raio, raio);
                g2.setColor(new Color(170, 175, 190));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        10f, new float[]{7f, 6f}, 0f));
                g2.drawRoundRect(3, 4, cw, ch, raio, raio);
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, cw / 11)));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(texto, 3 + (cw - fm.stringWidth(texto)) / 2, 4 + ch / 2 + fm.getAscent() / 2 - 2);
                g2.dispose();
            }
        };
        espaco.setPreferredSize(new Dimension(largura, altura));
        espaco.setMinimumSize(new Dimension(largura, altura));
        return espaco;
    }

    public Carta getCarta() {
        return carta;
    }

    /** Use quando o sprite terminar de baixar depois que a carta já apareceu. */
    public void setSprite(Image sprite) {
        this.sprite = sprite;
        repaint();
    }

    /** Deixa a carta com a borda dourada, por exemplo quando o jogador escolheu ela. */
    public void setSelecionada(boolean selecionada) {
        this.selecionada = selecionada;
        repaint();
    }

    // ------------------------------------------------------------------ desenho

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        int levantar = (mouseEmCima && isEnabled()) ? 4 : 0;
        int x = 3;
        int y = 4 - levantar;
        int cw = w - 7;
        int ch = h - 9;
        int raio = Math.max(10, cw / 9);

        // sombra
        g2.setColor(new Color(0, 0, 0, mouseEmCima ? 70 : 45));
        g2.fill(new RoundRectangle2D.Float(x + 2, y + 4 + levantar, cw, ch, raio, raio));

        Color[] cores = coresDaCarta();
        Color corForte = cores[0];
        Color corClara = cores[1];
        Color corTexto = cores[2];

        // fundo da carta
        RoundRectangle2D forma = new RoundRectangle2D.Float(x, y, cw, ch, raio, raio);
        g2.setPaint(new GradientPaint(x, y, corClara, x, y + ch, misturar(corClara, corForte, 0.35f)));
        g2.fill(forma);

        // moldura
        g2.setStroke(new BasicStroke(selecionada ? 4f : 3f));
        g2.setColor(selecionada ? new Color(240, 190, 40) : corForte);
        g2.draw(forma);

        if (carta instanceof CartaPokemon) {
            desenharPokemon(g2, (CartaPokemon) carta, x, y, cw, ch, corForte, corClara, corTexto);
        } else if (carta instanceof CartaEnergia) {
            desenharSimples(g2, "Energia", x, y, cw, ch, corForte, corClara, corTexto);
        } else if (carta instanceof CartaTreinador) {
            desenharSimples(g2, "Treinador", x, y, cw, ch, corForte, corClara, corTexto);
        } else {
            desenharSimples(g2, "", x, y, cw, ch, corForte, corClara, corTexto);
        }

        // carta que não pode ser jogada agora fica apagada
        if (!isEnabled()) {
            g2.setColor(new Color(255, 255, 255, 120));
            g2.fill(forma);
        }

        g2.dispose();
    }

    private void desenharPokemon(Graphics2D g2, CartaPokemon p, int x, int y, int cw, int ch,
                                 Color corForte, Color corClara, Color corTexto) {
        int pad = Math.max(6, cw / 16);
        int fonteNome = Math.max(11, cw / 10);
        int interno = cw - pad * 2;

        // cabeçalho: nome e HP
        g2.setColor(corTexto);
        g2.setFont(new Font("SansSerif", Font.BOLD, fonteNome));
        FontMetrics fm = g2.getFontMetrics();
        String hpTexto = p.getHpAtual() + "";
        Font fonteHp = new Font("SansSerif", Font.BOLD, fonteNome + 1);
        Font fontePequena = new Font("SansSerif", Font.PLAIN, Math.max(9, fonteNome - 3));
        int larguraHp = g2.getFontMetrics(fonteHp).stringWidth(hpTexto)
                + g2.getFontMetrics(fontePequena).stringWidth("HP ");
        int espacoNome = interno - larguraHp - 4;
        int tamanho = fonteNome;
        while (tamanho > 9 && fm.stringWidth(p.getNome()) > espacoNome) {
            tamanho--;
            g2.setFont(new Font("SansSerif", Font.BOLD, tamanho));
            fm = g2.getFontMetrics();
        }
        String nome = cortarTexto(p.getNome(), fm, espacoNome);
        int linhaTopo = y + pad + fm.getAscent();
        g2.drawString(nome, x + pad, linhaTopo);

        int xHp = x + cw - pad - larguraHp;
        g2.setFont(fontePequena);
        g2.drawString("HP ", xHp, linhaTopo);
        g2.setFont(fonteHp);
        g2.drawString(hpTexto, xHp + g2.getFontMetrics(fontePequena).stringWidth("HP "), linhaTopo);

        // barra de vida
        int yBarra = linhaTopo + 5;
        int alturaBarra = 5;
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(x + pad, yBarra, interno, alturaBarra, alturaBarra, alturaBarra);
        double fracao = p.getHpMaximo() > 0 ? (double) p.getHpAtual() / p.getHpMaximo() : 0;
        fracao = Math.max(0, Math.min(1, fracao));
        Color corVida = fracao > 0.5 ? new Color(76, 175, 80) : fracao > 0.25 ? new Color(255, 179, 0) : new Color(229, 57, 53);
        g2.setColor(corVida);
        g2.fillRoundRect(x + pad, yBarra, (int) (interno * fracao), alturaBarra, alturaBarra, alturaBarra);

        // área da imagem
        int yImagem = yBarra + alturaBarra + 5;
        int alturaRodape = Math.max(42, ch / 4);
        int alturaImagem = (y + ch - pad - alturaRodape) - yImagem;
        desenharAreaImagem(g2, x + pad, yImagem, interno, alturaImagem, corForte, corClara, corTexto);

        // etiqueta do tipo (e "Evolução" se não for básico)
        int yEtiqueta = yImagem + alturaImagem + 4;
        Font fonteEtiqueta = new Font("SansSerif", Font.BOLD, Math.max(9, fonteNome - 3));
        int xEtiqueta = desenharEtiqueta(g2, p.getTipoElemento(), x + pad, yEtiqueta, fonteEtiqueta, corForte, Color.WHITE);
        if (!p.isBasico()) {
            desenharEtiqueta(g2, "Evolução", xEtiqueta + 4, yEtiqueta, fonteEtiqueta, new Color(255, 255, 255, 170), corTexto);
        }

        // linha do ataque
        int alturaEtiqueta = g2.getFontMetrics(fonteEtiqueta).getHeight() + 2;
        int yAtaque = yEtiqueta + alturaEtiqueta + 3;
        int alturaAtaque = Math.max(16, fonteNome + 6);
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRoundRect(x + pad, yAtaque, interno, alturaAtaque, 8, 8);
        g2.setColor(corTexto);
        Font fonteAtaque = new Font("SansSerif", Font.PLAIN, Math.max(10, fonteNome - 2));
        g2.setFont(fonteAtaque);
        FontMetrics fma = g2.getFontMetrics();
        int baseAtaque = yAtaque + (alturaAtaque + fma.getAscent() - fma.getDescent()) / 2;
        g2.drawString("Ataque", x + pad + 6, baseAtaque);
        g2.setFont(fonteAtaque.deriveFont(Font.BOLD));
        String dano = String.valueOf(p.getDanoAtaque());
        g2.drawString(dano, x + pad + interno - 6 - g2.getFontMetrics().stringWidth(dano), baseAtaque);

        // bolinhas de energia (cheias = anexadas, vazias = espaço livre)
        int anexadas = p.getEnergiasAnexadas() == null ? 0 : p.getEnergiasAnexadas().size();
        int limite = Math.max(anexadas, p.getLimiteEnergias());
        int espacoLivre = (y + ch - pad) - (yAtaque + alturaAtaque + 3);
        int d = Math.max(5, Math.min(Math.min(10, espacoLivre), (interno - (limite - 1) * 3) / Math.max(1, limite)));
        int yBolinha = yAtaque + alturaAtaque + 3 + Math.max(0, (espacoLivre - d) / 2);
        g2.setStroke(new BasicStroke(1.3f));
        for (int i = 0; i < limite; i++) {
            int bx = x + pad + i * (d + 3);
            if (i < anexadas) {
                g2.setColor(corForte);
                g2.fillOval(bx, yBolinha, d, d);
            } else {
                g2.setColor(misturar(corForte, Color.WHITE, 0.4f));
                g2.drawOval(bx, yBolinha, d - 1, d - 1);
            }
        }
    }

    private void desenharSimples(Graphics2D g2, String categoria, int x, int y, int cw, int ch,
                                 Color corForte, Color corClara, Color corTexto) {
        int pad = Math.max(6, cw / 16);
        int fonteNome = Math.max(11, cw / 10);
        int interno = cw - pad * 2;

        Font fonteCategoria = new Font("SansSerif", Font.BOLD, Math.max(9, fonteNome - 3));
        int yEtiqueta = y + pad;
        if (!categoria.isEmpty()) {
            desenharEtiqueta(g2, categoria, x + pad, yEtiqueta, fonteCategoria, corForte, Color.WHITE);
        }

        int yImagem = yEtiqueta + g2.getFontMetrics(fonteCategoria).getHeight() + 8;
        int alturaNome = fonteNome * 3;
        int alturaImagem = (y + ch - pad - alturaNome) - yImagem;
        desenharAreaImagem(g2, x + pad, yImagem, interno, alturaImagem, corForte, corClara, corTexto);

        // nome centralizado embaixo, quebrando em até 2 linhas
        g2.setColor(corTexto);
        g2.setFont(new Font("SansSerif", Font.BOLD, fonteNome));
        FontMetrics fm = g2.getFontMetrics();
        String[] linhas = quebrarEmDuasLinhas(carta.getNome(), fm, interno);
        int yTexto = yImagem + alturaImagem + 6 + fm.getAscent();
        for (String linha : linhas) {
            if (linha == null || linha.isEmpty()) continue;
            int lx = x + (cw - fm.stringWidth(linha)) / 2;
            g2.drawString(linha, lx, yTexto);
            yTexto += fm.getHeight();
        }
    }

    private void desenharAreaImagem(Graphics2D g2, int ax, int ay, int aw, int ah,
                                    Color corForte, Color corClara, Color corTexto) {
        if (ah <= 4) return;
        g2.setColor(misturar(corClara, Color.WHITE, 0.45f));
        g2.fillRoundRect(ax, ay, aw, ah, 10, 10);
        g2.setColor(misturar(corForte, Color.WHITE, 0.3f));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(ax, ay, aw, ah, 10, 10);

        Image imagem = recortarBordasVazias(sprite);
        if (imagem != null && imagem.getWidth(null) > 0) {
            int iw = imagem.getWidth(null);
            int ih = imagem.getHeight(null);
            double escala = Math.min((aw - 10) / (double) iw, (ah - 10) / (double) ih);
            int dw = (int) (iw * escala);
            int dh = (int) (ih * escala);
            // sprites são pixel art: ampliar sem borrar fica mais nítido
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(imagem, ax + (aw - dw) / 2, ay + (ah - dh) / 2, dw, dh, null);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        } else {
            // sem imagem: mostra a inicial do nome num círculo
            int d = Math.min(aw, ah) * 3 / 5;
            int cx = ax + (aw - d) / 2;
            int cy = ay + (ah - d) / 2;
            g2.setColor(misturar(corForte, Color.WHITE, 0.55f));
            g2.fillOval(cx, cy, d, d);
            String inicial = carta.getNome().isEmpty() ? "?" : carta.getNome().substring(0, 1).toUpperCase();
            g2.setColor(corTexto);
            g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, d / 2)));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(inicial, cx + (d - fm.stringWidth(inicial)) / 2,
                    cy + (d + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    /** Desenha uma pílula com texto e devolve o x onde ela termina. */
    private int desenharEtiqueta(Graphics2D g2, String texto, int ex, int ey, Font fonte, Color fundo, Color corTexto) {
        if (texto == null) texto = "";
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();
        int ew = fm.stringWidth(texto) + 12;
        int eh = fm.getHeight() + 2;
        g2.setColor(fundo);
        g2.fillRoundRect(ex, ey, ew, eh, eh, eh);
        g2.setColor(corTexto);
        g2.drawString(texto, ex + 6, ey + (eh + fm.getAscent() - fm.getDescent()) / 2);
        return ex + ew;
    }

    // ------------------------------------------------------------------ cores

    /** Devolve {cor forte, cor clara, cor do texto} conforme o tipo da carta. */
    private Color[] coresDaCarta() {
        String tipo;
        if (carta instanceof CartaPokemon) {
            tipo = ((CartaPokemon) carta).getTipoElemento();
        } else if (carta instanceof CartaTreinador) {
            return new Color[]{new Color(94, 53, 177), new Color(237, 231, 246), new Color(49, 27, 146)};
        } else {
            tipo = carta.getNome(); // ex.: "Energia de Fogo"
        }
        String t = semAcento(tipo == null ? "" : tipo.toLowerCase());

        if (t.contains("fogo"))     return new Color[]{new Color(216, 90, 48),  new Color(250, 236, 231), new Color(113, 43, 19)};
        if (t.contains("agua"))     return new Color[]{new Color(30, 136, 229), new Color(227, 242, 253), new Color(13, 71, 161)};
        if (t.contains("planta"))   return new Color[]{new Color(67, 160, 71),  new Color(232, 245, 233), new Color(27, 94, 32)};
        if (t.contains("eletr"))    return new Color[]{new Color(249, 168, 37), new Color(255, 248, 225), new Color(130, 80, 0)};
        if (t.contains("psiq"))     return new Color[]{new Color(216, 27, 96),  new Color(252, 228, 236), new Color(136, 14, 79)};
        if (t.contains("luta") || t.contains("lutador"))
                                    return new Color[]{new Color(161, 87, 52),  new Color(239, 228, 220), new Color(94, 45, 20)};
        if (t.contains("pedra") || t.contains("terra"))
                                    return new Color[]{new Color(141, 110, 99), new Color(239, 235, 233), new Color(78, 52, 46)};
        if (t.contains("gelo"))     return new Color[]{new Color(0, 172, 193),  new Color(224, 247, 250), new Color(0, 96, 100)};
        if (t.contains("venen"))    return new Color[]{new Color(142, 36, 170), new Color(243, 229, 245), new Color(74, 20, 140)};
        if (t.contains("fantasma")) return new Color[]{new Color(81, 45, 168),  new Color(237, 231, 246), new Color(49, 27, 146)};
        if (t.contains("dragao"))   return new Color[]{new Color(48, 63, 159),  new Color(232, 234, 246), new Color(26, 35, 126)};
        if (t.contains("metal") || t.contains("aco"))
                                    return new Color[]{new Color(96, 125, 139), new Color(236, 239, 241), new Color(38, 50, 56)};
        if (t.contains("fada"))     return new Color[]{new Color(236, 64, 122), new Color(252, 228, 236), new Color(136, 14, 79)};
        if (t.contains("inseto"))   return new Color[]{new Color(124, 179, 66), new Color(241, 248, 233), new Color(51, 105, 30)};
        if (t.contains("voador"))   return new Color[]{new Color(121, 134, 203), new Color(232, 234, 246), new Color(40, 53, 147)};
        if (t.contains("noturno") || t.contains("sombr"))
                                    return new Color[]{new Color(66, 66, 66),   new Color(238, 238, 238), new Color(33, 33, 33)};
        // Normal e qualquer outro
        return new Color[]{new Color(158, 158, 158), new Color(245, 245, 245), new Color(66, 66, 66)};
    }

    // ------------------------------------------------------------------ utilidades

    /** Guarda os sprites já recortados, para não recortar de novo a cada repintura. */
    private static final java.util.Map<Image, Image> CACHE_RECORTE = new java.util.WeakHashMap<>();

    /** Os sprites da PokeAPI têm muita margem transparente; isso corta a margem. */
    private static Image recortarBordasVazias(Image original) {
        if (original == null) return null;
        Image pronto = CACHE_RECORTE.get(original);
        if (pronto != null) return pronto;

        int w = original.getWidth(null);
        int h = original.getHeight(null);
        if (w <= 0 || h <= 0) return original;

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();

        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((img.getRGB(x, y) >>> 24) > 20) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        Image resultado = (maxX < 0) ? original : img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
        CACHE_RECORTE.put(original, resultado);
        return resultado;
    }

    private static Color misturar(Color a, Color b, float fracaoB) {
        float f = Math.max(0, Math.min(1, fracaoB));
        return new Color(
                (int) (a.getRed() * (1 - f) + b.getRed() * f),
                (int) (a.getGreen() * (1 - f) + b.getGreen() * f),
                (int) (a.getBlue() * (1 - f) + b.getBlue() * f));
    }

    private static String semAcento(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static String cortarTexto(String texto, FontMetrics fm, int larguraMax) {
        if (fm.stringWidth(texto) <= larguraMax) return texto;
        String r = texto;
        while (r.length() > 1 && fm.stringWidth(r + "…") > larguraMax) {
            r = r.substring(0, r.length() - 1);
        }
        return r + "…";
    }

    private static String[] quebrarEmDuasLinhas(String texto, FontMetrics fm, int larguraMax) {
        if (fm.stringWidth(texto) <= larguraMax) return new String[]{texto};
        String[] palavras = texto.split(" ");
        StringBuilder primeira = new StringBuilder();
        int i = 0;
        while (i < palavras.length) {
            String teste = primeira.length() == 0 ? palavras[i] : primeira + " " + palavras[i];
            if (fm.stringWidth(teste) > larguraMax && primeira.length() > 0) break;
            primeira.setLength(0);
            primeira.append(teste);
            i++;
        }
        StringBuilder resto = new StringBuilder();
        for (; i < palavras.length; i++) {
            if (resto.length() > 0) resto.append(" ");
            resto.append(palavras[i]);
        }
        return new String[]{cortarTexto(primeira.toString(), fm, larguraMax),
                cortarTexto(resto.toString(), fm, larguraMax)};
    }
}