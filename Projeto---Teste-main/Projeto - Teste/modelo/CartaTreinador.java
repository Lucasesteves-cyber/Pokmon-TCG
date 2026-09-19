public class CartaTreinador extends Carta {
    private final String efeito;

    /**
     * Estratégia (Strategy) responsável pelo comportamento real deste efeito.
     * Resolvida automaticamente a partir do nome do efeito, via
     * {@link EfeitoTreinadorRegistro}, para não quebrar os pontos do código
     * que ainda criam cartas de treinador só pelo nome (ex.: {@code new
     * CartaTreinador("Poção", "Poção")}).
     */
    private final EfeitoTreinador estrategia;

    public CartaTreinador(String nome, String efeito) {
        super(nome, "Treinador");
        this.efeito = efeito;
        this.estrategia = EfeitoTreinadorRegistro.obter(efeito);
    }

    public String getEfeito() {
        return efeito;
    }

    /** Estratégia (Strategy) que executa o comportamento real deste efeito. */
    public EfeitoTreinador getEstrategia() {
        return estrategia;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + efeito + "]";
    }
}