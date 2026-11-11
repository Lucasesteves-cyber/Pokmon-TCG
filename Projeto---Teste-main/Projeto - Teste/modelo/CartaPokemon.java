package modelo;


import java.util.ArrayList;
import java.util.List;

/**
 * Carta de Pokémon: pode ser colocada em campo (ativo ou no banco), receber
 * energias, evoluir e atacar. Concentra o estado de batalha de uma criatura
 * (HP atual/máximo, dano de ataque, energias anexadas).
 *
 * <p>Instâncias desta classe são criadas preferencialmente por
 * {@link fabrica.CartaPokemonFactory}, que centraliza as regras de criação a partir
 * de dados fixos ou da PokeAPI.</p>
 */
public class CartaPokemon extends Carta {
    private String tipoElemento;
    private int hpMaximo;
    private int hpAtual;
    private int danoAtaque;
    private List<CartaEnergia> energiasAnexadas;
    private String evoluiDe;
    private int limiteEnergias;
    private boolean evoluiuNesteTurno;
    private int numeroDex = -1;

    /**
     * Cria uma carta de Pokémon básica (sem pré-evolução).
     *
     * @param nome         nome de exibição do Pokémon
     * @param tipoElemento tipo elemental (Água, Fogo, Planta etc.)
     * @param hpMaximo     HP máximo (e inicial) da carta
     * @param danoAtaque   dano base do ataque desta carta
     */
    public CartaPokemon(String nome, String tipoElemento, int hpMaximo, int danoAtaque) {
        this(nome, tipoElemento, hpMaximo, danoAtaque, null);
    }

    /**
     * @param nome         nome de exibição do Pokémon
     * @param tipoElemento tipo elemental (Água, Fogo, Planta etc.)
     * @param hpMaximo     HP máximo (e inicial) da carta
     * @param danoAtaque   dano base do ataque desta carta
     * @param evoluiDe     nome do Pokémon do qual esta carta evolui, ou {@code null} se for básico
     * @throws IllegalArgumentException se o HP for menor ou igual a zero, ou o dano for negativo
     */
    public CartaPokemon(String nome, String tipoElemento, int hpMaximo, int danoAtaque, String evoluiDe) {
        super(nome, "Pokemon");
        if (hpMaximo <= 0) {
            throw new IllegalArgumentException("O HP de " + nome + " precisa ser maior que zero (recebido: " + hpMaximo + ").");
        }
        if (danoAtaque < 0) {
            throw new IllegalArgumentException("O dano de " + nome + " não pode ser negativo (recebido: " + danoAtaque + ").");
        }
        this.tipoElemento = tipoElemento;
        this.hpMaximo = hpMaximo;
        this.hpAtual = hpMaximo;
        this.danoAtaque = danoAtaque;
        this.energiasAnexadas = new ArrayList<>();
        this.evoluiDe = evoluiDe;
        this.limiteEnergias = 6 + (int) (Math.random() * 3);
        this.evoluiuNesteTurno = false;
    }

    /** @return tipo elemental deste Pokémon */
    public String getTipoElemento() { return tipoElemento; }

    /** @return HP máximo desta carta */
    public int getHpMaximo() { return hpMaximo; }

    /** @return HP atual desta carta */
    public int getHpAtual() { return hpAtual; }

    /** @return dano base do ataque desta carta */
    public int getDanoAtaque() { return danoAtaque; }

    /** @return lista (mutável) das energias atualmente anexadas a esta carta */
    public List<CartaEnergia> getEnergiasAnexadas() { return energiasAnexadas; }

    /** @return nome do Pokémon do qual esta carta evolui, ou {@code null} se for básico */
    public String getEvoluiDe() { return evoluiDe; }

    /** @return {@code true} se esta carta é uma forma básica (não evolui de nada) */
    public boolean isBasico() { return evoluiDe == null; }

    /** @return número máximo de energias que podem ser anexadas a esta carta */
    public int getLimiteEnergias() { return limiteEnergias; }

    /** @return {@code true} se esta carta já evoluiu no turno atual */
    public boolean isEvoluiuNesteTurno() { return evoluiuNesteTurno; }

    /**
     * Marca (ou desmarca) que esta carta já evoluiu no turno atual.
     *
     * @param valor {@code true} se acabou de evoluir
     */
    public void setEvoluiuNesteTurno(boolean valor) { this.evoluiuNesteTurno = valor; }

    /** @return número da Pokédex desta carta, ou {@code -1} se não vier da PokeAPI (carta de time fixo) */
    public int getNumeroDex() { return numeroDex; }

    /**
     * Define o número da Pokédex desta carta (usado para buscar sua imagem).
     *
     * @param numeroDex número na Pokédex nacional
     */
    public void setNumeroDex(int numeroDex) { this.numeroDex = numeroDex; }

    /**
     * Aplica dano a esta carta, sem deixar o HP atual ficar negativo.
     *
     * @param dano quantidade de HP a remover
     */
    public void receberDano(int dano) {
        this.hpAtual -= dano;
        if (this.hpAtual < 0) this.hpAtual = 0;
    }

    /**
     * Restaura HP desta carta, sem ultrapassar o HP máximo.
     *
     * @param quantidade quantidade de HP a recuperar
     */
    public void curar(int quantidade) {
        this.hpAtual = Math.min(this.hpMaximo, this.hpAtual + quantidade);
    }

    /**
     * Anexa uma carta de energia a este Pokémon, respeitando o limite de
     * energias sorteado para a carta.
     *
     * @param energia carta de energia a anexar
     * @return {@code true} se a energia foi anexada; {@code false} se o limite já foi atingido
     */
    public boolean anexarEnergia(CartaEnergia energia) {
        if (energiasAnexadas.size() >= limiteEnergias) {
            return false;
        }
        energiasAnexadas.add(energia);
        return true;
    }

    /** @return quantidade de energias atualmente anexadas a esta carta */
    public int getQuantidadeEnergias() {
        return energiasAnexadas.size();
    }

    /** @return {@code true} se esta carta foi nocauteada (HP atual chegou a zero) */
    public boolean isNocauteado() {
        return hpAtual <= 0;
    }

    /** @return descrição textual da carta, com HP, energias e pré-evolução */
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