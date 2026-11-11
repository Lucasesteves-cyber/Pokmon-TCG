package fabrica;

import modelo.CartaTreinador;
import modelo.Jogador;

/**
 * Monta os baralhos padrão do jogo: os três times fixos (Água, Fogo e Planta),
 * as cartas de treinador que todo baralho recebe e as cartas-prêmio.
 *
 * <p>Antes, esses métodos ficavam dentro do protótipo de console
 * ({@code ui.App}) e a interface gráfica precisava chamar {@code App.adicionarTimeAgua(...)},
 * criando uma dependência estranha entre as duas interfaces. Agora ambas usam
 * esta classe (ou o enum {@link TimeFixo}, que delega para cá).</p>
 */
public final class BaralhosPadrao {

    /** Quantas cartas-prêmio cada jogador precisa conquistar para vencer. */
    public static final int QUANTIDADE_PREMIOS = 2;

    private BaralhosPadrao() { }

    /**
     * Coloca as {@value #QUANTIDADE_PREMIOS} cartas-prêmio ("Troféu") na pilha de prêmios do jogador.
     *
     * @param jogador jogador que recebe os prêmios
     */
    public static void adicionarPremios(Jogador jogador) {
        for (int i = 0; i < QUANTIDADE_PREMIOS; i++) {
            jogador.getPremios().add(new CartaTreinador("Troféu", "Troféu"));
        }
    }

    /** Adiciona ao baralho do jogador várias cópias de uma carta, criadas via {@link CartaPokemonFactory}. */
    private static void adicionarCopias(Jogador jogador, String nome, String tipo, int hp, int dano, String evoluiDe, int copias) {
        for (int i = 0; i < copias; i++) {
            jogador.adicionarAoBaralho(CartaPokemonFactory.criarFixo(nome, tipo, hp, dano, evoluiDe));
        }
    }

    /** Monta o time fixo do elemento Água no baralho do jogador. */
    static void adicionarTimeAgua(Jogador jogador) {
        adicionarCopias(jogador, "Froakie", "Água", 40, 10, null, 4);
        adicionarCopias(jogador, "Frogadier", "Água", 65, 30, "Froakie", 3);
        adicionarCopias(jogador, "Greninja", "Água", 90, 60, "Frogadier", 2);

        adicionarCopias(jogador, "Magikarp", "Água", 30, 5, null, 4);
        adicionarCopias(jogador, "Gyarados", "Água", 130, 80, "Magikarp", 2);

        adicionarCopias(jogador, "Squirtle", "Água", 50, 15, null, 4);
        adicionarCopias(jogador, "Wartortle", "Água", 80, 35, "Squirtle", 3);
        adicionarCopias(jogador, "Blastoise", "Água", 120, 70, "Wartortle", 4);

        adicionarCopias(jogador, "Psyduck", "Água", 50, 20, null, 4);
        adicionarCopias(jogador, "Golduck", "Água", 90, 55, "Psyduck", 4);

        adicionarCopias(jogador, "Eevee", "Normal", 55, 15, null, 4);
        adicionarCopias(jogador, "Vaporeon", "Água", 100, 50, "Eevee", 2);
    }

    /** Monta o time fixo do elemento Fogo no baralho do jogador. */
    static void adicionarTimeFogo(Jogador jogador) {
        adicionarCopias(jogador, "Charmander", "Fogo", 50, 15, null, 4);
        adicionarCopias(jogador, "Charmeleon", "Fogo", 75, 35, "Charmander", 3);
        adicionarCopias(jogador, "Charizard", "Fogo", 130, 90, "Charmeleon", 3);

        adicionarCopias(jogador, "Growlithe", "Fogo", 55, 20, null, 4);
        adicionarCopias(jogador, "Arcanine", "Fogo", 120, 75, "Growlithe", 2);

        adicionarCopias(jogador, "Torchic", "Fogo", 45, 15, null, 4);
        adicionarCopias(jogador, "Combusken", "Fogo", 70, 35, "Torchic", 4);
        adicionarCopias(jogador, "Blaziken", "Fogo", 125, 85, "Combusken", 3);

        adicionarCopias(jogador, "Chimchar", "Fogo", 45, 15, null, 4);
        adicionarCopias(jogador, "Monferno", "Fogo", 70, 35, "Chimchar", 4);
        adicionarCopias(jogador, "Infernape", "Fogo", 115, 80, "Monferno", 3);

        adicionarCopias(jogador, "Entei", "Fogo", 120, 70, null, 2);
    }

    /** Monta o time fixo do elemento Planta no baralho do jogador. */
    static void adicionarTimePlanta(Jogador jogador) {
        adicionarCopias(jogador, "Bulbasaur", "Planta", 55, 15, null, 4);
        adicionarCopias(jogador, "Ivysaur", "Planta", 80, 35, "Bulbasaur", 3);
        adicionarCopias(jogador, "Venusaur", "Planta", 130, 80, "Ivysaur", 2);

        adicionarCopias(jogador, "Chikorita", "Planta", 50, 15, null, 4);
        adicionarCopias(jogador, "Bayleef", "Planta", 75, 32, "Chikorita", 3);
        adicionarCopias(jogador, "Meganium", "Planta", 125, 75, "Bayleef", 2);

        adicionarCopias(jogador, "Treecko", "Planta", 45, 15, null, 4);
        adicionarCopias(jogador, "Grovyle", "Planta", 70, 33, "Treecko", 3);
        adicionarCopias(jogador, "Sceptile", "Planta", 120, 78, "Grovyle", 3);

        adicionarCopias(jogador, "Rowlet", "Planta", 45, 10, null, 4);
        adicionarCopias(jogador, "Dartrix", "Planta", 65, 28, "Rowlet", 4);
        adicionarCopias(jogador, "Decidueye", "Planta", 115, 72, "Dartrix", 2);

        adicionarCopias(jogador, "Celebi", "Planta", 100, 60, null, 2);
    }

    /**
     * Monta o conjunto padrão de cartas de treinador de efeito (Poção, Troca,
     * Evolução Rápida, Cura Total) no baralho do jogador. As 2 cartas-prêmio
     * "Troféu" (usadas como condição de vitória) são adicionadas separadamente,
     * por {@link #adicionarPremios(Jogador)}.
     *
     * @param jogador jogador que recebe as cartas no baralho
     */
    public static void adicionarCartasTreinador(Jogador jogador) {
        for (int i = 0; i < 4; i++) jogador.adicionarAoBaralho(new CartaTreinador("Poção", "Poção"));
        for (int i = 0; i < 3; i++) jogador.adicionarAoBaralho(new CartaTreinador("Troca", "Troca"));
        for (int i = 0; i < 4; i++) jogador.adicionarAoBaralho(new CartaTreinador("Evolução Rápida", "Evolução Rápida"));
        for (int i = 0; i < 4; i++) jogador.adicionarAoBaralho(new CartaTreinador("Cura Total", "Cura Total"));
    }
}
