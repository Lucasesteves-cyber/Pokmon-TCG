/**
 * Centraliza a criação de {@link CartaPokemon} (padrão de projeto
 * <b>Factory Method</b>).
 *
 * <p>Antes desta refatoração, o código tinha diversos pontos espalhados por
 * {@code AppGUI} e {@code App} chamando {@code new CartaPokemon(...)}
 * diretamente — alguns deles duplicando, linha por linha, a lógica de
 * "clonar" uma carta (copiar nome, tipo, HP, dano, pré-evolução e número da
 * Pokédex para uma nova instância), e outro aplicando valores padrão de HP e
 * dano quando os dados recebidos da PokeAPI vinham incompletos.</p>
 *
 * <p>Com a fábrica, esses três pontos de criação passam a ter uma única
 * implementação, testável isoladamente e livre de duplicação:</p>
 * <ul>
 *   <li>{@link #criarDaPokeAPI} — aplica os valores padrão de HP/dano quando
 *       a API não os retorna, e já registra o número da Pokédex;</li>
 *   <li>{@link #criarFixo} — cria uma carta com estatísticas já conhecidas,
 *       usada pelos times fixos (Água, Fogo, Planta);</li>
 *   <li>{@link #clonar} — cria uma cópia independente de uma carta-modelo,
 *       usada ao montar várias cópias da mesma carta em um baralho.</li>
 * </ul>
 */
public final class CartaPokemonFactory {

    private static final int HP_PADRAO = 50;
    private static final int DANO_PADRAO = 20;

    private CartaPokemonFactory() { }

    /**
     * Cria uma carta a partir de dados obtidos da PokeAPI, aplicando os
     * valores padrão de HP e dano quando a API não os informa.
     */
    public static CartaPokemon criarDaPokeAPI(int numeroDex, String nome, String tipo, int hp, int dano, String evoluiDe) {
        int hpFinal = hp > 0 ? hp : HP_PADRAO;
        int danoFinal = dano > 0 ? dano : DANO_PADRAO;

        CartaPokemon carta = new CartaPokemon(nome, tipo, hpFinal, danoFinal, evoluiDe);
        carta.setNumeroDex(numeroDex);
        return carta;
    }

    /** Cria uma carta básica (sem pré-evolução) com estatísticas já conhecidas, usada pelos times fixos. */
    public static CartaPokemon criarFixo(String nome, String tipo, int hp, int dano) {
        return new CartaPokemon(nome, tipo, hp, dano);
    }

    /** Cria uma carta evoluída com estatísticas já conhecidas, usada pelos times fixos. */
    public static CartaPokemon criarFixo(String nome, String tipo, int hp, int dano, String evoluiDe) {
        return new CartaPokemon(nome, tipo, hp, dano, evoluiDe);
    }

    /** Cria uma cópia independente de uma carta-modelo (mesmos atributos, estado de jogo zerado). */
    public static CartaPokemon clonar(CartaPokemon modelo) {
        CartaPokemon copia = new CartaPokemon(modelo.getNome(), modelo.getTipoElemento(),
                modelo.getHpMaximo(), modelo.getDanoAtaque(), modelo.getEvoluiDe());
        copia.setNumeroDex(modelo.getNumeroDex());
        return copia;
    }
}