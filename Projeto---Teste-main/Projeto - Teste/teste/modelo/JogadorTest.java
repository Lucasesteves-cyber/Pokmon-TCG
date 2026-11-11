package modelo;

import eventos.EventoJogo;
import eventos.TipoEvento;
import excecoes.BaralhoVazioException;
import excecoes.JogadaInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes das regras de jogo concentradas em {@link Jogador}: colocar Pokémon em
 * campo, energia, ataque, nocaute, evolução, recuo, compra de cartas e o envio
 * de eventos para os ouvintes (Observer).
 */
class JogadorTest {

    private Jogador ash;
    private Jogador gary;

    @BeforeEach
    void prepararJogadores() {
        ash = new Jogador("Ash");
        gary = new Jogador("Gary");
    }

    /** Coloca uma carta na mão do jogador e devolve a posição dela. */
    private static int naMao(Jogador jogador, Carta carta) {
        jogador.getMao().add(carta);
        return jogador.getMao().size() - 1;
    }

    private static CartaPokemon squirtle() { return new CartaPokemon("Squirtle", "Água", 50, 30); }
    private static CartaPokemon charmander() { return new CartaPokemon("Charmander", "Fogo", 100, 20); }

    /** Deixa um Pokémon como Ativo, já com 1 energia anexada. */
    private static CartaPokemon ativoComEnergia(Jogador jogador, CartaPokemon pokemon) {
        jogador.colocarPokemonEmCampo(naMao(jogador, pokemon), 0);
        jogador.anexarEnergia(pokemon);
        jogador.encerrarRodada(); // libera a energia da próxima rodada
        return pokemon;
    }

    // ------------------------------------------------------------ criação

