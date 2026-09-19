/**
 * Efeito da carta "Evolução Rápida": evolui um Pokémon em campo imediatamente,
 * sem esperar o Turno 2 e sem limite de uma evolução por turno.
 */
public class EfeitoEvolucaoRapida implements EfeitoTreinador {

    @Override
    public String getNome() { return "Evolução Rápida"; }

    @Override
    public TipoAlvo getTipoAlvoNecessario() { return TipoAlvo.CARTA_EVOLUCAO_E_ALVO; }

    @Override
    public boolean aplicar(Jogador jogador, int indiceNaMao, ParametrosEfeito parametros) {
        CartaPokemon alvoEmCampo = parametros.getAlvo();
        int indiceCartaEvolucao = parametros.getIndiceCartaEvolucao();

        // Guarda a referência da própria carta de treinador: o índice dela na mão
        // pode mudar assim que a carta de evolução for removida logo abaixo.
        Carta cartaTreinador = jogador.getMao().get(indiceNaMao);

        boolean sucesso = jogador.executarEvolucao(indiceCartaEvolucao, alvoEmCampo);
        if (!sucesso) {
            return false;
        }

        jogador.getMao().remove(cartaTreinador);

        System.out.println("⚡ " + jogador.getNome() + " usou Evolução Rápida — sem esperar turno, sem limite de vezes!");
        return true;
    }
}