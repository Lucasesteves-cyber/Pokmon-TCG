package eventos;

/**
 * Categorias de acontecimentos de uma partida. Permitem que cada
 * {@link OuvinteJogo} reaja de forma diferente a cada tipo de evento
 * (ex.: a interface gráfica poderia tocar um som só em {@link #NOCAUTE}).
 */
public enum TipoEvento {
    /** Um jogador comprou uma carta do baralho. */
    COMPRA,
    /** Um Pokémon foi colocado em campo (Ativo ou Banco). */
    POKEMON_EM_CAMPO,
    /** Uma energia foi anexada a um Pokémon. */
    ENERGIA,
    /** Um Pokémon evoluiu. */
    EVOLUCAO,
    /** Uma carta de Treinador foi usada. */
    TREINADOR,
    /** Um ataque foi realizado. */
    ATAQUE,
    /** Um Pokémon foi nocauteado. */
    NOCAUTE,
    /** Um jogador conquistou uma carta-prêmio. */
    PREMIO,
    /** O Pokémon Ativo foi trocado por um do Banco (recuo, carta Troca ou após nocaute). */
    TROCA_DE_ATIVO,
    /** Um jogador encerrou o turno. */
    FIM_DE_TURNO,
    /** Mensagens gerais da partida (sorteio de quem começa, avisos do Bot etc.). */
    INFORMACAO
}
