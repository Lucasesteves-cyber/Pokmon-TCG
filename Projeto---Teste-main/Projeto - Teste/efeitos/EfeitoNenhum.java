/**
 * Estratégia nula (padrão <i>Null Object</i>, complementar ao Strategy):
 * usada por cartas de treinador que não têm efeito jogável, como o
 * "Troféu" que representa uma carta-prêmio conquistada ao nocautear um
 * Pokémon adversário. Evita a necessidade de tratar {@code null} em toda
 * a interface gráfica.
 */
public class EfeitoNenhum implements EfeitoTreinador {

    @Override
    public String getNome() { return "Troféu"; }

    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.NENHUM; }

    @Override
    public boolean aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        // Carta apenas decorativa/de prêmio — não faz nada quando "usada".
        return false;
    }
}