package ui;

import efeitos.EfeitoTreinador;
import efeitos.ParametrosEfeito;
import eventos.OuvinteConsole;
import excecoes.BaralhoVazioException;
import excecoes.JogadaInvalidaException;
import fabrica.BaralhosPadrao;
import fabrica.TimeFixo;
import modelo.Carta;
import modelo.CartaPokemon;
import modelo.CartaTreinador;
import modelo.Jogador;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Protótipo em modo texto (console) do Pokémon TCG, mantido no projeto por
 * razões históricas. A versão jogável e mantida ativamente é a interface
 * gráfica em {@link AppGUI}; esta classe compartilha com ela as mesmas
 * classes de domínio ({@link Jogador}, {@link CartaPokemon}), os mesmos times
 * ({@link TimeFixo}) e o mesmo tratamento de exceções.
 */
public class App {

    private static final int TAMANHO_MAO_INICIAL = 6;

    private App() { }

    /**
     * Ponto de entrada do protótipo em modo texto.
     *
     * @param args não utilizado
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        OuvinteConsole console = new OuvinteConsole();

        System.out.println("=========== POKÉMON TCG - MODO VERSUS ===========");

        Jogador jogador1 = lerJogador(scanner, "Digite o nome do Treinador 1: ");
        Jogador jogador2 = lerJogador(scanner, "Digite o nome do Treinador 2: ");
        jogador1.adicionarOuvinte(console);
        jogador2.adicionarOuvinte(console);

        Set<TimeFixo> timesDisponiveis = EnumSet.allOf(TimeFixo.class);
        escolherTime(scanner, jogador1, timesDisponiveis);
        escolherTime(scanner, jogador2, timesDisponiveis);

        for (Jogador j : List.of(jogador1, jogador2)) {
            BaralhosPadrao.adicionarCartasTreinador(j);
            BaralhosPadrao.adicionarPremios(j);
            j.embaralhar();
            System.out.println("\n🃏 " + j.getNome() + " comprando mão inicial...");
            for (int i = 0; i < TAMANHO_MAO_INICIAL; i++) j.comprarCarta();
        }

        Jogador jogadorAtual = jogador1;
        Jogador adversario = jogador2;
        Set<Jogador> jaJogaramPrimeiroTurno = new java.util.HashSet<>();
        int numeroTurno = 1;
        boolean jogando = true;

        while (jogando) {
            System.out.println("\n######################## TURNO " + numeroTurno + " - VEZ DE "
                    + jogadorAtual.getNome().toUpperCase() + " ########################");

            // Ninguém compra no próprio primeiro turno (os dois já começam com 6 cartas).
            if (!jaJogaramPrimeiroTurno.add(jogadorAtual)) {
                try {
                    jogadorAtual.comprarCarta();
                } catch (BaralhoVazioException e) {
                    System.out.println("\n💀 " + e.getMessage());
                    System.out.println("🏆 " + adversario.getNome() + " venceu a partida!");
                    break;
                }
            }

            jogadorAtual.resetarEvolucoesDoTurno();
            mostrarTabuleiro(jogadorAtual);
            mostrarMao(jogadorAtual);
            System.out.println("🆚 Pokémon Ativo de " + adversario.getNome() + ": " + adversario.getPokemonAtivo());

            boolean turnoTerminou = false;
            int opcao = lerInteiro(scanner, menuDoTurno(jogadorAtual));

            try {
                switch (opcao) {
                    case 1: {
                        int indiceNaMao = lerInteiro(scanner, "Digite o número da carta da sua mão que quer baixar: ") - 1;
                        int destino = lerInteiro(scanner, "Colocar onde? (0 = Pokémon Ativo, 1 = Banco de Reservas) ");
                        jogadorAtual.colocarPokemonEmCampo(indiceNaMao, destino);
                        break;
                    }
                    case 2: {
                        int indice = lerInteiro(scanner, "Anexar Energia em qual Pokémon? (0 = Ativo, 1 a 5 = posição no Banco) ");
                        jogadorAtual.anexarEnergia(jogadorAtual.getPokemonDoCampoPorIndice(indice));
                        break;
                    }
                    case 3: {
                        int indiceNaMao = lerInteiro(scanner, "Digite o número da carta de Evolução na sua mão: ") - 1;
                        int indiceAlvo = lerInteiro(scanner, "Evoluir qual Pokémon em campo? (0 = Ativo, 1 a 5 = posição no Banco) ");
                        jogadorAtual.evoluir(indiceNaMao, jogadorAtual.getPokemonDoCampoPorIndice(indiceAlvo), numeroTurno);
                        break;
                    }
                    case 4:
                        usarCartaTreinador(scanner, jogadorAtual);
                        break;
                    case 5: {
                        int posicaoBanco = lerInteiro(scanner, "Trocar o Ativo por qual posição do Banco? (1 a 5) ");
                        jogadorAtual.recuar(posicaoBanco - 1);
                        break;
                    }
                    case 6:
                        jogadorAtual.atacar(adversario, numeroTurno);
                        if (jogadorAtual.getPremios().isEmpty() || adversario.semPokemonEmCampo()) {
                            System.out.println("\n🏆 " + jogadorAtual.getNome() + " venceu a partida!");
                            jogando = false;
                        }
                        turnoTerminou = true;
                        break;
                    case 7:
                        turnoTerminou = true;
                        break;
                    case 8:
                        System.out.println("Encerrando a partida... Até a próxima!");
                        jogando = false;
                        break;
                    default:
                        System.out.println("Opção inválida! Tente novamente.");
                }
            } catch (JogadaInvalidaException e) {
                System.out.println("⚠️ " + e.getMessage());
            }

            if (turnoTerminou && jogando) {
                jogadorAtual.encerrarRodada();
                Jogador troca = jogadorAtual;
                jogadorAtual = adversario;
                adversario = troca;
                numeroTurno++;
            }

            System.out.println("\n----------------------------------------------------\n");
        }

        scanner.close();
    }

    private static String menuDoTurno(Jogador jogador) {
        return "\n--- " + jogador.getNome() + ", O QUE VOCÊ QUER FAZER? ---\n"
                + "1. Baixar Pokémon Básico da mão para o campo\n"
                + "2. Anexar uma Energia a um Pokémon (1x por turno)\n"
                + "3. Evoluir um Pokémon em campo (1x por Pokémon, a partir do Turno 2)\n"
                + "4. Usar carta de Treinador\n"
                + "5. Recuar o Pokémon Ativo, trocando por um do Banco (1x por turno)\n"
                + "6. Atacar o Pokémon Ativo do adversário — ENCERRA O TURNO\n"
                + "7. Passar o turno sem atacar\n"
                + "8. Sair do jogo\n"
                + "Escolha uma opção: ";
    }

    /**
     * Usa uma carta de treinador de forma genérica: pergunta ao jogador o alvo
     * que a estratégia declara precisar ({@link EfeitoTreinador#getTipoAlvoNecessario()}),
     * sem comparar o nome do efeito.
     */
    private static void usarCartaTreinador(Scanner scanner, Jogador jogador) {
        int indiceNaMao = lerInteiro(scanner, "Digite o número da carta de Treinador na sua mão: ") - 1;
        if (indiceNaMao < 0 || indiceNaMao >= jogador.getMao().size()
                || !(jogador.getMao().get(indiceNaMao) instanceof CartaTreinador)) {
            throw new JogadaInvalidaException("Essa não é uma carta de Treinador válida!");
        }

        EfeitoTreinador efeito = ((CartaTreinador) jogador.getMao().get(indiceNaMao)).getEstrategia();
        ParametrosEfeito parametros = new ParametrosEfeito();

        switch (efeito.getTipoAlvoNecessario()) {
            case POKEMON_EM_CAMPO: {
                int indiceAlvo = lerInteiro(scanner, efeito.getTextoSelecaoAlvo() + " (0 = Ativo, 1 a 5 = posição no Banco) ");
                parametros.setAlvo(jogador.getPokemonDoCampoPorIndice(indiceAlvo));
                break;
            }
            case POKEMON_NO_BANCO:
                parametros.setIndiceBanco(lerInteiro(scanner, efeito.getTextoSelecaoAlvo() + " (1 a 5) ") - 1);
                break;
            case CARTA_EVOLUCAO_E_ALVO: {
                int indiceAlvo = lerInteiro(scanner, "Evoluir qual Pokémon em campo? (0 = Ativo, 1 a 5 = posição no Banco) ");
                int indiceEvolucao = lerInteiro(scanner, "Número da carta de Evolução na sua mão: ") - 1;
                parametros.setAlvo(jogador.getPokemonDoCampoPorIndice(indiceAlvo)).setIndiceCartaEvolucao(indiceEvolucao);
                break;
            }
            case NENHUM:
            default:
                break;
        }

        jogador.usarCartaTreinador(indiceNaMao, parametros);
    }

