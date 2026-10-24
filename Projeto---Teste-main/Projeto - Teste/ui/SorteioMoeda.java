package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

/**
 * Tela de "Cara ou Coroa" que decide quem joga o primeiro turno,
 * como na regra oficial do Pokémon TCG.
 *
 * <p>O Jogador 1 escolhe um lado, a moeda gira e o resultado define
 * quem começa. A classe só cuida da apresentação e do sorteio; quem
 * aplica o resultado na partida é o {@code iniciarPartida} da {@link AppGUI}.</p>
 *
 * <p>O giro é feito com um {@link Timer} do Swing: a cada quadro a largura
 * da moeda é multiplicada por |cos(ângulo)|, o que dá a ilusão de que ela
 * está girando no ar. O lado visível depende do sinal do cosseno.</p>
 */
public class SorteioMoeda extends JDialog {

    private static final int LARGURA = 640;
    private static final int ALTURA = 560;
    private static final int DURACAO_GIRO_MS = 1800;
    private static final int VOLTAS = 6;

    private final String nomeJogador1;
    private final String nomeJogador2;
    private final Random random = new Random();

    /** Ângulo atual da moeda (radianos). Cosseno positivo = Cara à mostra. */
    private double angulo = 0;
    /** Altura do "pulo" da moeda durante o giro, em pixels. */
    private int alturaPulo = 0;

    private boolean escolheuCara;
    private boolean deuCara;
    private boolean jogador1Comeca;
    private String mensagemResultado = null;

    private final JPanel painel;
    private final JButton btnCara;
    private final JButton btnCoroa;
    private final JButton btnComecar;

