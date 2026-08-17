import java.util.ArrayList;
import java.util.List;

public class CartaPokemon extends Carta {
    private String tipoElemento; // Fogo, Água, Planta, etc.
    private int hpMaximo;
    private int hpAtual;
    private int danoAtaque;
    private List<CartaEnergia> energiasAnexadas;
    private String evoluiDe; // nome do Pokémon que precisa estar em campo para evoluir para este. null = forma Básica
    private int limiteEnergias; // capacidade máxima de energias que esse Pokémon aguenta (sorteado entre 6 e 8)
    private boolean evoluiuNesteTurno; // trava a evolução desse Pokémon específico até o próximo turno dele

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
        this.limiteEnergias = 6 + (int) (Math.random() * 3); // sorteia 6, 7 ou 8
        this.evoluiuNesteTurno = false;
    }

    public String getTipoElemento() { return tipoElemento; }
    public int getHpMaximo() { return hpMaximo; }
    public int getHpAtual() { return hpAtual; }
    public int getDanoAtaque() { return danoAtaque; }
    public List<CartaEnergia> getEnergiasAnexadas() { return energiasAnexadas; }
    public String getEvoluiDe() { return evoluiDe; }
    public boolean isBasico() { return evoluiDe == null; }
    public int getLimiteEnergias() { return limiteEnergias; }
    public boolean isEvoluiuNesteTurno() { return evoluiuNesteTurno; }
    public void setEvoluiuNesteTurno(boolean valor) { this.evoluiuNesteTurno = valor; }

    public void receberDano(int dano) {
        this.hpAtual -= dano;
        if (this.hpAtual < 0) this.hpAtual = 0;
    }

    public void curar(int quantidade) {
        this.hpAtual = Math.min(this.hpMaximo, this.hpAtual + quantidade);
    }

    public boolean anexarEnergia(CartaEnergia energia) {
        if (energiasAnexadas.size() >= limiteEnergias) {
            return false; // atingiu o limite de energias desse Pokémon
        }
        energiasAnexadas.add(energia);
        return true;
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
                + " | Energias: " + energiasAnexadas.size() + "/" + limiteEnergias + "]";
        if (evoluiDe != null) {
            base += " (Evolui de " + evoluiDe + ")";
        }
        if (evoluiuNesteTurno) {
            base += " 🚫(já evoluiu neste turno)";
        }
        return base;
    }
}