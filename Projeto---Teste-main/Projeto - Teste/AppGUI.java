import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.HttpURLConnection;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.List;

public class AppGUI {

    private JFrame frame;
    private JLabel turnoLabel;
    private JLabel adversarioLabel;
    private JPanel campoJogadorPanel;
    private JPanel maoPanel;
    private JTextArea logArea;
    private JScrollPane logScroll;
    private final java.util.List<JComponent> toastsAtivos = new java.util.ArrayList<>();
    private JPanel flashPane;
    private int alphaFlash = 0;
    private Color corFlash = Color.RED;

    private Jogador jogador1;
    private Jogador jogador2;
    private Jogador jogadorAtual;
    private Jogador adversario;
    private Jogador jogadorBot;
    private boolean ehTurnoDoBot = false;

    private boolean jogador1PrimeiroTurno = true;
    private boolean jogador2PrimeiroTurno = true;
    private int numeroTurno = 1;
    private boolean jogoAtivo = true;

    private static final Color COR_AGUA = new Color(66, 133, 244);
    private static final Color COR_FOGO = new Color(230, 74, 25);
    private static final Color COR_PLANTA = new Color(76, 175, 80);
    private static final Color COR_NORMAL = new Color(158, 158, 158);
    private static final Color COR_TREINADOR = new Color(255, 193, 7);
    private static final Color COR_FUNDO = new Color(245, 247, 250);
    private static final Color COR_BANNER = new Color(33, 33, 66);

    private static final int LIMITE_BARALHO = 40;

    private final java.util.Map<String, ImageIcon> cacheSprites = new java.util.HashMap<>();
    private final java.util.Set<String> chavesCarregandoAgora = new java.util.HashSet<>();

    private final java.util.Map<Integer, ImageIcon> cacheSpritesPorNumero = new java.util.HashMap<>();
    private final java.util.Set<Integer> spritesCarregandoAgora = new java.util.HashSet<>();

    private final java.util.Map<Integer, String> cacheTipoPorNumero = new java.util.HashMap<>();
    private final java.util.Set<Integer> tiposCarregandoAgora = new java.util.HashSet<>();

    private static final java.util.Map<String, Integer> POKEDEX = new java.util.HashMap<>();
    static {
        POKEDEX.put("Bulbasaur", 1);
        POKEDEX.put("Ivysaur", 2);
        POKEDEX.put("Venusaur", 3);
        POKEDEX.put("Charmander", 4);
        POKEDEX.put("Charmeleon", 5);
        POKEDEX.put("Charizard", 6);
        POKEDEX.put("Squirtle", 7);
        POKEDEX.put("Wartortle", 8);
        POKEDEX.put("Blastoise", 9);
        POKEDEX.put("Psyduck", 54);
        POKEDEX.put("Golduck", 55);
        POKEDEX.put("Growlithe", 58);
        POKEDEX.put("Arcanine", 59);
        POKEDEX.put("Magikarp", 129);
        POKEDEX.put("Gyarados", 130);
        POKEDEX.put("Eevee", 133);
        POKEDEX.put("Vaporeon", 134);
        POKEDEX.put("Chikorita", 152);
        POKEDEX.put("Bayleef", 153);
        POKEDEX.put("Meganium", 154);
        POKEDEX.put("Entei", 244);
        POKEDEX.put("Celebi", 251);
        POKEDEX.put("Treecko", 252);
        POKEDEX.put("Grovyle", 253);
        POKEDEX.put("Sceptile", 254);
        POKEDEX.put("Torchic", 255);
        POKEDEX.put("Combusken", 256);
        POKEDEX.put("Blaziken", 257);
        POKEDEX.put("Chimchar", 390);
        POKEDEX.put("Monferno", 391);
        POKEDEX.put("Infernape", 392);
        POKEDEX.put("Froakie", 656);
        POKEDEX.put("Frogadier", 657);
        POKEDEX.put("Greninja", 658);
        POKEDEX.put("Rowlet", 722);
        POKEDEX.put("Dartrix", 723);
        POKEDEX.put("Decidueye", 724);
    }

    private static final String VERSAO_BUILD = "build: Gen 1-6";

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {

        }

