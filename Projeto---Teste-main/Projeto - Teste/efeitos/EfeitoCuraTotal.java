/** Efeito da carta "Cura Total": restaura o HP de um Pokémon em campo por completo. */
public class EfeitoCuraTotal implements EfeitoTreinador {

    @Override
    public String getNome() { return "Cura Total"; }

    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.POKEMON_EM_CAMPO; }

    @Override
    public String getTextoSelecaoAlvo() { return "Curar qual Pokémon? (Cura Total: HP completo)"; }

    @Override
    public boolean aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        CartaPokemon alvo = parametros.getAlvo();
        if (alvo == null) {
            System.out.println("Escolha um Pokémon válido em campo para curar!");
            return false;
        }

        alvo.curar(alvo.getHpMaximo());
        jogador.getMao().remove(indiceNaMao);

        System.out.println("\n💖 " + jogador.getNome() + " usou Cura Total em " + alvo.getNome()
                + "! HP restaurado por completo. (" + alvo.getHpAtual() + "/" + alvo.getHpMaximo() + ")");
        return true;
    }
}