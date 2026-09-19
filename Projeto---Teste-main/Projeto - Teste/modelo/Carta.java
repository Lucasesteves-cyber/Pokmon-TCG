/**
 * Classe base abstrata de todas as cartas do jogo (Pokémon, Energia e
 * Treinador). Concentra os dados comuns a qualquer carta — nome e tipo —
 * e é especializada por {@link CartaPokemon}, {@link CartaEnergia} e
 * {@link CartaTreinador} (herança e polimorfismo).
 */
public abstract class Carta {
    private String nome;
    private String tipoCarta;

    /**
     * @param nome      nome de exibição da carta
     * @param tipoCarta categoria da carta ("Pokémon", "Energia" ou "Treinador")
     */
    public Carta(String nome, String tipoCarta) {
        this.nome = nome;
        this.tipoCarta = tipoCarta;
    }

    /** @return nome de exibição da carta */
    public String getNome() {
        return nome;
    }

    /** @return categoria da carta ("Pokémon", "Energia" ou "Treinador") */
    public String getTipoCarta() {
        return tipoCarta;
    }

    @Override
    public String toString() {
        return nome + " (" + tipoCarta + ")";
    }
}