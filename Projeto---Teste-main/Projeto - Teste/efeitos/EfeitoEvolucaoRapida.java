package efeitos;

import eventos.TipoEvento;
import excecoes.JogadaInvalidaException;
import modelo.Carta;
import modelo.Jogador;

/**
 * Efeito da carta "Evolução Rápida": evolui um Pokémon em campo imediatamente,
 * sem esperar o Turno 2 e sem limite de uma evolução por turno.
 */
public class EfeitoEvolucaoRapida implements EfeitoTreinador {

    /** Cria a estratégia de Evolução Rápida. */
    public EfeitoEvolucaoRapida() { }

    /** {@inheritDoc} */
    @Override
    public String getNome() { return "Evolução Rápida"; }

    /** {@inheritDoc} */
    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.CARTA_EVOLUCAO_E_ALVO; }

    /**
     * Evolui o alvo com a carta de evolução escolhida, ignorando as restrições de turno,
     * e descarta a carta de treinador.
     *
     * @throws JogadaInvalidaException se a carta escolhida não for uma evolução válida do alvo
     */
    @Override
    public void aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        // Guarda a referência da própria carta de treinador: o índice dela na mão
        // pode mudar assim que a carta de evolução for removida logo abaixo.
        Carta cartaTreinador = jogador.getMao().get(indiceNaMao);

        jogador.executarEvolucao(parametros.getIndiceCartaEvolucao(), parametros.getAlvo());
        jogador.getMao().remove(cartaTreinador);

        jogador.notificar(TipoEvento.TREINADOR, "⚡ " + jogador.getNome() + " usou Evolução Rápida — sem esperar turno, sem limite de vezes!");
    }
}
