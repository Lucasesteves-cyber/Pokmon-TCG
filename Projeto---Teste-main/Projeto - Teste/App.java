import java.util.Scanner;

public class App {
    // Índices do array de disponibilidade de times: 0 = Água, 1 = Fogo, 2 = Planta
    private static final int AGUA = 0;
    private static final int FOGO = 1;
    private static final int PLANTA = 2;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ---------- CRIAÇÃO DOS 2 JOGADORES ----------
        System.out.println("=========== POKÉMON TCG - MODO VERSUS ===========");

        System.out.print("Digite o nome do Treinador 1: ");
        String nome1 = scanner.nextLine();
        Jogador jogador1 = new Jogador(nome1);

        System.out.print("Digite o nome do Treinador 2: ");
        String nome2 = scanner.nextLine();
        Jogador jogador2 = new Jogador(nome2);

        // Times disponíveis, compartilhado entre os dois jogadores.
        // Assim que um time é escolhido por alguém, ele some da lista do outro também.
        boolean[] timesDisponiveis = { true, true, true }; // Água, Fogo, Planta

        escolherTimes(scanner, jogador1, timesDisponiveis);
        escolherTimes(scanner, jogador2, timesDisponiveis);

        // Cartas de Treinador (Poção e Troca) — independem do time, todo mundo recebe
        adicionarCartasTreinador(jogador1);
        adicionarCartasTreinador(jogador2);

        // Embaralha os baralhos e compra a mão inicial de cada jogador
        int tamanhoMaoInicial = 6;
        jogador1.verificarBaralho();
        jogador2.verificarBaralho();

        System.out.println("\n🃏 " + jogador1.getNome() + " comprando mão inicial...");
        for (int i = 0; i < tamanhoMaoInicial; i++) jogador1.comprarCarta();

        System.out.println("🃏 " + jogador2.getNome() + " comprando mão inicial...");
        for (int i = 0; i < tamanhoMaoInicial; i++) jogador2.comprarCarta();

        // ---------- LOOP PRINCIPAL - TURNOS ALTERNADOS ----------
        Jogador jogadorAtual = jogador1;
        Jogador adversario = jogador2;

        boolean jogador1PrimeiroTurno = true;
        boolean jogador2PrimeiroTurno = true;

        boolean jogando = true;
        int numeroTurno = 1;

