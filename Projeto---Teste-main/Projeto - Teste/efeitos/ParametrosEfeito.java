/**
 * Agrupa os parâmetros que a interface gráfica (ou o console, no protótipo
 * de {@link App}) coleta do jogador para aplicar o efeito de uma carta de
 * treinador. Cada {@link EfeitoTreinador} concreto só lê os campos de que
 * realmente precisa — os demais ficam com seus valores padrão.
 */
public class ParametrosEfeito {

    /** Pokémon em campo escolhido como alvo (Poção, Cura Total, Evolução Rápida). */
    private CartaPokemon alvo;

    /** Posição do Pokémon no banco escolhido (Troca). -1 quando não se aplica. */
    private int indiceBanco = -1;

    /** Posição, na mão, da carta de evolução escolhida (Evolução Rápida). -1 quando não se aplica. */
    private int indiceCartaEvolucao = -1;

    public CartaPokemon getAlvo() { return alvo; }
    public void setAlvo(CartaPokemon alvo) { this.alvo = alvo; }

    public int getIndiceBanco() { return indiceBanco; }
    public void setIndiceBanco(int indiceBanco) { this.indiceBanco = indiceBanco; }

    public int getIndiceCartaEvolucao() { return indiceCartaEvolucao; }
    public void setIndiceCartaEvolucao(int indiceCartaEvolucao) { this.indiceCartaEvolucao = indiceCartaEvolucao; }
}