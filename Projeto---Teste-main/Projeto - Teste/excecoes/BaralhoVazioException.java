package excecoes;

/**
 * Lançada quando um jogador precisa comprar uma carta, mas o baralho dele
 * acabou. Pelas regras do TCG isso significa <b>derrota imediata</b>, por isso
 * a interface trata esta exceção encerrando a partida (e não apenas mostrando
 * um aviso, como faz com {@link JogadaInvalidaException}).
 */
public class BaralhoVazioException extends JogoException {

    private static final long serialVersionUID = 1L;

    private final String nomeJogador;

    /**
     * @param nomeJogador nome do jogador que ficou sem cartas
     */
    public BaralhoVazioException(String nomeJogador) {
        super("O baralho de " + nomeJogador + " acabou! Sem cartas para comprar, " + nomeJogador + " perde a partida.");
        this.nomeJogador = nomeJogador;
    }

    /** @return nome do jogador que ficou sem cartas para comprar */
    public String getNomeJogador() {
        return nomeJogador;
    }
}
