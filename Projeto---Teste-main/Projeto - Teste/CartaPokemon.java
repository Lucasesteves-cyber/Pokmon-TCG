import java.util.ArrayList;
import java.util.List;

public class CartaPokemon extends Carta {
    private String tipoElemento; // Fogo, Água, Planta, etc.
    private int hpMaximo;
    private int hpAtual;
    private int danoAtaque;
    private List<CartaEnergia> energiasAnexadas;

    public CartaPokemon(String nome, String tipoElemento, int hpMaximo, int danoAtaque) {
        super(nome, "Pokemon");
        this.tipoElemento = tipoElemento;
        this.hpMaximo = hpMaximo;
        this.hpAtual = hpMaximo; // Começa com vida cheia
        this.danoAtaque = danoAtaque;
        this.energiasAnexadas = new ArrayList<>();
    }

    public String getTipoElemento() { return tipoElemento; }
    public int getHpMaximo() { return hpMaximo; }
    public int getHpAtual() { return hpAtual; }
    public int getDanoAtaque() { return danoAtaque; }
    public List<CartaEnergia> getEnergiasAnexadas() { return energiasAnexadas; }

    public void receberDano(int dano) {
        this.hpAtual -= dano;
        if (this.hpAtual < 0) this.hpAtual = 0;
    }

    public void anexarEnergia(CartaEnergia energia) {
        energiasAnexadas.add(energia);
    }

    public int getQuantidadeEnergias() {
        return energiasAnexadas.size();
    }

    public boolean isNocauteado() {
        return hpAtual <= 0;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + tipoElemento + " | HP: " + hpAtual + "/" + hpMaximo
                + " | Energias: " + energiasAnexadas.size() + "]";
    }
}
