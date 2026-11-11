package servicos;

import excecoes.PokeApiException;
import fabrica.CartaPokemonFactory;
import modelo.CartaPokemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cliente da <a href="https://pokeapi.co/">PokeAPI</a>: busca nomes, tipos,
 * estatísticas e linhas evolutivas dos Pokémon.
 *
 * <p>Antes ficava espalhado dentro de {@code AppGUI} (que tinha 2 mil linhas),
 * misturando acesso à rede com código de tela e engolindo erros com
 * {@code catch (Exception e) { return null; }}. Agora toda falha vira uma
 * {@link PokeApiException} com mensagem clara, e quem chama decide o que
 * fazer com ela.</p>
 *
 * <p>Como o projeto não usa bibliotecas externas, o JSON é lido com
 * expressões regulares simples.</p>
 */
public class PokeApiCliente {

    private static final String URL_BASE = "https://pokeapi.co/api/v2/";
    private static final int TEMPO_LIMITE_MS = 8000;

    /** Último número da Pokédex suportado (fim da 6ª geração). */
    public static final int ULTIMO_NUMERO_DEX = 721;

    private static final Pattern PADRAO_NOME_LISTA = Pattern.compile("\"name\":\"([a-z0-9\\-]+)\"");
    private static final Pattern PADRAO_ESPECIE = Pattern.compile("\"species\":\\{\"name\":\"([^\"]+)\"");
    private static final Pattern PADRAO_PRIMEIRO_NOME = Pattern.compile("\"name\":\"([^\"]*)\"");
    private static final Pattern PADRAO_TIPO = Pattern.compile("\"type\":\\{\"name\":\"([a-z]+)\"");
    private static final Pattern PADRAO_PRE_EVOLUCAO = Pattern.compile(
            "\"evolves_from_species\":\\{\"name\":\"([a-z0-9\\-]+)\",\"url\":\"https://pokeapi\\.co/api/v2/pokemon-species/(\\d+)/\"");

    private static final Map<String, String> TIPOS_EM_PORTUGUES = Map.ofEntries(
            Map.entry("water", "Água"), Map.entry("fire", "Fogo"), Map.entry("grass", "Planta"),
            Map.entry("electric", "Elétrico"), Map.entry("psychic", "Psíquico"), Map.entry("ice", "Gelo"),
            Map.entry("dragon", "Dragão"), Map.entry("dark", "Sombrio"), Map.entry("fairy", "Fada"),
            Map.entry("fighting", "Lutador"), Map.entry("flying", "Voador"), Map.entry("poison", "Veneno"),
            Map.entry("ground", "Terra"), Map.entry("rock", "Pedra"), Map.entry("bug", "Inseto"),
            Map.entry("ghost", "Fantasma"), Map.entry("steel", "Aço"));

    /** Cria um cliente da PokeAPI. */
    public PokeApiCliente() { }

    /**
     * Busca os nomes de uma faixa de Pokémon (usado para listar uma geração).
     *
     * @param offset quantos Pokémon pular a partir do nº 1 (ex.: 151 para começar na 2ª geração)
     * @param limite quantos nomes trazer
     * @return nomes já formatados (ex.: "Mr mime"), na ordem da Pokédex
     * @throws PokeApiException se a requisição falhar
     */
    public List<String> buscarNomes(int offset, int limite) throws PokeApiException {
        String json = baixar(URL_BASE + "pokemon?offset=" + offset + "&limit=" + limite);
        List<String> nomes = new ArrayList<>();
        Matcher m = PADRAO_NOME_LISTA.matcher(json);
        while (m.find()) nomes.add(capitalizar(m.group(1)));
        return nomes;
    }

    /**
     * Busca os dados de um Pokémon e cria a carta correspondente via {@link CartaPokemonFactory}.
     *
     * @param numeroDex número na Pokédex (1 a {@value #ULTIMO_NUMERO_DEX})
     * @return a carta criada
     * @throws PokeApiException se o número for inválido ou a busca falhar
     */
    public CartaPokemon buscarPokemon(int numeroDex) throws PokeApiException {
        validarNumero(numeroDex);
        String jsonPokemon = baixar(URL_BASE + "pokemon/" + numeroDex);
        String jsonEspecie = baixar(URL_BASE + "pokemon-species/" + numeroDex);

        String nome = capitalizar(extrairNome(jsonPokemon));
        String tipo = extrairTipo(jsonPokemon);
        int hp = extrairStat(jsonPokemon, "hp");
        int dano = extrairStat(jsonPokemon, "attack");

        Matcher evo = PADRAO_PRE_EVOLUCAO.matcher(jsonEspecie);
        String evoluiDe = evo.find() ? capitalizar(evo.group(1)) : null;

        return CartaPokemonFactory.criarDaPokeAPI(numeroDex, nome, tipo, hp, dano, evoluiDe);
    }

    /**
     * Busca apenas o tipo (já em português) de um Pokémon.
     *
     * @param numeroDex número na Pokédex
     * @return tipo traduzido (ex.: "Fogo")
     * @throws PokeApiException se a busca falhar
     */
    public String buscarTipo(int numeroDex) throws PokeApiException {
        validarNumero(numeroDex);
        return extrairTipo(baixar(URL_BASE + "pokemon/" + numeroDex));
    }

