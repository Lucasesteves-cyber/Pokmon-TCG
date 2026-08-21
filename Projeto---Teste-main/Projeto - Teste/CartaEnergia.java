public class CartaEnergia extends Carta {
    private String tipoElemento;

    public CartaEnergia(String nome, String tipoElemento) {
        super(nome, "Energia");
        this.tipoElemento = tipoElemento;
    }

    public String getTipoElemento() {
        return tipoElemento;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + tipoElemento + "]";
    }
}