    private static Jogador lerJogador(Scanner scanner, String pergunta) {
        while (true) {
            System.out.print(pergunta);
            try {
                return new Jogador(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        }
    }

    /**
     * Lê um número inteiro do console. Se o usuário digitar algo que não é número,
     * o {@link Scanner} lança {@link InputMismatchException}; aqui ela é tratada
     * pedindo de novo, em vez de derrubar o programa.
     */
    private static int lerInteiro(Scanner scanner, String pergunta) {
        while (true) {
            System.out.print(pergunta);
            try {
                int valor = scanner.nextInt();
                scanner.nextLine();
                return valor;
            } catch (InputMismatchException e) {
                scanner.nextLine(); // descarta o que foi digitado
                System.out.println("⚠️ Digite apenas números.");
            }
        }
    }

    private static void escolherTime(Scanner scanner, Jogador jogador, Set<TimeFixo> disponiveis) {
        System.out.println("\n=========== " + jogador.getNome().toUpperCase() + ", ESCOLHA SEU TIME ===========");
        List<TimeFixo> times = new ArrayList<>(List.of(TimeFixo.values()));

        while (true) {
            for (int i = 0; i < times.size(); i++) {
                TimeFixo t = times.get(i);
                System.out.println((i + 1) + ". Time " + t.getNome() + (disponiveis.contains(t) ? "" : " ❌ (indisponível)"));
            }
            int opcao = lerInteiro(scanner, "Escolha uma opção: ");

            if (opcao < 1 || opcao > times.size()) {
                System.out.println("Opção inválida! Tente novamente.");
                continue;
            }
            TimeFixo escolhido = times.get(opcao - 1);
            if (!disponiveis.remove(escolhido)) {
                System.out.println("⚠️ O Time " + escolhido.getNome() + " já foi escolhido pelo adversário!");
                continue;
            }
            escolhido.montar(jogador);
            System.out.println(escolhido.getEmoji() + " Time " + escolhido.getNome() + " escolhido por " + jogador.getNome() + "!");
            return;
        }
    }

    /** Imprime no console um resumo do estado atual do jogador. */
    private static void mostrarTabuleiro(Jogador jogador) {
        System.out.println("\n================ TABULEIRO DE " + jogador.getNome().toUpperCase() + " ================");
        System.out.println("🔴 POKÉMON ATIVO: " + (jogador.getPokemonAtivo() != null ? jogador.getPokemonAtivo() : "[Nenhum]"));

        System.out.print("🔵 BANCO DE RESERVAS: ");
        if (jogador.getBanco().isEmpty()) {
            System.out.println("[Vazio]");
        } else {
            for (int i = 0; i < jogador.getBanco().size(); i++) {
                CartaPokemon p = jogador.getBanco().get(i);
                System.out.print("[" + (i + 1) + " - " + p.getNome() + " HP:" + p.getHpAtual() + "] ");
            }
            System.out.println();
        }

        System.out.println("🃏 CARTAS NA MÃO: " + jogador.getMao().size() + " cartas.");
        System.out.println("📚 BARALHO: " + jogador.getTamanhoBaralho() + " cartas restantes.");
        System.out.println("---- Limites do turno ----");
        System.out.println("⚡ Energia anexada: " + (jogador.isEnergiaAnexadaNestaRodada() ? "SIM (esgotado)" : "Disponível"));
        System.out.println("💥 Ataque usado: " + (jogador.isAtacouNestaRodada() ? "SIM (esgotado)" : "Disponível"));
        System.out.println("🔄 Recuo usado: " + (jogador.isRecuouNesteTurno() ? "SIM (esgotado)" : "Disponível"));
        System.out.println("====================================================\n");
    }

    /** Imprime no console as cartas atualmente na mão do jogador. */
    private static void mostrarMao(Jogador jogador) {
        List<Carta> mao = jogador.getMao();
        System.out.println("👋 CARTAS NA SUA MÃO:");
        if (mao.isEmpty()) {
            System.out.println("[Sua mão está vazia]\n");
            return;
        }

        System.out.println("  -- Pokémon --");
        boolean temPokemon = false;
        int evolucoesEscondidas = 0;
        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaPokemon) {
                CartaPokemon p = (CartaPokemon) mao.get(i);
                if (!p.isBasico() && !jogador.baseEmCampo(p.getEvoluiDe())) {
                    evolucoesEscondidas++;
                    continue;
                }
                String tag = !p.isBasico() ? " ✨(Pronto pra evoluir!)" : "";
                System.out.println("  " + (i + 1) + ". " + p + tag);
                temPokemon = true;
            }
        }
        if (!temPokemon) System.out.println("  [Nenhum]");
        if (evolucoesEscondidas > 0) {
            System.out.println("  🔒 " + evolucoesEscondidas + " evolução(ões) escondida(s) até você colocar a forma base em campo.");
        }

        System.out.println("  -- Treinador --");
        boolean temTreinador = false;
        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaTreinador) {
                System.out.println("  " + (i + 1) + ". " + mao.get(i));
                temTreinador = true;
            }
        }
        if (!temTreinador) System.out.println("  [Nenhuma]");

        System.out.println("  -- Energias --");
        System.out.println("  ♾️  Energia disponível: Ilimitada (basta anexar em qualquer Pokémon em campo).");
        System.out.println("      Cada Pokémon tem seu próprio limite (entre 6 e 8 energias).\n");
    }
}
