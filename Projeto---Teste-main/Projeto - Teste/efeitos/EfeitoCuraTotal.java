package efeitos;

import eventos.TipoEvento;
import excecoes.JogadaInvalidaException;
import modelo.CartaPokemon;
import modelo.Jogador;

/** Efeito da carta "Cura Total": restaura o HP de um Pokémon em campo por completo. */
public class EfeitoCuraTotal implements EfeitoTreinador {

    /** Cria a estratégia de Cura Total. */
    public EfeitoCuraTotal() { }

    /** {@inheritDoc} */
    @Override
    public String getNome() { return "Cura Total"; }

    /** {@inheritDoc} */
    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.POKEMON_EM_CAMPO; }

    /** {@inheritDoc} */
    @Override
    public String getTextoSelecaoAlvo() { return "Curar qual Pokémon? (Cura Total: HP completo)"; }

    /**
     * Restaura todo o HP do alvo e descarta a carta.
     *
     * @throws JogadaInvalidaException se nenhum alvo foi escolhido
     */
    @Override
    public void aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        CartaPokemon alvo = parametros.getAlvo();
        if (alvo == null) {
            throw new JogadaInvalidaException("Escolha um Pokémon válido em campo para curar!");
        }

        alvo.curar(alvo.getHpMaximo());
        jogador.getMao().remove(indiceNaMao);

        jogador.notificar(TipoEvento.TREINADOR, "💖 " + jogador.getNome() + " usou Cura Total em " + alvo.getNome()
                + "! HP restaurado por completo. (" + alvo.getHpAtual() + "/" + alvo.getHpMaximo() + ")");
    }
}
