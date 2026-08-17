import javax.swing.*;
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

    public static void main(String[] args) {
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
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout(8, 8));

        // ---- TOPO: informações do turno ----
        JPanel topoPanel = new JPanel(new GridLayout(2, 1));
        turnoLabel = new JLabel("", SwingConstants.CENTER);
        turnoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        adversarioLabel = new JLabel("", SwingConstants.CENTER);
        adversarioLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        topoPanel.add(turnoLabel);
        topoPanel.add(adversarioLabel);
        frame.add(topoPanel, BorderLayout.NORTH);

        // ---- CENTRO: campo + mão (esquerda) e log (direita) ----
        JPanel centroEsquerda = new JPanel();
        centroEsquerda.setLayout(new BoxLayout(centroEsquerda, BoxLayout.Y_AXIS));

        campoJogadorPanel = new JPanel();
        campoJogadorPanel.setBorder(BorderFactory.createTitledBorder("Seu Campo (clique num Pokémon pra agir)"));
        campoJogadorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        maoPanel = new JPanel();
        maoPanel.setBorder(BorderFactory.createTitledBorder("Sua Mão (clique numa carta pra jogar)"));
        maoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JScrollPane maoScroll = new JScrollPane(maoPanel);
        maoScroll.setPreferredSize(new Dimension(600, 220));

        centroEsquerda.add(campoJogadorPanel);
        centroEsquerda.add(maoScroll);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log da Partida"));
        logScroll.setPreferredSize(new Dimension(320, 400));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centroEsquerda, logScroll);
        splitPane.setResizeWeight(0.68);
        frame.add(splitPane, BorderLayout.CENTER);

        // ---- RODAPÉ: botões de ação geral ----
        JPanel rodapePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton btnAtacar = new JButton("⚔️ Atacar");
        btnAtacar.addActionListener(e -> acaoAtacar());

        JButton btnPassar = new JButton("🏳️ Passar Turno");
        btnPassar.addActionListener(e -> acaoPassarTurno());

        JButton btnSair = new JButton("🚪 Sair do Jogo");
        btnSair.addActionListener(e -> System.exit(0));

        rodapePanel.add(btnAtacar);
        rodapePanel.add(btnPassar);
        rodapePanel.add(btnSair);
        frame.add(rodapePanel, BorderLayout.SOUTH);
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
                + "   |   📚 Baralho: " + jogadorAtual.getTamanhoBaralho() + " cartas   |   🃏 Mão: " + jogadorAtual.getMao().size());

        montarCampoJogador();
        montarMaoJogador();

        frame.revalidate();
        frame.repaint();
    }

    private void montarCampoJogador() {
        campoJogadorPanel.removeAll();

        // Botão do Pokémon Ativo
        CartaPokemon ativo = jogadorAtual.getPokemonAtivo();
        JButton btnAtivo = new JButton("🔴 ATIVO\n" + (ativo != null ? formatarPokemon(ativo) : "[Vazio]"));
        btnAtivo.setPreferredSize(new Dimension(160, 70));
        if (ativo != null) {
            btnAtivo.addActionListener(e -> abrirMenuPokemon(ativo, -1));
        } else {
            btnAtivo.setEnabled(false);
        }
        campoJogadorPanel.add(btnAtivo);

        // Botões do Banco (0 a 4)
        List<CartaPokemon> banco = jogadorAtual.getBanco();
        for (int i = 0; i < 5; i++) {
            if (i < banco.size()) {
                CartaPokemon p = banco.get(i);
                JButton btnBanco = new JButton("🔵 BANCO " + (i + 1) + "\n" + formatarPokemon(p));
                btnBanco.setPreferredSize(new Dimension(160, 70));
                final int indiceBanco = i;
                btnBanco.addActionListener(e -> abrirMenuPokemon(p, indiceBanco));
                campoJogadorPanel.add(btnBanco);
            } else {
                JButton btnVazio = new JButton("🔵 BANCO " + (i + 1) + "\n[Vazio]");
                btnVazio.setPreferredSize(new Dimension(160, 70));
                btnVazio.setEnabled(false);
                campoJogadorPanel.add(btnVazio);
            }
        }
    }

    private String formatarPokemon(CartaPokemon p) {
        return p.getNome() + " | HP " + p.getHpAtual() + "/" + p.getHpMaximo()
                + " | ⚡" + p.getQuantidadeEnergias() + "/" + p.getLimiteEnergias();
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

                String rotulo = (p.isBasico() ? "🟢 " : "✨ ") + p.getNome() + "\n" + p.getTipoElemento()
                        + " | HP " + p.getHpMaximo() + " | Dano " + p.getDanoAtaque();
                JButton btnCarta = new JButton(rotulo);
                btnCarta.setPreferredSize(new Dimension(150, 60));
                btnCarta.addActionListener(e -> acaoClicarCartaPokemon(indice, p));
                maoPanel.add(btnCarta);

            } else if (carta instanceof CartaTreinador) {
                CartaTreinador t = (CartaTreinador) carta;
                JButton btnCarta = new JButton("📘 " + t.getNome() + "\n[" + t.getEfeito() + "]");
                btnCarta.setPreferredSize(new Dimension(150, 60));
                btnCarta.setBackground(new Color(220, 235, 255));
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
            CartaPokemon alvo = escolherPokemonEmCampoDialog("Curar qual Pokémon?");
            if (alvo != null) jogadorAtual.usarPocao(indiceNaMao, alvo);

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
        }

        atualizarTela();
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