        while (jogando) {
            System.out.println("\n######################## TURNO " + numeroTurno + " - VEZ DE " + jogadorAtual.getNome().toUpperCase() + " ########################");

            // ---- PASSO 1: Compra obrigatória (exceto no primeiríssimo turno, que já teve a mão inicial) ----
            boolean ehPrimeiroTurnoDoJogadorAtual = (jogadorAtual == jogador1) ? jogador1PrimeiroTurno : jogador2PrimeiroTurno;
            if (!ehPrimeiroTurnoDoJogadorAtual) {
                boolean conseguiuComprar = jogadorAtual.comprarCarta();
                if (!conseguiuComprar) {
                    System.out.println("\n💀 " + jogadorAtual.getNome() + " não tem mais cartas para comprar e PERDE O JOGO imediatamente!");
                    System.out.println("🏆 " + adversario.getNome() + " venceu a partida!");
                    jogando = false;
                    break;
                }
            } else {
                if (jogadorAtual == jogador1) jogador1PrimeiroTurno = false;
                else jogador2PrimeiroTurno = false;
            }

            // Libera novamente a evolução dos Pokémon deste jogador pro turno que está começando
            jogadorAtual.resetarEvolucoesDoTurno();

            jogadorAtual.mostrarTabuleiro();
            jogadorAtual.mostrarMao();
            System.out.println("🆚 Pokémon Ativo de " + adversario.getNome() + ": " + adversario.getPokemonAtivo());

            System.out.println("\n--- " + jogadorAtual.getNome() + ", O QUE VOCÊ QUER FAZER? (Passo 2: Ações livres) ---");
            System.out.println("1. Baixar Pokémon(s) Básico(s) da mão para o campo (sem limite)");
            System.out.println("2. Anexar uma Energia a um Pokémon (1x por turno)");
            System.out.println("3. Evoluir um Pokémon em campo (1x por Pokémon, a partir do Turno 2)");
            System.out.println("4. Usar carta de Treinador (Poção ou Troca)");
            System.out.println("5. Recuar o Pokémon Ativo, trocando por um do Banco (1x por turno)");
            System.out.println("6. Atacar o Pokémon Ativo do adversário — ENCERRA O TURNO");
            System.out.println("7. Passar o turno sem atacar");
            System.out.println("8. Sair do jogo");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();

            if (opcao == 1) {
                System.out.print("Digite o número da carta da sua mão que quer baixar: ");
                int numeroCarta = scanner.nextInt();
                int indiceNaMao = numeroCarta - 1;

                System.out.println("Colocar onde? (0 = Pokémon Ativo, 1 = Banco de Reservas)");
                int destino = scanner.nextInt();

                jogadorAtual.colocarPokemonEmCampo(indiceNaMao, destino);

            } else if (opcao == 2) {
                System.out.println("Anexar Energia em qual Pokémon? (0 = Ativo, 1 a 5 = posição no Banco)");
                int indiceDestino = scanner.nextInt();
                CartaPokemon destino = jogadorAtual.getPokemonDoCampoPorIndice(indiceDestino);

                jogadorAtual.anexarEnergia(destino);

            } else if (opcao == 3) {
                System.out.print("Digite o número da carta de Evolução na sua mão: ");
                int numeroCarta = scanner.nextInt();
                int indiceNaMao = numeroCarta - 1;

                System.out.println("Evoluir qual Pokémon em campo? (0 = Ativo, 1 a 5 = posição no Banco)");
                int indiceAlvo = scanner.nextInt();
                CartaPokemon alvo = jogadorAtual.getPokemonDoCampoPorIndice(indiceAlvo);

                jogadorAtual.evoluir(indiceNaMao, alvo, numeroTurno);

            } else if (opcao == 4) {
                System.out.print("Digite o número da carta de Treinador na sua mão: ");
                int numeroCarta = scanner.nextInt();
                int indiceNaMao = numeroCarta - 1;

                if (indiceNaMao < 0 || indiceNaMao >= jogadorAtual.getMao().size()
                        || !(jogadorAtual.getMao().get(indiceNaMao) instanceof CartaTreinador)) {
                    System.out.println("Essa não é uma carta de Treinador válida!");
                } else {
                    CartaTreinador cartaTreinador = (CartaTreinador) jogadorAtual.getMao().get(indiceNaMao);

                    if (cartaTreinador.getEfeito().equalsIgnoreCase("Poção")) {
                        System.out.println("Curar qual Pokémon? (0 = Ativo, 1 a 5 = posição no Banco)");
                        int indiceAlvo = scanner.nextInt();
                        CartaPokemon alvo = jogadorAtual.getPokemonDoCampoPorIndice(indiceAlvo);
                        jogadorAtual.usarPocao(indiceNaMao, alvo);

                    } else if (cartaTreinador.getEfeito().equalsIgnoreCase("Troca")) {
                        System.out.println("Trocar o Ativo por qual posição do Banco? (1 a 5)");
                        int posicaoBanco = scanner.nextInt();
                        jogadorAtual.usarTroca(indiceNaMao, posicaoBanco - 1);
                    }
                }

            } else if (opcao == 5) {
                System.out.println("Trocar o Ativo por qual posição do Banco? (1 a 5)");
                int posicaoBanco = scanner.nextInt();
                int indiceBanco = posicaoBanco - 1;

                jogadorAtual.recuar(indiceBanco);

            } else if (opcao == 6) {
                boolean atacou = jogadorAtual.atacar(adversario);

                if (atacou) {
                    // Passo 3: atacar encerra o turno imediatamente
                    jogadorAtual.encerrarRodada();
                    System.out.println("➡️ Turno encerrado automaticamente após o ataque.");

                    Jogador troca = jogadorAtual;
                    jogadorAtual = adversario;
                    adversario = troca;

                    numeroTurno++;
                }

            } else if (opcao == 7) {
                jogadorAtual.encerrarRodada();

                // Troca de turno: quem jogava vira adversário, e vice-versa
                Jogador troca = jogadorAtual;
                jogadorAtual = adversario;
                adversario = troca;

                numeroTurno++;

            } else if (opcao == 8) {
                System.out.println("Encerrando a partida... Até a próxima!");
                jogando = false;
            } else {
                System.out.println("Opção inválida! Tente novamente.");
            }

            System.out.println("\n----------------------------------------------------\n");
        }

