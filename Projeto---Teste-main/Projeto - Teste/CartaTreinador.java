public class CartaTreinador extends Carta {
    private String efeito; // "Poção" ou "Troca"

    public CartaTreinador(String nome, String efeito) {
        super(nome, "Treinador");
        this.efeito = efeito;
    }

    public String getEfeito() {
        return efeito;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + efeito + "]";
    }
}