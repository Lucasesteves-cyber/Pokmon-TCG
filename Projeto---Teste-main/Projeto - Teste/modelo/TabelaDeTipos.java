package modelo;

import java.util.Map;

/**
 * Triângulo de fraquezas simplificado do jogo: <b>Água vence Fogo</b>,
 * <b>Fogo vence Planta</b> e <b>Planta vence Água</b>. Um ataque super efetivo
 * causa dano dobrado.
 *
 * <p>As vantagens ficam num {@link Map} (tipo atacante → tipo que ele vence),
 * no lugar da antiga sequência de {@code if}s dentro de {@link Jogador}. Para
 * adicionar uma nova vantagem basta incluir uma entrada no mapa.</p>
 */
public final class TabelaDeTipos {

    private static final Map<String, String> VENCE = Map.of(
            "água", "fogo",
            "fogo", "planta",
            "planta", "água"
    );

    private TabelaDeTipos() { }

    /**
     * @param tipoAtacante tipo elemental de quem ataca (ex.: "Água")
     * @param tipoDefensor tipo elemental de quem recebe o ataque (ex.: "Fogo")
     * @return {@code true} se o atacante tem vantagem sobre o defensor
     */
    public static boolean eSuperEfetivo(String tipoAtacante, String tipoDefensor) {
        if (tipoAtacante == null || tipoDefensor == null) return false;
        String vencido = VENCE.get(tipoAtacante.toLowerCase());
        return vencido != null && vencido.equals(tipoDefensor.toLowerCase());
    }
}