        scanner.close();
    }

    // ---------- ESCOLHA DE TIME (com exclusividade entre os jogadores) ----------

    private static void escolherTimes(Scanner scanner, Jogador jogador, boolean[] disponivel) {
        boolean escolheu = false;

        System.out.println("\n=========== " + jogador.getNome().toUpperCase() + ", ESCOLHA SEU TIME ===========");

        while (!escolheu) {
            System.out.println("\n1. Time Água" + (!disponivel[AGUA] ? " ❌ (indisponível)" : ""));
            System.out.println("2. Time Fogo" + (!disponivel[FOGO] ? " ❌ (indisponível)" : ""));
            System.out.println("3. Time Planta" + (!disponivel[PLANTA] ? " ❌ (indisponível)" : ""));
            System.out.print("Escolha uma opção: ");

            int opcaoTime = scanner.nextInt();

            if (opcaoTime == 1) {
                if (disponivel[AGUA]) {
                    adicionarTimeAgua(jogador);
                    disponivel[AGUA] = false;
                    escolheu = true;
                    System.out.println("💧 Time Água escolhido por " + jogador.getNome() + "!");
                } else {
                    System.out.println("⚠️ O Time Água já foi escolhido pelo adversário!");
                }
            } else if (opcaoTime == 2) {
                if (disponivel[FOGO]) {
                    adicionarTimeFogo(jogador);
                    disponivel[FOGO] = false;
                    escolheu = true;
                    System.out.println("🔥 Time Fogo escolhido por " + jogador.getNome() + "!");
                } else {
                    System.out.println("⚠️ O Time Fogo já foi escolhido pelo adversário!");
                }
            } else if (opcaoTime == 3) {
                if (disponivel[PLANTA]) {
                    adicionarTimePlanta(jogador);
                    disponivel[PLANTA] = false;
                    escolheu = true;
                    System.out.println("🌿 Time Planta escolhido por " + jogador.getNome() + "!");
                } else {
                    System.out.println("⚠️ O Time Planta já foi escolhido pelo adversário!");
                }
            } else {
                System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }

    // ---------- TIMES DISPONÍVEIS ----------
    // Cada método adiciona a linha evolutiva completa daquele elemento no baralho, com múltiplas cópias
    // (regra oficial: até 4 cópias por carta com o mesmo nome). Básicos x3, evoluções x2, lendários x1.

    static void adicionarCopias(Jogador jogador, String nome, String tipo, int hp, int dano, String evoluiDe, int copias) {
        for (int i = 0; i < copias; i++) {
            if (evoluiDe == null) {
                jogador.adicionarAoBaralho(new CartaPokemon(nome, tipo, hp, dano));
            } else {
                jogador.adicionarAoBaralho(new CartaPokemon(nome, tipo, hp, dano, evoluiDe));
            }
        }
    }

    static void adicionarTimeAgua(Jogador jogador) {
        adicionarCopias(jogador, "Froakie", "Água", 40, 10, null, 4);
        adicionarCopias(jogador, "Frogadier", "Água", 65, 30, "Froakie", 4);
        adicionarCopias(jogador, "Greninja", "Água", 90, 60, "Frogadier", 3);

        adicionarCopias(jogador, "Magikarp", "Água", 30, 5, null, 4);
        adicionarCopias(jogador, "Gyarados", "Água", 130, 80, "Magikarp", 3);

        adicionarCopias(jogador, "Squirtle", "Água", 50, 15, null, 4);
        adicionarCopias(jogador, "Wartortle", "Água", 80, 35, "Squirtle", 4);
        adicionarCopias(jogador, "Blastoise", "Água", 120, 70, "Wartortle", 4);

        adicionarCopias(jogador, "Psyduck", "Água", 50, 20, null, 4);
        adicionarCopias(jogador, "Golduck", "Água", 90, 55, "Psyduck", 4);

        adicionarCopias(jogador, "Eevee", "Normal", 55, 15, null, 4);
        adicionarCopias(jogador, "Vaporeon", "Água", 100, 50, "Eevee", 3);
    }

    static void adicionarTimeFogo(Jogador jogador) {
        adicionarCopias(jogador, "Charmander", "Fogo", 50, 15, null, 4);
        adicionarCopias(jogador, "Charmeleon", "Fogo", 75, 35, "Charmander", 4);
        adicionarCopias(jogador, "Charizard", "Fogo", 130, 90, "Charmeleon", 4);

        adicionarCopias(jogador, "Growlithe", "Fogo", 55, 20, null, 4);
        adicionarCopias(jogador, "Arcanine", "Fogo", 120, 75, "Growlithe", 3);

        adicionarCopias(jogador, "Torchic", "Fogo", 45, 15, null, 4);
        adicionarCopias(jogador, "Combusken", "Fogo", 70, 35, "Torchic", 4);
        adicionarCopias(jogador, "Blaziken", "Fogo", 125, 85, "Combusken", 4);

        adicionarCopias(jogador, "Chimchar", "Fogo", 45, 15, null, 4);
        adicionarCopias(jogador, "Monferno", "Fogo", 70, 35, "Chimchar", 4);
        adicionarCopias(jogador, "Infernape", "Fogo", 115, 80, "Monferno", 4);

        adicionarCopias(jogador, "Entei", "Fogo", 120, 70, null, 2); // Lendário, não evolui, raro (2 cópias)
    }

    static void adicionarTimePlanta(Jogador jogador) {
        adicionarCopias(jogador, "Bulbasaur", "Planta", 55, 15, null, 4);
        adicionarCopias(jogador, "Ivysaur", "Planta", 80, 35, "Bulbasaur", 4);
        adicionarCopias(jogador, "Venusaur", "Planta", 130, 80, "Ivysaur", 3);

        adicionarCopias(jogador, "Chikorita", "Planta", 50, 15, null, 4);
        adicionarCopias(jogador, "Bayleef", "Planta", 75, 32, "Chikorita", 4);
        adicionarCopias(jogador, "Meganium", "Planta", 125, 75, "Bayleef", 3);

        adicionarCopias(jogador, "Treecko", "Planta", 45, 15, null, 4);
        adicionarCopias(jogador, "Grovyle", "Planta", 70, 33, "Treecko", 4);
        adicionarCopias(jogador, "Sceptile", "Planta", 120, 78, "Grovyle", 3);

        adicionarCopias(jogador, "Rowlet", "Planta", 45, 10, null, 4);
        adicionarCopias(jogador, "Dartrix", "Planta", 65, 28, "Rowlet", 4);
        adicionarCopias(jogador, "Decidueye", "Planta", 115, 72, "Dartrix", 2);

        adicionarCopias(jogador, "Celebi", "Planta", 100, 60, null, 2); // Lendário, não evolui, raro (2 cópias)
    }

    // ---------- CARTAS DE TREINADOR ----------
    // Independem do time escolhido — todo jogador recebe as mesmas cartas de item

    static void adicionarCartasTreinador(Jogador jogador) {
        for (int i = 0; i < 4; i++) jogador.adicionarAoBaralho(new CartaTreinador("Poção", "Poção"));
        for (int i = 0; i < 3; i++) jogador.adicionarAoBaralho(new CartaTreinador("Troca", "Troca"));
        for (int i = 0; i < 4; i++) jogador.adicionarAoBaralho(new CartaTreinador("Evolução Rápida", "Evolução Rápida"));
        for (int i = 0; i < 4; i++) jogador.adicionarAoBaralho(new CartaTreinador("Cura Total", "Cura Total"));
    }
}