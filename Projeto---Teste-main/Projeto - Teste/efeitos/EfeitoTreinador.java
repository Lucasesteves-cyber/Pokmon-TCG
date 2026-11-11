package efeitos;

import excecoes.JogadaInvalidaException;
import modelo.Jogador;

/**
 * Estratégia (padrão de projeto <b>Strategy</b>) que encapsula o
 * comportamento de um efeito de carta de treinador.
 *
 * <p>Antes desta refatoração, o {@code Jogador} tinha um método dedicado
 * para cada efeito ({@code usarPocao}, {@code usarCuraTotal}, {@code usarTroca},
 * {@code usarEvolucaoRapida}), cada um repetindo a mesma validação de
 * índice/tipo de carta, e a interface gráfica decidia qual usar por meio de
 * uma cadeia de {@code if/else} comparando o nome do efeito
 * ({@code carta.getEfeito().equalsIgnoreCase("Poção")}, etc.).</p>
 *
 * <p>Com o Strategy, cada efeito passa a ser uma classe própria que
 * implementa esta interface, e tanto o {@link Jogador} quanto a interface
 * gráfica tratam qualquer carta de treinador de forma uniforme, chamando
 * {@code carta.getEstrategia().aplicar(...)} — sem precisar saber, em tempo de
 * compilação, qual efeito específico está sendo executado. Isso torna fácil
 * adicionar um novo efeito de treinador no futuro sem alterar nenhum
 * código existente (apenas criando uma nova classe e registrando-a em
 * {@link EfeitoTreinadorRegistro}).</p>
 */
public interface EfeitoTreinador {

    /** @return nome de exibição do efeito (usado em telas e no baralho) */
    String getNome();

    /** @return tipo de alvo que a interface precisa coletar do jogador antes de chamar {@link #aplicar} */
    TipoAlvo getTipoAlvoNecessario();

    /** @return texto exibido ao jogador ao pedir o alvo (ignorado quando {@link #getTipoAlvoNecessario()} é {@code NENHUM}) */
    default String getTextoSelecaoAlvo() {
        return "Escolha o alvo:";
    }

    /**
     * Aplica o efeito desta carta de treinador.
     *
     * @param jogador      jogador que está usando a carta
     * @param indiceNaMao  posição da carta de treinador na mão do jogador
     * @param parametros   alvo(s) já coletados pela interface gráfica
     * @throws JogadaInvalidaException se o efeito não puder ser aplicado (nesse caso a carta continua na mão)
     */
    void aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros);
}