package excecoes;

/**
 * Falha ao ler ou gravar os arquivos do jogo (por exemplo, o histórico de
 * partidas em {@link persistencia.HistoricoPartidas}).
 *
 * <p>É <b>verificada</b>: problemas de disco (pasta sem permissão, arquivo
 * travado por outro programa) são previsíveis, e a interface precisa decidir o
 * que fazer — normalmente avisar o jogador e continuar a partida, porque perder
 * o histórico não deve impedir ninguém de jogar. A {@code IOException}
 * original fica disponível em {@link #getCause()}.</p>
 */
public class PersistenciaException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem descrição amigável do problema
     * @param causa    exceção de I/O original
     */
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
