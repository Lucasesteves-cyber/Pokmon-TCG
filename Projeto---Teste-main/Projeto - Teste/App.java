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

        boolean jogando = true;

        while (jogando) {
            jogador.mostrarTabuleiro();
            jogador.mostrarMao();

            System.out.println("--- O QUE VOCÊ QUER FAZER? ---");
            System.out.println("1. Baixar um Pokémon da mão para o campo");
            System.out.println("2. Sair do jogo");
            System.out.print("Escolha uma opção: ");
            
            int opcao = scanner.nextInt();

            if (opcao == 1) {
                System.out.print("Digite o número da carta da sua mão que quer baixar: ");
                int numeroCarta = scanner.nextInt();
                int indiceNaMao = numeroCarta - 1;
                
                jogador.colocarPokemonEmCampo(indiceNaMao);
                
            } else if (opcao == 2) {
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
