import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Versão gráfica (Java Swing) do Pokémon TCG.
 * Reaproveita 100% da lógica de jogo já pronta em Jogador/CartaPokemon/CartaEnergia/CartaTreinador —
 * aqui só trocamos a "interface" de Scanner/console por botões e janelas.
 */
public class AppGUI {

    private JFrame frame;
    private JLabel turnoLabel;
    private JLabel adversarioLabel;
    private JPanel campoJogadorPanel;
    private JPanel maoPanel;
    private JTextArea logArea;

    private Jogador jogador1;
    private Jogador jogador2;
    private Jogador jogadorAtual;
    private Jogador adversario;

    private boolean jogador1PrimeiroTurno = true;
    private boolean jogador2PrimeiroTurno = true;
    private int numeroTurno = 1;
    private boolean jogoAtivo = true;

    // Cores por tipo de Pokémon, usadas nos botões pra deixar visual mais bonito
    private static final Color COR_AGUA = new Color(66, 133, 244);
    private static final Color COR_FOGO = new Color(230, 74, 25);
    private static final Color COR_PLANTA = new Color(76, 175, 80);
    private static final Color COR_NORMAL = new Color(158, 158, 158);
    private static final Color COR_TREINADOR = new Color(255, 193, 7);
    private static final Color COR_FUNDO = new Color(245, 247, 250);
    private static final Color COR_BANNER = new Color(33, 33, 66);

    // Cache de imagens já baixadas, pra não buscar de novo toda vez que a tela atualiza
    private final java.util.Map<String, ImageIcon> cacheSprites = new java.util.HashMap<>();

