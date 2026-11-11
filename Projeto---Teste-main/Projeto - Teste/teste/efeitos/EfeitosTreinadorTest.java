package efeitos;

import excecoes.JogadaInvalidaException;
import modelo.CartaPokemon;
import modelo.CartaTreinador;
import modelo.Jogador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes das estratégias de carta de Treinador (padrão Strategy). Todas são
 * usadas pelo mesmo ponto de entrada, {@link Jogador#usarCartaTreinador}.
 */
class EfeitosTreinadorTest {

    private Jogador jogador;
    private CartaPokemon ativo;

    @BeforeEach
    void preparar() {
        jogador = new Jogador("Misty");
        ativo = new CartaPokemon("Staryu", "Água", 100, 20);
        jogador.getMao().add(ativo);
        jogador.colocarPokemonEmCampo(0, 0);
    }

    private int cartaNaMao(String efeito) {
        jogador.getMao().add(new CartaTreinador(efeito, efeito));
        return jogador.getMao().size() - 1;
    }

    @Test
    @DisplayName("Registro devolve a estratégia certa para cada nome")
    void registroResolveEstrategias() {
        assertInstanceOf(EfeitoPocao.class, EfeitoTreinadorRegistro.obter("poção"));
        assertInstanceOf(EfeitoTroca.class, EfeitoTreinadorRegistro.obter("Troca"));
        assertInstanceOf(EfeitoNenhum.class, EfeitoTreinadorRegistro.obter("Carta que não existe"));
        assertInstanceOf(EfeitoNenhum.class, EfeitoTreinadorRegistro.obter(null));
    }

    @Test
    @DisplayName("Poção cura 30 de HP e é descartada")
    void pocaoCura30() {
        ativo.receberDano(50);
        int indice = cartaNaMao("Poção");

        jogador.usarCartaTreinador(indice, new ParametrosEfeito().setAlvo(ativo));

        assertEquals(80, ativo.getHpAtual());
        assertTrue(jogador.getMao().isEmpty());
    }

    @Test
    @DisplayName("Poção não passa do HP máximo")
    void pocaoRespeitaHpMaximo() {
        ativo.receberDano(10);
        jogador.usarCartaTreinador(cartaNaMao("Poção"), new ParametrosEfeito().setAlvo(ativo));
        assertEquals(100, ativo.getHpAtual());
    }

    @Test
    @DisplayName("Poção sem alvo dá erro e a carta continua na mão")
    void pocaoSemAlvo() {
        int indice = cartaNaMao("Poção");
        assertThrows(JogadaInvalidaException.class,
                () -> jogador.usarCartaTreinador(indice, new ParametrosEfeito()));
        assertEquals(1, jogador.getMao().size());
    }

    @Test
    @DisplayName("Cura Total restaura todo o HP")
    void curaTotal() {
        ativo.receberDano(99);
        jogador.usarCartaTreinador(cartaNaMao("Cura Total"), new ParametrosEfeito().setAlvo(ativo));
        assertEquals(100, ativo.getHpAtual());
    }

    @Test
    @DisplayName("Troca muda o Ativo sem gastar energia")
    void trocaSemCusto() {
        CartaPokemon reserva = new CartaPokemon("Psyduck", "Água", 50, 20);
        jogador.getMao().add(reserva);
        jogador.colocarPokemonEmCampo(0, 1);

        jogador.usarCartaTreinador(cartaNaMao("Troca"), new ParametrosEfeito().setIndiceBanco(0));

        assertSame(reserva, jogador.getPokemonAtivo());
        assertTrue(jogador.getBanco().contains(ativo));
        assertFalse(jogador.isRecuouNesteTurno(), "a Troca não conta como o recuo do turno");
    }

    @Test
    @DisplayName("Troca com o Banco vazio explica o motivo")
    void trocaComBancoVazio() {
        int indice = cartaNaMao("Troca");
        JogadaInvalidaException erro = assertThrows(JogadaInvalidaException.class,
                () -> jogador.usarCartaTreinador(indice, new ParametrosEfeito().setIndiceBanco(0)));
        assertTrue(erro.getMessage().contains("Banco está vazio"));
    }

    @Test
    @DisplayName("Evolução Rápida evolui mesmo no Turno 1 e descarta as duas cartas")
    void evolucaoRapida() {
        jogador.getMao().add(new CartaPokemon("Starmie", "Água", 110, 50, "Staryu"));
        int indiceTreinador = cartaNaMao("Evolução Rápida");

        jogador.usarCartaTreinador(indiceTreinador,
                new ParametrosEfeito().setAlvo(ativo).setIndiceCartaEvolucao(0));

        assertEquals("Starmie", jogador.getPokemonAtivo().getNome());
        assertTrue(jogador.getMao().isEmpty());
    }

    @Test
    @DisplayName("Troféu não tem efeito para usar")
    void trofeuSemEfeito() {
        int indice = cartaNaMao("Troféu");
        assertThrows(JogadaInvalidaException.class,
                () -> jogador.usarCartaTreinador(indice, new ParametrosEfeito()));
        assertEquals(1, jogador.getMao().size());
    }

    @Test
    @DisplayName("Carta que não é de Treinador é recusada")
    void naoEhTreinador() {
        jogador.getMao().add(new CartaPokemon("Goldeen", "Água", 50, 20));
        assertThrows(JogadaInvalidaException.class,
                () -> jogador.usarCartaTreinador(0, new ParametrosEfeito()));
    }
}
