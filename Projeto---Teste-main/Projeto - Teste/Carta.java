public abstract class Carta {
    private String nome;
    private String tipoCarta; // "Pokemon", "Energia" ou "Treinador"

    public Carta(String nome, String tipoCarta) {
        this.nome = nome;
        this.tipoCarta = tipoCarta;
    }

    public String getNome() {
        return nome;
    }

    public String getTipoCarta() {
        return tipoCarta;
    }

    @Override
    public String toString() {
        return nome + " (" + tipoCarta + ")";
    }
}
