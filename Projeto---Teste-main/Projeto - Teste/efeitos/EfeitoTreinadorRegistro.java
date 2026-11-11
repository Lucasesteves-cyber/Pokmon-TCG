package efeitos;


import java.util.HashMap;
import java.util.Map;

/**
 * Associa o nome de um efeito de treinador (o texto usado historicamente no
 * baralho e nas telas) à instância de {@link EfeitoTreinador} correspondente.
 *
 * <p>Isso mantém a compatibilidade com o restante do código, que ainda cria
 * cartas de treinador a partir do nome do efeito (ex.: {@code new
 * CartaTreinador("Poção")}), enquanto por trás dos panos o comportamento
 * real passa a ser resolvido por Strategy em vez de comparações de String
 * espalhadas pelo código.</p>
 */
public final class EfeitoTreinadorRegistro {

    private static final Map<String, EfeitoTreinador> EFEITOS = new HashMap<>();
    private static final EfeitoTreinador SEM_EFEITO = new EfeitoNenhum();

    static {
        registrar(new EfeitoPocao());
        registrar(new EfeitoCuraTotal());
        registrar(new EfeitoTroca());
        registrar(new EfeitoEvolucaoRapida());
        registrar(SEM_EFEITO);
    }

    private EfeitoTreinadorRegistro() { }

    private static void registrar(EfeitoTreinador efeito) {
        EFEITOS.put(efeito.getNome().toLowerCase(), efeito);
    }

    /**
     * Obtém a estratégia associada ao nome do efeito. Nomes desconhecidos
     * recebem a estratégia {@link EfeitoNenhum}, para que uma carta de
     * treinador nunca fique sem uma estratégia válida.
     *
     * @param nomeEfeito nome do efeito (sem diferenciar maiúsculas/minúsculas)
     * @return a estratégia registrada, ou um {@link EfeitoNenhum} se o nome for desconhecido
     */
    public static EfeitoTreinador obter(String nomeEfeito) {
        EfeitoTreinador efeito = EFEITOS.get(nomeEfeito == null ? "" : nomeEfeito.toLowerCase());
        return efeito != null ? efeito : SEM_EFEITO;
    }
}