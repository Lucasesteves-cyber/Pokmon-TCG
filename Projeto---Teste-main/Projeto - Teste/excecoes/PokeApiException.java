package excecoes;

/**
 * Falha ao buscar dados na PokeAPI (sem internet, tempo esgotado, Pokémon
 * inexistente ou resposta em formato inesperado).
 *
 * <p>Diferente das exceções de regra, esta é <b>verificada</b> ({@link Exception}):
 * falhas de rede são esperadas e o compilador obriga quem chama
 * {@link servicos.PokeApiCliente} a decidir o que fazer (mostrar "verifique sua
 * internet", pular o Pokémon no sorteio do Bot etc.). A causa original
 * ({@code IOException}, {@code NumberFormatException}...) é preservada em
 * {@link #getCause()} para facilitar a depuração.</p>
 */
public class PokeApiException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem descrição amigável do problema
     */
    public PokeApiException(String mensagem) {
        super(mensagem);
    }

    /**
     * @param mensagem descrição amigável do problema
     * @param causa    exceção original que provocou a falha
     */
    public PokeApiException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
