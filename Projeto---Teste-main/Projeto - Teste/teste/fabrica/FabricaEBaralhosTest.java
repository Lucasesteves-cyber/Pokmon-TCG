package fabrica;

import modelo.Carta;
import modelo.CartaPokemon;
import modelo.CartaTreinador;
import modelo.Jogador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Testes da {@link CartaPokemonFactory}, de {@link BaralhosPadrao} e de {@link TimeFixo}. */
class FabricaEBaralhosTest {

    @Test
    @DisplayName("Dados incompletos da PokeAPI recebem HP e dano padrão")
    void valoresPadraoDaPokeApi() {
        CartaPokemon carta = CartaPokemonFactory.criarDaPokeAPI(25, "Pikachu", "Elétrico", 0, -5, null);

        assertEquals(CartaPokemonFactory.HP_PADRAO, carta.getHpMaximo());
        assertEquals(CartaPokemonFactory.DANO_PADRAO, carta.getDanoAtaque());
        assertEquals(25, carta.getNumeroDex());
    }

    @Test
    @DisplayName("Clone é uma carta nova e independente")
    void cloneIndependente() {
        CartaPokemon original = CartaPokemonFactory.criarDaPokeAPI(5, "Charmeleon", "Fogo", 80, 30, "Charmander");
        original.receberDano(50);

        CartaPokemon copia = CartaPokemonFactory.clonar(original);

        assertNotSame(original, copia);
        assertEquals("Charmeleon", copia.getNome());
        assertEquals("Charmander", copia.getEvoluiDe());
        assertEquals(5, copia.getNumeroDex());
        assertEquals(80, copia.getHpAtual(), "a cópia começa com HP cheio");
    }

    @Test
    @DisplayName("Cada time fixo tem 40 Pokémon e toda evolução tem sua pré-evolução no time")
    void timesFixosConsistentes() {
        for (TimeFixo time : TimeFixo.values()) {
            Jogador jogador = new Jogador("Teste");
            time.montar(jogador);

            Set<String> nomes = new HashSet<>();
            for (Carta c : jogador.getBaralho()) nomes.add(c.getNome());

            assertEquals(40, jogador.getTamanhoBaralho(), "time " + time.getNome());
            for (Carta c : jogador.getBaralho()) {
                CartaPokemon p = (CartaPokemon) c;
                if (!p.isBasico()) {
                    assertTrue(nomes.contains(p.getEvoluiDe()),
                            p.getNome() + " evolui de " + p.getEvoluiDe() + ", que não está no time " + time.getNome());
                }
            }
        }
    }

    @Test
    @DisplayName("Cartas de treinador e prêmios padrão")
    void treinadoresEPremios() {
        Jogador jogador = new Jogador("Teste");
        BaralhosPadrao.adicionarCartasTreinador(jogador);
        BaralhosPadrao.adicionarPremios(jogador);

        assertEquals(15, jogador.getTamanhoBaralho());
        assertTrue(jogador.getBaralho().stream().allMatch(c -> c instanceof CartaTreinador));
        assertEquals(BaralhosPadrao.QUANTIDADE_PREMIOS, jogador.getPremios().size());
    }
}