    @Test
    @DisplayName("Nome vazio não é aceito")
    void nomeVazioLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> new Jogador("   "));
        assertThrows(IllegalArgumentException.class, () -> new Jogador(null));
    }

    // ------------------------------------------------------------ campo

    @Test
    @DisplayName("Pokémon Básico pode ser colocado como Ativo e sai da mão")
    void colocaBasicoComoAtivo() {
        CartaPokemon s = squirtle();
        ash.colocarPokemonEmCampo(naMao(ash, s), 0);

        assertSame(s, ash.getPokemonAtivo());
        assertTrue(ash.getMao().isEmpty());
    }

    @Test
    @DisplayName("Evolução não pode entrar direto em campo")
    void evolucaoNaoEntraDiretoEmCampo() {
        int indice = naMao(ash, new CartaPokemon("Wartortle", "Água", 80, 35, "Squirtle"));

        JogadaInvalidaException erro = assertThrows(JogadaInvalidaException.class,
                () -> ash.colocarPokemonEmCampo(indice, 1));
        assertTrue(erro.getMessage().contains("Squirtle"), "a mensagem deve dizer de quem ele evolui");
        assertEquals(1, ash.getMao().size(), "a carta deve continuar na mão");
    }

    @Test
    @DisplayName("Não dá para ter dois Pokémon Ativos")
    void naoColocaSegundoAtivo() {
        ash.colocarPokemonEmCampo(naMao(ash, squirtle()), 0);
        int indice = naMao(ash, charmander());

        assertThrows(JogadaInvalidaException.class, () -> ash.colocarPokemonEmCampo(indice, 0));
    }

    @Test
    @DisplayName("O Banco aceita no máximo 5 Pokémon")
    void bancoLimitadoACinco() {
        for (int i = 0; i < Jogador.LIMITE_BANCO; i++) {
            ash.colocarPokemonEmCampo(naMao(ash, squirtle()), 1);
        }
        int sexto = naMao(ash, squirtle());

        assertThrows(JogadaInvalidaException.class, () -> ash.colocarPokemonEmCampo(sexto, 1));
        assertEquals(Jogador.LIMITE_BANCO, ash.getBanco().size());
    }

    @Test
    @DisplayName("Posição inexistente na mão gera mensagem clara")
    void posicaoInvalidaNaMao() {
        JogadaInvalidaException erro = assertThrows(JogadaInvalidaException.class,
                () -> ash.colocarPokemonEmCampo(3, 0));
        assertTrue(erro.getMessage().contains("Posição inválida"));
    }

    // ------------------------------------------------------------ energia

    @Test
    @DisplayName("Só 1 energia por rodada, liberada de novo ao encerrar a rodada")
    void umaEnergiaPorRodada() {
        CartaPokemon s = squirtle();
        ash.colocarPokemonEmCampo(naMao(ash, s), 0);

        ash.anexarEnergia(s);
        assertThrows(JogadaInvalidaException.class, () -> ash.anexarEnergia(s));

        ash.encerrarRodada();
        assertDoesNotThrow(() -> ash.anexarEnergia(s));
        assertEquals(2, s.getQuantidadeEnergias());
    }

    @Test
    @DisplayName("Não passa do limite de energias da carta")
    void respeitaLimiteDeEnergias() {
        CartaPokemon s = squirtle();
        ash.colocarPokemonEmCampo(naMao(ash, s), 0);
        for (int i = 0; i < s.getLimiteEnergias(); i++) {
            ash.anexarEnergia(s);
            ash.encerrarRodada();
        }

        assertThrows(JogadaInvalidaException.class, () -> ash.anexarEnergia(s));
        assertEquals(s.getLimiteEnergias(), s.getQuantidadeEnergias());
    }

    // ------------------------------------------------------------ ataque

    @Test
    @DisplayName("Não é permitido atacar no Turno 1")
    void naoAtacaNoPrimeiroTurno() {
        ativoComEnergia(ash, squirtle());
        ativoComEnergia(gary, charmander());

        assertThrows(JogadaInvalidaException.class, () -> ash.atacar(gary, 1));
    }

    @Test
    @DisplayName("Sem energia não há ataque")
    void naoAtacaSemEnergia() {
        ash.colocarPokemonEmCampo(naMao(ash, squirtle()), 0);
        ativoComEnergia(gary, charmander());

        JogadaInvalidaException erro = assertThrows(JogadaInvalidaException.class, () -> ash.atacar(gary, 2));
        assertTrue(erro.getMessage().contains("energia"));
    }

    @Test
    @DisplayName("Água contra Fogo causa dano dobrado")
    void ataqueSuperEfetivoDobraODano() {
        ativoComEnergia(ash, squirtle());                   // 30 de dano
        CartaPokemon alvo = ativoComEnergia(gary, charmander()); // 100 de HP

        ash.atacar(gary, 2);

        assertEquals(40, alvo.getHpAtual()); // 100 - 30*2
        assertTrue(ash.isAtacouNestaRodada());
    }

    @Test
    @DisplayName("Só 1 ataque por rodada")
    void umAtaquePorRodada() {
        ativoComEnergia(ash, squirtle());
        ativoComEnergia(gary, new CartaPokemon("Snorlax", "Normal", 300, 10));

        ash.atacar(gary, 2);
        assertThrows(JogadaInvalidaException.class, () -> ash.atacar(gary, 2));
    }

    @Test
    @DisplayName("Nocaute: vai para a Zona Morta, atacante ganha prêmio e o Banco sobe")
    void nocauteCompleto() {
        ash.getPremios().add(new CartaTreinador("Troféu", "Troféu"));
        ativoComEnergia(ash, new CartaPokemon("Mewtwo", "Psíquico", 100, 200));
        CartaPokemon vitima = ativoComEnergia(gary, charmander());
        CartaPokemon reserva = squirtle();
        gary.colocarPokemonEmCampo(naMao(gary, reserva), 1);

        ash.atacar(gary, 2);

        assertTrue(vitima.isNocauteado());
        assertTrue(gary.getZonaMorta().contains(vitima));
        assertSame(reserva, gary.getPokemonAtivo(), "o primeiro do Banco vira o novo Ativo");
        assertTrue(ash.getPremios().isEmpty(), "o prêmio sai da pilha...");
        assertEquals(1, ash.getMao().size(), "...e vai para a mão de quem atacou");
    }

    // ------------------------------------------------------------ evolução

    @Test
    @DisplayName("Evoluir mantém energias e dano já sofrido")
    void evolucaoTransfereEnergiasEDano() {
        CartaPokemon base = ativoComEnergia(ash, squirtle());
        base.receberDano(20);
        CartaPokemon evolucao = new CartaPokemon("Wartortle", "Água", 80, 35, "Squirtle");

        ash.evoluir(naMao(ash, evolucao), base, 2);

        assertSame(evolucao, ash.getPokemonAtivo());
        assertEquals(60, evolucao.getHpAtual());
        assertEquals(1, evolucao.getQuantidadeEnergias());
        assertTrue(evolucao.isEvoluiuNesteTurno());
    }

    @Test
    @DisplayName("Evoluir para a forma errada mostra de quem ela evolui")
    void evolucaoErradaLancaExcecao() {
        CartaPokemon base = ativoComEnergia(ash, squirtle());
        int indice = naMao(ash, new CartaPokemon("Charmeleon", "Fogo", 75, 35, "Charmander"));

        JogadaInvalidaException erro = assertThrows(JogadaInvalidaException.class,
                () -> ash.evoluir(indice, base, 2));
        assertEquals("Charmeleon não evolui de Squirtle! Ele evolui de Charmander.", erro.getMessage());
    }

    @Test
    @DisplayName("Não evolui no Turno 1 nem duas vezes no mesmo turno")
    void restricoesDeTurnoNaEvolucao() {
        CartaPokemon base = ativoComEnergia(ash, squirtle());
        int indice = naMao(ash, new CartaPokemon("Wartortle", "Água", 80, 35, "Squirtle"));
        assertThrows(JogadaInvalidaException.class, () -> ash.evoluir(indice, base, 1));

        ash.evoluir(indice, base, 2);
        CartaPokemon wartortle = ash.getPokemonAtivo();
        int indiceBlastoise = naMao(ash, new CartaPokemon("Blastoise", "Água", 120, 70, "Wartortle"));
        assertThrows(JogadaInvalidaException.class, () -> ash.evoluir(indiceBlastoise, wartortle, 2));
    }

    @Test
    @DisplayName("Encontra na mão só as evoluções do Pokémon pedido")
    void encontraEvolucoesNaMao() {
        CartaPokemon base = squirtle();
        naMao(ash, charmander());
        naMao(ash, new CartaPokemon("Wartortle", "Água", 80, 35, "Squirtle"));
        naMao(ash, new CartaPokemon("Charmeleon", "Fogo", 75, 35, "Charmander"));

        assertEquals(List.of(1), ash.indicesDeEvolucoesNaMao(base));
        assertEquals(0, ash.indiceDoPrimeiroBasicoNaMao());
    }

    // ------------------------------------------------------------ recuo

    @Test
    @DisplayName("Recuar troca com o Banco e custa 1 energia")
    void recuoCustaEnergia() {
        CartaPokemon ativo = ativoComEnergia(ash, squirtle());
        CartaPokemon reserva = charmander();
        ash.colocarPokemonEmCampo(naMao(ash, reserva), 1);

        ash.recuar(0);

        assertSame(reserva, ash.getPokemonAtivo());
        assertTrue(ash.getBanco().contains(ativo));
        assertEquals(0, ativo.getQuantidadeEnergias());
        assertThrows(JogadaInvalidaException.class, () -> ash.recuar(0), "só 1 recuo por turno");
    }

    @Test
    @DisplayName("Recuo com posição inválida não gasta energia")
    void recuoInvalidoNaoGastaEnergia() {
        CartaPokemon ativo = ativoComEnergia(ash, squirtle());
        ash.colocarPokemonEmCampo(naMao(ash, charmander()), 1);

        assertThrows(JogadaInvalidaException.class, () -> ash.recuar(4));
        assertEquals(1, ativo.getQuantidadeEnergias());
        assertFalse(ash.isRecuouNesteTurno());
    }

    // ------------------------------------------------------------ baralho

    @Test
    @DisplayName("Comprar com o baralho vazio lança BaralhoVazioException")
    void baralhoVazio() {
        ash.adicionarAoBaralho(squirtle());
        ash.comprarCarta();

        BaralhoVazioException erro = assertThrows(BaralhoVazioException.class, ash::comprarCarta);
        assertEquals("Ash", erro.getNomeJogador());
        assertEquals(1, ash.getMao().size());
    }

    // ------------------------------------------------------------ Observer

    @Test
    @DisplayName("Ouvintes recebem os eventos na ordem em que acontecem")
    void ouvintesRecebemEventos() {
        List<EventoJogo> recebidos = new ArrayList<>();
        ash.adicionarOuvinte(recebidos::add);

        ash.adicionarAoBaralho(squirtle());
        ash.comprarCarta();
        ash.colocarPokemonEmCampo(0, 0);
        ash.anexarEnergia(ash.getPokemonAtivo());

        assertEquals(List.of(TipoEvento.COMPRA, TipoEvento.POKEMON_EM_CAMPO, TipoEvento.ENERGIA),
                recebidos.stream().map(EventoJogo::tipo).toList());
    }

    @Test
    @DisplayName("Jogada inválida não gera evento, e ouvinte removido não recebe mais nada")
    void semEventoEmJogadaInvalidaENemAposRemover() {
        List<EventoJogo> recebidos = new ArrayList<>();
        eventos.OuvinteJogo ouvinte = recebidos::add;
        ash.adicionarOuvinte(ouvinte);

        assertThrows(JogadaInvalidaException.class, () -> ash.anexarEnergia(null));
        assertTrue(recebidos.isEmpty());

        ash.removerOuvinte(ouvinte);
        ash.encerrarRodada();
        assertTrue(recebidos.isEmpty());
    }
}