        SwingUtilities.invokeLater(() -> new AppGUI().iniciar());
    }

    private void iniciar() {

        boolean modoSolo = escolherModoDialog();

        boolean[] timesDisponiveis = { true, true, true };

        String nome1 = pedirNomeEstilizado("Digite o seu nome de Treinador:", "Treinador 1");
        jogador1 = new Jogador(nome1);
        escolherTimeDialog(jogador1, timesDisponiveis);

        if (modoSolo) {

            jogador2 = new Jogador("Bot 🤖");
            jogadorBot = jogador2;
            construirTimeAleatorioBotComLoading(jogador2);
        } else {

            jogadorBot = null;
            String nome2 = pedirNomeEstilizado("Digite o nome do Treinador 2:", "Treinador 2");
            jogador2 = new Jogador(nome2);
            escolherTimeDialog(jogador2, timesDisponiveis);
        }

        App.adicionarCartasTreinador(jogador1);
        App.adicionarCartasTreinador(jogador2);

        jogador1.verificarBaralho();
        jogador2.verificarBaralho();
        for (int i = 0; i < 6; i++) jogador1.comprarCarta();
        for (int i = 0; i < 6; i++) jogador2.comprarCarta();

        jogadorAtual = jogador1;
        adversario = jogador2;

        montarJanela();

        redirecionarConsoleParaLog();

        frame.setVisible(true);

        iniciarTurno();
    }

    private Image imagemMenuCharizard;
    private Image imagemMenuBlastoise;
    private Image imagemMenuVenusaur;

    /**
     * Desenha uma Pokébola vetorial (não uma imagem baixada), já ABERTA — a metade vermelha
     * sobe, a metade branca desce, com um brilho suave saindo do meio. Como é desenhada na hora
     * com formas geométricas, fica sempre nítida em qualquer tamanho de tela, sem pixelar.
     */
    private void desenharPokebolaAberta(Graphics2D g2, int centerX, int centerY, int tamanho) {
        int raio = tamanho / 2;

        Object antialiasAntigo = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float raioMax = raio * 1.5f;
        RadialGradientPaint brilhoFundo = new RadialGradientPaint(
                new Point(centerX, centerY), raioMax,
                new float[]{0f, 1f},
                new Color[]{new Color(255, 250, 200, 90), new Color(255, 250, 200, 0)}
        );
        g2.setPaint(brilhoFundo);
        g2.fillOval((int) (centerX - raioMax), (int) (centerY - raioMax), (int) (raioMax * 2), (int) (raioMax * 2));

        // ---- METADE DE BAIXO: branca ----
        GradientPaint branco = new GradientPaint(
                centerX - raio, centerY, new Color(255, 255, 255),
                centerX + raio, centerY + raio, new Color(215, 215, 220)
        );
        g2.setPaint(branco);
        g2.fillArc(centerX - raio, centerY - raio, tamanho, tamanho, 180, 180);

        // ---- METADE DE CIMA: vermelha brilhante ----
        RadialGradientPaint vermelhoEsferico = new RadialGradientPaint(
                new Point(centerX - (int) (raio * 0.35), centerY - (int) (raio * 0.55)), tamanho * 0.85f,
                new float[]{0f, 0.55f, 1f},
                new Color[]{new Color(255, 160, 160), new Color(225, 55, 55), new Color(160, 15, 15)}
        );
        g2.setPaint(vermelhoEsferico);
        g2.fillArc(centerX - raio, centerY - raio, tamanho, tamanho, 0, 180);

        // Reflexo de luz na tampa vermelha, dando o brilho "esférico" de bola de verdade
        int reflexoLargura = (int) (tamanho * 0.24);
        int reflexoAltura = (int) (tamanho * 0.14);
        g2.setColor(new Color(255, 255, 255, 130));
        g2.fillOval(centerX - raio + (int) (tamanho * 0.16), centerY - raio + (int) (tamanho * 0.14),
                reflexoLargura, reflexoAltura);

        // ---- Contorno externo da bola inteira ----
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(centerX - raio, centerY - raio, tamanho, tamanho);

        // ---- Faixa preta no meio (equador da bola) ----
        int alturaFaixa = Math.max(6, tamanho / 12);
        g2.setColor(new Color(15, 15, 15));
        g2.fillRect(centerX - raio, centerY - alturaFaixa / 2, tamanho, alturaFaixa);

        // ---- Botão central ----
        int botaoRaioExterno = Math.max(10, tamanho / 9);
        g2.setColor(new Color(15, 15, 15));
        g2.fillOval(centerX - botaoRaioExterno, centerY - botaoRaioExterno, botaoRaioExterno * 2, botaoRaioExterno * 2);
        int botaoRaioMeio = (int) (botaoRaioExterno * 0.72);
        g2.setColor(Color.WHITE);
        g2.fillOval(centerX - botaoRaioMeio, centerY - botaoRaioMeio, botaoRaioMeio * 2, botaoRaioMeio * 2);
        int botaoRaioInterno = (int) (botaoRaioExterno * 0.42);
        g2.setColor(new Color(15, 15, 15));
        g2.drawOval(centerX - botaoRaioMeio, centerY - botaoRaioMeio, botaoRaioMeio * 2, botaoRaioMeio * 2);
        g2.setColor(new Color(230, 230, 235));
        g2.fillOval(centerX - botaoRaioInterno, centerY - botaoRaioInterno, botaoRaioInterno * 2, botaoRaioInterno * 2);

        if (antialiasAntigo != null) g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasAntigo);
    }

    /**
     * Escreve um texto com um contorno escuro em volta — assim ele continua legível não importa
     * o que estiver desenhado atrás (imagem de fundo, cor clara, etc), sem depender do fundo.
     */
    private void desenharTextoComContorno(Graphics2D g2, String texto, int centerX, int y, Font fonte, Color corTexto, Color corContorno) {
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();
        int posX = centerX - fm.stringWidth(texto) / 2;

        g2.setColor(corContorno);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0) {
                    g2.drawString(texto, posX + dx, y + dy);
                }
            }
        }
        g2.setColor(corTexto);
        g2.drawString(texto, posX, y);
    }

    /**
     * Desenha um Pokémon numa posição ao redor de um círculo (feito tipo roda de tipos elemental).
     * anguloGraus: 0° = direita, 90° = embaixo, 180° = esquerda, 270° = em cima.
     */
    private void desenharRivalNoCirculo(Graphics2D g2, Image imagem, int centroX, int centroY, int raioCirculo, double anguloGraus, int tamanho) {
        if (imagem == null) return;
        double rad = Math.toRadians(anguloGraus);
        int px = centroX + (int) (raioCirculo * Math.cos(rad));
        int py = centroY + (int) (raioCirculo * Math.sin(rad));
        g2.drawImage(imagem, px - tamanho / 2, py - tamanho / 2, tamanho, tamanho, null);
    }

    /**
     * Aplica um desfoque (borrão) numa imagem — usado nas artes grandes de fundo (Charizard,
     * Blastoise, Venusaur), pra ficarem só uma ambientação, sem competir com o texto/pokébola.
     */
    private BufferedImage aplicarDesfoque(BufferedImage origem, int raio) {
        int tamanhoKernel = raio * 2 + 1;
        float peso = 1.0f / (tamanhoKernel * tamanhoKernel);
        float[] dados = new float[tamanhoKernel * tamanhoKernel];
        java.util.Arrays.fill(dados, peso);
        Kernel kernel = new Kernel(tamanhoKernel, tamanhoKernel, dados);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(origem, null);
    }

    /**
     * Cria um painel com o visual padrão das telas de abertura: fundo em gradiente escuro
     * com a Pokébola translúcida atrás. Reaproveitado em todas as telas antes do campo de batalha.
     */
    private JPanel criarPainelComFundo() {
        JPanel painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                GradientPaint gradiente = new GradientPaint(0, 0, new Color(20, 20, 45), 0, getHeight(), new Color(55, 40, 95));
                g2.setPaint(gradiente);
                g2.fillRect(0, 0, getWidth(), getHeight());

                int tamanhoBola = (int) (getHeight() * 0.28);
                desenharPokebolaAberta(g2, getWidth() / 2, tamanhoBola / 2 + 40, tamanhoBola);
            }
        };
        painel.setLayout(null);
        painel.setOpaque(true);
        return painel;
    }

    /**
     * Tela estilizada (mesmo visual das outras) pra pedir o nome do treinador,
     * com campo de texto centralizado ao invés do JOptionPane padrão.
     */
    private String pedirNomeEstilizado(String pergunta, String valorPadrao) {
        final String[] resultado = { valorPadrao };
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();

        JDialog dialogo = new JDialog((Frame) null, "Pokémon TCG", true);
        dialogo.setSize(tela);
        dialogo.setLocation(0, 0);
        dialogo.setResizable(false);
        dialogo.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel painel = criarPainelComFundo();
        painel.setPreferredSize(tela);

        JLabel titulo = new JLabel(pergunta, SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titulo.setForeground(Color.WHITE);
        titulo.setBounds(0, tela.height / 2 - 120, tela.width, 50);

        int larguraCampo = 420;
        JTextField campoTexto = new JTextField();
        campoTexto.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        campoTexto.setHorizontalAlignment(JTextField.CENTER);
        campoTexto.setBounds((tela.width - larguraCampo) / 2, tela.height / 2 - 50, larguraCampo, 48);

        JButton btnConfirmar = criarBotaoMenuPrincipal("✅ Confirmar");
        btnConfirmar.setBounds((tela.width - 320) / 2, tela.height / 2 + 20, 320, 55);
        btnConfirmar.addActionListener(e -> {
            String texto = campoTexto.getText().trim();
            resultado[0] = texto.isEmpty() ? valorPadrao : texto;
            dialogo.dispose();
        });
        campoTexto.addActionListener(e -> btnConfirmar.doClick()); // Enter também confirma

        painel.add(titulo);
        painel.add(campoTexto);
        painel.add(btnConfirmar);

        dialogo.setContentPane(painel);
        dialogo.setVisible(true);

        return resultado[0];
    }

    private boolean escolherModoDialog() {
        final boolean[] resultado = { true };
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();

        JDialog menuDialog = new JDialog((Frame) null, "Pokémon TCG", true);
        menuDialog.setSize(tela);
        menuDialog.setLocation(0, 0);
        menuDialog.setResizable(false);
        menuDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel fundoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                GradientPaint gradiente = new GradientPaint(0, 0, new Color(20, 20, 45), 0, getHeight(), new Color(55, 40, 95));
                g2.setPaint(gradiente);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Os 3 "rivais de elemento" borrados ao fundo, todos do mesmo tamanho, arrumados
                // em círculo (tipo a roda de tipos do Pokémon GO) — mais espaçados entre si, mas
                // nas mesmas posições/direções de antes
                int centroCirculoX = getWidth() / 2;
                int centroCirculoY = (int) (getHeight() * 0.55);
                int raioCirculo = (int) (getHeight() * 0.42);
                int tamanhoRival = (int) (getHeight() * 0.48);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
                desenharRivalNoCirculo(g2, imagemMenuCharizard, centroCirculoX, centroCirculoY, raioCirculo, 90, tamanhoRival);
                desenharRivalNoCirculo(g2, imagemMenuBlastoise, centroCirculoX, centroCirculoY, raioCirculo, 210, tamanhoRival);
                desenharRivalNoCirculo(g2, imagemMenuVenusaur, centroCirculoX, centroCirculoY, raioCirculo, 330, tamanhoRival);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                // Pokébola fechada e o título logo abaixo dela — sempre desenhados por cima,
                // então nunca ficam ilegíveis por causa do que estiver atrás
                int centroX = getWidth() / 2;
                int tamanhoBola = (int) (getHeight() * 0.24);
                int centroYBola = (int) (getHeight() * 0.20);
                desenharPokebolaAberta(g2, centroX, centroYBola, tamanhoBola);

                desenharTextoComContorno(g2, "POKÉMON TCG", centroX, centroYBola + tamanhoBola / 2 + 55,
                        new Font("Segoe UI", Font.BOLD, 50), Color.WHITE, new Color(20, 15, 40));
            }
        };
        fundoPanel.setLayout(null);
        fundoPanel.setPreferredSize(tela);

        int larguraBotao = 320;
        int alturaBotao = 60;
        int xBotao = (tela.width - larguraBotao) / 2;

        JButton btnSolo = criarBotaoMenuPrincipal("🎮 Jogar vs Bot");
        btnSolo.setBounds(xBotao, tela.height / 2 + 40, larguraBotao, alturaBotao);
        btnSolo.addActionListener(e -> {
            resultado[0] = true;
            menuDialog.dispose();
        });

        JButton btnPvp = criarBotaoMenuPrincipal("👥 PvP (2 Jogadores)");
        btnPvp.setBounds(xBotao, tela.height / 2 + 120, larguraBotao, alturaBotao);
        btnPvp.addActionListener(e -> {
            resultado[0] = false;
            menuDialog.dispose();
        });

        JButton btnSair = criarBotaoMenuPrincipal("🚪 Sair");
        btnSair.setBounds(xBotao, tela.height / 2 + 200, larguraBotao, alturaBotao);
        btnSair.addActionListener(e -> System.exit(0));

        fundoPanel.add(btnSolo);
        fundoPanel.add(btnPvp);
        fundoPanel.add(btnSair);

        menuDialog.setContentPane(fundoPanel);
        carregarImagensMenu(fundoPanel);

        menuDialog.setVisible(true);

        return resultado[0];
    }

    private JButton criarBotaoMenuPrincipal(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 18));
        botao.setBackground(new Color(255, 193, 7));
        botao.setForeground(new Color(40, 30, 0));
        botao.setOpaque(true);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        return botao;
    }

    private void carregarImagensMenu(JPanel painelParaRepintar) {
        new Thread(() -> {
            BufferedImage charizard = baixarImagemBuffered("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png");
            BufferedImage blastoise = baixarImagemBuffered("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/9.png");
            BufferedImage venusaur = baixarImagemBuffered("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/3.png");

            Image charizardDesfocado = charizard != null ? aplicarDesfoque(charizard, 8) : null;
            Image blastoiseDesfocado = blastoise != null ? aplicarDesfoque(blastoise, 8) : null;
            Image venusaurDesfocado = venusaur != null ? aplicarDesfoque(venusaur, 8) : null;

            SwingUtilities.invokeLater(() -> {
                imagemMenuCharizard = charizardDesfocado;
                imagemMenuBlastoise = blastoiseDesfocado;
                imagemMenuVenusaur = venusaurDesfocado;
                painelParaRepintar.repaint();
            });
        }).start();
    }

    private BufferedImage baixarImagemBuffered(String url) {
        try {
            return ImageIO.read(java.net.URI.create(url).toURL());
        } catch (Exception e) {
            return null;
        }
    }

    private void construirTimeAleatorioBotComLoading(Jogador bot) {
        JDialog carregando = new JDialog((Frame) null, "Aguarde...", true);
        carregando.setSize(400, 140);
        carregando.setLocationRelativeTo(null);
        carregando.setLayout(new BorderLayout(10, 10));
        carregando.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JLabel mensagem = new JLabel("🤖 O Bot está sorteando o time dele na PokeAPI...", SwingConstants.CENTER);
        mensagem.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        JProgressBar barra = new JProgressBar();
        barra.setIndeterminate(true);

        carregando.add(mensagem, BorderLayout.CENTER);
        carregando.add(barra, BorderLayout.SOUTH);

        new Thread(() -> {
            construirTimeAleatorioBot(bot);
            SwingUtilities.invokeLater(carregando::dispose);
        }).start();

        carregando.setVisible(true);
    }

    private static final CartaPokemon CARTA_FALHA_SORTEIO = new CartaPokemon("_FALHA_", "Normal", 1, 1);

    private void construirTimeAleatorioBot(Jogador bot) {
        final int TOTAL_TENTATIVAS = 40;
        final int PARALELISMO = 8;

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(PARALELISMO);
        java.util.concurrent.BlockingQueue<CartaPokemon> resultados = new java.util.concurrent.LinkedBlockingQueue<>();
        java.util.Random sorteio = new java.util.Random();

        for (int i = 0; i < TOTAL_TENTATIVAS; i++) {
            final int numeroDex = 1 + sorteio.nextInt(721);
            executor.submit(() -> {
                CartaPokemon carta = buscarPokemon(numeroDex);
                resultados.offer(carta != null ? carta : CARTA_FALHA_SORTEIO);
            });
        }
        executor.shutdown();

        int recebidos = 0;
        while (recebidos < TOTAL_TENTATIVAS && bot.getTamanhoBaralho() < LIMITE_BARALHO) {
            CartaPokemon carta;
            try {
                carta = resultados.take();
            } catch (InterruptedException e) {
                break;
            }
            recebidos++;

            if (carta == CARTA_FALHA_SORTEIO) {
                continue;
            }

            int quantidade = carta.isBasico() ? 4 : 2;
            int espacoRestante = LIMITE_BARALHO - bot.getTamanhoBaralho();
            if (quantidade > espacoRestante) quantidade = espacoRestante;
            if (quantidade <= 0) break;

            bot.adicionarAoBaralho(carta);
            for (int i = 1; i < quantidade; i++) {
                CartaPokemon copia = new CartaPokemon(carta.getNome(), carta.getTipoElemento(),
                        carta.getHpMaximo(), carta.getDanoAtaque(), carta.getEvoluiDe());
                copia.setNumeroDex(carta.getNumeroDex());
                bot.adicionarAoBaralho(copia);
            }
        }

        executor.shutdownNow();
    }

    private void escolherTimeDialog(Jogador jogador, boolean[] disponivel) {
        while (true) {
            final String[] escolha = { null };
            Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();

            JDialog dialogo = new JDialog((Frame) null, "Pokémon TCG", true);
            dialogo.setSize(tela);
            dialogo.setLocation(0, 0);
            dialogo.setResizable(false);
            dialogo.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel painel = criarPainelComFundo();
            painel.setPreferredSize(tela);

            JLabel titulo = new JLabel(jogador.getNome() + ", escolha seu time:", SwingConstants.CENTER);
            titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
            titulo.setForeground(Color.WHITE);
            titulo.setBounds(0, tela.height / 2 - 260, tela.width, 50);
            painel.add(titulo);

            int larguraBotao = 380;
            int alturaBotao = 55;
            int gap = 15;
            int xBotao = (tela.width - larguraBotao) / 2;

            java.util.List<String[]> opcoes = new java.util.ArrayList<>();
            if (disponivel[0]) opcoes.add(new String[]{"\uD83D\uDCA7 Água", "AGUA"});
            if (disponivel[1]) opcoes.add(new String[]{"\uD83D\uDD25 Fogo", "FOGO"});
            if (disponivel[2]) opcoes.add(new String[]{"\uD83C\uDF3F Planta", "PLANTA"});
            opcoes.add(new String[]{"\uD83C\uDF0D Montar Time Customizado (Gen 1 a 6)", "CUSTOM"});

            int yAtual = tela.height / 2 - 180;
            for (String[] opcao : opcoes) {
                JButton botao = criarBotaoMenuPrincipal(opcao[0]);
                botao.setBounds(xBotao, yAtual, larguraBotao, alturaBotao);
                String codigo = opcao[1];
                botao.addActionListener(e -> {
                    escolha[0] = codigo;
                    dialogo.dispose();
                });
                painel.add(botao);
                yAtual += alturaBotao + gap;
            }

            dialogo.setContentPane(painel);
            dialogo.setVisible(true);

            if (escolha[0] == null) continue;

            if (escolha[0].equals("AGUA")) {
                App.adicionarTimeAgua(jogador);
                disponivel[0] = false;
                return;
            } else if (escolha[0].equals("FOGO")) {
                App.adicionarTimeFogo(jogador);
                disponivel[1] = false;
                return;
            } else if (escolha[0].equals("PLANTA")) {
                App.adicionarTimePlanta(jogador);
                disponivel[2] = false;
                return;
            } else if (escolha[0].equals("CUSTOM")) {
                boolean concluiu = abrirConstrutorDeTime(jogador);
                if (concluiu) return;
            }
        }
    }

    private boolean abrirConstrutorDeTime(Jogador jogador) {
        JDialog dialog = new JDialog((Frame) null, "Montar Time Customizado — " + jogador.getNome(), true);
        dialog.setSize(650, 650);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.getContentPane().setBackground(COR_FUNDO);

        // Cabeçalho com um mini-desenho da Pokébola, dando aquele toque visual sem prejudicar a leitura da lista
        JPanel cabecalhoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        cabecalhoPanel.setBackground(COR_BANNER);
        JPanel iconePokebola = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharPokebolaAberta((Graphics2D) g, 20, 20, 32);
            }
        };
        iconePokebola.setOpaque(false);
        iconePokebola.setPreferredSize(new Dimension(40, 40));
        JLabel tituloCabecalho = new JLabel("Montar Time Customizado — " + jogador.getNome());
        tituloCabecalho.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tituloCabecalho.setForeground(Color.WHITE);
        cabecalhoPanel.add(iconePokebola);
        cabecalhoPanel.add(tituloCabecalho);

        JPanel topoPanel = new JPanel(new BorderLayout());
        topoPanel.add(cabecalhoPanel, BorderLayout.NORTH);

        JPanel geracoesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        geracoesPanel.setBackground(COR_FUNDO);
        JButton btnGen1 = new JButton("1ª Geração (1-151)");
        JButton btnGen2 = new JButton("2ª Geração (152-251)");
        JButton btnGen3 = new JButton("3ª Geração (252-386)");
        JButton btnGen4 = new JButton("4ª Geração (387-493)");
        JButton btnGen5 = new JButton("5ª Geração (494-649)");
        JButton btnGen6 = new JButton("6ª Geração (650-721)");
        for (JButton b : new JButton[]{btnGen1, btnGen2, btnGen3, btnGen4, btnGen5, btnGen6}) {
            b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            geracoesPanel.add(b);
        }

        DefaultListModel<String> modeloLista = new DefaultListModel<>();
        JList<String> listaPokemon = new JList<>(modeloLista);
        listaPokemon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listaPokemon.setFixedCellHeight(38);
        JScrollPane listaScroll = new JScrollPane(listaPokemon);
        listaScroll.setBorder(criarBordaTitulo("Selecione um Pokémon"));

        JLabel statusLabel = new JLabel("Escolha uma geração pra começar a explorar.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JLabel contadorLabel = new JLabel("0 Pokémon adicionados ao time.");
        contadorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        int[] offsetAtual = {0};
        int[] contador = {0};

        listaPokemon.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                int numeroDex = offsetAtual[0] + index + 1;
                String tipo = obterTipoParaLista(numeroDex, listaPokemon);
                String prefixoTipo = tipo != null ? emojiDoTipo(tipo) + " " : "";
                label.setText(prefixoTipo + "#" + numeroDex + "  " + value);
                label.setIcon(obterSpriteParaLista(numeroDex, listaPokemon));
                label.setIconTextGap(10);
                return label;
            }
        });

        ActionListener carregarGeracao = e -> {
            Object origem = e.getSource();
            int offset;
            int limite;
            if (origem == btnGen1) { offset = 0; limite = 151; }
            else if (origem == btnGen2) { offset = 151; limite = 100; }
            else if (origem == btnGen3) { offset = 251; limite = 135; }
            else if (origem == btnGen4) { offset = 386; limite = 107; }
            else if (origem == btnGen5) { offset = 493; limite = 156; }
            else { offset = 649; limite = 72; }

            offsetAtual[0] = offset;
            statusLabel.setText("⏳ Carregando lista de Pokémon...");
            modeloLista.clear();

            new Thread(() -> {
                try {
                    java.util.List<String> nomes = buscarNomesGeracao(offset, limite);
                    SwingUtilities.invokeLater(() -> {
                        for (String n : nomes) modeloLista.addElement(capitalizar(n));
                        statusLabel.setText("✅ " + nomes.size() + " Pokémon carregados. Selecione um e clique em Adicionar.");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("❌ Erro ao carregar (verifique sua internet)."));
                }
            }).start();
        };
        btnGen1.addActionListener(carregarGeracao);
        btnGen2.addActionListener(carregarGeracao);
        btnGen3.addActionListener(carregarGeracao);
        btnGen4.addActionListener(carregarGeracao);
        btnGen5.addActionListener(carregarGeracao);
        btnGen6.addActionListener(carregarGeracao);

        JButton btnAdicionar = new JButton("➕ Adicionar Selecionado ao Time");
        btnAdicionar.addActionListener(e -> {
            int indice = listaPokemon.getSelectedIndex();
            if (indice == -1) {
                JOptionPane.showMessageDialog(dialog, "Selecione um Pokémon na lista primeiro!");
                return;
            }

            if (contador[0] >= LIMITE_BARALHO) {
                JOptionPane.showMessageDialog(dialog, "⚠️ Seu time já está no limite de " + LIMITE_BARALHO
                        + " cartas! Não é possível adicionar mais Pokémon.");
                return;
            }

            int numeroDex = offsetAtual[0] + indice + 1;
            btnAdicionar.setEnabled(false);
            statusLabel.setText("⏳ Buscando linha evolutiva completa...");

            new Thread(() -> {
                java.util.List<CartaPokemon> linhaCompleta = buscarLinhaCompleta(numeroDex);
                SwingUtilities.invokeLater(() -> {
                    if (linhaCompleta.isEmpty()) {
                        btnAdicionar.setEnabled(true);
                        statusLabel.setText("❌ Erro ao buscar esse Pokémon. Tenta de novo.");
                        return;
                    }

                    StringBuilder resumo = new StringBuilder();
                    for (CartaPokemon carta : linhaCompleta) {
                        if (contador[0] >= LIMITE_BARALHO) break;

                        int quantidade = carta.isBasico() ? 4 : 2;

                        int espacoRestante = LIMITE_BARALHO - contador[0];
                        if (quantidade > espacoRestante) quantidade = espacoRestante;
                        if (quantidade <= 0) break;

                        jogador.adicionarAoBaralho(carta);

                        for (int i = 1; i < quantidade; i++) {
                            CartaPokemon copia = new CartaPokemon(
                                    carta.getNome(), carta.getTipoElemento(),
                                    carta.getHpMaximo(), carta.getDanoAtaque(), carta.getEvoluiDe());
                            copia.setNumeroDex(carta.getNumeroDex());
                            jogador.adicionarAoBaralho(copia);
                        }
                        contador[0] += quantidade;
                        resumo.append(quantidade).append("x ").append(carta.getNome()).append(", ");
                    }

                    contadorLabel.setText(contador[0] + " / " + LIMITE_BARALHO + " Pokémon adicionados ao time.");
                    if (resumo.length() > 0) {
                        resumo.setLength(resumo.length() - 2);
                        statusLabel.setText("✅ Linha completa adicionada: " + resumo);
                    } else {
                        statusLabel.setText("⚠️ Não havia mais espaço no time (limite de " + LIMITE_BARALHO + " cartas).");
                    }
                    btnAdicionar.setEnabled(contador[0] < LIMITE_BARALHO);
                });
            }).start();
        });

        JButton btnConcluir = new JButton("✅ Concluir Time");
        boolean[] concluiuComSucesso = {false};
        btnConcluir.addActionListener(e -> {
            if (contador[0] == 0) {
                JOptionPane.showMessageDialog(dialog, "Adicione pelo menos 1 Pokémon antes de concluir!");
                return;
            }
            concluiuComSucesso[0] = true;
            dialog.dispose();
        });

        JPanel rodapePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        rodapePanel.setBackground(COR_FUNDO);
        rodapePanel.add(contadorLabel);
        rodapePanel.add(btnAdicionar);
        rodapePanel.add(btnConcluir);

        JPanel sulPanel = new JPanel(new BorderLayout());
        sulPanel.setBackground(COR_FUNDO);
        sulPanel.add(statusLabel, BorderLayout.NORTH);
        sulPanel.add(rodapePanel, BorderLayout.SOUTH);

        JScrollPane geracoesScroll = new JScrollPane(geracoesPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        geracoesScroll.setBorder(BorderFactory.createEmptyBorder());
        geracoesScroll.setPreferredSize(new Dimension(600, 50));

        topoPanel.add(geracoesScroll, BorderLayout.CENTER);
        dialog.add(topoPanel, BorderLayout.NORTH);
        dialog.add(listaScroll, BorderLayout.CENTER);
        dialog.add(sulPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);

        return concluiuComSucesso[0];
    }

    private java.util.List<String> buscarNomesGeracao(int offset, int limite) throws Exception {
        String json = fetchUrl("https://pokeapi.co/api/v2/pokemon?offset=" + offset + "&limit=" + limite);
        java.util.List<String> nomes = new java.util.ArrayList<>();
        Matcher m = Pattern.compile("\"name\":\"([a-z0-9\\-]+)\"").matcher(json);
        while (m.find()) nomes.add(m.group(1));
        return nomes;
    }

    private CartaPokemon buscarPokemon(int numeroDex) {
        try {
            String jsonPokemon = fetchUrl("https://pokeapi.co/api/v2/pokemon/" + numeroDex);
            String nome = capitalizar(extrairString(jsonPokemon, "name"));

            Matcher tipoMatcher = Pattern.compile("\"type\":\\{\"name\":\"([a-z]+)\"").matcher(jsonPokemon);
            String tipoIngles = tipoMatcher.find() ? tipoMatcher.group(1) : "normal";
            String tipo = traduzirTipo(tipoIngles);

            int hp = extrairStat(jsonPokemon, "hp");
            int dano = extrairStat(jsonPokemon, "attack");
            if (hp <= 0) hp = 50;
            if (dano <= 0) dano = 20;

            String jsonSpecies = fetchUrl("https://pokeapi.co/api/v2/pokemon-species/" + numeroDex);
            Matcher evoMatcher = Pattern.compile("\"evolves_from_species\":\\{\"name\":\"([a-z0-9\\-]+)\"").matcher(jsonSpecies);
            String evoluiDe = evoMatcher.find() ? capitalizar(evoMatcher.group(1)) : null;

            CartaPokemon carta = new CartaPokemon(nome, tipo, hp, dano, evoluiDe);
            carta.setNumeroDex(numeroDex);
            return carta;
        } catch (Exception e) {
            return null;
        }
    }

    private int buscarNumeroPreEvolucao(int numeroDex) {
        try {
            String jsonSpecies = fetchUrl("https://pokeapi.co/api/v2/pokemon-species/" + numeroDex);
            Matcher m = Pattern.compile(
                    "\"evolves_from_species\":\\{\"name\":\"[a-z0-9\\-]+\",\"url\":\"https://pokeapi\\.co/api/v2/pokemon-species/(\\d+)/\""
            ).matcher(jsonSpecies);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {

        }
        return -1;
    }

    private java.util.List<CartaPokemon> buscarLinhaCompleta(int numeroDexEscolhido) {
        java.util.List<CartaPokemon> cadeia = new java.util.ArrayList<>();
        java.util.List<Integer> numerosJaVistos = new java.util.ArrayList<>();

        int numeroAtual = numeroDexEscolhido;
        while (numeroAtual != -1 && !numerosJaVistos.contains(numeroAtual)) {
            numerosJaVistos.add(numeroAtual);

            CartaPokemon carta = buscarPokemon(numeroAtual);
            if (carta == null) break;

            cadeia.add(0, carta);
            numeroAtual = buscarNumeroPreEvolucao(numeroAtual);
        }

        return cadeia;
    }

    private int extrairStat(String json, String nomeStat) {
        Matcher m = Pattern.compile("\"base_stat\":(\\d+),\"effort\":\\d+,\"stat\":\\{\"name\":\"" + nomeStat + "\"").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private String extrairString(String json, String chave) {
        Matcher m = Pattern.compile("\"" + chave + "\":\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : "desconhecido";
    }

    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return s;
        String limpo = s.replace("-", " ");
        return limpo.substring(0, 1).toUpperCase() + limpo.substring(1);
    }

    private String traduzirTipo(String tipoIngles) {
        switch (tipoIngles) {
            case "water": return "Água";
            case "fire": return "Fogo";
            case "grass": return "Planta";
            case "electric": return "Elétrico";
            case "psychic": return "Psíquico";
            case "ice": return "Gelo";
            case "dragon": return "Dragão";
            case "dark": return "Sombrio";
            case "fairy": return "Fada";
            case "fighting": return "Lutador";
            case "flying": return "Voador";
            case "poison": return "Veneno";
            case "ground": return "Terra";
            case "rock": return "Pedra";
            case "bug": return "Inseto";
            case "ghost": return "Fantasma";
            case "steel": return "Aço";
            default: return "Normal";
        }
    }

    private String emojiDoTipo(String tipo) {
        switch (tipo) {
            case "Água": return "\uD83D\uDCA7";
            case "Fogo": return "\uD83D\uDD25";
            case "Planta": return "\uD83C\uDF3F";
            case "Elétrico": return "\u26A1";
            case "Psíquico": return "\uD83D\uDD2E";
            case "Gelo": return "\u2744\uFE0F";
            case "Dragão": return "\uD83D\uDC09";
            case "Sombrio": return "\uD83C\uDF11";
            case "Fada": return "\u2728";
            case "Lutador": return "\uD83E\uDD4A";
            case "Voador": return "\uD83D\uDD4A\uFE0F";
            case "Veneno": return "\u2620\uFE0F";
            case "Terra": return "\uD83C\uDF0D";
            case "Pedra": return "\uD83E\uDEA8";
            case "Inseto": return "\uD83D\uDC1B";
            case "Fantasma": return "\uD83D\uDC7B";
            case "Aço": return "\u2699\uFE0F";
            case "Normal": return "\u2B50";
            default: return "\u2754";
        }
    }

    private String fetchUrl(String urlStr) throws Exception {
        java.net.URL url = java.net.URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String linha;
            while ((linha = reader.readLine()) != null) sb.append(linha);
        }
        return sb.toString();
    }

    private void montarJanela() {
        frame = new JFrame("Pokémon TCG - Versus [" + VERSAO_BUILD + "]");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 800);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(COR_FUNDO);

        flashPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (alphaFlash > 0) {
                    g.setColor(new Color(corFlash.getRed(), corFlash.getGreen(), corFlash.getBlue(), alphaFlash));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        flashPane.setOpaque(false);
        flashPane.setLayout(null);
        frame.setGlassPane(flashPane);
        flashPane.setVisible(true);

        JPanel topoPanel = new JPanel(new GridLayout(2, 1));
        topoPanel.setBackground(COR_BANNER);
        topoPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        turnoLabel = new JLabel("", SwingConstants.CENTER);
        turnoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        turnoLabel.setForeground(Color.WHITE);

        adversarioLabel = new JLabel("", SwingConstants.CENTER);
        adversarioLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        adversarioLabel.setForeground(new Color(200, 210, 230));

        topoPanel.add(turnoLabel);
        topoPanel.add(adversarioLabel);
        frame.add(topoPanel, BorderLayout.NORTH);

        JPanel centroPanel = new JPanel();
        centroPanel.setLayout(new BoxLayout(centroPanel, BoxLayout.Y_AXIS));
        centroPanel.setBackground(COR_FUNDO);
        centroPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        campoJogadorPanel = new JPanel();
        campoJogadorPanel.setBackground(COR_FUNDO);
        campoJogadorPanel.setBorder(criarBordaTitulo("🎮 Seu Campo de Batalha"));
        campoJogadorPanel.setLayout(new BorderLayout(12, 0));

        maoPanel = new JPanel();
        maoPanel.setBackground(COR_FUNDO);
        maoPanel.setBorder(criarBordaTitulo("🃏 Sua Mão (clique numa carta pra jogar)"));
        maoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JScrollPane maoScroll = new JScrollPane(maoPanel);
        maoScroll.setPreferredSize(new Dimension(620, 260));
        maoScroll.setBorder(BorderFactory.createEmptyBorder());

        centroPanel.add(campoJogadorPanel);
        centroPanel.add(Box.createVerticalStrut(28));
        centroPanel.add(maoScroll);

        frame.add(centroPanel, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(28, 28, 38));
        logArea.setForeground(new Color(180, 255, 180));
        logArea.setMargin(new Insets(8, 8, 8, 8));
        logScroll = new JScrollPane(logArea);
        logScroll.setBorder(criarBordaTitulo("📜 Log da Partida"));

        JPanel rodapePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        rodapePanel.setBackground(COR_BANNER);

        JButton btnAtacar = criarBotaoAcao("⚔️ Atacar", new Color(211, 47, 47));
        btnAtacar.addActionListener(e -> acaoAtacar());

        JButton btnPassar = criarBotaoAcao("🏳️ Passar Turno", new Color(96, 125, 139));
        btnPassar.addActionListener(e -> acaoPassarTurno());

        JButton btnHistorico = criarBotaoAcao("📜 Ver Histórico", new Color(84, 58, 133));
        btnHistorico.addActionListener(e -> abrirHistoricoDialog());

        JButton btnSair = criarBotaoAcao("🚪 Sair do Jogo", new Color(66, 66, 66));
        btnSair.addActionListener(e -> System.exit(0));

        rodapePanel.add(btnAtacar);
        rodapePanel.add(btnPassar);
        rodapePanel.add(btnHistorico);
        rodapePanel.add(btnSair);
        frame.add(rodapePanel, BorderLayout.SOUTH);
    }

    private void abrirHistoricoDialog() {
        JDialog dialogHistorico = new JDialog(frame, "📜 Histórico da Partida", false);
        dialogHistorico.setSize(480, 550);
        dialogHistorico.setLocationRelativeTo(frame);
        dialogHistorico.setLayout(new BorderLayout());
        dialogHistorico.add(logScroll, BorderLayout.CENTER);
        dialogHistorico.setVisible(true);
    }

    private javax.swing.border.Border criarBordaTitulo(String titulo) {
        javax.swing.border.TitledBorder borda = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 210), 1), titulo);
        borda.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        borda.setTitleColor(COR_BANNER);
        return borda;
    }

    private JButton criarBotaoAcao(String texto, Color cor) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 15));
        botao.setBackground(cor);
        botao.setForeground(Color.WHITE);
        botao.setOpaque(true);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setPreferredSize(new Dimension(170, 42));
        return botao;
    }

    private Color corDoTipo(String tipo) {
        switch (tipo) {
            case "Água": return COR_AGUA;
            case "Fogo": return COR_FOGO;
            case "Planta": return COR_PLANTA;
            default: return COR_NORMAL;
        }
    }

    private String barraHP(int hpAtual, int hpMaximo) {
        int total = 10;
        int preenchido = (int) Math.round((hpAtual / (double) hpMaximo) * total);
        preenchido = Math.max(0, Math.min(total, preenchido));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < total; i++) sb.append(i < preenchido ? "█" : "░");
        return sb.toString();
    }

    private ImageIcon carregarSprite(CartaPokemon carta) {
        int numero = carta.getNumeroDex() > 0 ? carta.getNumeroDex() : -1;

        if (numero == -1) {
            Integer numeroPorNome = POKEDEX.get(carta.getNome());
            if (numeroPorNome != null) numero = numeroPorNome;
        }

        if (numero == -1) {
            return null;
        }

        String chaveCache = carta.getNome() + "#" + numero;
        if (cacheSprites.containsKey(chaveCache)) {
            return cacheSprites.get(chaveCache);
        }

        if (!chavesCarregandoAgora.contains(chaveCache)) {
            chavesCarregandoAgora.add(chaveCache);
            final int numeroFinal = numero;

            new Thread(() -> {
                ImageIcon icone = null;
                try {
                    String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numeroFinal + ".png";
                    java.awt.Image imagem = ImageIO.read(java.net.URI.create(url).toURL());
                    java.awt.Image redimensionada = imagem.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
                    icone = new ImageIcon(redimensionada);
                } catch (Exception e) {

                }

                ImageIcon iconeFinal = icone;
                SwingUtilities.invokeLater(() -> {
                    cacheSprites.put(chaveCache, iconeFinal);
                    chavesCarregandoAgora.remove(chaveCache);
                    if (frame != null && frame.isShowing()) {
                        atualizarTela();
                    }
                });
            }).start();
        }

        return null;
    }

    private ImageIcon obterSpriteParaLista(int numeroDex, JList<String> lista) {
        if (cacheSpritesPorNumero.containsKey(numeroDex)) {
            return cacheSpritesPorNumero.get(numeroDex);
        }

        if (!spritesCarregandoAgora.contains(numeroDex)) {
            spritesCarregandoAgora.add(numeroDex);

            new Thread(() -> {
                ImageIcon icone = null;
                try {
                    String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numeroDex + ".png";
                    java.awt.Image imagem = ImageIO.read(java.net.URI.create(url).toURL());
                    java.awt.Image redimensionada = imagem.getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);
                    icone = new ImageIcon(redimensionada);
                } catch (Exception e) {
                    icone = null;
                }

                ImageIcon iconeFinal = icone;
                SwingUtilities.invokeLater(() -> {
                    cacheSpritesPorNumero.put(numeroDex, iconeFinal);
                    spritesCarregandoAgora.remove(numeroDex);
                    lista.repaint();
                });
            }).start();
        }

        return null;
    }

    /**
     * Busca (em segundo plano, com cache) o tipo elemental de um Pokémon pra mostrar o símbolo
     * do elemento do lado do nome na lista, antes mesmo de você adicionar ele ao time.
     */
    private String obterTipoParaLista(int numeroDex, JList<String> lista) {
        if (cacheTipoPorNumero.containsKey(numeroDex)) {
            return cacheTipoPorNumero.get(numeroDex);
        }

        if (!tiposCarregandoAgora.contains(numeroDex)) {
            tiposCarregandoAgora.add(numeroDex);

            new Thread(() -> {
                String tipo = null;
                try {
                    String json = fetchUrl("https://pokeapi.co/api/v2/pokemon/" + numeroDex);
                    Matcher m = Pattern.compile("\"type\":\\{\"name\":\"([a-z]+)\"").matcher(json);
                    if (m.find()) {
                        tipo = traduzirTipo(m.group(1));
                    }
                } catch (Exception e) {
                    tipo = null;
                }

                String tipoFinal = tipo;
                SwingUtilities.invokeLater(() -> {
                    cacheTipoPorNumero.put(numeroDex, tipoFinal);
                    tiposCarregandoAgora.remove(numeroDex);
                    lista.repaint();
                });
            }).start();
        }

        return null;
    }

    private void redirecionarConsoleParaLog() {
        PrintStream logStream = new PrintStream(new OutputStream() {
            private final StringBuilder linhaAtual = new StringBuilder();

            @Override
            public void write(int b) {
                char c = (char) b;
                logArea.append(String.valueOf(c));
                logArea.setCaretPosition(logArea.getDocument().getLength());

                if (c == '\n') {
                    String linha = linhaAtual.toString().trim();
                    linhaAtual.setLength(0);
                    if (!linha.isEmpty()) {
                        SwingUtilities.invokeLater(() -> mostrarNotificacao(linha));
                    }
                } else {
                    linhaAtual.append(c);
                }
            }
        }, true);
        System.setOut(logStream);
    }

    private void iniciarTurno() {
        if (!jogoAtivo) return;

        boolean primeiroTurno = (jogadorAtual == jogador1) ? jogador1PrimeiroTurno : jogador2PrimeiroTurno;
        if (!primeiroTurno) {
            boolean conseguiuComprar = jogadorAtual.comprarCarta();
            if (!conseguiuComprar) {
                encerrarJogoComDerrota(jogadorAtual, adversario);
                return;
            }
        } else {
            if (jogadorAtual == jogador1) jogador1PrimeiroTurno = false;
            else jogador2PrimeiroTurno = false;
        }

        jogadorAtual.resetarEvolucoesDoTurno();
        ehTurnoDoBot = (jogadorBot != null && jogadorAtual == jogadorBot);
        atualizarTela();

        if (ehTurnoDoBot) {

            Timer timer = new Timer(1200, e -> executarTurnoBot());
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void trocarTurno() {
        Jogador troca = jogadorAtual;
        jogadorAtual = adversario;
        adversario = troca;
        numeroTurno++;
        iniciarTurno();
    }

    private void executarTurnoBot() {
        if (!jogoAtivo) return;

        Jogador bot = jogadorAtual;
        Jogador oponenteDoBot = adversario;

        if (bot.getPokemonAtivo() == null) {
            int indiceBasico = encontrarIndiceBasicoNaMao(bot);
            if (indiceBasico != -1) {
                bot.colocarPokemonEmCampo(indiceBasico, 0);
            }
        }

        int indiceBasico = encontrarIndiceBasicoNaMao(bot);
        while (indiceBasico != -1 && bot.getBanco().size() < 5) {
            bot.colocarPokemonEmCampo(indiceBasico, 1);
            indiceBasico = encontrarIndiceBasicoNaMao(bot);
        }

        if (bot.getPokemonAtivo() != null) {
            int indiceEvolucao = encontrarIndiceEvolucaoParaAlvo(bot, bot.getPokemonAtivo());
            if (indiceEvolucao != -1) {
                bot.evoluir(indiceEvolucao, bot.getPokemonAtivo(), numeroTurno);
            }
        }

        if (bot.getPokemonAtivo() != null) {
            bot.anexarEnergia(bot.getPokemonAtivo());
        }

        atualizarTela();

        if (bot.getPokemonAtivo() != null && bot.getPokemonAtivo().getQuantidadeEnergias() >= 1) {
            boolean atacou = bot.atacar(oponenteDoBot);
            if (atacou) {
                mostrarFlashDeAtaque(new Color(220, 40, 40));

                if (oponenteDoBot.getPokemonAtivo() == null && oponenteDoBot.getBanco().isEmpty()) {
                    jogoAtivo = false;
                    mostrarTelaFimDeJogo(bot, oponenteDoBot, "Não sobrou nenhum Pokémon em campo!");
                    return;
                }

                bot.encerrarRodada();
                System.out.println("🤖 " + bot.getNome() + " encerrou o turno após atacar.");
                trocarTurno();
                return;
            }
        }

        bot.encerrarRodada();
        System.out.println("🤖 " + bot.getNome() + " passou o turno.");
        trocarTurno();
    }

    private int encontrarIndiceBasicoNaMao(Jogador jogador) {
        List<Carta> mao = jogador.getMao();
        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaPokemon && ((CartaPokemon) mao.get(i)).isBasico()) {
                return i;
            }
        }
        return -1;
    }

    private int encontrarIndiceEvolucaoParaAlvo(Jogador jogador, CartaPokemon alvo) {
        List<Carta> mao = jogador.getMao();
        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaPokemon) {
                CartaPokemon carta = (CartaPokemon) mao.get(i);
                if (!carta.isBasico() && carta.getEvoluiDe().equalsIgnoreCase(alvo.getNome())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void encerrarJogoComDerrota(Jogador perdedor, Jogador vencedor) {
        jogoAtivo = false;
        turnoLabel.setText("🏆 " + vencedor.getNome() + " venceu a partida!");
        mostrarTelaFimDeJogo(vencedor, perdedor, perdedor.getNome() + " não tinha mais cartas pra comprar!");
    }

    private void mostrarTelaFimDeJogo(Jogador vencedor, Jogador perdedor, String motivo) {
        JDialog telaFim = new JDialog(frame, "Fim de Jogo", true);
        telaFim.setSize(480, 380);
        telaFim.setLocationRelativeTo(frame);
        telaFim.setLayout(new BorderLayout());
        telaFim.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel banner = new JPanel(new GridLayout(2, 1));
        banner.setBackground(new Color(255, 193, 7));
        banner.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));

        JLabel trofeu = new JLabel("🏆", SwingConstants.CENTER);
        trofeu.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));

        JLabel tituloVencedor = new JLabel(vencedor.getNome() + " VENCEU!", SwingConstants.CENTER);
        tituloVencedor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        tituloVencedor.setForeground(COR_BANNER);

        banner.add(trofeu);
        banner.add(tituloVencedor);

        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setBackground(COR_FUNDO);
        corpo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel motivoLabel = new JLabel("<html><div style='text-align:center;'>💀 " + perdedor.getNome()
                + " perdeu a partida.<br>" + motivo + "</div></html>", SwingConstants.CENTER);
        motivoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        motivoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel turnoFinalLabel = new JLabel("Partida encerrada no Turno " + numeroTurno + ".", SwingConstants.CENTER);
        turnoFinalLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        turnoFinalLabel.setForeground(Color.GRAY);
        turnoFinalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        turnoFinalLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JButton btnFechar = criarBotaoAcao("🚪 Fechar o Jogo", new Color(66, 66, 66));
        btnFechar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnFechar.addActionListener(e -> System.exit(0));

        corpo.add(motivoLabel);
        corpo.add(turnoFinalLabel);
        corpo.add(btnFechar);

        telaFim.add(banner, BorderLayout.NORTH);
        telaFim.add(corpo, BorderLayout.CENTER);

        telaFim.setVisible(true);
    }

    private void atualizarTela() {
        turnoLabel.setText("TURNO " + numeroTurno + " — VEZ DE " + jogadorAtual.getNome().toUpperCase());
        adversarioLabel.setText("🆚 Pokémon Ativo de " + adversario.getNome() + ": "
                + (adversario.getPokemonAtivo() != null ? adversario.getPokemonAtivo().toString() : "[Nenhum]")
                + "   |   🃏 Sua Mão: " + jogadorAtual.getMao().size() + " cartas");

        montarCampoJogador();
        montarMaoJogador();

        frame.revalidate();
        frame.repaint();
    }

    private void montarCampoJogador() {
        campoJogadorPanel.removeAll();

        JPanel zonaEsquerda = new JPanel();
        zonaEsquerda.setLayout(new BoxLayout(zonaEsquerda, BoxLayout.Y_AXIS));
        zonaEsquerda.setBackground(COR_FUNDO);

        JPanel linhaAtivo = new JPanel(new GridLayout(1, 5, 10, 6));
        linhaAtivo.setBackground(COR_FUNDO);
        JLabel rotuloAtivo = new JLabel("⚔️ ZONA AVANÇADA (Ativo)");
        rotuloAtivo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rotuloAtivo.setForeground(COR_BANNER);

        CartaPokemon ativo = jogadorAtual.getPokemonAtivo();
        JButton btnAtivo = new JButton(ativo != null ? htmlPokemon("🔴 ATIVO", ativo) : htmlVazio("🔴 ATIVO"));
        estilizarBotaoCampo(btnAtivo, ativo);
        if (ativo != null) {
            btnAtivo.addActionListener(e -> abrirMenuPokemon(ativo, -1));
        } else {
            btnAtivo.setEnabled(false);
        }

        linhaAtivo.add(criarEspacoInvisivel());
        linhaAtivo.add(criarEspacoInvisivel());
        linhaAtivo.add(btnAtivo);
        linhaAtivo.add(criarEspacoInvisivel());
        linhaAtivo.add(criarEspacoInvisivel());

        JPanel linhaBanco = new JPanel(new GridLayout(1, 5, 10, 6));
        linhaBanco.setBackground(COR_FUNDO);

        List<CartaPokemon> banco = jogadorAtual.getBanco();
        for (int i = 0; i < 5; i++) {
            if (i < banco.size()) {
                CartaPokemon p = banco.get(i);
                JButton btnBanco = new JButton(htmlPokemon("🔵 BANCO " + (i + 1), p));
                estilizarBotaoCampo(btnBanco, p);
                final int indiceBanco = i;
                btnBanco.addActionListener(e -> abrirMenuPokemon(p, indiceBanco));
                linhaBanco.add(btnBanco);
            } else {
                JButton btnVazio = new JButton(htmlVazio("🔵 BANCO " + (i + 1)));
                estilizarBotaoCampo(btnVazio, null);
                btnVazio.setEnabled(false);
                linhaBanco.add(btnVazio);
            }
        }

        JLabel rotuloBanco = new JLabel("🛡️ BANCO DE RESERVAS");
        rotuloBanco.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rotuloBanco.setForeground(COR_BANNER);
        rotuloBanco.setBorder(BorderFactory.createEmptyBorder(4, 12, 0, 0));

        zonaEsquerda.add(rotuloAtivo);
        zonaEsquerda.add(linhaAtivo);
        zonaEsquerda.add(rotuloBanco);
        zonaEsquerda.add(linhaBanco);

        campoJogadorPanel.add(zonaEsquerda, BorderLayout.CENTER);

        JPanel zonaDireita = new JPanel();
        zonaDireita.setLayout(new BoxLayout(zonaDireita, BoxLayout.Y_AXIS));
        zonaDireita.setBackground(COR_FUNDO);
        zonaDireita.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JButton btnBaralho = new JButton("<html><div style='text-align:center;'>📚 <b>BARALHO</b><br><br>"
                + "<b style='font-size:16px;'>" + jogadorAtual.getTamanhoBaralho() + "</b><br>cartas restantes</div></html>");
        btnBaralho.setPreferredSize(new Dimension(150, 130));
        btnBaralho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBaralho.setBackground(new Color(84, 58, 133));
        btnBaralho.setForeground(Color.WHITE);
        btnBaralho.setOpaque(true);
        btnBaralho.setFocusPainted(false);
        btnBaralho.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBaralho.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Você tem " + jogadorAtual.getTamanhoBaralho() + " carta(s) no baralho.\n\n"
                        + "A compra é automática: você puxa 1 carta sozinha no início de cada turno seu — não precisa clicar aqui pra comprar.\n\n"
                        + "⚠️ Se o baralho zerar na hora de comprar, você perde o jogo na hora!",
                "Baralho", JOptionPane.INFORMATION_MESSAGE));

        List<CartaPokemon> zonaMorta = jogadorAtual.getZonaMorta();
        JButton btnZonaMorta = new JButton(construirRotuloZonaMorta(zonaMorta));
        btnZonaMorta.setPreferredSize(new Dimension(150, 130));
        btnZonaMorta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnZonaMorta.setBackground(new Color(66, 66, 66));
        btnZonaMorta.setForeground(Color.WHITE);
        btnZonaMorta.setOpaque(true);
        btnZonaMorta.setFocusPainted(false);
        btnZonaMorta.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (!zonaMorta.isEmpty()) {
            ImageIcon spriteUltimoMorto = carregarSprite(zonaMorta.get(zonaMorta.size() - 1));
            if (spriteUltimoMorto != null) {
                btnZonaMorta.setIcon(spriteUltimoMorto);
                btnZonaMorta.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnZonaMorta.setHorizontalTextPosition(SwingConstants.CENTER);
            }
        }
        btnZonaMorta.addActionListener(e -> mostrarZonaMortaDialog(zonaMorta));

        zonaDireita.add(btnBaralho);
        zonaDireita.add(Box.createVerticalStrut(10));
        zonaDireita.add(btnZonaMorta);

        campoJogadorPanel.add(zonaDireita, BorderLayout.EAST);
    }

    private String construirRotuloZonaMorta(List<CartaPokemon> zonaMorta) {
        if (zonaMorta.isEmpty()) {
            return "<html><div style='text-align:center;'>☠️ <b>ZONA MORTA</b><br><br>[Vazio]</div></html>";
        }
        CartaPokemon ultimo = zonaMorta.get(zonaMorta.size() - 1);
        return "<html><div style='text-align:center;'>☠️ <b>ZONA MORTA</b><br>"
                + "<b>" + ultimo.getNome() + "</b><br>(" + zonaMorta.size() + " no total)</div></html>";
    }

    private void mostrarZonaMortaDialog(List<CartaPokemon> zonaMorta) {
        if (zonaMorta.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Nenhum Pokémon seu foi nocauteado ainda!");
            return;
        }
        StringBuilder sb = new StringBuilder("☠️ Pokémon nocauteados (o mais recente primeiro):\n\n");
        for (int i = zonaMorta.size() - 1; i >= 0; i--) {
            sb.append("• ").append(zonaMorta.get(i).getNome()).append("\n");
        }
        JOptionPane.showMessageDialog(frame, sb.toString(), "Zona Morta", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel criarEspacoInvisivel() {
        JPanel espaco = new JPanel();
        espaco.setOpaque(false);
        espaco.setPreferredSize(new Dimension(170, 140));
        return espaco;
    }

    private void estilizarBotaoCampo(JButton botao, CartaPokemon p) {
        botao.setPreferredSize(new Dimension(170, 140));
        botao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        botao.setOpaque(true);
        botao.setBorderPainted(true);
        botao.setFocusPainted(false);
        botao.setVerticalTextPosition(SwingConstants.BOTTOM);
        botao.setHorizontalTextPosition(SwingConstants.CENTER);
        if (p != null) {
            botao.setBackground(corDoTipo(p.getTipoElemento()));
            botao.setForeground(Color.WHITE);
            ImageIcon sprite = carregarSprite(p);
            if (sprite != null) botao.setIcon(sprite);
        } else {
            botao.setBackground(new Color(230, 230, 235));
            botao.setForeground(new Color(120, 120, 120));
        }
    }

    private String htmlPokemon(String cabecalho, CartaPokemon p) {
        return "<html><div style='text-align:center; width:150px;'>"
                + "<b>" + cabecalho + "</b><br>"
                + emojiDoTipo(p.getTipoElemento()) + " <b>" + p.getNome() + "</b><br>"
                + barraHP(p.getHpAtual(), p.getHpMaximo()) + "<br>"
                + "HP " + p.getHpAtual() + "/" + p.getHpMaximo() + "<br>"
                + "⚡ " + p.getQuantidadeEnergias() + "/" + p.getLimiteEnergias()
                + "</div></html>";
    }

    private String htmlVazio(String cabecalho) {
        return "<html><div style='text-align:center;'><b>" + cabecalho + "</b><br>[Vazio]</div></html>";
    }

    private void montarMaoJogador() {
        maoPanel.removeAll();

        List<Carta> mao = jogadorAtual.getMao();
        for (int i = 0; i < mao.size(); i++) {
            Carta carta = mao.get(i);
            final int indice = i;

            if (carta instanceof CartaPokemon) {
                CartaPokemon p = (CartaPokemon) carta;

                if (!p.isBasico() && !jogadorAtual.baseEmCampo(p.getEvoluiDe())) {
                    continue;
                }

                String rotulo = "<html><div style='text-align:center; width:130px;'>"
                        + (p.isBasico() ? "🟢 <b>" : "✨ <b>") + p.getNome() + "</b><br>"
                        + emojiDoTipo(p.getTipoElemento()) + " " + p.getTipoElemento() + "<br>"
                        + "HP " + p.getHpMaximo() + " | Dano " + p.getDanoAtaque()
                        + "</div></html>";
                JButton btnCarta = new JButton(rotulo);
                btnCarta.setPreferredSize(new Dimension(150, 110));
                btnCarta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnCarta.setBackground(corDoTipo(p.getTipoElemento()));
                btnCarta.setForeground(Color.WHITE);
                btnCarta.setOpaque(true);
                btnCarta.setFocusPainted(false);
                btnCarta.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnCarta.setHorizontalTextPosition(SwingConstants.CENTER);
                ImageIcon spriteMao = carregarSprite(p);
                if (spriteMao != null) btnCarta.setIcon(spriteMao);
                btnCarta.addActionListener(e -> acaoClicarCartaPokemon(indice, p));
                maoPanel.add(btnCarta);

            } else if (carta instanceof CartaTreinador) {
                CartaTreinador t = (CartaTreinador) carta;
                String rotulo = "<html><div style='text-align:center; width:120px;'>📘 <b>"
                        + t.getNome() + "</b><br>[" + t.getEfeito() + "]</div></html>";
                JButton btnCarta = new JButton(rotulo);
                btnCarta.setPreferredSize(new Dimension(150, 65));
                btnCarta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnCarta.setBackground(COR_TREINADOR);
                btnCarta.setForeground(new Color(60, 45, 0));
                btnCarta.setOpaque(true);
                btnCarta.setFocusPainted(false);
                btnCarta.addActionListener(e -> acaoClicarCartaTreinador(indice, t));
                maoPanel.add(btnCarta);
            }
        }
    }

    private void abrirMenuPokemon(CartaPokemon pokemon, int indiceBanco) {
        if (ehTurnoDoBot) return;
        boolean ehAtivo = (indiceBanco == -1);

        java.util.List<String> opcoes = new java.util.ArrayList<>();
        opcoes.add("⚡ Anexar Energia");
        opcoes.add("✨ Evoluir");
        if (!ehAtivo) opcoes.add("🔄 Recuar pra cá (trocar com o Ativo)");

        String escolha = (String) JOptionPane.showInputDialog(
                frame,
                pokemon.getNome() + " — o que fazer?",
                "Ação",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes.toArray(),
                opcoes.get(0)
        );

        if (escolha == null) return;

        if (escolha.contains("Anexar Energia")) {
            jogadorAtual.anexarEnergia(pokemon);
        } else if (escolha.contains("Evoluir")) {
            evoluirPokemonEscolhido(pokemon);
        } else if (escolha.contains("Recuar")) {
            jogadorAtual.recuar(indiceBanco);
        }

        atualizarTela();
    }

    private void evoluirPokemonEscolhido(CartaPokemon alvo) {
        List<Carta> mao = jogadorAtual.getMao();
        java.util.List<Integer> indicesValidos = new java.util.ArrayList<>();
        java.util.List<String> nomesValidos = new java.util.ArrayList<>();

        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaPokemon) {
                CartaPokemon carta = (CartaPokemon) mao.get(i);
                if (!carta.isBasico() && carta.getEvoluiDe().equalsIgnoreCase(alvo.getNome())) {
                    indicesValidos.add(i);
                    nomesValidos.add(carta.getNome());
                }
            }
        }

        if (indicesValidos.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Você não tem nenhuma evolução de " + alvo.getNome() + " na mão!");
            return;
        }

        String escolha = (String) JOptionPane.showInputDialog(
                frame, "Evoluir " + alvo.getNome() + " para qual forma?", "Evoluir",
                JOptionPane.QUESTION_MESSAGE, null, nomesValidos.toArray(), nomesValidos.get(0)
        );

        if (escolha == null) return;

        int posicaoNaLista = nomesValidos.indexOf(escolha);
        int indiceNaMao = indicesValidos.get(posicaoNaLista);

        jogadorAtual.evoluir(indiceNaMao, alvo, numeroTurno);
    }

    private void acaoClicarCartaPokemon(int indiceNaMao, CartaPokemon carta) {
        if (ehTurnoDoBot) return;
        if (!carta.isBasico()) {
            JOptionPane.showMessageDialog(frame,
                    "⚠️ " + carta.getNome() + " é uma evolução! Clique no Pokémon em campo (não na carta da mão) pra evoluir.");
            return;
        }

        Object[] opcoes = { "🔴 Pokémon Ativo", "🔵 Banco de Reservas" };
        int escolha = JOptionPane.showOptionDialog(frame,
                "Colocar " + carta.getNome() + " onde?",
                "Baixar Pokémon",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[0]);

        if (escolha == 0) {
            jogadorAtual.colocarPokemonEmCampo(indiceNaMao, 0);
        } else if (escolha == 1) {
            jogadorAtual.colocarPokemonEmCampo(indiceNaMao, 1);
        } else {
            return;
        }

        atualizarTela();
    }

    private void acaoClicarCartaTreinador(int indiceNaMao, CartaTreinador carta) {
        if (ehTurnoDoBot) return;
        if (carta.getEfeito().equalsIgnoreCase("Poção")) {
            CartaPokemon alvo = escolherPokemonEmCampoDialog("Curar qual Pokémon? (Poção: +30 HP)");
            if (alvo != null) jogadorAtual.usarPocao(indiceNaMao, alvo);

        } else if (carta.getEfeito().equalsIgnoreCase("Cura Total")) {
            CartaPokemon alvo = escolherPokemonEmCampoDialog("Curar qual Pokémon? (Cura Total: HP completo)");
            if (alvo != null) jogadorAtual.usarCuraTotal(indiceNaMao, alvo);

        } else if (carta.getEfeito().equalsIgnoreCase("Troca")) {
            List<CartaPokemon> banco = jogadorAtual.getBanco();
            if (banco.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Seu Banco está vazio!");
                return;
            }
            java.util.List<String> nomes = new java.util.ArrayList<>();
            for (CartaPokemon p : banco) nomes.add(p.getNome() + " (HP " + p.getHpAtual() + "/" + p.getHpMaximo() + ")");

            String escolha = (String) JOptionPane.showInputDialog(frame, "Trocar o Ativo por quem do Banco?",
                    "Troca", JOptionPane.QUESTION_MESSAGE, null, nomes.toArray(), nomes.get(0));
            if (escolha == null) return;

            int indiceBanco = nomes.indexOf(escolha);
            jogadorAtual.usarTroca(indiceNaMao, indiceBanco);

        } else if (carta.getEfeito().equalsIgnoreCase("Evolução Rápida")) {
            usarEvolucaoRapidaFluxo(indiceNaMao);
        }

        atualizarTela();
    }

    private void usarEvolucaoRapidaFluxo(int indiceCartaTreinador) {

        CartaPokemon alvo = escolherPokemonEmCampoDialog("Evolução Rápida — qual Pokémon vai evoluir?");
        if (alvo == null) return;

        List<Carta> mao = jogadorAtual.getMao();
        java.util.List<Integer> indicesValidos = new java.util.ArrayList<>();
        java.util.List<String> nomesValidos = new java.util.ArrayList<>();

        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaPokemon) {
                CartaPokemon carta = (CartaPokemon) mao.get(i);
                if (!carta.isBasico() && carta.getEvoluiDe().equalsIgnoreCase(alvo.getNome())) {
                    indicesValidos.add(i);
                    nomesValidos.add(carta.getNome());
                }
            }
        }

        if (indicesValidos.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Você não tem nenhuma evolução de " + alvo.getNome() + " na mão!");
            return;
        }

        String escolha = (String) JOptionPane.showInputDialog(
                frame, "Evoluir " + alvo.getNome() + " para qual forma?", "Evolução Rápida",
                JOptionPane.QUESTION_MESSAGE, null, nomesValidos.toArray(), nomesValidos.get(0)
        );
        if (escolha == null) return;

        int posicaoNaLista = nomesValidos.indexOf(escolha);
        int indiceCartaEvolucao = indicesValidos.get(posicaoNaLista);

        jogadorAtual.usarEvolucaoRapida(indiceCartaTreinador, indiceCartaEvolucao, alvo);
    }

    private CartaPokemon escolherPokemonEmCampoDialog(String titulo) {
        java.util.List<CartaPokemon> opcoesPokemon = new java.util.ArrayList<>();
        java.util.List<String> nomes = new java.util.ArrayList<>();

        if (jogadorAtual.getPokemonAtivo() != null) {
            opcoesPokemon.add(jogadorAtual.getPokemonAtivo());
            nomes.add("🔴 Ativo: " + jogadorAtual.getPokemonAtivo().getNome() + " (HP " + jogadorAtual.getPokemonAtivo().getHpAtual() + "/" + jogadorAtual.getPokemonAtivo().getHpMaximo() + ")");
        }
        for (CartaPokemon p : jogadorAtual.getBanco()) {
            opcoesPokemon.add(p);
            nomes.add("🔵 Banco: " + p.getNome() + " (HP " + p.getHpAtual() + "/" + p.getHpMaximo() + ")");
        }

        if (opcoesPokemon.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Você não tem nenhum Pokémon em campo!");
            return null;
        }

        String escolha = (String) JOptionPane.showInputDialog(frame, titulo, "Escolha",
                JOptionPane.QUESTION_MESSAGE, null, nomes.toArray(), nomes.get(0));
        if (escolha == null) return null;

        return opcoesPokemon.get(nomes.indexOf(escolha));
    }

    private void mostrarFlashDeAtaque(Color cor) {
        corFlash = cor;

        Timer timer = new Timer(25, null);
        final int[] passo = {0};
        timer.addActionListener(e -> {
            passo[0]++;
            if (passo[0] <= 4) {
                alphaFlash = 140;
            } else {
                alphaFlash = Math.max(0, alphaFlash - 15);
            }
            flashPane.repaint();

            if (alphaFlash <= 0 && passo[0] > 4) {
                timer.stop();
            }
        });
        timer.start();
    }

    private void mostrarNotificacao(String texto) {
        if (frame == null || !frame.isShowing()) return;

        JLabel toast = new JLabel("<html><div style='padding:4px 8px;'>" + texto + "</div></html>");
        toast.setOpaque(true);
        toast.setBackground(new Color(33, 33, 66, 235));
        toast.setForeground(Color.WHITE);
        toast.setFont(new Font("Segoe UI", Font.BOLD, 13));
        toast.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1));

        Dimension tamanhoPreferido = toast.getPreferredSize();
        int largura = Math.min(460, Math.max(220, tamanhoPreferido.width + 16));
        int altura = 36;
        int margem = 18;

        int x = frame.getWidth() - largura - margem;
        int y = frame.getHeight() - margem - altura - (toastsAtivos.size() * (altura + 8));

        toast.setBounds(x, Math.max(10, y), largura, altura);

        flashPane.add(toast);
        flashPane.setComponentZOrder(toast, 0);
        toastsAtivos.add(toast);
        flashPane.revalidate();
        flashPane.repaint();

        Timer timer = new Timer(2800, e -> {
            flashPane.remove(toast);
            toastsAtivos.remove(toast);
            reposicionarToasts();
            flashPane.revalidate();
            flashPane.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void reposicionarToasts() {
        int altura = 36;
        int margem = 18;
        for (int i = 0; i < toastsAtivos.size(); i++) {
            JComponent t = toastsAtivos.get(i);
            int x = frame.getWidth() - t.getWidth() - margem;
            int y = frame.getHeight() - margem - altura - (i * (altura + 8));
            t.setLocation(x, Math.max(10, y));
        }
    }

    private void acaoAtacar() {
        if (ehTurnoDoBot) return;
        boolean sucesso = jogadorAtual.atacar(adversario);
        if (sucesso) {
            mostrarFlashDeAtaque(new Color(220, 40, 40));

            if (adversario.getPokemonAtivo() == null && adversario.getBanco().isEmpty()) {
                jogoAtivo = false;
                mostrarTelaFimDeJogo(jogadorAtual, adversario, "Não sobrou nenhum Pokémon em campo!");
                return;
            }

            jogadorAtual.encerrarRodada();
            System.out.println("➡️ Turno encerrado automaticamente após o ataque.");
            trocarTurno();
        } else {
            atualizarTela();
        }
    }

    private void acaoPassarTurno() {
        if (ehTurnoDoBot) return;
        jogadorAtual.encerrarRodada();
        trocarTurno();
    }
}
