package efeitos;

import eventos.TipoEvento;
import excecoes.JogadaInvalidaException;
import modelo.Jogador;

/** Efeito da carta "Troca": recua o Pokémon ativo gratuitamente, sem gastar energia. */
public class EfeitoTroca implements EfeitoTreinador {

    /** Cria a estratégia de Troca. */
    public EfeitoTroca() { }

    /** {@inheritDoc} */
    @Override
    public String getNome() { return "Troca"; }

    /** {@inheritDoc} */
    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.POKEMON_NO_BANCO; }

    /** {@inheritDoc} */
    @Override
    public String getTextoSelecaoAlvo() { return "Trocar o Ativo por quem do Banco?"; }

    /**
     * Troca o Ativo pelo Pokémon do Banco escolhido, sem custo de energia, e descarta a carta.
     *
     * @throws JogadaInvalidaException se não houver Ativo, se o Banco estiver vazio ou se a posição for inválida
     */
    @Override
    public void aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        // A validação (Ativo, Banco vazio, posição) fica em Jogador.trocarAtivoComBanco,
        // a mesma usada pelo recuo — assim a regra não é escrita duas vezes.
        jogador.trocarAtivoComBanco(parametros.getIndiceBanco());
        jogador.getMao().remove(indiceNaMao);

        jogador.notificar(TipoEvento.TREINADOR, "💨 " + jogador.getNome() + " usou a carta Troca — recuo grátis, sem gastar energia!");
    }
}
