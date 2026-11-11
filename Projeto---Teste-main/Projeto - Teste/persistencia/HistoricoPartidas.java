package persistencia;

import excecoes.PersistenciaException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Guarda o resultado de cada partida num arquivo de texto (formato CSV
 * separado por ponto e vírgula) e lê esse arquivo de volta para montar o
 * placar geral.
 *
 * <p>Exemplo do conteúdo de {@code dados/historico_partidas.csv}:</p>
 * <pre>
 * data_hora;vencedor;perdedor;modo;turnos;motivo
 * 2026-11-11T10:42:13;Renato;Bot;Solo;14;Conquistou os 2 prêmios!
 * </pre>
 *
 * <p><b>Escrita:</b> {@link #registrar(ResultadoPartida)} acrescenta uma linha
 * no fim do arquivo (cria a pasta e o cabeçalho na primeira vez).
 * <b>Leitura:</b> {@link #carregar()} lê todas as linhas; linhas corrompidas
 * (editadas à mão, por exemplo) são ignoradas em vez de derrubar o jogo.</p>
 *
 * <p>Erros de disco são convertidos em {@link PersistenciaException}, com uma
 * mensagem que diz qual arquivo deu problema.</p>
 */
public class HistoricoPartidas {

    /** Caminho padrão do arquivo, relativo à pasta de onde o jogo é executado. */
    public static final Path CAMINHO_PADRAO = Paths.get("dados", "historico_partidas.csv");

    static final String CABECALHO = "data_hora;vencedor;perdedor;modo;turnos;motivo";
    private static final String SEPARADOR = ";";
    private static final int QUANTIDADE_COLUNAS = 6;

    private final Path arquivo;
    private int linhasIgnoradasNaUltimaLeitura;

    /** Usa o arquivo padrão {@link #CAMINHO_PADRAO}. */
    public HistoricoPartidas() {
        this(CAMINHO_PADRAO);
    }

    /**
     * Usa um arquivo específico (útil nos testes, que gravam numa pasta temporária).
     *
     * @param arquivo caminho do arquivo CSV
     */
    public HistoricoPartidas(Path arquivo) {
        this.arquivo = arquivo;
    }

    /** @return caminho do arquivo usado por este histórico */
    public Path getArquivo() {
        return arquivo;
    }

    /**
     * Acrescenta o resultado de uma partida ao fim do arquivo.
     *
     * @param resultado resultado a salvar
     * @throws PersistenciaException se não for possível criar a pasta ou gravar o arquivo
     */
    public void registrar(ResultadoPartida resultado) throws PersistenciaException {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            boolean arquivoNovo = Files.notExists(arquivo) || Files.size(arquivo) == 0;

            try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (arquivoNovo) {
                    escritor.write(CABECALHO);
                    escritor.newLine();
                }
                escritor.write(paraLinha(resultado));
                escritor.newLine();
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível salvar o histórico em \"" + arquivo.toAbsolutePath()
                    + "\": " + e.getMessage(), e);
        }
    }

    /**
     * Lê todas as partidas salvas, da mais antiga para a mais recente.
     *
     * @return lista de resultados (vazia se o arquivo ainda não existir)
     * @throws PersistenciaException se o arquivo existir mas não puder ser lido
     */
    public List<ResultadoPartida> carregar() throws PersistenciaException {
        linhasIgnoradasNaUltimaLeitura = 0;
        if (Files.notExists(arquivo)) {
            return Collections.emptyList();
        }

        List<ResultadoPartida> resultados = new ArrayList<>();
        try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.isBlank() || linha.equals(CABECALHO)) continue;
                try {
                    resultados.add(deLinha(linha));
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    linhasIgnoradasNaUltimaLeitura++; // linha corrompida: pula e segue lendo
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o histórico em \"" + arquivo.toAbsolutePath()
                    + "\": " + e.getMessage(), e);
        }
        return resultados;
    }

    /**
     * Conta quantas vitórias cada jogador tem no histórico.
     *
     * @return mapa nome → vitórias, do maior para o menor número de vitórias
     * @throws PersistenciaException se o arquivo não puder ser lido
     */
    public Map<String, Integer> vitoriasPorJogador() throws PersistenciaException {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (ResultadoPartida r : carregar()) {
            contagem.merge(r.vencedor(), 1, Integer::sum);
        }
        List<Map.Entry<String, Integer>> ordenado = new ArrayList<>(contagem.entrySet());
        ordenado.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : ordenado) resultado.put(e.getKey(), e.getValue());
        return resultado;
    }

    /** @return quantas linhas inválidas foram puladas na última chamada de {@link #carregar()} */
    public int getLinhasIgnoradasNaUltimaLeitura() {
        return linhasIgnoradasNaUltimaLeitura;
    }

    private static String paraLinha(ResultadoPartida r) {
        return String.join(SEPARADOR,
                r.dataHora().withNano(0).toString(),
                limpar(r.vencedor()), limpar(r.perdedor()), limpar(r.modo()),
                String.valueOf(r.turnos()), limpar(r.motivo()));
    }

    private static ResultadoPartida deLinha(String linha) {
        String[] campos = linha.split(SEPARADOR, -1);
        if (campos.length != QUANTIDADE_COLUNAS) {
            throw new IllegalArgumentException("Esperava " + QUANTIDADE_COLUNAS + " colunas, encontrou " + campos.length);
        }
        return new ResultadoPartida(LocalDateTime.parse(campos[0]), campos[1], campos[2], campos[3],
                Integer.parseInt(campos[4]), campos[5]);
    }

    /** Remove o separador e quebras de linha de um texto, para ele não quebrar o CSV. */
    private static String limpar(String texto) {
        if (texto == null) return "";
        return texto.replace(SEPARADOR, ",").replace("\r", " ").replace("\n", " ").trim();
    }
}
