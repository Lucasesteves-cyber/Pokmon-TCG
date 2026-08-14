import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Digite o seu nome de Treinador: ");
        String nomeTreinador = scanner.nextLine();
        Jogador jogador = new Jogador(nomeTreinador);

        // Adicionando cartas na mão para começar testando
        jogador.getMao().add(new CartaPokemon("Pikachu", "Elétrico", 60, 30));
        jogador.getMao().add(new CartaPokemon("Charmander", "Fogo", 60, 20));
        jogador.getMao().add(new CartaPokemon("Bulbasaur", "Planta", 70, 10));
        jogador.getMao().add(new CartaEnergia("Energia de Fogo", "Fogo"));
        jogador.getMao().add(new CartaEnergia("Energia Elétrica", "Elétrico"));

        // Oponente de teste, já com um Pokémon Ativo em campo e com energia, para poder revidar os ataques
        Jogador oponente = new Jogador("Rival");
        oponente.getMao().add(new CartaPokemon("Squirtle", "Água", 60, 20));
        oponente.getMao().add(new CartaEnergia("Energia de Água", "Água"));
        oponente.colocarPokemonEmCampo(0);
        oponente.anexarEnergia(0, oponente.getPokemonAtivo());
        oponente.encerrarRodada(); // reseta o limite de energia usado na preparação, sem gastar rodada de verdade

        boolean jogando = true;
        int numeroRodada = 1;

        while (jogando) {
            System.out.println("\n######################## RODADA " + numeroRodada + " ########################");
            jogador.mostrarTabuleiro();
            jogador.mostrarMao();
            System.out.println("🆚 Pokémon Ativo do " + oponente.getNome() + ": " + oponente.getPokemonAtivo());

            System.out.println("\n--- O QUE VOCÊ QUER FAZER? ---");
            System.out.println("1. Baixar um Pokémon da mão para o campo");
            System.out.println("2. Anexar uma Energia a um Pokémon (1x por rodada)");
            System.out.println("3. Atacar o Pokémon Ativo do oponente (1x por rodada)");
            System.out.println("4. Passar a rodada");
            System.out.println("5. Sair do jogo");
            System.out.print("Escolha uma opção: ");
            
            int opcao = scanner.nextInt();

            if (opcao == 1) {
                System.out.print("Digite o número da carta da sua mão que quer baixar: ");
                int numeroCarta = scanner.nextInt();
                int indiceNaMao = numeroCarta - 1;
                
                jogador.colocarPokemonEmCampo(indiceNaMao);

            } else if (opcao == 2) {
                System.out.print("Digite o número da carta de Energia na sua mão: ");
                int numeroCarta = scanner.nextInt();
                int indiceNaMao = numeroCarta - 1;

                System.out.println("Anexar em qual Pokémon? (0 = Ativo, 1 a 5 = posição no Banco)");
                int indiceDestino = scanner.nextInt();
                CartaPokemon destino = jogador.getPokemonDoCampoPorIndice(indiceDestino);

                jogador.anexarEnergia(indiceNaMao, destino);

            } else if (opcao == 3) {
                jogador.atacar(oponente);

            } else if (opcao == 4) {
                jogador.encerrarRodada();

                // O Rival revida automaticamente se ainda tiver um Pokémon Ativo vivo
                if (oponente.getPokemonAtivo() != null) {
                    oponente.atacar(jogador);
                    oponente.encerrarRodada();
                }

                numeroRodada++;

            } else if (opcao == 5) {
                System.out.println("Encerrando a partida... Até a próxima!");
                jogando = false;
            } else {
                System.out.println("Opção inválida! Tente novamente.");
            }
            
            System.out.println("\n----------------------------------------------------\n");
        }

        scanner.close();
    }
}
