package ui;

import efeitos.EfeitoTreinador;
import efeitos.ParametrosEfeito;
import efeitos.TipoAlvo;
import eventos.EventoJogo;
import eventos.TipoEvento;
import excecoes.BaralhoVazioException;
import excecoes.JogadaInvalidaException;
import excecoes.PersistenciaException;
import excecoes.PokeApiException;
import fabrica.BaralhosPadrao;
import fabrica.CartaPokemonFactory;
import fabrica.TimeFixo;
import modelo.Carta;
import modelo.CartaPokemon;
import modelo.CartaTreinador;
import modelo.Jogador;
import persistencia.HistoricoPartidas;
import persistencia.ResultadoPartida;
import servicos.PokeApiCliente;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.awt.*;
import java.awt.image.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface gráfica (Swing) do Pokémon TCG — é o "coração" do projeto,
 * responsável por toda a experiência jogável: telas iniciais (nome, modo,
 * time), tabuleiro de batalha, diálogos de ação e telas de fim de partida.
 *
 * <p>Esta classe cuida apenas da apresentação e da coleta de escolhas do
 * jogador; as regras de negócio da partida ficam em {@link Jogador},
 * {@link CartaPokemon} e nas estratégias de {@link EfeitoTreinador}. A
 * criação de cartas de Pokémon (tanto vindas da PokeAPI quanto dos times
 * fixos) é sempre feita através de {@link CartaPokemonFactory}.</p>
 *
 * <p>Tudo o que acontece na partida chega aqui como {@link EventoJogo}: a
 * interface se inscreve como ouvinte (padrão <b>Observer</b>) dos dois
 * jogadores e mostra cada evento como notificação e no histórico. Jogadas
 * inválidas chegam como {@link JogadaInvalidaException} e são exibidas num
 * único ponto, {@link #executarJogada(Runnable)}.</p>
 */
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
    private static final int LARGURA_ATIVO = 150, ALTURA_ATIVO = 210;
    private static final int LARGURA_BANCO = 120, ALTURA_BANCO = 168;
    private static final Color COR_FUNDO = new Color(245, 247, 250);
    private static final Color COR_BANNER = new Color(33, 33, 66);

    private static final int LIMITE_BARALHO = 40;
    private static final int TAMANHO_MAO_INICIAL = 6;
    private static final String URL_SPRITES = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/";

    private final PokeApiCliente pokeApi = new PokeApiCliente();
    private final HistoricoPartidas historico = new HistoricoPartidas();

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
    private static final String NOME_BOT = "Bot";

    /**
     * Ponto de entrada da versão gráfica do jogo.
     *
     * @param args não utilizado
     */
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            // Não é grave: o Swing continua com o tema do sistema operacional.
            System.err.println("Não foi possível aplicar o tema padrão do Swing: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> new AppGUI().iniciar());
    }

    private AppGUI() { }

    private String nomeJogador1Salvo;
    private String nomeJogador2Salvo;
    private List<CartaPokemon> composicaoJogador1;
    private List<CartaPokemon> composicaoJogador2;
    private boolean modoSoloSalvo;

    /**
     * Conduz o fluxo de telas iniciais (modo de jogo, nome do treinador e
     * montagem de time) e, ao final, chama {@link #iniciarPartida} com as
     * escolhas feitas pelo(s) jogador(es).
     */
    private void iniciar() {
        modoSoloSalvo = escolherModoDialog();
        Set<TimeFixo> timesDisponiveis = EnumSet.allOf(TimeFixo.class);

        nomeJogador1Salvo = pedirNomeEstilizado("Digite o seu nome de Treinador:", "Treinador 1");
        composicaoJogador1 = montarComposicao(nomeJogador1Salvo, timesDisponiveis, false);

        nomeJogador2Salvo = modoSoloSalvo ? NOME_BOT : pedirNomeEstilizado("Digite o nome do Treinador 2:", "Treinador 2");
        composicaoJogador2 = montarComposicao(nomeJogador2Salvo, timesDisponiveis, modoSoloSalvo);

        reiniciarComMesmaEquipe();
    }

    /**
     * Monta o time de um jogador: pela tela de escolha (time fixo ou customizado)
     * ou, no caso do Bot, sorteando Pokémon na PokeAPI.
     *
     * @return cópias das cartas de Pokémon escolhidas (sem as cartas de treinador)
     */
    private List<CartaPokemon> montarComposicao(String nome, Set<TimeFixo> timesDisponiveis, boolean ehBot) {
        Jogador temporario = new Jogador(nome);
        if (ehBot) {
            construirTimeAleatorioBotComLoading(temporario);
        } else {
            escolherTimeDialog(temporario, timesDisponiveis);
        }
        return capturarComposicao(temporario);
    }

    private List<CartaPokemon> capturarComposicao(Jogador jogador) {
        List<CartaPokemon> composicao = new java.util.ArrayList<>();
        for (Carta carta : jogador.getBaralho()) {
            if (carta instanceof CartaPokemon) {
                CartaPokemon p = (CartaPokemon) carta;
                CartaPokemon copia = CartaPokemonFactory.clonar(p);
                composicao.add(copia);
            }
        }
        return composicao;
    }

    private Jogador construirJogadorFresco(String nome, List<CartaPokemon> composicao) {
        Jogador jogador = new Jogador(nome);
        for (CartaPokemon modelo : composicao) {
            CartaPokemon copia = CartaPokemonFactory.clonar(modelo);
            jogador.adicionarAoBaralho(copia);
        }
        return jogador;
    }

    /**
     * Monta os dois {@link Jogador}, popula seus baralhos com as composições
     * escolhidas, monta a janela principal (na primeira partida) e dá início
     * à primeira rodada.
     */
    private void iniciarPartida(String nome1, List<CartaPokemon> composicao1, String nome2, List<CartaPokemon> composicao2, boolean modoSolo) {
        jogador1 = construirJogadorFresco(nome1, composicao1);
        jogador2 = construirJogadorFresco(nome2, composicao2);
        jogadorBot = modoSolo ? jogador2 : null;

        for (Jogador jogador : List.of(jogador1, jogador2)) {
            BaralhosPadrao.adicionarCartasTreinador(jogador);
            BaralhosPadrao.adicionarPremios(jogador);
            jogador.embaralhar();
            for (int i = 0; i < TAMANHO_MAO_INICIAL; i++) jogador.comprarCarta();
        }

        // Cara ou Coroa decide quem joga o primeiro turno (regra oficial do TCG).
        boolean jogador1Comeca = SorteioMoeda.sortearQuemComeca(jogador1.getNome(), jogador2.getNome());
        jogadorAtual = jogador1Comeca ? jogador1 : jogador2;
        adversario = jogador1Comeca ? jogador2 : jogador1;
        jogador1PrimeiroTurno = true;
        jogador2PrimeiroTurno = true;
        numeroTurno = 1;
        jogoAtivo = true;

        if (frame == null) {
            montarJanela();
        } else {
            logArea.setText("");
        }

        // Observer: a interface passa a ouvir os eventos dos dois jogadores desta partida.
        jogador1.adicionarOuvinte(this::aoEventoDoJogo);
        jogador2.adicionarOuvinte(this::aoEventoDoJogo);

        frame.setVisible(true);

        registrarMensagem("Cara ou Coroa: " + jogadorAtual.getNome() + " começa a partida!");
        iniciarTurno();
    }

    private void reiniciarComMesmaEquipe() {
        iniciarPartida(nomeJogador1Salvo, composicaoJogador1, nomeJogador2Salvo, composicaoJogador2, modoSoloSalvo);
    }

    private void reiniciarTrocandoEquipe() {
        Set<TimeFixo> timesDisponiveis = EnumSet.allOf(TimeFixo.class);
        composicaoJogador1 = montarComposicao(nomeJogador1Salvo, timesDisponiveis, false);
        composicaoJogador2 = montarComposicao(nomeJogador2Salvo, timesDisponiveis, modoSoloSalvo);
        reiniciarComMesmaEquipe();
    }

    private Image imagemMenuCharizard;
    private Image imagemMenuBlastoise;
    private Image imagemMenuVenusaur;

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

        GradientPaint branco = new GradientPaint(
                centerX - raio, centerY, new Color(255, 255, 255),
                centerX + raio, centerY + raio, new Color(215, 215, 220)
        );
        g2.setPaint(branco);
        g2.fillArc(centerX - raio, centerY - raio, tamanho, tamanho, 180, 180);

        RadialGradientPaint vermelhoEsferico = new RadialGradientPaint(
                new Point(centerX - (int) (raio * 0.35), centerY - (int) (raio * 0.55)), tamanho * 0.85f,
                new float[]{0f, 0.55f, 1f},
                new Color[]{new Color(255, 160, 160), new Color(225, 55, 55), new Color(160, 15, 15)}
        );
        g2.setPaint(vermelhoEsferico);
        g2.fillArc(centerX - raio, centerY - raio, tamanho, tamanho, 0, 180);

        int reflexoLargura = (int) (tamanho * 0.24);
        int reflexoAltura = (int) (tamanho * 0.14);
        g2.setColor(new Color(255, 255, 255, 130));
        g2.fillOval(centerX - raio + (int) (tamanho * 0.16), centerY - raio + (int) (tamanho * 0.14),
                reflexoLargura, reflexoAltura);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(centerX - raio, centerY - raio, tamanho, tamanho);

        int alturaFaixa = Math.max(6, tamanho / 12);
        g2.setColor(new Color(15, 15, 15));
        g2.fillRect(centerX - raio, centerY - alturaFaixa / 2, tamanho, alturaFaixa);

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

    private void desenharRivalNoCirculo(Graphics2D g2, Image imagem, int centroX, int centroY, int raioCirculo, double anguloGraus, int tamanho) {
        if (imagem == null) return;
        double rad = Math.toRadians(anguloGraus);
        int px = centroX + (int) (raioCirculo * Math.cos(rad));
        int py = centroY + (int) (raioCirculo * Math.sin(rad));
        g2.drawImage(imagem, px - tamanho / 2, py - tamanho / 2, tamanho, tamanho, null);
    }

    private BufferedImage aplicarDesfoque(BufferedImage origem, int raio) {
        int tamanhoKernel = raio * 2 + 1;
        float peso = 1.0f / (tamanhoKernel * tamanhoKernel);
        float[] dados = new float[tamanhoKernel * tamanhoKernel];
        java.util.Arrays.fill(dados, peso);
        Kernel kernel = new Kernel(tamanhoKernel, tamanhoKernel, dados);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(origem, null);
    }

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

    /** Exibe a tela estilizada de entrada de nome do treinador e retorna o valor digitado (ou o padrão). */
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
        campoTexto.addActionListener(e -> btnConfirmar.doClick());

        painel.add(titulo);
        painel.add(campoTexto);
        painel.add(btnConfirmar);

        dialogo.setContentPane(painel);
        dialogo.setVisible(true);

        return resultado[0];
    }

    /** Exibe a tela de escolha de modo. @return {@code true} para modo Solo (vs Bot), {@code false} para Versus */
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

                int centroCirculoX = getWidth() / 2;
                int centroCirculoY = (int) (getHeight() * 0.55);
                int raioCirculo = (int) (getHeight() * 0.42);
                int tamanhoRival = (int) (getHeight() * 0.48);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
                desenharRivalNoCirculo(g2, imagemMenuCharizard, centroCirculoX, centroCirculoY, raioCirculo, 90, tamanhoRival);
                desenharRivalNoCirculo(g2, imagemMenuBlastoise, centroCirculoX, centroCirculoY, raioCirculo, 210, tamanhoRival);
                desenharRivalNoCirculo(g2, imagemMenuVenusaur, centroCirculoX, centroCirculoY, raioCirculo, 330, tamanhoRival);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

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
            BufferedImage charizard = baixarImagemBuffered(URL_SPRITES + "other/official-artwork/6.png");
            BufferedImage blastoise = baixarImagemBuffered(URL_SPRITES + "other/official-artwork/9.png");
            BufferedImage venusaur = baixarImagemBuffered(URL_SPRITES + "other/official-artwork/3.png");

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

    /**
     * Baixa uma imagem da internet. Sem conexão o jogo continua normalmente,
     * apenas sem a ilustração, por isso a falha é registrada e devolve {@code null}.
     */
    private static BufferedImage baixarImagemBuffered(String url) {
        try {
            return ImageIO.read(URI.create(url).toURL());
        } catch (IOException e) {
            System.err.println("Imagem indisponível (" + url + "): " + e.getMessage());
            return null;
        }
    }

    /** Baixa o sprite oficial de um Pokémon já redimensionado, ou {@code null} se não estiver disponível. */
    private static ImageIcon baixarSprite(int numeroDex, int tamanho) {
        BufferedImage imagem = baixarImagemBuffered(URL_SPRITES + numeroDex + ".png");
        if (imagem == null) return null;
        return new ImageIcon(imagem.getScaledInstance(tamanho, tamanho, java.awt.Image.SCALE_SMOOTH));
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
            final int numeroDex = 1 + sorteio.nextInt(PokeApiCliente.ULTIMO_NUMERO_DEX);
            executor.submit(() -> {
                try {
                    resultados.offer(pokeApi.buscarPokemon(numeroDex));
                } catch (PokeApiException e) {
                    // Um Pokémon que falhou não impede o sorteio: o Bot só tenta o próximo.
                    System.err.println("Bot pulou o Pokémon #" + numeroDex + ": " + e.getMessage());
                    resultados.offer(CARTA_FALHA_SORTEIO);
                }
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
                CartaPokemon copia = CartaPokemonFactory.clonar(carta);
                bot.adicionarAoBaralho(copia);
            }
        }

        executor.shutdownNow();
    }

    /**
     * Exibe a tela de montagem de time (fixo ou customizado via PokeAPI) e
     * popula o baralho do jogador informado com a composição escolhida.
     */
    private void escolherTimeDialog(Jogador jogador, Set<TimeFixo> disponiveis) {
        final String CUSTOM = "CUSTOM";
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
            for (TimeFixo time : disponiveis) {
                opcoes.add(new String[]{time.getEmoji() + " " + time.getNome(), time.name()});
            }
            opcoes.add(new String[]{"\uD83C\uDF0D Montar Time Customizado (Gen 1 a 6)", CUSTOM});

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

            if (escolha[0].equals(CUSTOM)) {
                if (abrirConstrutorDeTime(jogador)) return;
            } else {
                TimeFixo time = TimeFixo.valueOf(escolha[0]);
                time.montar(jogador);
                disponiveis.remove(time);
                return;
            }
        }
    }

    private boolean abrirConstrutorDeTime(Jogador jogador) {
        JDialog dialog = new JDialog((Frame) null, "Montar Time Customizado — " + jogador.getNome(), true);
        dialog.setSize(650, 650);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.getContentPane().setBackground(COR_FUNDO);

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
                    java.util.List<String> nomes = pokeApi.buscarNomes(offset, limite);
                    SwingUtilities.invokeLater(() -> {
                        for (String n : nomes) modeloLista.addElement(n);
                        statusLabel.setText("✅ " + nomes.size() + " Pokémon carregados. Selecione um e clique em Adicionar.");
                    });
                } catch (PokeApiException ex) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("❌ " + ex.getMessage()));
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
                java.util.List<CartaPokemon> linhaCompleta;
                try {
                    linhaCompleta = pokeApi.buscarLinhaEvolutiva(numeroDex);
                } catch (PokeApiException ex) {
                    SwingUtilities.invokeLater(() -> {
                        btnAdicionar.setEnabled(true);
                        statusLabel.setText("❌ " + ex.getMessage());
                    });
                    return;
                }
                SwingUtilities.invokeLater(() -> {

                    StringBuilder resumo = new StringBuilder();
                    for (CartaPokemon carta : linhaCompleta) {
                        if (contador[0] >= LIMITE_BARALHO) break;

                        int quantidade = carta.isBasico() ? 4 : 2;

                        int espacoRestante = LIMITE_BARALHO - contador[0];
                        if (quantidade > espacoRestante) quantidade = espacoRestante;
                        if (quantidade <= 0) break;

                        jogador.adicionarAoBaralho(carta);

                        for (int i = 1; i < quantidade; i++) {
                            CartaPokemon copia = CartaPokemonFactory.clonar(carta);
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

    /** Monta a janela principal do tabuleiro de batalha (chamada apenas uma vez, na primeira partida). */
    private void montarJanela() {
        frame = new JFrame("Pokémon TCG - Versus [" + VERSAO_BUILD + "]");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setUndecorated(true);
        frame.setSize(tela);
        frame.setLocation(0, 0);
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
        campoJogadorPanel.setBorder(criarBordaTitulo("Seu Campo de Batalha"));
        campoJogadorPanel.setLayout(new BorderLayout(12, 0));

        maoPanel = new JPanel();
        maoPanel.setBackground(COR_FUNDO);
        maoPanel.setBorder(criarBordaTitulo("Sua Mão (clique numa carta pra jogar)"));
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

        JButton btnAtacar = criarBotaoAcao("Atacar", new Color(211, 47, 47));
        btnAtacar.addActionListener(e -> acaoAtacar());

        JButton btnPassar = criarBotaoAcao("Passar Turno", new Color(96, 125, 139));
        btnPassar.addActionListener(e -> acaoPassarTurno());

        JButton btnHistorico = criarBotaoAcao("Ver Histórico", new Color(84, 58, 133));
        btnHistorico.addActionListener(e -> abrirHistoricoDialog());

        JButton btnSair = criarBotaoAcao("Sair do Jogo", new Color(66, 66, 66));
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
                ImageIcon iconeFinal = baixarSprite(numeroFinal, 96);
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
                ImageIcon iconeFinal = baixarSprite(numeroDex, 32);
                SwingUtilities.invokeLater(() -> {
                    cacheSpritesPorNumero.put(numeroDex, iconeFinal);
                    spritesCarregandoAgora.remove(numeroDex);
                    lista.repaint();
                });
            }).start();
        }

        return null;
    }

    private String obterTipoParaLista(int numeroDex, JList<String> lista) {
        if (cacheTipoPorNumero.containsKey(numeroDex)) {
            return cacheTipoPorNumero.get(numeroDex);
        }

        if (!tiposCarregandoAgora.contains(numeroDex)) {
            tiposCarregandoAgora.add(numeroDex);

            new Thread(() -> {
                String tipo;
                try {
                    tipo = pokeApi.buscarTipo(numeroDex);
                } catch (PokeApiException e) {
                    tipo = null; // a lista continua funcionando, só sem o emoji do tipo
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

    /**
     * Ouvinte (Observer) dos eventos dos dois jogadores: escreve o evento no
     * histórico e mostra uma notificação na tela.
     *
     * <p>Substitui o antigo truque de redirecionar o {@code System.out} para a
     * tela, que também quebrava acentos e emojis (convertia cada byte do UTF-8
     * num caractere separado).</p>
     */
    private void aoEventoDoJogo(EventoJogo evento) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> aoEventoDoJogo(evento));
            return;
        }
        if (evento.tipo() == TipoEvento.COMPRA && ehTurnoDoBot) {
            // Não revela ao humano qual carta o Bot comprou.
            registrarMensagem("🤖 " + jogadorBot.getNome() + " comprou uma carta.");
            return;
        }
        registrarMensagem(evento.mensagem());
    }

    /** Escreve uma linha no histórico da partida e mostra a mesma mensagem como notificação. */
    private void registrarMensagem(String mensagem) {
        if (logArea != null) {
            logArea.append(mensagem + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
        mostrarNotificacao(mensagem);
    }

    /**
     * Executa uma jogada do jogador humano. Se ela desrespeitar alguma regra, o
     * {@link Jogador} lança {@link JogadaInvalidaException} e a mensagem dela é
     * mostrada aqui — o único lugar da interface que trata esse erro.
     *
     * @return {@code true} se a jogada foi feita
     */
    private boolean executarJogada(Runnable jogada) {
        try {
            jogada.run();
            return true;
        } catch (JogadaInvalidaException e) {
            registrarMensagem("⚠️ " + e.getMessage());
            return false;
        }
    }

    /**
     * Jogada do Bot: ele tenta ações que podem não ser permitidas (ex.: evoluir
     * no Turno 1). Nesse caso a exceção só é registrada no histórico, sem
     * notificação, e o Bot segue para a próxima ação.
     */
    private boolean tentarJogadaDoBot(Runnable jogada) {
        try {
            jogada.run();
            return true;
        } catch (JogadaInvalidaException e) {
            if (logArea != null) logArea.append("🤖 (jogada ignorada) " + e.getMessage() + "\n");
            return false;
        }
    }

    private void iniciarTurno() {
        if (!jogoAtivo) return;

        boolean primeiroTurno = (jogadorAtual == jogador1) ? jogador1PrimeiroTurno : jogador2PrimeiroTurno;
        ehTurnoDoBot = (jogadorBot != null && jogadorAtual == jogadorBot);
        if (!primeiroTurno) {
            try {
                jogadorAtual.comprarCarta();
            } catch (BaralhoVazioException e) {
                encerrarJogoComDerrota(jogadorAtual, adversario);
                return;
            }
        } else {
            if (jogadorAtual == jogador1) jogador1PrimeiroTurno = false;
            else jogador2PrimeiroTurno = false;
        }

        jogadorAtual.resetarEvolucoesDoTurno();
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

    /**
     * IA simples do Bot: coloca Pokémon em campo, evolui o Ativo, anexa energia
     * e ataca se puder. Cada ação passa por {@link #tentarJogadaDoBot}.
     */
    private void executarTurnoBot() {
        if (!jogoAtivo) return;

        Jogador bot = jogadorAtual;
        Jogador oponenteDoBot = adversario;

        if (bot.getPokemonAtivo() == null && bot.indiceDoPrimeiroBasicoNaMao() != -1) {
            tentarJogadaDoBot(() -> bot.colocarPokemonEmCampo(bot.indiceDoPrimeiroBasicoNaMao(), 0));
        }

        while (bot.indiceDoPrimeiroBasicoNaMao() != -1 && bot.getBanco().size() < Jogador.LIMITE_BANCO) {
            if (!tentarJogadaDoBot(() -> bot.colocarPokemonEmCampo(bot.indiceDoPrimeiroBasicoNaMao(), 1))) break;
        }

        CartaPokemon ativo = bot.getPokemonAtivo();
        if (ativo != null) {
            List<Integer> evolucoes = bot.indicesDeEvolucoesNaMao(ativo);
            if (!evolucoes.isEmpty()) {
                tentarJogadaDoBot(() -> bot.evoluir(evolucoes.get(0), ativo, numeroTurno));
            }
            tentarJogadaDoBot(() -> bot.anexarEnergia(bot.getPokemonAtivo()));
        }

        atualizarTela();

        if (bot.getPokemonAtivo() != null && bot.getPokemonAtivo().getQuantidadeEnergias() >= 1
                && tentarJogadaDoBot(() -> bot.atacar(oponenteDoBot, numeroTurno))) {
            mostrarFlashDeAtaque(new Color(220, 40, 40));
            if (verificarVitoriaAposAtaque(bot, oponenteDoBot)) return;

            bot.encerrarRodada();
            registrarMensagem("🤖 " + bot.getNome() + " encerrou o turno após atacar.");
            trocarTurno();
            return;
        }

        bot.encerrarRodada();
        registrarMensagem("🤖 " + bot.getNome() + " passou o turno.");
        trocarTurno();
    }

    /**
     * Confere se o ataque que acabou de acontecer terminou a partida
     * (atacante pegou todos os prêmios ou o defensor ficou sem Pokémon).
     * Usado tanto pelo jogador humano quanto pelo Bot.
     *
     * @return {@code true} se a partida acabou
     */
    private boolean verificarVitoriaAposAtaque(Jogador atacante, Jogador defensor) {
        String motivo = null;
        if (atacante.getPremios().isEmpty()) {
            motivo = "Conquistou os " + BaralhosPadrao.QUANTIDADE_PREMIOS + " prêmios!";
        } else if (defensor.semPokemonEmCampo()) {
            motivo = "Não sobrou nenhum Pokémon em campo!";
        }
        if (motivo == null) return false;

        jogoAtivo = false;
        mostrarTelaFimDeJogo(atacante, defensor, motivo);
        return true;
    }

    private void encerrarJogoComDerrota(Jogador perdedor, Jogador vencedor) {
        jogoAtivo = false;
        turnoLabel.setText("🏆 " + vencedor.getNome() + " venceu a partida!");
        mostrarTelaFimDeJogo(vencedor, perdedor, perdedor.getNome() + " não tinha mais cartas pra comprar!");
    }

    /**
     * Salva o resultado no arquivo de histórico e monta o texto do placar geral
     * dos dois jogadores. Se o arquivo não puder ser lido ou gravado, a partida
     * não é afetada: o texto devolvido explica o problema.
     */
    private String salvarResultadoEMontarPlacar(Jogador vencedor, Jogador perdedor, String motivo) {
        try {
            historico.registrar(new ResultadoPartida(LocalDateTime.now(), vencedor.getNome(), perdedor.getNome(),
                    jogadorBot != null ? "Solo" : "PvP", numeroTurno, motivo));

            Map<String, Integer> vitorias = historico.vitoriasPorJogador();
            int totalPartidas = historico.carregar().size();
            return "📊 Placar geral (" + totalPartidas + " partidas salvas): "
                    + vencedor.getNome() + " " + vitorias.getOrDefault(vencedor.getNome(), 0) + " vitória(s)  •  "
                    + perdedor.getNome() + " " + vitorias.getOrDefault(perdedor.getNome(), 0) + " vitória(s)";
        } catch (PersistenciaException e) {
            System.err.println(e.getMessage());
            return "⚠️ " + e.getMessage();
        }
    }

    /** Exibe a tela de fim de partida com o resultado e as opções de jogar novamente. */
    private void mostrarTelaFimDeJogo(Jogador vencedor, Jogador perdedor, String motivo) {
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        String textoPlacar = salvarResultadoEMontarPlacar(vencedor, perdedor, motivo);

        JDialog telaFim = new JDialog(frame, "Fim de Jogo", true);
        telaFim.setSize(tela);
        telaFim.setLocation(0, 0);
        telaFim.setResizable(false);
        telaFim.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel painel = criarPainelComFundo();
        painel.setPreferredSize(tela);

        int centroX = tela.width / 2;

        JLabel trofeu = new JLabel("🏆", SwingConstants.CENTER);
        trofeu.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 90));
        trofeu.setBounds(0, (int) (tela.height * 0.10), tela.width, 110);

        JLabel tituloVencedor = new JLabel(vencedor.getNome().toUpperCase() + " VENCEU!", SwingConstants.CENTER);
        tituloVencedor.setFont(new Font("Segoe UI", Font.BOLD, 44));
        tituloVencedor.setForeground(new Color(255, 215, 0));
        tituloVencedor.setBounds(0, (int) (tela.height * 0.24), tela.width, 60);

        JLabel motivoLabel = new JLabel("<html><div style='text-align:center;'>💀 " + perdedor.getNome()
                + " perdeu a partida.<br>" + motivo + "</div></html>", SwingConstants.CENTER);
        motivoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        motivoLabel.setForeground(Color.WHITE);
        motivoLabel.setBounds(0, (int) (tela.height * 0.33), tela.width, 60);

        JLabel turnoFinalLabel = new JLabel("Partida encerrada no Turno " + numeroTurno + ".", SwingConstants.CENTER);
        turnoFinalLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        turnoFinalLabel.setForeground(new Color(210, 210, 230));
        turnoFinalLabel.setBounds(0, (int) (tela.height * 0.40), tela.width, 25);

        JLabel placarLabel = new JLabel(textoPlacar, SwingConstants.CENTER);
        placarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        placarLabel.setForeground(new Color(255, 230, 150));
        placarLabel.setBounds(0, (int) (tela.height * 0.40) + 28, tela.width, 25);

        int larguraBotao = 380;
        int alturaBotao = 58;
        int xBotao = centroX - larguraBotao / 2;

        JButton btnMesmaEquipe = criarBotaoMenuPrincipal("🔄 Jogar Novamente (Mesma Equipe)");
        btnMesmaEquipe.setBounds(xBotao, (int) (tela.height * 0.50), larguraBotao, alturaBotao);
        btnMesmaEquipe.addActionListener(e -> {
            telaFim.dispose();
            reiniciarComMesmaEquipe();
        });

        JButton btnTrocarEquipe = criarBotaoMenuPrincipal("🔁 Jogar Novamente (Trocar Time)");
        btnTrocarEquipe.setBounds(xBotao, (int) (tela.height * 0.50) + alturaBotao + 16, larguraBotao, alturaBotao);
        btnTrocarEquipe.addActionListener(e -> {
            telaFim.dispose();
            reiniciarTrocandoEquipe();
        });

        JButton btnFechar = criarBotaoAcao("🚪 Fechar o Jogo", new Color(66, 66, 66));
        btnFechar.setBounds(xBotao, (int) (tela.height * 0.50) + (alturaBotao + 16) * 2, larguraBotao, alturaBotao);
        btnFechar.addActionListener(e -> System.exit(0));

        painel.add(trofeu);
        painel.add(tituloVencedor);
        painel.add(motivoLabel);
        painel.add(turnoFinalLabel);
        painel.add(placarLabel);
        painel.add(btnMesmaEquipe);
        painel.add(btnTrocarEquipe);
        painel.add(btnFechar);

        telaFim.setContentPane(painel);
        telaFim.setVisible(true);
    }

    private void atualizarTela() {
        turnoLabel.setText("TURNO " + numeroTurno + " — VEZ DE " + jogadorAtual.getNome().toUpperCase());
        adversarioLabel.setText("Pokémon Ativo de " + adversario.getNome() + ": "
                + (adversario.getPokemonAtivo() != null ? adversario.getPokemonAtivo().toString() : "[Nenhum]")
                + "   |   Sua Mão: " + jogadorAtual.getMao().size() + " cartas");

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
        JLabel rotuloAtivo = new JLabel("ZONA AVANÇADA (Ativo)");
        rotuloAtivo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rotuloAtivo.setForeground(COR_BANNER);

        CartaPokemon ativo = jogadorAtual.getPokemonAtivo();
        JComponent cartaAtivo;
        if (ativo != null) {
            CartaVisual btnAtivo = new CartaVisual(ativo, imagemDoSprite(ativo), LARGURA_ATIVO, ALTURA_ATIVO);
            btnAtivo.addActionListener(e -> abrirMenuPokemon(ativo, -1));
            cartaAtivo = btnAtivo;
        } else {
            cartaAtivo = CartaVisual.espacoVazio("ATIVO", LARGURA_ATIVO, ALTURA_ATIVO);
        }

        linhaAtivo.add(criarEspacoInvisivel());
        linhaAtivo.add(criarEspacoInvisivel());
        linhaAtivo.add(centralizar(cartaAtivo));
        linhaAtivo.add(criarEspacoInvisivel());
        linhaAtivo.add(criarEspacoInvisivel());

        JPanel linhaBanco = new JPanel(new GridLayout(1, 5, 10, 6));
        linhaBanco.setBackground(COR_FUNDO);

        List<CartaPokemon> banco = jogadorAtual.getBanco();
        for (int i = 0; i < 5; i++) {
            if (i < banco.size()) {
                CartaPokemon p = banco.get(i);
                CartaVisual btnBanco = new CartaVisual(p, imagemDoSprite(p), LARGURA_BANCO, ALTURA_BANCO);
                final int indiceBanco = i;
                btnBanco.addActionListener(e -> abrirMenuPokemon(p, indiceBanco));
                linhaBanco.add(centralizar(btnBanco));
            } else {
                linhaBanco.add(centralizar(CartaVisual.espacoVazio("BANCO " + (i + 1), LARGURA_BANCO, ALTURA_BANCO)));
            }
        }

        JLabel rotuloBanco = new JLabel("BANCO DE RESERVAS");
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
                btnZonaMorta.setIcon(new ImageIcon(spriteUltimoMorto.getImage()
                        .getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
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
            return "<html><div style='text-align:center;'><b>ZONA MORTA</b><br><br>[Vazio]</div></html>";
        }
        CartaPokemon ultimo = zonaMorta.get(zonaMorta.size() - 1);
        return "<html><div style='text-align:center;'><b>ZONA MORTA</b><br>"
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

    /** Devolve a imagem do sprite (ou null enquanto ainda está baixando). */
    private Image imagemDoSprite(CartaPokemon p) {
        ImageIcon icone = carregarSprite(p);
        return icone != null ? icone.getImage() : null;
    }

    /** Coloca a carta no centro da célula, para o GridLayout não esticar a carta. */
    private JPanel centralizar(JComponent componente) {
        JPanel caixa = new JPanel(new GridBagLayout());
        caixa.setOpaque(false);
        caixa.add(componente);
        return caixa;
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

                CartaVisual btnCarta = new CartaVisual(p, imagemDoSprite(p));
                btnCarta.addActionListener(e -> acaoClicarCartaPokemon(indice, p));
                maoPanel.add(btnCarta);

            } else if (carta instanceof CartaTreinador) {
                CartaTreinador t = (CartaTreinador) carta;
                CartaVisual btnCarta = new CartaVisual(t);
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
            executarJogada(() -> jogadorAtual.anexarEnergia(pokemon));
        } else if (escolha.contains("Evoluir")) {
            evoluirPokemonEscolhido(pokemon);
        } else if (escolha.contains("Recuar")) {
            executarJogada(() -> jogadorAtual.recuar(indiceBanco));
        }

        atualizarTela();
    }

    private void evoluirPokemonEscolhido(CartaPokemon alvo) {
        int indiceNaMao = escolherEvolucaoNaMao(alvo, "Evoluir");
        if (indiceNaMao != -1) {
            executarJogada(() -> jogadorAtual.evoluir(indiceNaMao, alvo, numeroTurno));
        }
    }

    /**
     * Mostra as evoluções do alvo que o jogador tem na mão e deixa escolher uma.
     * Usado tanto pela evolução normal quanto pela carta Evolução Rápida.
     *
     * @return posição da evolução escolhida na mão, ou {@code -1} se não houver ou o jogador cancelar
     */
    private int escolherEvolucaoNaMao(CartaPokemon alvo, String tituloDialogo) {
        List<Integer> indicesValidos = jogadorAtual.indicesDeEvolucoesNaMao(alvo);
        if (indicesValidos.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Você não tem nenhuma evolução de " + alvo.getNome() + " na mão!");
            return -1;
        }

        java.util.List<String> nomesValidos = new java.util.ArrayList<>();
        for (int indice : indicesValidos) nomesValidos.add(jogadorAtual.getMao().get(indice).getNome());

        String escolha = (String) JOptionPane.showInputDialog(
                frame, "Evoluir " + alvo.getNome() + " para qual forma?", tituloDialogo,
                JOptionPane.QUESTION_MESSAGE, null, nomesValidos.toArray(), nomesValidos.get(0)
        );
        if (escolha == null) return -1;
        return indicesValidos.get(nomesValidos.indexOf(escolha));
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

        if (escolha != 0 && escolha != 1) return;
        executarJogada(() -> jogadorAtual.colocarPokemonEmCampo(indiceNaMao, escolha));

        atualizarTela();
    }

    private void acaoClicarCartaTreinador(int indiceNaMao, CartaTreinador carta) {
        if (ehTurnoDoBot) return;

        EfeitoTreinador efeito = carta.getEstrategia();
        ParametrosEfeito parametros = new ParametrosEfeito();
        TipoAlvo tipoAlvo = efeito.getTipoAlvoNecessario();

        switch (tipoAlvo) {

            case POKEMON_EM_CAMPO: {
                CartaPokemon alvo = escolherPokemonEmCampoDialog(efeito.getTextoSelecaoAlvo());
                if (alvo == null) return;
                parametros.setAlvo(alvo);
                executarJogada(() -> jogadorAtual.usarCartaTreinador(indiceNaMao, parametros));
                break;
            }

            case POKEMON_NO_BANCO: {
                List<CartaPokemon> banco = jogadorAtual.getBanco();
                if (banco.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Seu Banco está vazio!");
                    return;
                }
                java.util.List<String> nomes = new java.util.ArrayList<>();
                for (CartaPokemon p : banco) nomes.add(p.getNome() + " (HP " + p.getHpAtual() + "/" + p.getHpMaximo() + ")");

                String escolha = (String) JOptionPane.showInputDialog(frame, efeito.getTextoSelecaoAlvo(),
                        efeito.getNome(), JOptionPane.QUESTION_MESSAGE, null, nomes.toArray(), nomes.get(0));
                if (escolha == null) return;

                parametros.setIndiceBanco(nomes.indexOf(escolha));
                executarJogada(() -> jogadorAtual.usarCartaTreinador(indiceNaMao, parametros));
                break;
            }

            case CARTA_EVOLUCAO_E_ALVO:
                usarEvolucaoRapidaFluxo(indiceNaMao);
                break;

            case NENHUM:
            default:
                // carta sem efeito jogável (ex.: Troféu): a própria estratégia explica o motivo
                executarJogada(() -> jogadorAtual.usarCartaTreinador(indiceNaMao, parametros));
                break;
        }

        atualizarTela();
    }

    private void usarEvolucaoRapidaFluxo(int indiceCartaTreinador) {
        CartaPokemon alvo = escolherPokemonEmCampoDialog("Evolução Rápida — qual Pokémon vai evoluir?");
        if (alvo == null) return;

        int indiceCartaEvolucao = escolherEvolucaoNaMao(alvo, "Evolução Rápida");
        if (indiceCartaEvolucao == -1) return;

        ParametrosEfeito parametros = new ParametrosEfeito().setAlvo(alvo).setIndiceCartaEvolucao(indiceCartaEvolucao);
        executarJogada(() -> jogadorAtual.usarCartaTreinador(indiceCartaTreinador, parametros));
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

    private static final int LARGURA_MAXIMA_TOAST = 460;
    private static final int MARGEM_TOAST = 18;

    private void mostrarNotificacao(String texto) {
        if (frame == null || !frame.isShowing()) return;

        JLabel toast = criarToast(texto, null);
        if (toast.getPreferredSize().width > LARGURA_MAXIMA_TOAST) {
            // Texto longo: fixa a largura para o HTML quebrar a linha e calcular a altura certa.
            toast = criarToast(texto, LARGURA_MAXIMA_TOAST - 24);
        }
        Dimension tamanho = toast.getPreferredSize();
        toast.setSize(Math.min(LARGURA_MAXIMA_TOAST, Math.max(220, tamanho.width + 16)), Math.max(36, tamanho.height + 8));

        flashPane.add(toast);
        flashPane.setComponentZOrder(toast, 0);
        toastsAtivos.add(toast);
        reposicionarToasts();
        flashPane.revalidate();
        flashPane.repaint();

        JLabel toastFinal = toast;
        Timer timer = new Timer(2800, e -> {
            flashPane.remove(toastFinal);
            toastsAtivos.remove(toastFinal);
            reposicionarToasts();
            flashPane.revalidate();
            flashPane.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private JLabel criarToast(String texto, Integer larguraTexto) {
        String estilo = "padding:4px 8px;" + (larguraTexto != null ? " width:" + larguraTexto + "px;" : "");
        JLabel toast = new JLabel("<html><div style='" + estilo + "'>" + texto + "</div></html>");
        toast.setOpaque(true);
        toast.setBackground(new Color(33, 33, 66, 235));
        toast.setForeground(Color.WHITE);
        toast.setFont(new Font("Segoe UI", Font.BOLD, 13));
        toast.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1));
        return toast;
    }

    /** Empilha as notificações de baixo para cima, respeitando a altura de cada uma. */
    private void reposicionarToasts() {
        int y = frame.getHeight() - MARGEM_TOAST;
        for (JComponent t : toastsAtivos) {
            y -= t.getHeight();
            t.setLocation(frame.getWidth() - t.getWidth() - MARGEM_TOAST, Math.max(10, y));
            y -= 8;
        }
    }

    private void acaoAtacar() {
        if (ehTurnoDoBot) return;
        if (!executarJogada(() -> jogadorAtual.atacar(adversario, numeroTurno))) {
            atualizarTela();
            return;
        }

        mostrarFlashDeAtaque(new Color(220, 40, 40));
        if (verificarVitoriaAposAtaque(jogadorAtual, adversario)) return;

        jogadorAtual.encerrarRodada();
        registrarMensagem("➡️ Turno encerrado automaticamente após o ataque.");
        trocarTurno();
    }

    private void acaoPassarTurno() {
        if (ehTurnoDoBot) return;
        jogadorAtual.encerrarRodada();
        trocarTurno();
    }
}