    // Número da Pokédex de cada Pokémon do jogo, usado pra montar a URL do sprite
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
        POKEDEX.put("Entei", 244);
        POKEDEX.put("Celebi", 251);
        POKEDEX.put("Treecko", 252);
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
    }

    public static void main(String[] args) {
        // Usa o Look and Feel "Metal" pra garantir que as cores dos botões apareçam
        // (o visual nativo do Windows às vezes ignora cor de fundo customizada em botão)
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // se falhar, segue com o visual padrão mesmo
        }

        SwingUtilities.invokeLater(() -> new AppGUI().iniciar());
    }

    private void iniciar() {
        // ---------- CRIAÇÃO DOS 2 JOGADORES (via caixas de diálogo) ----------
        String nome1 = JOptionPane.showInputDialog(null, "Digite o nome do Treinador 1:", "Pokémon TCG - Versus", JOptionPane.QUESTION_MESSAGE);
        if (nome1 == null || nome1.trim().isEmpty()) nome1 = "Treinador 1";
        jogador1 = new Jogador(nome1);

        String nome2 = JOptionPane.showInputDialog(null, "Digite o nome do Treinador 2:", "Pokémon TCG - Versus", JOptionPane.QUESTION_MESSAGE);
        if (nome2 == null || nome2.trim().isEmpty()) nome2 = "Treinador 2";
        jogador2 = new Jogador(nome2);

        boolean[] timesDisponiveis = { true, true, true }; // Água, Fogo, Planta

        escolherTimeDialog(jogador1, timesDisponiveis);
        escolherTimeDialog(jogador2, timesDisponiveis);

        App.adicionarCartasTreinador(jogador1);
        App.adicionarCartasTreinador(jogador2);

        jogador1.verificarBaralho();
        jogador2.verificarBaralho();
        for (int i = 0; i < 6; i++) jogador1.comprarCarta();
        for (int i = 0; i < 6; i++) jogador2.comprarCarta();

        jogadorAtual = jogador1;
        adversario = jogador2;

        montarJanela();

        // Redireciona o System.out pra também aparecer no log da tela
        // (assim TODAS as mensagens que já existem no Jogador.java aparecem aqui de graça)
        redirecionarConsoleParaLog();

        frame.setVisible(true);

        iniciarTurno();
    }

    // ---------- ESCOLHA DE TIME (janela de diálogo) ----------

    private void escolherTimeDialog(Jogador jogador, boolean[] disponivel) {
        while (true) {
            java.util.List<String> opcoes = new java.util.ArrayList<>();
            if (disponivel[0]) opcoes.add("💧 Água");
            if (disponivel[1]) opcoes.add("🔥 Fogo");
            if (disponivel[2]) opcoes.add("🌿 Planta");

            String escolha = (String) JOptionPane.showInputDialog(
                    frame,
                    jogador.getNome() + ", escolha seu time:",
                    "Escolha de Time",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcoes.toArray(),
                    opcoes.get(0)
            );

            if (escolha == null) continue; // obriga escolher, não deixa fechar sem selecionar

            if (escolha.contains("Água")) {
                App.adicionarTimeAgua(jogador);
                disponivel[0] = false;
                return;
            } else if (escolha.contains("Fogo")) {
                App.adicionarTimeFogo(jogador);
                disponivel[1] = false;
                return;
            } else if (escolha.contains("Planta")) {
                App.adicionarTimePlanta(jogador);
                disponivel[2] = false;
                return;
            }
        }
    }

    // ---------- MONTAGEM DA JANELA ----------

    private void montarJanela() {
        frame = new JFrame("Pokémon TCG - Versus");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 800); // tamanho usado caso o usuário desmaximize a janela depois
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // abre já em tela cheia (maximizada)
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(COR_FUNDO);

        // ---- TOPO: banner colorido com informações do turno ----
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

        // ---- CENTRO: campo + mão (esquerda) e log (direita) ----
        JPanel centroEsquerda = new JPanel();
        centroEsquerda.setLayout(new BoxLayout(centroEsquerda, BoxLayout.Y_AXIS));
        centroEsquerda.setBackground(COR_FUNDO);
        centroEsquerda.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        campoJogadorPanel = new JPanel();
        campoJogadorPanel.setBackground(COR_FUNDO);
        campoJogadorPanel.setBorder(criarBordaTitulo("🎮 Seu Campo de Batalha"));
        campoJogadorPanel.setLayout(new BorderLayout(12, 0));

        maoPanel = new JPanel();
        maoPanel.setBackground(COR_FUNDO);
        maoPanel.setBorder(criarBordaTitulo("🃏 Sua Mão (clique numa carta pra jogar)"));
        maoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JScrollPane maoScroll = new JScrollPane(maoPanel);
        maoScroll.setPreferredSize(new Dimension(620, 240));
        maoScroll.setBorder(BorderFactory.createEmptyBorder());

        centroEsquerda.add(campoJogadorPanel);
        centroEsquerda.add(Box.createVerticalStrut(28)); // desce mais a mão, separando bem do campo
        centroEsquerda.add(maoScroll);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(28, 28, 38));
        logArea.setForeground(new Color(180, 255, 180));
        logArea.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(criarBordaTitulo("📜 Log da Partida"));
        logScroll.setPreferredSize(new Dimension(320, 400));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centroEsquerda, logScroll);
        splitPane.setResizeWeight(0.68);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setBackground(COR_FUNDO);
        frame.add(splitPane, BorderLayout.CENTER);

        // ---- RODAPÉ: botões de ação geral ----
        JPanel rodapePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        rodapePanel.setBackground(COR_BANNER);

        JButton btnAtacar = criarBotaoAcao("⚔️ Atacar", new Color(211, 47, 47));
        btnAtacar.addActionListener(e -> acaoAtacar());

        JButton btnPassar = criarBotaoAcao("🏳️ Passar Turno", new Color(96, 125, 139));
        btnPassar.addActionListener(e -> acaoPassarTurno());

        JButton btnSair = criarBotaoAcao("🚪 Sair do Jogo", new Color(66, 66, 66));
        btnSair.addActionListener(e -> System.exit(0));

        rodapePanel.add(btnAtacar);
        rodapePanel.add(btnPassar);
        rodapePanel.add(btnSair);
        frame.add(rodapePanel, BorderLayout.SOUTH);
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

    /**
     * Cor de fundo do botão de acordo com o tipo elemental do Pokémon.
     */
    private Color corDoTipo(String tipo) {
        switch (tipo) {
            case "Água": return COR_AGUA;
            case "Fogo": return COR_FOGO;
            case "Planta": return COR_PLANTA;
            default: return COR_NORMAL;
        }
    }

    /**
     * Gera uma barrinha de HP em texto (████░░░░) proporcional ao HP atual.
     */
    private String barraHP(int hpAtual, int hpMaximo) {
        int total = 10;
        int preenchido = (int) Math.round((hpAtual / (double) hpMaximo) * total);
        preenchido = Math.max(0, Math.min(total, preenchido));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < total; i++) sb.append(i < preenchido ? "█" : "░");
        return sb.toString();
    }

    /**
     * Busca (e cacheia) a ilustração de um Pokémon a partir do nome, usando os sprites públicos
     * do PokeAPI. Se não tiver internet ou o nome não estiver mapeado, retorna null (sem travar o jogo).
     */
    private ImageIcon carregarSprite(String nomePokemon) {
        if (cacheSprites.containsKey(nomePokemon)) {
            return cacheSprites.get(nomePokemon);
        }

        Integer numero = POKEDEX.get(nomePokemon);
        if (numero == null) {
            cacheSprites.put(nomePokemon, null);
            return null;
        }

        try {
            String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + numero + ".png";
            java.awt.Image imagem = ImageIO.read(java.net.URI.create(url).toURL());
            java.awt.Image redimensionada = imagem.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
            ImageIcon icone = new ImageIcon(redimensionada);
            cacheSprites.put(nomePokemon, icone);
            return icone;
        } catch (Exception e) {
            // Sem internet, nome não encontrado, ou qualquer outro problema — só segue sem imagem
            cacheSprites.put(nomePokemon, null);
            return null;
        }
    }

    private void redirecionarConsoleParaLog() {
        PrintStream logStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                logArea.append(String.valueOf((char) b));
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        }, true);
        System.setOut(logStream);
    }

    // ---------- CONTROLE DE TURNO ----------

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
        atualizarTela();
    }

    private void trocarTurno() {
        Jogador troca = jogadorAtual;
        jogadorAtual = adversario;
        adversario = troca;
        numeroTurno++;
        iniciarTurno();
    }

    private void encerrarJogoComDerrota(Jogador perdedor, Jogador vencedor) {
        jogoAtivo = false;
        JOptionPane.showMessageDialog(frame,
                "💀 " + perdedor.getNome() + " não tem mais cartas pra comprar e perdeu o jogo!\n🏆 " + vencedor.getNome() + " venceu a partida!",
                "Fim de Jogo",
                JOptionPane.INFORMATION_MESSAGE);
        turnoLabel.setText("🏆 " + vencedor.getNome() + " venceu a partida!");
    }

    // ---------- ATUALIZAÇÃO VISUAL ----------

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

        // ---- ZONA ESQUERDA: Ativo (avançado) em cima, Banco embaixo ----
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

        // Grid de 5 colunas: só a 3ª (índice 2) tem o Ativo, o resto fica invisível —
        // assim ele fica alinhado exatamente em cima do Banco 3
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

        // ---- ZONA DIREITA: Baralho em cima, Zona Morta embaixo ----
        JPanel zonaDireita = new JPanel();
        zonaDireita.setLayout(new BoxLayout(zonaDireita, BoxLayout.Y_AXIS));
        zonaDireita.setBackground(COR_FUNDO);
        zonaDireita.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        // Monte do Baralho — só informativo (a compra é automática no início de cada turno)
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

        // Zona Morta — pilha de descarte com os Pokémon nocauteados (o último a morrer fica visível em cima)
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
            ImageIcon spriteUltimoMorto = carregarSprite(zonaMorta.get(zonaMorta.size() - 1).getNome());
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

    /**
     * Cria um espaço vazio do mesmo tamanho de um botão de campo, usado pra alinhar
     * o Ativo exatamente em cima do Banco 3 (mesma largura de coluna nos dois grids).
     */
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
            ImageIcon sprite = carregarSprite(p.getNome());
            if (sprite != null) botao.setIcon(sprite);
        } else {
            botao.setBackground(new Color(230, 230, 235));
            botao.setForeground(new Color(120, 120, 120));
        }
    }

    private String htmlPokemon(String cabecalho, CartaPokemon p) {
        return "<html><div style='text-align:center; width:150px;'>"
                + "<b>" + cabecalho + "</b><br>"
                + "<b>" + p.getNome() + "</b><br>"
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

                // Esconde evoluções cuja forma base ainda não está em campo (mesma regra do terminal)
                if (!p.isBasico() && !jogadorAtual.baseEmCampo(p.getEvoluiDe())) {
                    continue;
                }

                String rotulo = "<html><div style='text-align:center; width:130px;'>"
                        + (p.isBasico() ? "🟢 <b>" : "✨ <b>") + p.getNome() + "</b><br>"
                        + p.getTipoElemento() + "<br>"
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
                ImageIcon spriteMao = carregarSprite(p.getNome());
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

    // ---------- AÇÕES: CLIQUE NUM POKÉMON EM CAMPO ----------

    private void abrirMenuPokemon(CartaPokemon pokemon, int indiceBanco) {
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

    // ---------- AÇÕES: CLIQUE NUMA CARTA DA MÃO ----------

    private void acaoClicarCartaPokemon(int indiceNaMao, CartaPokemon carta) {
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
        // Passo 1: escolher qual Pokémon em campo vai evoluir
        CartaPokemon alvo = escolherPokemonEmCampoDialog("Evolução Rápida — qual Pokémon vai evoluir?");
        if (alvo == null) return;

        // Passo 2: procurar na mão as cartas de evolução compatíveis com esse Pokémon
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

    /**
     * Mostra um diálogo listando o Ativo + Banco do jogador atual, e retorna o Pokémon escolhido (ou null).
     */
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

    // ---------- AÇÕES: BOTÕES DE RODAPÉ ----------

    private void acaoAtacar() {
        boolean sucesso = jogadorAtual.atacar(adversario);
        if (sucesso) {
            jogadorAtual.encerrarRodada();
            System.out.println("➡️ Turno encerrado automaticamente após o ataque.");
            trocarTurno();
        } else {
            atualizarTela();
        }
    }

    private void acaoPassarTurno() {
        jogadorAtual.encerrarRodada();
        trocarTurno();
    }
}