    /**
     * Busca a linha evolutiva completa até o Pokémon escolhido (ex.: escolher
     * Charizard traz Charmander, Charmeleon e Charizard, nessa ordem).
     *
     * @param numeroDex número na Pokédex do Pokémon escolhido
     * @return cartas da forma básica até a escolhida
     * @throws PokeApiException se alguma das buscas falhar
     */
    public List<CartaPokemon> buscarLinhaEvolutiva(int numeroDex) throws PokeApiException {
        List<CartaPokemon> cadeia = new ArrayList<>();
        Set<Integer> jaVistos = new HashSet<>(); // protege contra laços na resposta da API

        int atual = numeroDex;
        while (atual != -1 && jaVistos.add(atual)) {
            cadeia.add(0, buscarPokemon(atual));
            atual = buscarNumeroPreEvolucao(atual);
        }
        return cadeia;
    }

    private int buscarNumeroPreEvolucao(int numeroDex) throws PokeApiException {
        Matcher m = PADRAO_PRE_EVOLUCAO.matcher(baixar(URL_BASE + "pokemon-species/" + numeroDex));
        return m.find() ? Integer.parseInt(m.group(2)) : -1;
    }

    /**
     * Converte o nome de tipo em inglês usado pela API para o nome em português do jogo.
     *
     * @param tipoIngles ex.: "fire"
     * @return ex.: "Fogo" (tipos desconhecidos viram "Normal")
     */
    public static String traduzirTipo(String tipoIngles) {
        return TIPOS_EM_PORTUGUES.getOrDefault(tipoIngles, "Normal");
    }

    /**
     * Formata um nome vindo da API: troca hífens por espaço e deixa a primeira letra maiúscula.
     *
     * @param texto ex.: "mr-mime"
     * @return ex.: "Mr mime"
     */
    public static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        String limpo = texto.replace("-", " ");
        return limpo.substring(0, 1).toUpperCase() + limpo.substring(1);
    }

    /**
     * Extrai de um JSON da PokeAPI o valor de uma estatística base.
     *
     * @param json     resposta do endpoint {@code /pokemon/{id}}
     * @param nomeStat ex.: "hp" ou "attack"
     * @return o valor, ou 0 se não encontrado
     */
    static int extrairStat(String json, String nomeStat) {
        Matcher m = Pattern.compile("\"base_stat\":(\\d+),\"effort\":\\d+,\"stat\":\\{\"name\":\"" + Pattern.quote(nomeStat) + "\"")
                .matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    /**
     * @param json resposta do endpoint {@code /pokemon/{id}}
     * @return o primeiro tipo do Pokémon, traduzido
     */
    static String extrairTipo(String json) {
        Matcher m = PADRAO_TIPO.matcher(json);
        return traduzirTipo(m.find() ? m.group(1) : "normal");
    }

    /**
     * Lê o nome do Pokémon. Prefere o campo {@code "species":{"name":...}}, que é
     * inequívoco; se não existir, usa o primeiro {@code "name"} do JSON (o
     * comportamento original do projeto).
     */
    static String extrairNome(String json) throws PokeApiException {
        Matcher especie = PADRAO_ESPECIE.matcher(json);
        if (especie.find()) return especie.group(1);

        Matcher qualquer = PADRAO_PRIMEIRO_NOME.matcher(json);
        if (qualquer.find()) return qualquer.group(1);

        throw new PokeApiException("A PokeAPI respondeu num formato inesperado (nome do Pokémon ausente).");
    }

    private static void validarNumero(int numeroDex) throws PokeApiException {
        if (numeroDex < 1 || numeroDex > ULTIMO_NUMERO_DEX) {
            throw new PokeApiException("Número da Pokédex inválido: " + numeroDex
                    + ". O jogo aceita do 1 ao " + ULTIMO_NUMERO_DEX + ".");
        }
    }

    /**
     * Faz um GET e devolve o corpo da resposta. Transforma qualquer falha de rede
     * ({@link IOException}) ou código HTTP de erro numa {@link PokeApiException}.
     */
    private String baixar(String endereco) throws PokeApiException {
        HttpURLConnection conexao = null;
        try {
            conexao = (HttpURLConnection) URI.create(endereco).toURL().openConnection();
            conexao.setRequestMethod("GET");
            conexao.setConnectTimeout(TEMPO_LIMITE_MS);
            conexao.setReadTimeout(TEMPO_LIMITE_MS);

            int status = conexao.getResponseCode();
            if (status == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new PokeApiException("A PokeAPI não encontrou esse Pokémon (" + endereco + ").");
            }
            if (status != HttpURLConnection.HTTP_OK) {
                throw new PokeApiException("A PokeAPI respondeu com erro " + status + ". Tente de novo em instantes.");
            }

            StringBuilder corpo = new StringBuilder();
            try (BufferedReader leitor = new BufferedReader(
                    new InputStreamReader(conexao.getInputStream(), StandardCharsets.UTF_8))) {
                String linha;
                while ((linha = leitor.readLine()) != null) corpo.append(linha);
            }
            return corpo.toString();
        } catch (IOException e) {
            throw new PokeApiException("Não foi possível falar com a PokeAPI. Verifique sua conexão com a internet.", e);
        } finally {
            if (conexao != null) conexao.disconnect();
        }
    }
}
