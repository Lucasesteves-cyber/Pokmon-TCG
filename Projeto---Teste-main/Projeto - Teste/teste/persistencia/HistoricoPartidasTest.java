package persistencia;

import excecoes.PersistenciaException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de leitura e escrita do arquivo de histórico. Cada teste usa uma
 * pasta temporária própria, apagada no final, para não mexer no histórico real.
 */
class HistoricoPartidasTest {

    private Path pastaTemporaria;
    private Path arquivo;
    private HistoricoPartidas historico;

    @BeforeEach
    void criarPasta() throws IOException {
        pastaTemporaria = Files.createTempDirectory("historico-teste");
        arquivo = pastaTemporaria.resolve("subpasta").resolve("historico.csv");
        historico = new HistoricoPartidas(arquivo);
    }

    @AfterEach
    void apagarPasta() throws IOException {
        try (Stream<Path> caminhos = Files.walk(pastaTemporaria)) {
            caminhos.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    private static ResultadoPartida partida(String vencedor, String perdedor) {
        return new ResultadoPartida(LocalDateTime.of(2026, 11, 11, 10, 30, 15), vencedor, perdedor, "PvP", 12,
                "Conquistou os 2 prêmios!");
    }

    @Test
    @DisplayName("Sem arquivo ainda, o histórico é vazio (e não dá erro)")
    void arquivoInexistente() throws PersistenciaException {
        assertTrue(historico.carregar().isEmpty());
    }

    @Test
    @DisplayName("O que é gravado é lido de volta igual, com acentos")
    void gravaELe() throws PersistenciaException {
        ResultadoPartida original = partida("Renato", "João");
        historico.registrar(original);

        assertEquals(List.of(original), historico.carregar());
        assertTrue(Files.exists(arquivo), "a pasta e o arquivo devem ser criados");
    }

    @Test
    @DisplayName("O cabeçalho é escrito só uma vez")
    void cabecalhoUnico() throws PersistenciaException, IOException {
        historico.registrar(partida("A", "B"));
        historico.registrar(partida("B", "A"));

        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        assertEquals(3, linhas.size());
        assertEquals(HistoricoPartidas.CABECALHO, linhas.get(0));
    }

    @Test
    @DisplayName("Linhas corrompidas são puladas e contadas")
    void linhaCorrompida() throws PersistenciaException, IOException {
        historico.registrar(partida("A", "B"));
        Files.writeString(arquivo, "isso não é uma partida\n2026-99-99T00:00;x;y;PvP;1;z\n",
                StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        historico.registrar(partida("C", "D"));

        assertEquals(2, historico.carregar().size());
        assertEquals(2, historico.getLinhasIgnoradasNaUltimaLeitura());
    }

    @Test
    @DisplayName("Ponto e vírgula no nome não quebra o arquivo")
    void separadorNoNome() throws PersistenciaException {
        historico.registrar(partida("Ash;Ketchum", "Gary"));
        assertEquals("Ash,Ketchum", historico.carregar().get(0).vencedor());
    }

    @Test
    @DisplayName("Placar conta vitórias e ordena do maior para o menor")
    void placar() throws PersistenciaException {
        historico.registrar(partida("Ana", "Bot"));
        historico.registrar(partida("Bot", "Ana"));
        historico.registrar(partida("Bot", "Renato"));

        Map<String, Integer> vitorias = historico.vitoriasPorJogador();

        assertEquals(List.of("Bot", "Ana"), List.copyOf(vitorias.keySet()));
        assertEquals(2, vitorias.get("Bot"));
        assertNull(vitorias.get("Renato"));
    }

    @Test
    @DisplayName("Erro de disco vira PersistenciaException com o caminho na mensagem")
    void erroDeDisco() throws IOException {
        Path ehUmArquivo = pastaTemporaria.resolve("arquivo.txt");
        Files.writeString(ehUmArquivo, "x");
        HistoricoPartidas impossivel = new HistoricoPartidas(ehUmArquivo.resolve("historico.csv"));

        PersistenciaException erro = assertThrows(PersistenciaException.class,
                () -> impossivel.registrar(partida("A", "B")));
        assertTrue(erro.getMessage().contains("historico.csv"));
        assertNotNull(erro.getCause());
    }
}
