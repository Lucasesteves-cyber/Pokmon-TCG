import java.util.*;

public class CartaPokemon extends Carta {
    private static final Set<String> NOMES_LENDARIOS = new HashSet<>(Arrays.asList(
            "Articuno", "Zapdos", "Moltres", "Mewtwo", "Mew",
            "Raikou", "Entei", "Suicune", "Lugia", "Ho oh", "Celebi",
            "Regirock", "Regice", "Registeel", "Latias", "Latios", "Kyogre", "Groudon", "Rayquaza", "Jirachi", "Deoxys",
            "Uxie", "Mesprit", "Azelf", "Dialga", "Palkia", "Heatran", "Regigigas", "Giratina", "Cresselia",
            "Phione", "Manaphy", "Darkrai", "Shaymin", "Arceus",
            "Victini", "Cobalion", "Terrakion", "Virizion", "Tornadus", "Thundurus", "Reshiram", "Zekrom",
            "Landorus", "Kyurem", "Keldeo", "Meloetta", "Genesect",
            "Xerneas", "Yveltal", "Zygarde", "Diancie", "Hoopa", "Volcanion"
    ));

    private String tipoElemento;
    private int hpMaximo;
    private int hpAtual;
    private int danoAtaque;
    private List<CartaEnergia> energiasAnexadas;
    private String evoluiDe;
    private int limiteEnergias;
    private boolean evoluiuNesteTurno;
    private int numeroDex = -1;
    private boolean megaEvoluido;

    public CartaPokemon(String nome, String tipoElemento, int hpMaximo, int danoAtaque) {
        this(nome, tipoElemento, hpMaximo, danoAtaque, null);
    }

    public CartaPokemon(String nome, String tipoElemento, int hpMaximo, int danoAtaque, String evoluiDe) {
        super(nome, "Pokemon");
        this.tipoElemento = tipoElemento;
        this.hpMaximo = hpMaximo;
        this.hpAtual = hpMaximo;
        this.danoAtaque = danoAtaque;
        this.energiasAnexadas = new ArrayList<>();
        this.evoluiDe = evoluiDe;
        this.limiteEnergias = 6 + (int) (Math.random() * 3);
        this.evoluiuNesteTurno = false;
        this.megaEvoluido = false;
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
    public int getNumeroDex() { return numeroDex; }
    public void setNumeroDex(int numeroDex) { this.numeroDex = numeroDex; }
    public boolean isLendario() { return NOMES_LENDARIOS.contains(getNome()); }
    public boolean isMegaEvoluido() { return megaEvoluido; }

    public void aplicarMegaEvolucao() {
        if (megaEvoluido) return;
        int hpAdicional = 60;
        int danoAdicional = 45;
        this.hpMaximo += hpAdicional;
        this.hpAtual += hpAdicional;
        this.danoAtaque += danoAdicional;
        this.megaEvoluido = true;
    }

    public void receberDano(int dano) {
        this.hpAtual -= dano;
        if (this.hpAtual < 0) this.hpAtual = 0;
    }

    public void curar(int quantidade) {
        this.hpAtual = Math.min(this.hpMaximo, this.hpAtual + quantidade);
    }

    public boolean anexarEnergia(CartaEnergia energia) {
        if (energiasAnexadas.size() >= limiteEnergias) {
            return false;
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
        if (megaEvoluido) {
            base += " ✨MEGA";
        }
        return base;
    }
}
