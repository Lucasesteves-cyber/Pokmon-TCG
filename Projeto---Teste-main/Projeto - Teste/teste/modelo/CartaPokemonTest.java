package modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de dano, cura, nocaute e validação da {@link CartaPokemon}. */
class CartaPokemonTest {

    @Test
    @DisplayName("Dano não deixa o HP negativo e zera = nocaute")
    void danoENocaute() {
        CartaPokemon p = new CartaPokemon("Pikachu", "Elétrico", 60, 20);
        p.receberDano(100);
        assertEquals(0, p.getHpAtual());
        assertTrue(p.isNocauteado());
    }

    @Test
    @DisplayName("Cura não ultrapassa o HP máximo")
    void curaLimitada() {
        CartaPokemon p = new CartaPokemon("Pikachu", "Elétrico", 60, 20);
        p.receberDano(10);
        p.curar(50);
        assertEquals(60, p.getHpAtual());
    }

    @Test
    @DisplayName("Limite de energias fica entre 6 e 8")
    void limiteDeEnergias() {
        for (int i = 0; i < 50; i++) {
            int limite = new CartaPokemon("Pikachu", "Elétrico", 60, 20).getLimiteEnergias();
            assertTrue(limite >= 6 && limite <= 8, "limite fora da faixa: " + limite);
        }
    }

    @Test
    @DisplayName("HP zero ou dano negativo são recusados")
    void valoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new CartaPokemon("X", "Normal", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new CartaPokemon("X", "Normal", 10, -1));
    }
}
