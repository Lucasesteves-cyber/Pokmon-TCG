package eventos;

/**
 * Ouvinte que simplesmente imprime cada evento no console. Usado pelo
 * protótipo em terminal ({@link ui.App}).
 */
public class OuvinteConsole implements OuvinteJogo {

    /** Cria a ouvinte que escreve no console. */
    public OuvinteConsole() { }

    /** Imprime a mensagem do evento numa linha do console. */
    @Override
    public void aoOcorrerEvento(EventoJogo evento) {
        System.out.println(evento.mensagem());
    }
}
