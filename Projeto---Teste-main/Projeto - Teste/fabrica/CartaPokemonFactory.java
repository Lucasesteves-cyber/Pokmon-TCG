package fabrica;

import modelo.CartaPokemon;

/**
 * Centraliza a criação de {@link CartaPokemon}.
 *
 * <p><b>Sobre o nome do padrão:</b> esta classe é uma <i>Simple Factory</i>
 * (também chamada de <i>Static Factory</i>): uma classe com métodos estáticos
 * que concentram a lógica de criação. Ela <b>não</b> é o <i>Factory Method</i>
 * do catálogo GoF, que depende de herança (uma classe abstrata declara o
 * método de criação e as subclasses decidem o que criar). Os dois padrões GoF
 * do projeto são o <b>Strategy</b> ({@link efeitos.EfeitoTreinador}) e o
 * <b>Observer</b> ({@link eventos.OuvinteJogo}); a fábrica é um recurso de
 * organização complementar.</p>
 *
 * <p>Antes dela, o código tinha diversos pontos espalhados por
 * {@code AppGUI} e {@code App} chamando {@code new CartaPokemon(...)}
 * diretamente — alguns deles duplicando, linha por linha, a lógica de
 * "clonar" uma carta, e outro aplicando valores padrão de HP e dano quando os
 * dados recebidos da PokeAPI vinham incompletos. Agora há um único lugar:</p>
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

    /** HP usado quando a PokeAPI não informa o HP do Pokémon. */
    public static final int HP_PADRAO = 50;

    /** Dano usado quando a PokeAPI não informa o ataque do Pokémon. */
    public static final int DANO_PADRAO = 20;

    private CartaPokemonFactory() { }

    /**
     * Cria uma carta a partir de dados obtidos da PokeAPI, aplicando os
     * valores padrão de HP e dano quando a API não os informa.
     *
     * @param numeroDex número na Pokédex (usado para buscar a imagem)
     * @param nome      nome do Pokémon
     * @param tipo      tipo elemental já traduzido (ex.: "Fogo")
     * @param hp        HP vindo da API (valores menores ou iguais a zero usam {@link #HP_PADRAO})
     * @param dano      ataque vindo da API (valores menores ou iguais a zero usam {@link #DANO_PADRAO})
     * @param evoluiDe  nome da pré-evolução, ou {@code null} se for básico
     * @return a carta criada
     */
    public static CartaPokemon criarDaPokeAPI(int numeroDex, String nome, String tipo, int hp, int dano, String evoluiDe) {
        int hpFinal = hp > 0 ? hp : HP_PADRAO;
        int danoFinal = dano > 0 ? dano : DANO_PADRAO;

        CartaPokemon carta = new CartaPokemon(nome, tipo, hpFinal, danoFinal, evoluiDe);
        carta.setNumeroDex(numeroDex);
        return carta;
    }

    /**
     * Cria uma carta de Pokémon básico com estatísticas fixas (times prontos).
     *
     * @param nome nome do Pokémon
     * @param tipo tipo elemental
     * @param hp   HP máximo
     * @param dano dano do ataque
     * @return a carta criada
     */
    public static CartaPokemon criarFixo(String nome, String tipo, int hp, int dano) {
        return new CartaPokemon(nome, tipo, hp, dano);
    }

    /**
     * Cria uma carta de evolução com estatísticas fixas (times prontos).
     *
     * @param nome     nome do Pokémon
     * @param tipo     tipo elemental
     * @param hp       HP máximo
     * @param dano     dano do ataque
     * @param evoluiDe nome da pré-evolução, ou {@code null} se for básico
     * @return a carta criada
     */
    public static CartaPokemon criarFixo(String nome, String tipo, int hp, int dano, String evoluiDe) {
        return new CartaPokemon(nome, tipo, hp, dano, evoluiDe);
    }

    /**
     * Cria uma cópia nova e independente de uma carta (HP cheio, sem energias),
     * preservando nome, tipo, HP máximo, dano, pré-evolução e número da Pokédex.
     *
     * @param modelo carta a ser copiada
     * @return a cópia
     */
    public static CartaPokemon clonar(CartaPokemon modelo) {
        CartaPokemon copia = new CartaPokemon(modelo.getNome(), modelo.getTipoElemento(),
                modelo.getHpMaximo(), modelo.getDanoAtaque(), modelo.getEvoluiDe());
        copia.setNumeroDex(modelo.getNumeroDex());
        return copia;
    }
}
