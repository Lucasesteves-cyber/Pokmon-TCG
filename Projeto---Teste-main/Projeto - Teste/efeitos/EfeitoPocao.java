/** Efeito da carta "Poção": cura 30 de HP de um Pokémon em campo. */
public class EfeitoPocao implements EfeitoTreinador {

    private static final int CURA = 30;

    @Override
    public String getNome() { return "Poção"; }

    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.POKEMON_EM_CAMPO; }

    @Override
    public String getTextoSelecaoAlvo() { return "Curar qual Pokémon? (Poção: +30 HP)"; }

    @Override
    public boolean aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        CartaPokemon alvo = parametros.getAlvo();
        if (alvo == null) {
            System.out.println("Escolha um Pokémon válido em campo para curar!");
            return false;
        }

        alvo.curar(CURA);
        jogador.getMao().remove(indiceNaMao);

        System.out.println("\n💊 " + jogador.getNome() + " usou Poção em " + alvo.getNome() + "! Curou " + CURA
                + " HP. (" + alvo.getHpAtual() + "/" + alvo.getHpMaximo() + ")");
        return true;
    }
}