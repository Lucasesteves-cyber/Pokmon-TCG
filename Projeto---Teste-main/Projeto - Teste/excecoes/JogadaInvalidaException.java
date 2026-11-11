package excecoes;

/**
 * Lançada quando o jogador tenta uma ação que as regras não permitem naquele
 * momento: atacar no primeiro turno, anexar duas energias na mesma rodada,
 * evoluir para a forma errada, escolher uma posição inexistente etc.
 *
 * <p>A mensagem sempre explica <b>o que</b> deu errado e, quando possível,
 * <b>o que fazer</b> (ex.: "Charmeleon não evolui de Squirtle! Ele evolui de
 * Charmander."), porque é exatamente ela que aparece na tela.</p>
 */
public class JogadaInvalidaException extends JogoException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem explicação da regra violada, exibida ao jogador
     */
    public JogadaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
