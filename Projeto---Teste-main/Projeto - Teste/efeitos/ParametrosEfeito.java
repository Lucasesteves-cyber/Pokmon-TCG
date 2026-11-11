package efeitos;

import modelo.CartaPokemon;

/**
 * Agrupa os parâmetros que a interface gráfica (ou o console, no protótipo
 * de {@link ui.App}) coleta do jogador para aplicar o efeito de uma carta de
 * treinador. Cada {@link EfeitoTreinador} concreto só lê os campos de que
 * realmente precisa — os demais ficam com seus valores padrão.
 */
public class ParametrosEfeito {

    /** Cria a conjunto de parâmetros vazio (sem alvo). */
    public ParametrosEfeito() { }

    /** Pokémon em campo escolhido como alvo (Poção, Cura Total, Evolução Rápida). */
    private CartaPokemon alvo;

    /** Posição do Pokémon no banco escolhido (Troca). -1 quando não se aplica. */
    private int indiceBanco = -1;

    /** Posição, na mão, da carta de evolução escolhida (Evolução Rápida). -1 quando não se aplica. */
    private int indiceCartaEvolucao = -1;

    /** @return Pokémon em campo escolhido como alvo, ou {@code null} se não se aplica */
    public CartaPokemon getAlvo() { return alvo; }

    /**
     * @param alvo Pokémon em campo escolhido como alvo
     * @return este próprio objeto, para encadear chamadas
     */
    public ParametrosEfeito setAlvo(CartaPokemon alvo) { this.alvo = alvo; return this; }

    /** @return posição do Banco escolhida, ou {@code -1} se não se aplica */
    public int getIndiceBanco() { return indiceBanco; }

    /**
     * @param indiceBanco posição (0 a 4) do Pokémon do Banco escolhido
     * @return este próprio objeto, para encadear chamadas
     */
    public ParametrosEfeito setIndiceBanco(int indiceBanco) { this.indiceBanco = indiceBanco; return this; }

    /** @return posição, na mão, da carta de evolução escolhida, ou {@code -1} se não se aplica */
    public int getIndiceCartaEvolucao() { return indiceCartaEvolucao; }

    /**
     * @param indiceCartaEvolucao posição, na mão, da carta de evolução escolhida
     * @return este próprio objeto, para encadear chamadas
     */
    public ParametrosEfeito setIndiceCartaEvolucao(int indiceCartaEvolucao) { this.indiceCartaEvolucao = indiceCartaEvolucao; return this; }
}