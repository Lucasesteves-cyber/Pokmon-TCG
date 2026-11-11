package eventos;

/**
 * Um acontecimento da partida, enviado pelo {@link modelo.Jogador} a todos os
 * seus {@link OuvinteJogo}s (padrão <b>Observer</b>).
 *
 * <p>É um {@code record} (Java 16+): imutável, com construtor, getters,
 * {@code equals}, {@code hashCode} e {@code toString} gerados automaticamente.</p>
 *
 * @param tipo     categoria do evento
 * @param mensagem texto pronto para ser exibido ao jogador
 */
public record EventoJogo(TipoEvento tipo, String mensagem) {
}
