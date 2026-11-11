package eventos;

/**
 * Observador dos eventos de uma partida (papel <i>Observer</i> do padrão
 * <b>Observer</b>, GoF). O <i>Subject</i> é o {@link modelo.Jogador}: ele avisa
 * todos os ouvintes inscritos com
 * {@link modelo.Jogador#adicionarOuvinte(OuvinteJogo)} sempre que algo acontece
 * (ataque, evolução, nocaute...).
 *
 * <p>Com isso, as regras do jogo não sabem <b>como</b> os eventos são
 * exibidos: a interface gráfica mostra um toast e grava no histórico, o
 * protótipo em terminal imprime no console, e os testes simplesmente guardam
 * os eventos numa lista para conferir — tudo sem mudar uma linha de
 * {@code Jogador}.</p>
 *
 * <p>Como tem um único método abstrato, pode ser implementado com uma lambda:
 * {@code jogador.adicionarOuvinte(evento -> System.out.println(evento.mensagem()));}</p>
 */
@FunctionalInterface
public interface OuvinteJogo {

    /**
     * Chamado pelo {@link modelo.Jogador} logo depois de um acontecimento da partida.
     *
     * @param evento o que aconteceu
     */
    void aoOcorrerEvento(EventoJogo evento);
}
