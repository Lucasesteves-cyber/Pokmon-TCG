package efeitos;

import excecoes.JogadaInvalidaException;
import modelo.Jogador;

/**
 * Estratégia nula (padrão <i>Null Object</i>, complementar ao Strategy):
 * usada por cartas de treinador que não têm efeito jogável, como o
 * "Troféu" que representa uma carta-prêmio conquistada ao nocautear um
 * Pokémon adversário. Evita a necessidade de tratar {@code null} em toda
 * a interface gráfica.
 */
public class EfeitoNenhum implements EfeitoTreinador {

    /** Cria a estratégia sem efeito. */
    public EfeitoNenhum() { }

    /** {@inheritDoc} */
    @Override
    public String getNome() { return "Troféu"; }

    /** {@inheritDoc} */
    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.NENHUM; }

    /**
     * Cartas sem efeito não podem ser "usadas".
     *
     * @throws JogadaInvalidaException sempre, explicando que a carta é apenas um prêmio
     */
    @Override
    public void aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        throw new JogadaInvalidaException("O Troféu é só uma carta-prêmio — ele não tem efeito para usar.");
    }
}
