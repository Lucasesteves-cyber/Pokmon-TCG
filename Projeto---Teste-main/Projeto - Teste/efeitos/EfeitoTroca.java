/** Efeito da carta "Troca": recua o Pokémon ativo gratuitamente, sem gastar energia. */
public class EfeitoTroca implements EfeitoTreinador {

    @Override
    public String getNome() { return "Troca"; }

    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.POKEMON_NO_BANCO; }

    @Override
    public String getTextoSelecaoAlvo() { return "Trocar o Ativo por quem do Banco?"; }

    @Override
    public boolean aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        int indiceBanco = parametros.getIndiceBanco();

        if (jogador.getPokemonAtivo() == null) {
            System.out.println("Você não tem Pokémon Ativo para trocar!");
            return false;
        }
        if (jogador.getBanco().isEmpty()) {
            System.out.println("Seu Banco está vazio, não há para quem trocar!");
            return false;
        }
        if (indiceBanco < 0 || indiceBanco >= jogador.getBanco().size()) {
            System.out.println("Posição inválida no Banco!");
            return false;
        }

        jogador.trocarAtivoComBanco(indiceBanco);
        jogador.getMao().remove(indiceNaMao);

        System.out.println("💨 " + jogador.getNome() + " usou a carta Troca — recuo grátis, sem gastar energia!");
        return true;
    }
}