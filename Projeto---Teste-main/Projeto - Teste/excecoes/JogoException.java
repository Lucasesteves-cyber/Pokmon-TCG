package excecoes;

/**
 * Exceção base para qualquer violação das regras do jogo durante uma partida.
 *
 * <p>É <b>não verificada</b> ({@link RuntimeException}) de propósito: uma jogada
 * inválida é um erro do jogador, não uma falha do sistema, e quem decide o que
 * fazer com ela é sempre a interface (mostrar um aviso na tela ou no console).
 * Assim, as regras em {@link modelo.Jogador} ficam livres de {@code throws} em
 * toda assinatura, e a interface captura as exceções num único ponto.</p>
 *
 * <p>Subclasses deixam claro o <i>tipo</i> do problema:</p>
 * <ul>
 *   <li>{@link JogadaInvalidaException} – a ação desrespeita uma regra (ex.: atacar no Turno 1);</li>
 *   <li>{@link BaralhoVazioException} – o jogador precisava comprar, mas o baralho acabou (derrota).</li>
 * </ul>
 */
public class JogoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com uma mensagem pensada para ser mostrada ao jogador.
     *
     * @param mensagem explicação do problema, em linguagem do jogo
     */
    public JogoException(String mensagem) {
        super(mensagem);
    }
}
