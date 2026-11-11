package fabrica;

import modelo.Jogador;

import java.util.function.Consumer;

/**
 * Os três times prontos do jogo. Cada constante sabe seu nome de exibição,
 * seu emoji e como montar o próprio baralho.
 *
 * <p>Substitui os antigos códigos soltos ({@code 0 = Água}, {@code "AGUA"}...)
 * e as cadeias de {@code if/else} que existiam tanto em {@code App} quanto em
 * {@code AppGUI}. Os times ainda disponíveis numa partida são guardados num
 * {@code EnumSet} (um {@link java.util.Set} otimizado para enums).</p>
 */
public enum TimeFixo {

    /** Time Água: Froakie, Magikarp, Squirtle, Psyduck e Eevee/Vaporeon. */
    AGUA("Água", "💧", BaralhosPadrao::adicionarTimeAgua),

    /** Time Fogo: Charmander, Growlithe, Torchic, Chimchar e Entei. */
    FOGO("Fogo", "🔥", BaralhosPadrao::adicionarTimeFogo),

    /** Time Planta: Bulbasaur, Chikorita, Treecko, Rowlet e Celebi. */
    PLANTA("Planta", "🌿", BaralhosPadrao::adicionarTimePlanta);

    private final String nome;
    private final String emoji;
    private final Consumer<Jogador> montador;

    TimeFixo(String nome, String emoji, Consumer<Jogador> montador) {
        this.nome = nome;
        this.emoji = emoji;
        this.montador = montador;
    }

    /** @return nome de exibição do time (ex.: "Fogo") */
    public String getNome() { return nome; }

    /** @return emoji que representa o time */
    public String getEmoji() { return emoji; }

    /**
     * Adiciona ao baralho do jogador todas as cartas de Pokémon deste time.
     *
     * @param jogador jogador que recebe as cartas
     */
    public void montar(Jogador jogador) {
        montador.accept(jogador);
    }
}
