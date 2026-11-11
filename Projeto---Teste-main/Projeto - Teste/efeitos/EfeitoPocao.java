package efeitos;

import eventos.TipoEvento;
import excecoes.JogadaInvalidaException;
import modelo.CartaPokemon;
import modelo.Jogador;

/** Efeito da carta "Poção": cura {@value #CURA} de HP de um Pokémon em campo. */
public class EfeitoPocao implements EfeitoTreinador {

    /** Cria a estratégia de Poção. */
    public EfeitoPocao() { }

    /** Quantidade de HP recuperada pela Poção. */
    public static final int CURA = 30;

    /** {@inheritDoc} */
    @Override
    public String getNome() { return "Poção"; }

    /** {@inheritDoc} */
    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.POKEMON_EM_CAMPO; }

    /** {@inheritDoc} */
    @Override
    public String getTextoSelecaoAlvo() { return "Curar qual Pokémon? (Poção: +" + CURA + " HP)"; }

    /**
     * Cura {@value #CURA} de HP do alvo (sem passar do HP máximo) e descarta a carta.
     *
     * @throws JogadaInvalidaException se nenhum alvo foi escolhido
     */
    @Override
    public void aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        CartaPokemon alvo = parametros.getAlvo();
        if (alvo == null) {
            throw new JogadaInvalidaException("Escolha um Pokémon válido em campo para curar!");
        }

        alvo.curar(CURA);
        jogador.getMao().remove(indiceNaMao);

        jogador.notificar(TipoEvento.TREINADOR, "💊 " + jogador.getNome() + " usou Poção em " + alvo.getNome()
                + "! Curou até " + CURA + " HP. (" + alvo.getHpAtual() + "/" + alvo.getHpMaximo() + ")");
    }
}
