package modelo;

import efeitos.EfeitoTreinador;
import efeitos.EfeitoTreinadorRegistro;

/**
 * Carta de Treinador: uma carta de efeito (Poção, Troca, Evolução Rápida,
 * Cura Total) ou uma carta-prêmio (Troféu). O comportamento de cada efeito
 * fica numa {@link EfeitoTreinador} (padrão <b>Strategy</b>), escolhida
 * pelo nome do efeito no momento em que a carta é criada.
 */
public class CartaTreinador extends Carta {
    private final String efeito;

    /**
     * Estratégia (Strategy) responsável pelo comportamento real deste efeito.
     * Resolvida automaticamente a partir do nome do efeito, via
     * {@link EfeitoTreinadorRegistro}, para não quebrar os pontos do código
     * que ainda criam cartas de treinador só pelo nome (ex.: {@code new
     * CartaTreinador("Poção", "Poção")}).
     */
    private final EfeitoTreinador estrategia;

    /**
     * @param nome   nome de exibição da carta
     * @param efeito nome do efeito (ex.: "Poção"); nomes desconhecidos viram uma carta sem efeito
     */
    public CartaTreinador(String nome, String efeito) {
        super(nome, "Treinador");
        this.efeito = efeito;
        this.estrategia = EfeitoTreinadorRegistro.obter(efeito);
    }

    /** @return nome do efeito desta carta */
    public String getEfeito() {
        return efeito;
    }

    /** @return estratégia (Strategy) que executa o comportamento real deste efeito */
    public EfeitoTreinador getEstrategia() {
        return estrategia;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + efeito + "]";
    }
}