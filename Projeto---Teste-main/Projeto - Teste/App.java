import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Digite o seu nome de Treinador: ");
        String nomeTreinador = scanner.nextLine();
        Jogador jogador = new Jogador(nomeTreinador);

        // ---------- ESCOLHA DE TIME ----------
        boolean temAgua = false;
        boolean temFogo = false;
        boolean temPlanta = false;
        boolean escolhendo = true;

        System.out.println("\n=========== ESCOLHA SEU TIME ===========");
        System.out.println("Você pode escolher mais de um time. Digite o número e aperte Enter.");

        while (escolhendo) {
            System.out.println("\n1. Time Água" + (temAgua ? " ✅ (já escolhido)" : ""));
            System.out.println("2. Time Fogo" + (temFogo ? " ✅ (já escolhido)" : ""));
            System.out.println("3. Time Planta" + (temPlanta ? " ✅ (já escolhido)" : ""));
            System.out.println("0. Concluir escolha e começar a partida");
            System.out.print("Escolha uma opção: ");

            int opcaoTime = scanner.nextInt();

            if (opcaoTime == 1) {
                if (!temAgua) {
                    adicionarTimeAgua(jogador);
                    temAgua = true;
                    System.out.println("💧 Time Água adicionado à sua mão!");
                } else {
                    System.out.println("Você já escolheu o Time Água!");
                }
            } else if (opcaoTime == 2) {
                if (!temFogo) {
                    adicionarTimeFogo(jogador);
                    temFogo = true;
                    System.out.println("🔥 Time Fogo adicionado à sua mão!");
                } else {
                    System.out.println("Você já escolheu o Time Fogo!");
                }
            } else if (opcaoTime == 3) {
                if (!temPlanta) {
                    adicionarTimePlanta(jogador);
                    temPlanta = true;
                    System.out.println("🌿 Time Planta adicionado à sua mão!");
                } else {
                    System.out.println("Você já escolheu o Time Planta!");
                }
            } else if (opcaoTime == 0) {
                if (!temAgua && !temFogo && !temPlanta) {
                    System.out.println("⚠️ Escolha pelo menos 1 time antes de começar!");
                } else {
                    escolhendo = false;
                }
            } else {
                System.out.println("Opção inválida! Tente novamente.");
            }
        }

        // Embaralha o baralho montado e compra a mão inicial
        jogador.verificarBaralho();
        int tamanhoMaoInicial = 6;
        System.out.println("\n🃏 Comprando sua mão inicial de " + tamanhoMaoInicial + " cartas...");
        for (int i = 0; i < tamanhoMaoInicial; i++) {
            jogador.comprarCarta();
        }

        // Oponente de teste, já com um Pokémon Ativo em campo e com energia, para poder revidar os ataques
        Jogador oponente = new Jogador("Rival");
        oponente.getMao().add(new CartaPokemon("Blastoise", "Água", 120, 70));
        oponente.colocarPokemonEmCampo(0, 0);
        oponente.anexarEnergia(oponente.getPokemonAtivo());
        oponente.encerrarRodada(); // reseta o limite de energia usado na preparação, sem gastar rodada de verdade

        boolean jogando = true;
        int numeroRodada = 1;

        while (jogando) {
            System.out.println("\n######################## RODADA " + numeroRodada + " ########################");

            // A partir da 2ª rodada, o jogador compra 1 carta automaticamente no início do turno
            if (numeroRodada > 1) {
                jogador.comprarCarta();
            }

            jogador.mostrarTabuleiro();
            jogador.mostrarMao();
            System.out.println("🆚 Pokémon Ativo do " + oponente.getNome() + ": " + oponente.getPokemonAtivo());

            System.out.println("\n--- O QUE VOCÊ QUER FAZER? ---");
            System.out.println("1. Baixar um Pokémon Básico da mão para o campo");
            System.out.println("2. Anexar uma Energia a um Pokémon (1x por rodada)");
            System.out.println("3. Evoluir um Pokémon em campo (1x por rodada)");
            System.out.println("4. Atacar o Pokémon Ativo do oponente (1x por rodada)");
            System.out.println("5. Passar a rodada");
            System.out.println("6. Sair do jogo");
            System.out.print("Escolha uma opção: ");
            
            int opcao = scanner.nextInt();

            if (opcao == 1) {
                System.out.print("Digite o número da carta da sua mão que quer baixar: ");
                int numeroCarta = scanner.nextInt();
                int indiceNaMao = numeroCarta - 1;

                System.out.println("Colocar onde? (0 = Pokémon Ativo, 1 = Banco de Reservas)");
                int destino = scanner.nextInt();

                jogador.colocarPokemonEmCampo(indiceNaMao, destino);

            } else if (opcao == 2) {
                System.out.println("Anexar Energia em qual Pokémon? (0 = Ativo, 1 a 5 = posição no Banco)");
                int indiceDestino = scanner.nextInt();
                CartaPokemon destino = jogador.getPokemonDoCampoPorIndice(indiceDestino);

                jogador.anexarEnergia(destino);

            } else if (opcao == 3) {
                System.out.print("Digite o número da carta de Evolução na sua mão: ");
                int numeroCarta = scanner.nextInt();
                int indiceNaMao = numeroCarta - 1;

                System.out.println("Evoluir qual Pokémon em campo? (0 = Ativo, 1 a 5 = posição no Banco)");
                int indiceAlvo = scanner.nextInt();
                CartaPokemon alvo = jogador.getPokemonDoCampoPorIndice(indiceAlvo);

                jogador.evoluir(indiceNaMao, alvo);

            } else if (opcao == 4) {
                jogador.atacar(oponente);

            } else if (opcao == 5) {
                jogador.encerrarRodada();

                // O Rival revida automaticamente se ainda tiver um Pokémon Ativo vivo
                if (oponente.getPokemonAtivo() != null) {
                    oponente.atacar(jogador);
                    oponente.encerrarRodada();
                }

                numeroRodada++;

            } else if (opcao == 6) {
                System.out.println("Encerrando a partida... Até a próxima!");
                jogando = false;
            } else {
                System.out.println("Opção inválida! Tente novamente.");
            }
            
            System.out.println("\n----------------------------------------------------\n");
        }

        scanner.close();
    }

    // ---------- TIMES DISPONÍVEIS ----------
    // Cada método adiciona a linha evolutiva completa + energias daquele elemento na mão do jogador

    private static void adicionarTimeAgua(Jogador jogador) {
        jogador.adicionarAoBaralho(new CartaPokemon("Froakie", "Água", 40, 10));
        jogador.adicionarAoBaralho(new CartaPokemon("Frogadier", "Água", 65, 30, "Froakie"));
        jogador.adicionarAoBaralho(new CartaPokemon("Greninja", "Água", 90, 60, "Frogadier"));

        jogador.adicionarAoBaralho(new CartaPokemon("Magikarp", "Água", 30, 5));
        jogador.adicionarAoBaralho(new CartaPokemon("Gyarados", "Água", 130, 80, "Magikarp"));

        jogador.adicionarAoBaralho(new CartaPokemon("Squirtle", "Água", 50, 15));
        jogador.adicionarAoBaralho(new CartaPokemon("Wartortle", "Água", 80, 35, "Squirtle"));
        jogador.adicionarAoBaralho(new CartaPokemon("Blastoise", "Água", 120, 70, "Wartortle"));

        jogador.adicionarAoBaralho(new CartaPokemon("Psyduck", "Água", 50, 20));
        jogador.adicionarAoBaralho(new CartaPokemon("Golduck", "Água", 90, 55, "Psyduck"));

        jogador.adicionarAoBaralho(new CartaPokemon("Eevee", "Normal", 55, 15));
        jogador.adicionarAoBaralho(new CartaPokemon("Vaporeon", "Água", 100, 50, "Eevee"));

    }

    private static void adicionarTimeFogo(Jogador jogador) {
        jogador.adicionarAoBaralho(new CartaPokemon("Charmander", "Fogo", 50, 15));
        jogador.adicionarAoBaralho(new CartaPokemon("Charmeleon", "Fogo", 75, 35, "Charmander"));
        jogador.adicionarAoBaralho(new CartaPokemon("Charizard", "Fogo", 130, 90, "Charmeleon"));

        jogador.adicionarAoBaralho(new CartaPokemon("Growlithe", "Fogo", 55, 20));
        jogador.adicionarAoBaralho(new CartaPokemon("Arcanine", "Fogo", 120, 75, "Growlithe"));

        jogador.adicionarAoBaralho(new CartaPokemon("Torchic", "Fogo", 45, 15));
        jogador.adicionarAoBaralho(new CartaPokemon("Combusken", "Fogo", 70, 35, "Torchic"));
        jogador.adicionarAoBaralho(new CartaPokemon("Blaziken", "Fogo", 125, 85, "Combusken"));

        jogador.adicionarAoBaralho(new CartaPokemon("Chimchar", "Fogo", 45, 15));
        jogador.adicionarAoBaralho(new CartaPokemon("Monferno", "Fogo", 70, 35, "Chimchar"));
        jogador.adicionarAoBaralho(new CartaPokemon("Infernape", "Fogo", 115, 80, "Monferno"));

        jogador.adicionarAoBaralho(new CartaPokemon("Entei", "Fogo", 120, 70)); // Lendário, não evolui

    }

    private static void adicionarTimePlanta(Jogador jogador) {
        jogador.adicionarAoBaralho(new CartaPokemon("Bulbasaur", "Planta", 55, 15));
        jogador.adicionarAoBaralho(new CartaPokemon("Ivysaur", "Planta", 80, 35, "Bulbasaur"));
        jogador.adicionarAoBaralho(new CartaPokemon("Venusaur", "Planta", 130, 80, "Ivysaur"));

        jogador.adicionarAoBaralho(new CartaPokemon("Chikorita", "Planta", 50, 15)); // ainda não tem evolução na mão
        jogador.adicionarAoBaralho(new CartaPokemon("Treecko", "Planta", 45, 15));   // ainda não tem evolução na mão
        jogador.adicionarAoBaralho(new CartaPokemon("Rowlet", "Planta", 45, 10));    // ainda não tem evolução na mão

        jogador.adicionarAoBaralho(new CartaPokemon("Celebi", "Planta", 100, 60)); // Lendário, não evolui

    }
}
