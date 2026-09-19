/**
 * Carta de Energia: anexada a um {@link CartaPokemon} em campo, habilita
 * seus ataques. Cada energia tem um tipo elemental (Água, Fogo, Planta etc.).
 */
public class CartaEnergia extends Carta {
    private String tipoElemento;

    /**
     * @param nome         nome de exibição da carta de energia
     * @param tipoElemento tipo elemental da energia (Água, Fogo, Planta etc.)
     */
    public CartaEnergia(String nome, String tipoElemento) {
        super(nome, "Energia");
        this.tipoElemento = tipoElemento;
    }

    /** @return tipo elemental desta energia */
    public String getTipoElemento() {
        return tipoElemento;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + tipoElemento + "]";
    }
}