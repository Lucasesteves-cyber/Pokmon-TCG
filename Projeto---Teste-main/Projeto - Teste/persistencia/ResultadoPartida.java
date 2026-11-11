package persistencia;

import java.time.LocalDateTime;

/**
 * Resultado de uma partida terminada, do jeito que fica salvo no arquivo de
 * histórico.
 *
 * @param dataHora momento em que a partida terminou
 * @param vencedor nome de quem venceu
 * @param perdedor nome de quem perdeu
 * @param modo     "Solo" (contra o Bot) ou "PvP"
 * @param turnos   número do turno em que a partida terminou
 * @param motivo   por que a partida acabou (ex.: "Conquistou os 2 prêmios!")
 */
public record ResultadoPartida(LocalDateTime dataHora, String vencedor, String perdedor,
                               String modo, int turnos, String motivo) {
}
