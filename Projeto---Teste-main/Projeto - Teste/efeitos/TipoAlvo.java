/**
 * Descreve que tipo de alvo a interface gráfica precisa coletar do jogador
 * antes de aplicar um {@link EfeitoTreinador}.
 *
 * <p>Faz parte do padrão de projeto <b>Strategy</b> aplicado às cartas de
 * treinador: em vez de a interface gráfica decidir "na unha" (com uma cadeia
 * de if/else sobre o nome do efeito) qual diálogo abrir, cada estratégia
 * concreta simplesmente declara o tipo de alvo que precisa, e a interface
 * gráfica trata isso de forma genérica.</p>
 */
public enum TipoAlvo {
    /** Precisa de um Pokémon em campo (ativo ou no banco) como alvo. Ex.: Poção, Cura Total. */
    POKEMON_EM_CAMPO,

    /** Precisa de uma posição do banco para onde trocar o Pokémon ativo. Ex.: Troca. */
    POKEMON_NO_BANCO,

    /** Precisa de uma carta de evolução na mão e de um Pokémon em campo. Ex.: Evolução Rápida. */
    CARTA_EVOLUCAO_E_ALVO,

    /** Não precisa de nenhum alvo adicional (ex.: cartas de prêmio sem efeito, como o Troféu). */
    NENHUM
}