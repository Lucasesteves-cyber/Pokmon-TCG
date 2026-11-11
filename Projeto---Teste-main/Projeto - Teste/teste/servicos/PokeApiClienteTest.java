package servicos;

import excecoes.PokeApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes da leitura das respostas da PokeAPI. Não acessam a internet: usam
 * trechos de JSON no mesmo formato que a API devolve.
 */
class PokeApiClienteTest {

    private static final String JSON_CHARMANDER = "{\"abilities\":[{\"ability\":{\"name\":\"blaze\",\"url\":\"x\"}}],"
            + "\"name\":\"charmander\",\"species\":{\"name\":\"charmander\",\"url\":\"x\"},"
            + "\"stats\":[{\"base_stat\":39,\"effort\":0,\"stat\":{\"name\":\"hp\",\"url\":\"x\"}},"
            + "{\"base_stat\":52,\"effort\":0,\"stat\":{\"name\":\"attack\",\"url\":\"x\"}}],"
            + "\"types\":[{\"slot\":1,\"type\":{\"name\":\"fire\",\"url\":\"x\"}}]}";

    @Test
    @DisplayName("Lê HP e ataque das estatísticas")
    void leEstatisticas() {
        assertEquals(39, PokeApiCliente.extrairStat(JSON_CHARMANDER, "hp"));
        assertEquals(52, PokeApiCliente.extrairStat(JSON_CHARMANDER, "attack"));
        assertEquals(0, PokeApiCliente.extrairStat(JSON_CHARMANDER, "speed"));
    }

    @Test
    @DisplayName("Lê o nome da espécie, e não o da primeira habilidade")
    void leNome() throws PokeApiException {
        assertEquals("charmander", PokeApiCliente.extrairNome(JSON_CHARMANDER));
        assertThrows(PokeApiException.class, () -> PokeApiCliente.extrairNome("{}"));
    }

    @Test
    @DisplayName("Traduz o tipo para português")
    void traduzTipo() {
        assertEquals("Fogo", PokeApiCliente.extrairTipo(JSON_CHARMANDER));
        assertEquals("Água", PokeApiCliente.traduzirTipo("water"));
        assertEquals("Normal", PokeApiCliente.traduzirTipo("tipo-inventado"));
    }

    @Test
    @DisplayName("Formata nomes com hífen")
    void capitaliza() {
        assertEquals("Mr mime", PokeApiCliente.capitalizar("mr-mime"));
    }

    @Test
    @DisplayName("Número fora da Pokédex é recusado antes de acessar a rede")
    void numeroInvalido() {
        PokeApiCliente cliente = new PokeApiCliente();
        PokeApiException erro = assertThrows(PokeApiException.class, () -> cliente.buscarPokemon(0));
        assertTrue(erro.getMessage().contains("721"));
        assertThrows(PokeApiException.class, () -> cliente.buscarTipo(9999));
    }
}
