import java.util.ArrayList;
import java.util.List;

public class CartaPokemon extends Carta {
    private String tipoElemento; // Fogo, Água, Planta, etc.
    private int hpMaximo;
    private int hpAtual;
    private int danoAtaque;
    private List<CartaEnergia> energiasAnexadas;
    private String evoluiDe; // nome do Pokémon que precisa estar em campo para evoluir para este. null = forma Básica

    // Construtor para Pokémon Básico (não evolui de nada)
    public CartaPokemon(String nome, String tipoElemento, int hpMaximo, int danoAtaque) {
        this(nome, tipoElemento, hpMaximo, danoAtaque, null);
    }

    // Construtor para Pokémon Evoluído (evolui de outro Pokémon)
    public CartaPokemon(String nome, String tipoElemento, int hpMaximo, int danoAtaque, String evoluiDe) {
        super(nome, "Pokemon");
        this.tipoElemento = tipoElemento;
        this.hpMaximo = hpMaximo;
        this.hpAtual = hpMaximo; // Começa com vida cheia
        this.danoAtaque = danoAtaque;
        this.energiasAnexadas = new ArrayList<>();
        this.evoluiDe = evoluiDe;
    }

    public String getTipoElemento() { return tipoElemento; }
    public int getHpMaximo() { return hpMaximo; }
    public int getHpAtual() { return hpAtual; }
    public int getDanoAtaque() { return danoAtaque; }
    public List<CartaEnergia> getEnergiasAnexadas() { return energiasAnexadas; }
    public String getEvoluiDe() { return evoluiDe; }
    public boolean isBasico() { return evoluiDe == null; }

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
        String base = super.toString() + " [" + tipoElemento + " | HP: " + hpAtual + "/" + hpMaximo
                + " | Energias: " + energiasAnexadas.size() + "]";
        if (evoluiDe != null) {
            base += " (Evolui de " + evoluiDe + ")";
        }
        return base;
    }
}
