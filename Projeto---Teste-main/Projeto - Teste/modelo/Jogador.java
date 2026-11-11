package modelo;

import efeitos.EfeitoTreinador;
import efeitos.ParametrosEfeito;
import eventos.EventoJogo;
import eventos.OuvinteJogo;
import eventos.TipoEvento;
import excecoes.BaralhoVazioException;
import excecoes.JogadaInvalidaException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa um jogador (humano ou controlado pelo Bot) e todo o seu estado
 * de jogo: baralho, mão, Pokémon ativo, banco, zona morta e prêmios.
 * Concentra as regras de negócio da partida (comprar carta, jogar Pokémon,
 * anexar energia, evoluir, atacar, usar cartas de treinador), enquanto a
 * interface gráfica ({@link ui.AppGUI}) cuida apenas de coletar as escolhas do
 * jogador e exibir o resultado.
 *
 * <p><b>Tratamento de erros:</b> quando uma ação desrespeita as regras, o
 * método lança {@link JogadaInvalidaException} com uma mensagem explicando o
 * motivo; quando o baralho acaba na hora de comprar, lança
 * {@link BaralhoVazioException}. Nenhum método imprime nada nem devolve
 * {@code false} para indicar erro.</p>
 *
 * <p><b>Observer:</b> este é o <i>Subject</i> do padrão Observer. Cada ação
 * bem-sucedida gera um {@link EventoJogo}, entregue a todos os
 * {@link OuvinteJogo} inscritos com {@link #adicionarOuvinte(OuvinteJogo)}.</p>
 */
public class Jogador {

    /** Quantidade máxima de Pokémon no Banco de Reservas. */
    public static final int LIMITE_BANCO = 5;

    /** Quantas energias o Pokémon Ativo precisa descartar para recuar. */
    public static final int CUSTO_RECUO = 1;

    /** Turno a partir do qual ataques e evoluções normais são permitidos. */
    public static final int TURNO_MINIMO_ATAQUE_E_EVOLUCAO = 2;

    private final String nome;
    private final List<Carta> baralho = new ArrayList<>();
    private final List<Carta> mao = new ArrayList<>();
    private CartaPokemon pokemonAtivo;
    private final List<CartaPokemon> banco = new ArrayList<>();
    private final List<CartaPokemon> zonaMorta = new ArrayList<>();
    private final List<Carta> premios = new ArrayList<>();
    private final List<OuvinteJogo> ouvintes = new ArrayList<>();

    private boolean energiaAnexadaNestaRodada;
    private boolean atacouNestaRodada;
    private boolean recuouNesteTurno;

    /**
     * Cria um novo jogador com baralho, mão, banco e prêmios vazios.
     *
     * @param nome nome do treinador (não pode ser vazio)
     * @throws IllegalArgumentException se o nome for {@code null} ou vazio
     */
    public Jogador(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do treinador não pode ficar vazio.");
        }
        this.nome = nome.trim();
    }

    // ------------------------------------------------------------------
    // Observer
    // ------------------------------------------------------------------

    /**
     * Inscreve um ouvinte para receber os eventos deste jogador.
     *
     * @param ouvinte quem será avisado a cada acontecimento da partida
     */
    public void adicionarOuvinte(OuvinteJogo ouvinte) {
        if (ouvinte != null && !ouvintes.contains(ouvinte)) {
            ouvintes.add(ouvinte);
        }
    }

    /**
     * Cancela a inscrição de um ouvinte.
     *
     * @param ouvinte ouvinte inscrito anteriormente
     */
    public void removerOuvinte(OuvinteJogo ouvinte) {
        ouvintes.remove(ouvinte);
    }

    /**
     * Avisa todos os ouvintes inscritos de que algo aconteceu.
     *
     * <p>É público porque as estratégias de {@link EfeitoTreinador}, que ficam
     * em outro pacote, também precisam anunciar o efeito que aplicaram.</p>
     *
     * @param tipo     categoria do evento
     * @param mensagem texto a ser exibido
     */
    public void notificar(TipoEvento tipo, String mensagem) {
        EventoJogo evento = new EventoJogo(tipo, mensagem);
        // Copia a lista para que um ouvinte possa se remover durante a notificação.
        for (OuvinteJogo ouvinte : new ArrayList<>(ouvintes)) {
            ouvinte.aoOcorrerEvento(evento);
        }
    }

    // ------------------------------------------------------------------
    // Baralho e mão
    // ------------------------------------------------------------------

    /**
     * Adiciona uma carta ao final do baralho (usado ao montar o time, antes de embaralhar).
     *
     * @param carta carta a adicionar
     */
    public void adicionarAoBaralho(Carta carta) {
        this.baralho.add(carta);
    }

    /** Embaralha o baralho deste jogador em ordem aleatória. */
    public void embaralhar() {
        Collections.shuffle(this.baralho);
    }

    /**
     * Compra a carta do topo do baralho para a mão.
     *
     * @throws BaralhoVazioException se não houver carta para comprar (derrota pelas regras)
     */
    public void comprarCarta() {
        if (baralho.isEmpty()) {
            throw new BaralhoVazioException(nome);
        }
        Carta cartaComprada = baralho.remove(0);
        mao.add(cartaComprada);
        notificar(TipoEvento.COMPRA, nome + " comprou: " + cartaComprada.getNome());
    }

    /** @return quantidade de cartas restantes no baralho */
    public int getTamanhoBaralho() {
        return baralho.size();
    }

    // ------------------------------------------------------------------
    // Ações do turno
    // ------------------------------------------------------------------

    /**
     * Coloca uma carta de Pokémon básica (da mão) em campo.
     *
     * @param indiceNaMao posição da carta na mão
     * @param destino     0 para colocar como Pokémon Ativo, 1 para colocar no Banco
     * @throws JogadaInvalidaException se a carta não for um Pokémon Básico, se o
     *                                 destino estiver ocupado/cheio ou se o destino for inválido
     */
    public void colocarPokemonEmCampo(int indiceNaMao, int destino) {
        Carta carta = cartaDaMao(indiceNaMao);

        if (!(carta instanceof CartaPokemon)) {
            throw new JogadaInvalidaException(carta.getNome() + " não é um Pokémon!");
        }
        CartaPokemon pokemon = (CartaPokemon) carta;

        if (!pokemon.isBasico()) {
            throw new JogadaInvalidaException(pokemon.getNome() + " é uma evolução (evolui de " + pokemon.getEvoluiDe()
                    + ")! Ele não pode entrar direto em campo — evolua um " + pokemon.getEvoluiDe() + " que já esteja em campo.");
        }

        if (destino == 0) {
            if (pokemonAtivo != null) {
                throw new JogadaInvalidaException("Você já tem um Pokémon Ativo (" + pokemonAtivo.getNome()
                        + "). Escolha o Banco ou troque de Ativo primeiro.");
            }
            mao.remove(indiceNaMao);
            pokemonAtivo = pokemon;
            notificar(TipoEvento.POKEMON_EM_CAMPO, "🔥 " + nome + " colocou " + pokemon.getNome() + " como seu Pokémon Ativo!");
        } else if (destino == 1) {
            if (banco.size() >= LIMITE_BANCO) {
                throw new JogadaInvalidaException("O Banco de Reservas está cheio (máximo de " + LIMITE_BANCO + " Pokémon)!");
            }
            mao.remove(indiceNaMao);
            banco.add(pokemon);
            notificar(TipoEvento.POKEMON_EM_CAMPO, "💤 " + nome + " colocou " + pokemon.getNome() + " no Banco de Reservas.");
        } else {
            throw new JogadaInvalidaException("Destino inválido! Escolha 0 (Ativo) ou 1 (Banco).");
        }
    }

    /**
     * Anexa uma nova carta de energia (do tipo do próprio alvo) a um Pokémon em campo,
     * respeitando o limite de 1 energia anexada por rodada.
     *
     * @param destino Pokémon em campo que vai receber a energia
     * @throws JogadaInvalidaException se já houve energia nesta rodada, se não houver
     *                                 alvo ou se o alvo estiver no limite de energias
     */
    public void anexarEnergia(CartaPokemon destino) {
        if (energiaAnexadaNestaRodada) {
            throw new JogadaInvalidaException("Você já anexou uma Energia nesta rodada! Só é permitida 1 por rodada.");
        }
        if (destino == null) {
            throw new JogadaInvalidaException("Não há Pokémon para receber a energia!");
        }

        CartaEnergia energia = new CartaEnergia("Energia de " + destino.getTipoElemento(), destino.getTipoElemento());
        if (!destino.anexarEnergia(energia)) {
            throw new JogadaInvalidaException(destino.getNome() + " já está no limite máximo de energias ("
                    + destino.getLimiteEnergias() + ")! Não é possível anexar mais.");
        }

        energiaAnexadaNestaRodada = true;
        notificar(TipoEvento.ENERGIA, "⚡ " + nome + " anexou uma Energia de " + destino.getTipoElemento() + " em "
                + destino.getNome() + "! (" + destino.getQuantidadeEnergias() + "/" + destino.getLimiteEnergias() + ")");
    }

    /**
     * Localiza um Pokémon em campo pelo índice usado nos menus (0 = Ativo, 1 a 5 = posição no Banco).
     *
     * @param indice 0 para o Ativo, 1 a 5 para a posição no Banco
     * @return o Pokémon correspondente, ou {@code null} se o índice for inválido ou não houver Pokémon ali
     */
    public CartaPokemon getPokemonDoCampoPorIndice(int indice) {
        if (indice == 0) {
            return pokemonAtivo;
        } else if (indice >= 1 && indice <= banco.size()) {
            return banco.get(indice - 1);
        }
        return null;
    }

    /**
     * Evolui um Pokémon em campo, respeitando a regra de que evolução só é
     * permitida a partir do Turno 2 e uma vez por turno por Pokémon.
     *
     * @param indiceNaMao posição, na mão, da carta de evolução a usar
     * @param alvoEmCampo Pokémon em campo que vai evoluir
     * @param turnoAtual  número do turno atual da partida
     * @throws JogadaInvalidaException se a evolução não for permitida
     */
    public void evoluir(int indiceNaMao, CartaPokemon alvoEmCampo, int turnoAtual) {
        if (turnoAtual < TURNO_MINIMO_ATAQUE_E_EVOLUCAO) {
            throw new JogadaInvalidaException("Evolução só é permitida a partir do Turno "
                    + TURNO_MINIMO_ATAQUE_E_EVOLUCAO + "! Espere o próximo turno.");
        }
        if (alvoEmCampo != null && alvoEmCampo.isEvoluiuNesteTurno()) {
            throw new JogadaInvalidaException(alvoEmCampo.getNome() + " já evoluiu neste turno! Espere o próximo turno.");
        }
        executarEvolucao(indiceNaMao, alvoEmCampo);
    }

    /**
     * Executa a troca da carta em campo pela sua evolução, transferindo energias e
     * dano já sofrido, <b>sem</b> checar as restrições de turno. É público porque a
     * estratégia {@link efeitos.EfeitoEvolucaoRapida} (outro pacote) existe
     * justamente para pular essas restrições.
     *
     * @param indiceNaMao posição, na mão, da carta de evolução
     * @param alvoEmCampo Pokémon em campo que vai evoluir
     * @throws JogadaInvalidaException se a carta não for uma evolução válida para o alvo
     */
    public void executarEvolucao(int indiceNaMao, CartaPokemon alvoEmCampo) {
        Carta carta = cartaDaMao(indiceNaMao);

        if (!(carta instanceof CartaPokemon)) {
            throw new JogadaInvalidaException(carta.getNome() + " não é um Pokémon!");
        }
        CartaPokemon cartaEvolucao = (CartaPokemon) carta;

        if (cartaEvolucao.isBasico()) {
            throw new JogadaInvalidaException(cartaEvolucao.getNome() + " é uma forma Básica, não uma evolução!");
        }
        if (alvoEmCampo == null) {
            throw new JogadaInvalidaException("Escolha um Pokémon em campo válido para evoluir!");
        }
        if (!cartaEvolucao.getEvoluiDe().equalsIgnoreCase(alvoEmCampo.getNome())) {
            throw new JogadaInvalidaException(cartaEvolucao.getNome() + " não evolui de " + alvoEmCampo.getNome()
                    + "! Ele evolui de " + cartaEvolucao.getEvoluiDe() + ".");
        }

        int indiceNoBanco = banco.indexOf(alvoEmCampo);
        if (alvoEmCampo != pokemonAtivo && indiceNoBanco == -1) {
            throw new JogadaInvalidaException(alvoEmCampo.getNome() + " não está em campo!");
        }

        int danoJaSofrido = alvoEmCampo.getHpMaximo() - alvoEmCampo.getHpAtual();
        for (CartaEnergia energia : alvoEmCampo.getEnergiasAnexadas()) {
            cartaEvolucao.anexarEnergia(energia);
        }
        if (danoJaSofrido > 0) {
            cartaEvolucao.receberDano(danoJaSofrido);
        }

        if (alvoEmCampo == pokemonAtivo) {
            pokemonAtivo = cartaEvolucao;
        } else {
            banco.set(indiceNoBanco, cartaEvolucao);
        }

        mao.remove(indiceNaMao);
        cartaEvolucao.setEvoluiuNesteTurno(true);

        notificar(TipoEvento.EVOLUCAO, "✨ " + alvoEmCampo.getNome() + " evoluiu para " + cartaEvolucao.getNome() + "!");
    }

    /**
     * Ponto de entrada único para usar qualquer carta de treinador.
     *
     * <p>A validação comum (índice e tipo de carta) fica aqui, e o
     * comportamento específico de cada efeito é delegado à sua
     * {@link EfeitoTreinador} (padrão de projeto <b>Strategy</b>), obtida a
     * partir da própria carta.</p>
     *
     * @param indiceNaMao posição da carta de treinador na mão
     * @param parametros  alvo(s) escolhidos pelo jogador
     * @throws JogadaInvalidaException se a carta não for de treinador ou se o efeito não puder ser aplicado
     */
    public void usarCartaTreinador(int indiceNaMao, ParametrosEfeito parametros) {
        Carta carta = cartaDaMao(indiceNaMao);
        if (!(carta instanceof CartaTreinador)) {
            throw new JogadaInvalidaException(carta.getNome() + " não é uma carta de Treinador!");
        }
        EfeitoTreinador efeito = ((CartaTreinador) carta).getEstrategia();
        efeito.aplicar(this, indiceNaMao, parametros);
    }

    /** Libera todos os Pokémon em campo para poderem evoluir novamente no próximo turno. */
    public void resetarEvolucoesDoTurno() {
        if (pokemonAtivo != null) pokemonAtivo.setEvoluiuNesteTurno(false);
        for (CartaPokemon p : banco) p.setEvoluiuNesteTurno(false);
    }

    /**
     * Executa um ataque do Pokémon Ativo deste jogador contra o Pokémon
     * Ativo do oponente, aplicando dano (com bônus de super efetividade por
     * tipo) e verificando nocaute.
     *
     * @param oponente   jogador adversário
     * @param turnoAtual número do turno atual (ataque só é permitido a partir do Turno 2)
     * @throws JogadaInvalidaException se o ataque não for permitido agora
     */
    public void atacar(Jogador oponente, int turnoAtual) {
        if (turnoAtual < TURNO_MINIMO_ATAQUE_E_EVOLUCAO) {
            throw new JogadaInvalidaException("Não é permitido atacar no primeiro turno da partida! Espere o próximo turno.");
        }
        if (atacouNestaRodada) {
            throw new JogadaInvalidaException("Você já atacou nesta rodada! Só é permitido 1 ataque por rodada.");
        }
        if (pokemonAtivo == null) {
            throw new JogadaInvalidaException(nome + " não tem Pokémon Ativo para atacar! (Só o Pokémon Ativo pode atacar.)");
        }
        if (oponente.pokemonAtivo == null) {
            throw new JogadaInvalidaException("O oponente não tem Pokémon Ativo para ser atacado!");
        }
        if (pokemonAtivo.getQuantidadeEnergias() < 1) {
            throw new JogadaInvalidaException(pokemonAtivo.getNome() + " não tem energia suficiente para atacar! Anexe pelo menos 1 Energia.");
        }

        CartaPokemon alvo = oponente.pokemonAtivo;
        boolean superEfetivo = TabelaDeTipos.eSuperEfetivo(pokemonAtivo.getTipoElemento(), alvo.getTipoElemento());
        int dano = superEfetivo ? pokemonAtivo.getDanoAtaque() * 2 : pokemonAtivo.getDanoAtaque();

        alvo.receberDano(dano);
        atacouNestaRodada = true;
        notificar(TipoEvento.ATAQUE, "💥 " + pokemonAtivo.getNome() + " atacou " + alvo.getNome() + " causando " + dano + " de dano!"
                + (superEfetivo ? " 🔥 É SUPER EFETIVO! (Fraqueza — dano dobrado)" : ""));

        if (alvo.isNocauteado()) {
            resolverNocaute(oponente, alvo);
        } else {
            notificar(TipoEvento.INFORMACAO, alvo.getNome() + " ficou com " + alvo.getHpAtual() + "/" + alvo.getHpMaximo() + " HP.");
        }
    }

    private void resolverNocaute(Jogador oponente, CartaPokemon nocauteado) {
        notificar(TipoEvento.NOCAUTE, "☠️ " + nocauteado.getNome() + " foi Nocauteado!");
        oponente.zonaMorta.add(nocauteado);
        oponente.pokemonAtivo = null;

        if (!premios.isEmpty()) {
            mao.add(premios.remove(0));
            notificar(TipoEvento.PREMIO, "🏅 " + nome + " conquistou um prêmio! (faltam " + premios.size() + ")");
        }

        if (!oponente.banco.isEmpty()) {
            oponente.pokemonAtivo = oponente.banco.remove(0);
            notificar(TipoEvento.TROCA_DE_ATIVO, "🔄 " + oponente.getNome() + " enviou " + oponente.pokemonAtivo.getNome()
                    + " do Banco para o campo!");
        } else {
            notificar(TipoEvento.INFORMACAO, oponente.getNome() + " não tem mais Pokémon no Banco!");
        }
    }

    /**
     * Troca o Pokémon Ativo por um do Banco, sem custo. Usado pelo recuo e pela
     * estratégia {@link efeitos.EfeitoTroca} (por isso é público).
     *
     * @param indiceBanco posição (0 a 4) do Pokémon do Banco que vira o novo Ativo
     * @throws JogadaInvalidaException se não houver Ativo, se o Banco estiver vazio ou se a posição for inválida
     */
    public void trocarAtivoComBanco(int indiceBanco) {
        if (pokemonAtivo == null) {
            throw new JogadaInvalidaException("Você não tem Pokémon Ativo para trocar!");
        }
        if (banco.isEmpty()) {
            throw new JogadaInvalidaException("Seu Banco está vazio, não há para quem trocar!");
        }
        if (indiceBanco < 0 || indiceBanco >= banco.size()) {
            throw new JogadaInvalidaException("Posição inválida no Banco! Escolha de 1 a " + banco.size() + ".");
        }

        CartaPokemon antigoAtivo = pokemonAtivo;
        pokemonAtivo = banco.remove(indiceBanco);
        banco.add(antigoAtivo);
        notificar(TipoEvento.TROCA_DE_ATIVO, "🔄 " + nome + " trocou " + antigoAtivo.getNome() + " e colocou "
                + pokemonAtivo.getNome() + " como novo Ativo!");
    }

    /**
     * Recua o Pokémon Ativo para o Banco (trocando por outro), pagando o
     * custo de {@value #CUSTO_RECUO} energia. Diferente da carta Troca, só pode
     * ser feito 1 vez por turno.
     *
     * @param indiceBanco posição (0 a 4) do Pokémon do Banco que vira o novo Ativo
     * @throws JogadaInvalidaException se o recuo não for permitido
     */
    public void recuar(int indiceBanco) {
        if (recuouNesteTurno) {
            throw new JogadaInvalidaException("Você já recuou nesta rodada! Só é permitido 1 recuo por turno.");
        }
        if (pokemonAtivo != null && pokemonAtivo.getQuantidadeEnergias() < CUSTO_RECUO) {
            throw new JogadaInvalidaException(pokemonAtivo.getNome() + " não tem energia suficiente pra pagar o custo de recuo ("
                    + CUSTO_RECUO + ")!");
        }

        CartaPokemon quemRecua = pokemonAtivo;
        trocarAtivoComBanco(indiceBanco); // valida Ativo, Banco e posição

        // Só paga o custo depois que a troca deu certo.
        for (int i = 0; i < CUSTO_RECUO; i++) {
            quemRecua.getEnergiasAnexadas().remove(0);
        }
        recuouNesteTurno = true;
    }

    /** Reinicia os controles de "uma vez por rodada" (energia, ataque, recuo) ao fim do turno. */
    public void encerrarRodada() {
        energiaAnexadaNestaRodada = false;
        atacouNestaRodada = false;
        recuouNesteTurno = false;
        notificar(TipoEvento.FIM_DE_TURNO, "🔚 " + nome + " encerrou o turno.");
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /**
     * Procura, na mão, todas as cartas que são evolução direta do Pokémon informado.
     * Usado pela interface (para listar as opções) e pela IA do Bot.
     *
     * @param alvo Pokémon em campo que se quer evoluir
     * @return posições na mão das evoluções possíveis (lista vazia se não houver)
     */
    public List<Integer> indicesDeEvolucoesNaMao(CartaPokemon alvo) {
        List<Integer> indices = new ArrayList<>();
        if (alvo == null) return indices;
        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaPokemon) {
                CartaPokemon carta = (CartaPokemon) mao.get(i);
                if (!carta.isBasico() && carta.getEvoluiDe().equalsIgnoreCase(alvo.getNome())) {
                    indices.add(i);
                }
            }
        }
        return indices;
    }

    /** @return posição na mão do primeiro Pokémon Básico, ou {@code -1} se não houver */
    public int indiceDoPrimeiroBasicoNaMao() {
        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaPokemon && ((CartaPokemon) mao.get(i)).isBasico()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param nomePokemon nome a procurar
     * @return {@code true} se já existe, em campo (ativo ou banco), um Pokémon com o nome informado
     */
    public boolean baseEmCampo(String nomePokemon) {
        if (nomePokemon == null) return false;
        if (pokemonAtivo != null && pokemonAtivo.getNome().equalsIgnoreCase(nomePokemon)) {
            return true;
        }
        for (CartaPokemon p : banco) {
            if (p.getNome().equalsIgnoreCase(nomePokemon)) {
                return true;
            }
        }
        return false;
    }

    /** @return {@code true} se este jogador não tem nenhum Pokémon em campo (nem Ativo nem no Banco) */
    public boolean semPokemonEmCampo() {
        return pokemonAtivo == null && banco.isEmpty();
    }

    /** @return {@code true} se este jogador já anexou energia nesta rodada */
    public boolean isEnergiaAnexadaNestaRodada() { return energiaAnexadaNestaRodada; }

    /** @return {@code true} se este jogador já atacou nesta rodada */
    public boolean isAtacouNestaRodada() { return atacouNestaRodada; }

    /** @return {@code true} se este jogador já recuou nesta rodada */
    public boolean isRecuouNesteTurno() { return recuouNesteTurno; }

    /** @return nome deste jogador (treinador) */
    public String getNome() { return nome; }

    /** @return lista (mutável) das cartas atualmente na mão */
    public List<Carta> getMao() { return mao; }

    /** @return lista (mutável) das cartas restantes no baralho */
    public List<Carta> getBaralho() { return baralho; }

    /** @return Pokémon Ativo deste jogador, ou {@code null} se não houver nenhum em campo */
    public CartaPokemon getPokemonAtivo() { return pokemonAtivo; }

    /** @return lista (mutável) dos Pokémon no Banco de Reservas */
    public List<CartaPokemon> getBanco() { return banco; }

    /** @return lista (mutável) dos Pokémon nocauteados (zona morta) */
    public List<CartaPokemon> getZonaMorta() { return zonaMorta; }

    /** @return lista (mutável) das cartas-prêmio ainda não conquistadas; vazia quando o jogador vence a partida */
    public List<Carta> getPremios() { return premios; }

    /**
     * Devolve a carta da mão na posição informada, validando o índice.
     * Centraliza a validação que antes era repetida em cada ação.
     */
    private Carta cartaDaMao(int indiceNaMao) {
        if (indiceNaMao < 0 || indiceNaMao >= mao.size()) {
            throw new JogadaInvalidaException("Posição inválida na mão! Escolha uma carta entre 1 e " + mao.size() + ".");
        }
        return mao.get(indiceNaMao);
    }
}