    private SorteioMoeda(String nomeJogador1, String nomeJogador2) {
        super((Frame) null, "Cara ou Coroa", true);
        this.nomeJogador1 = nomeJogador1;
        this.nomeJogador2 = nomeJogador2;

        setUndecorated(true);
        setSize(LARGURA, ALTURA);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharTela((Graphics2D) g);
            }
        };
        painel.setLayout(null);

        btnCara = criarBotao("CARA");
        btnCoroa = criarBotao("COROA");
        btnComecar = criarBotao("Começar partida");

        int yBotoes = ALTURA - 110;
        btnCara.setBounds(LARGURA / 2 - 170, yBotoes, 160, 52);
        btnCoroa.setBounds(LARGURA / 2 + 10, yBotoes, 160, 52);
        btnComecar.setBounds(LARGURA / 2 - 130, yBotoes, 260, 52);
        btnComecar.setVisible(false);

        btnCara.addActionListener(e -> girar(true));
        btnCoroa.addActionListener(e -> girar(false));
        btnComecar.addActionListener(e -> dispose());

        painel.add(btnCara);
        painel.add(btnCoroa);
        painel.add(btnComecar);
        setContentPane(painel);
    }

    /**
     * Abre a tela do sorteio e espera o jogador terminar.
     *
     * @param nomeJogador1 nome de quem escolhe o lado da moeda
     * @param nomeJogador2 nome do outro jogador (ou "Bot")
     * @return {@code true} se o Jogador 1 começa; {@code false} se o Jogador 2 começa
     */
    public static boolean sortearQuemComeca(String nomeJogador1, String nomeJogador2) {
        SorteioMoeda tela = new SorteioMoeda(nomeJogador1, nomeJogador2);
        tela.setVisible(true); // modal: só volta depois do dispose()
        return tela.jogador1Comeca;
    }

    /** Sorteia o resultado e anima a moeda até cair no lado sorteado. */
    private void girar(boolean escolheuCara) {
        this.escolheuCara = escolheuCara;
        this.deuCara = random.nextBoolean();
        this.jogador1Comeca = (escolheuCara == deuCara);

        btnCara.setVisible(false);
        btnCoroa.setVisible(false);

        // Cara termina em múltiplo de 2π (cosseno = 1); Coroa termina meia volta depois (cosseno = -1).
        double anguloFinal = VOLTAS * 2 * Math.PI + (deuCara ? 0 : Math.PI);
        long inicio = System.currentTimeMillis();

        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            double t = Math.min(1.0, (System.currentTimeMillis() - inicio) / (double) DURACAO_GIRO_MS);
            double suavizado = 1 - Math.pow(1 - t, 3); // começa rápido e desacelera
            angulo = anguloFinal * suavizado;
            alturaPulo = (int) (Math.sin(Math.PI * t) * 90);

            if (t >= 1.0) {
                timer.stop();
                angulo = anguloFinal;
                alturaPulo = 0;
                String quemComeca = jogador1Comeca ? nomeJogador1 : nomeJogador2;
                mensagemResultado = quemComeca + " começa a partida!";
                btnComecar.setVisible(true);
            }
            painel.repaint();
        });
        timer.start();
    }

    // ======================= Desenho =======================

    private void desenharTela(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = painel.getWidth();
        int h = painel.getHeight();

        g2.setPaint(new GradientPaint(0, 0, new Color(20, 20, 45), 0, h, new Color(55, 40, 95)));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(255, 193, 7));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(1, 1, w - 3, h - 3);

        desenharTextoCentralizado(g2, "CARA OU COROA", w / 2, 62, new Font("Segoe UI", Font.BOLD, 36), Color.WHITE);

        String subtitulo;
        if (mensagemResultado != null) {
            subtitulo = nomeJogador1 + " escolheu " + (escolheuCara ? "CARA" : "COROA")
                    + " e deu " + (deuCara ? "CARA" : "COROA") + ".";
        } else if (btnCara.isVisible()) {
            subtitulo = nomeJogador1 + ", escolha um lado da moeda:";
        } else {
            subtitulo = "Girando...";
        }
        desenharTextoCentralizado(g2, subtitulo, w / 2, 100, new Font("Segoe UI", Font.PLAIN, 18), new Color(200, 210, 230));

        int tamanhoMoeda = 170;
        int centroX = w / 2;
        int centroY = 255 - alturaPulo;
        desenharSombra(g2, centroX, 255 + tamanhoMoeda / 2 + 18, tamanhoMoeda);
        desenharMoeda(g2, centroX, centroY, tamanhoMoeda);

        if (mensagemResultado != null) {
            desenharTextoCentralizado(g2, mensagemResultado, w / 2, 395, new Font("Segoe UI", Font.BOLD, 26), new Color(255, 213, 79));
            desenharTextoCentralizado(g2, "Quem começa não pode atacar nem evoluir no 1º turno.", w / 2, 425,
                    new Font("Segoe UI", Font.PLAIN, 14), new Color(180, 185, 210));
        }
    }

    /** Sombra no "chão" que encolhe quando a moeda sobe. */
    private void desenharSombra(Graphics2D g2, int centroX, int y, int tamanho) {
        double escala = 1 - alturaPulo / 180.0;
        int largura = (int) (tamanho * 0.8 * escala);
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(centroX - largura / 2, y - 8, largura, 16);
    }

    /**
     * Desenha a moeda achatada no eixo X conforme o ângulo do giro.
     * Cara = Pokébola; Coroa = estrela dourada.
     */
    private void desenharMoeda(Graphics2D g2, int centroX, int centroY, int tamanho) {
        double cos = Math.cos(angulo);
        boolean mostrandoCara = cos >= 0;
        int largura = Math.max(4, (int) (tamanho * Math.abs(cos)));
        int x = centroX - largura / 2;
        int y = centroY - tamanho / 2;

        // Borda (espessura da moeda)
        g2.setColor(new Color(150, 110, 20));
        g2.fillOval(x - 3, y - 3, largura + 6, tamanho + 6);

        Shape antigoClip = g2.getClip();
        g2.clip(new Ellipse2D.Double(x, y, largura, tamanho));

        if (mostrandoCara) {
            desenharFaceCara(g2, x, y, largura, tamanho);
        } else {
            desenharFaceCoroa(g2, x, y, largura, tamanho);
        }
        g2.setClip(antigoClip);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(255, 223, 120));
        g2.drawOval(x, y, largura, tamanho);
    }

    /** Face "Cara": uma Pokébola. */
    private void desenharFaceCara(Graphics2D g2, int x, int y, int largura, int tamanho) {
        int meio = y + tamanho / 2;
        g2.setColor(new Color(220, 40, 40));
        g2.fillRect(x, y, largura, tamanho / 2);
        g2.setColor(new Color(245, 245, 245));
        g2.fillRect(x, meio, largura, tamanho / 2 + 1);

        int faixa = tamanho / 12;
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(x, meio - faixa / 2, largura, faixa);

        double escalaX = largura / (double) tamanho;
        int botaoExterno = tamanho / 3;
        int botaoInterno = tamanho / 5;
        int centroX = x + largura / 2;
        g2.fillOval(centroX - (int) (botaoExterno * escalaX / 2), meio - botaoExterno / 2,
                (int) (botaoExterno * escalaX), botaoExterno);
        g2.setColor(Color.WHITE);
        g2.fillOval(centroX - (int) (botaoInterno * escalaX / 2), meio - botaoInterno / 2,
                (int) (botaoInterno * escalaX), botaoInterno);
    }

    /** Face "Coroa": fundo dourado com uma estrela. */
    private void desenharFaceCoroa(Graphics2D g2, int x, int y, int largura, int tamanho) {
        g2.setPaint(new GradientPaint(x, y, new Color(255, 214, 90), x + largura, y + tamanho, new Color(214, 156, 30)));
        g2.fillRect(x, y, largura, tamanho);

        double escalaX = largura / (double) tamanho;
        int centroX = x + largura / 2;
        int centroY = y + tamanho / 2;
        double raioExterno = tamanho * 0.32;
        double raioInterno = raioExterno * 0.45;

        Polygon estrela = new Polygon();
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? raioExterno : raioInterno;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            estrela.addPoint(centroX + (int) (Math.cos(a) * r * escalaX), centroY + (int) (Math.sin(a) * r));
        }
        g2.setColor(new Color(150, 100, 10));
        g2.fillPolygon(estrela);
        g2.setColor(new Color(255, 240, 180));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(estrela);
    }

    private void desenharTextoCentralizado(Graphics2D g2, String texto, int centroX, int y, Font fonte, Color cor) {
        g2.setFont(fonte);
        int posX = centroX - g2.getFontMetrics().stringWidth(texto) / 2;
        g2.setColor(new Color(15, 10, 35));
        g2.drawString(texto, posX + 2, y + 2);
        g2.setColor(cor);
        g2.drawString(texto, posX, y);
    }

    private JButton criarBotao(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 18));
        botao.setBackground(new Color(255, 193, 7));
        botao.setForeground(new Color(40, 30, 0));
        botao.setOpaque(true);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        return botao;
    }
}