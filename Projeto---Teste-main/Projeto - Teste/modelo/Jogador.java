import java.util.*;

/**
 * Representa um jogador (humano ou controlado pelo Bot) e todo o seu estado
 * de jogo: baralho, mão, Pokémon ativo, banco, zona morta e prêmios.
 * Concentra as regras de negócio da partida (comprar carta, jogar Pokémon,
 * anexar energia, evoluir, atacar, usar cartas de treinador), enquanto a
 * interface gráfica ({@link AppGUI}) cuida apenas de coletar as escolhas do
 * jogador e exibir o resultado.
 */
public class Jogador {
    private String nome;
    private List<Carta> baralho;
    private List<Carta> mao;
    private CartaPokemon pokemonAtivo;
    private List<CartaPokemon> banco;
    private List<CartaPokemon> zonaMorta;
    private List<Carta> premios;

    private boolean energiaAnexadaNestaRodada;
    private boolean atacouNestaRodada;
    private boolean recuouNesteTurno;

    /** Cria um novo jogador com baralho, mão, banco e prêmios vazios. */
    public Jogador(String nome) {
        this.nome = nome;
        this.baralho = new ArrayList<>();
        this.mao = new ArrayList<>();
        this.banco = new ArrayList<>();
        this.zonaMorta = new ArrayList<>();
        this.premios = new ArrayList<>();
        this.pokemonAtivo = null;
        this.energiaAnexadaNestaRodada = false;
        this.atacouNestaRodada = false;
        this.recuouNesteTurno = false;
    }

    /** Adiciona uma carta ao final do baralho (usado ao montar o time, antes de embaralhar). */
    public void adicionarAoBaralho(Carta carta) {
        this.baralho.add(carta);
    }

    /** Embaralha o baralho deste jogador em ordem aleatória. */
    public void verificarBaralho() {
        Collections.shuffle(this.baralho);
    }

    /**
     * Compra a carta do topo do baralho para a mão.
     * @return {@code true} se havia carta para comprar; {@code false} se o baralho estava vazio
     */
    public boolean comprarCarta() {
        if (!baralho.isEmpty()) {
            Carta cartaComprada = baralho.remove(0);
            mao.add(cartaComprada);
            System.out.println(nome + " comprou: " + cartaComprada.getNome());
            return true;
        } else {
            System.out.println("O baralho de " + nome + " acabou!");
            return false;
        }
    }

    /** @return quantidade de cartas restantes no baralho */
    public int getTamanhoBaralho() {
        return baralho.size();
    }

    /**
     * Coloca uma carta de Pokémon básica (da mão) em campo.
     *
     * @param indiceNaMao posição da carta na mão
     * @param destino     0 para colocar como Pokémon Ativo, 1 para colocar no Banco
     */
    public void colocarPokemonEmCampo(int indiceNaMao, int destino) {
        if (indiceNaMao < 0 || indiceNaMao >= mao.size()) {
            System.out.println("Posição inválida na mão!");
            return;
        }

        Carta carta = mao.get(indiceNaMao);

        if (!(carta instanceof CartaPokemon)) {
            System.out.println("Essa carta não é um Pokémon!");
            return;
        }

        CartaPokemon pokemon = (CartaPokemon) carta;

        if (!pokemon.isBasico()) {
            System.out.println("⚠️ " + pokemon.getNome() + " é uma evolução (evolui de " + pokemon.getEvoluiDe()
                    + ")! Você não pode colocá-lo direto em campo — use a opção de Evoluir em cima de um "
                    + pokemon.getEvoluiDe() + " que já esteja em campo.");
            return;
        }

        if (destino == 0) {
            if (pokemonAtivo != null) {
                System.out.println("⚠️ Você já tem um Pokémon Ativo (" + pokemonAtivo.getNome()
                        + "). Escolha o Banco ou troque de Ativo primeiro.");
                return;
            }
            pokemonAtivo = (CartaPokemon) carta;
            mao.remove(indiceNaMao);
            System.out.println("\n🔥 " + nome + " colocou " + pokemonAtivo.getNome() + " como seu Pokémon Ativo!");

        } else if (destino == 1) {
            if (banco.size() >= 5) {
                System.out.println("O Banco de Reservas está cheio!");
                return;
            }
            banco.add((CartaPokemon) carta);
            mao.remove(indiceNaMao);
            System.out.println("\n💤 " + nome + " colocou " + carta.getNome() + " no Banco de Reservas.");

        } else {
            System.out.println("Destino inválido! Escolha 0 (Ativo) ou 1 (Banco).");
        }
    }

    /**
     * Anexa uma nova carta de energia (do tipo do próprio alvo) a um Pokémon em campo,
     * respeitando o limite de 1 energia anexada por rodada.
     *
     * @return {@code true} se a energia foi anexada com sucesso
     */
    public boolean anexarEnergia(CartaPokemon destino) {
        if (energiaAnexadaNestaRodada) {
            System.out.println("⚠️ Você já anexou uma Energia nesta rodada! Só é permitida 1 por rodada.");
            return false;
        }

        if (destino == null) {
            System.out.println("Não há Pokémon para receber a energia!");
            return false;
        }

        CartaEnergia energia = new CartaEnergia("Energia de " + destino.getTipoElemento(), destino.getTipoElemento());
        boolean sucesso = destino.anexarEnergia(energia);

        if (!sucesso) {
            System.out.println("⚠️ " + destino.getNome() + " já está no limite máximo de energias ("
                    + destino.getLimiteEnergias() + ")! Não é possível anexar mais.");
            return false;
        }

        energiaAnexadaNestaRodada = true;
        System.out.println("\n⚡ " + nome + " anexou uma Energia de " + destino.getTipoElemento() + " em "
                + destino.getNome() + "! (" + destino.getQuantidadeEnergias() + "/" + destino.getLimiteEnergias() + ")");
        return true;
    }

    /**
     * Localiza um Pokémon em campo pelo índice usado nos menus (0 = Ativo, 1 a 5 = posição no Banco).
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
     * @param indiceNaMao  posição, na mão, da carta de evolução a usar
     * @param alvoEmCampo  Pokémon em campo que vai evoluir
     * @param turnoAtual   número do turno atual da partida
     */
    public boolean evoluir(int indiceNaMao, CartaPokemon alvoEmCampo, int turnoAtual) {
        if (turnoAtual < 2) {
            System.out.println("⚠️ Evolução só é permitida a partir do Turno 2! Espere o próximo turno.");
            return false;
        }

        if (alvoEmCampo != null && alvoEmCampo.isEvoluiuNesteTurno()) {
            System.out.println("⚠️ " + alvoEmCampo.getNome() + " já evoluiu neste turno! Espere o próximo turno.");
            return false;
        }

        return executarEvolucao(indiceNaMao, alvoEmCampo);
    }

    // Pacote-padrão (sem modificador) de propósito: precisa ser chamado pela
    // estratégia EfeitoEvolucaoRapida, que fica fora da classe Jogador.
    boolean executarEvolucao(int indiceNaMao, CartaPokemon alvoEmCampo) {
        if (indiceNaMao < 0 || indiceNaMao >= mao.size()) {
            System.out.println("Posição inválida na mão!");
            return false;
        }

        Carta carta = mao.get(indiceNaMao);

        if (!(carta instanceof CartaPokemon)) {
            System.out.println("Essa carta não é um Pokémon!");
            return false;
        }

        CartaPokemon cartaEvolucao = (CartaPokemon) carta;

        if (cartaEvolucao.isBasico()) {
            System.out.println("⚠️ " + cartaEvolucao.getNome() + " é uma forma Básica, não uma evolução!");
            return false;
        }

        if (alvoEmCampo == null) {
            System.out.println("Escolha um Pokémon em campo válido para evoluir!");
            return false;
        }

        if (!cartaEvolucao.getEvoluiDe().equalsIgnoreCase(alvoEmCampo.getNome())) {
            System.out.println("⚠️ " + cartaEvolucao.getNome() + " não evolui de " + alvoEmCampo.getNome()
                    + "! Ele evolui de " + cartaEvolucao.getEvoluiDe() + ".");
            return false;
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
            int indiceNoBanco = banco.indexOf(alvoEmCampo);
            if (indiceNoBanco == -1) {
                System.out.println("Esse Pokémon não está em campo!");
                return false;
            }
            banco.set(indiceNoBanco, cartaEvolucao);
        }

        mao.remove(indiceNaMao);
        cartaEvolucao.setEvoluiuNesteTurno(true);

        System.out.println("\n✨ " + alvoEmCampo.getNome() + " evoluiu para " + cartaEvolucao.getNome() + "!");
        return true;
    }

    /**
     * Ponto de entrada único para usar qualquer carta de treinador.
     *
     * <p>Antes desta refatoração, cada efeito de treinador tinha seu próprio
     * método aqui em {@code Jogador} (um para Poção, outro para Cura Total,
     * outro para Troca...), todos repetindo a mesma validação de índice e
     * tipo de carta. Agora essa validação fica centralizada aqui, e o
     * comportamento específico de cada efeito é delegado à sua
     * {@link EfeitoTreinador} (padrão de projeto <b>Strategy</b>), obtida a
     * partir da própria carta.</p>
     */
    public boolean usarCartaTreinador(int indiceNaMao, ParametrosEfeito parametros) {
        if (indiceNaMao < 0 || indiceNaMao >= mao.size()) {
            System.out.println("Posição inválida na mão!");
            return false;
        }

        Carta carta = mao.get(indiceNaMao);
        if (!(carta instanceof CartaTreinador)) {
            System.out.println("Essa carta não é uma carta de Treinador!");
            return false;
        }

        EfeitoTreinador efeito = ((CartaTreinador) carta).getEstrategia();
        return efeito.aplicar(this, indiceNaMao, parametros);
    }

    /**
     * Atalho de compatibilidade para usar uma carta "Evolução Rápida". Internamente
     * delega para {@link #usarCartaTreinador} e para a estratégia {@link EfeitoEvolucaoRapida}.
     */
    public boolean usarEvolucaoRapida(int indiceCartaTreinador, int indiceCartaEvolucao, CartaPokemon alvoEmCampo) {
        ParametrosEfeito parametros = new ParametrosEfeito();
        parametros.setAlvo(alvoEmCampo);
        parametros.setIndiceCartaEvolucao(indiceCartaEvolucao);
        return usarCartaTreinador(indiceCartaTreinador, parametros);
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
     * @return {@code true} se o ataque foi executado
     */
    public boolean atacar(Jogador oponente, int turnoAtual) {
        if (turnoAtual < 2) {
            System.out.println("⚠️ Não é permitido atacar no primeiro turno da partida! Espere o próximo turno.");
            return false;
        }

        if (atacouNestaRodada) {
            System.out.println("⚠️ Você já atacou nesta rodada! Só é permitido 1 ataque por rodada.");
            return false;
        }

        if (pokemonAtivo == null) {
            System.out.println(nome + " não tem Pokémon Ativo para atacar! (Só o Pokémon Ativo pode atacar — os do Banco não atacam.)");
            return false;
        }

        if (oponente.pokemonAtivo == null) {
            System.out.println("O oponente não tem Pokémon Ativo para ser atacado!");
            return false;
        }

        if (pokemonAtivo.getQuantidadeEnergias() < 1) {
            System.out.println("⚠️ " + pokemonAtivo.getNome() + " não tem energia suficiente para atacar!");
            return false;
        }

        int dano = pokemonAtivo.getDanoAtaque();
        CartaPokemon alvo = oponente.pokemonAtivo;

        boolean superEfetivo = eSuperEfetivo(pokemonAtivo.getTipoElemento(), alvo.getTipoElemento());
        if (superEfetivo) {
            dano *= 2;
        }

        System.out.println("\n💥 " + pokemonAtivo.getNome() + " atacou " + alvo.getNome() + " causando " + dano + " de dano!"
                + (superEfetivo ? " 🔥 É SUPER EFETIVO! (Fraqueza — dano dobrado)" : ""));
        alvo.receberDano(dano);
        atacouNestaRodada = true;

        if (alvo.isNocauteado()) {
            System.out.println("☠️ " + alvo.getNome() + " foi Nocauteado!");
            oponente.zonaMorta.add(alvo);
            oponente.pokemonAtivo = null;

            if (!premios.isEmpty()) {
                Carta premio = premios.remove(0);
                mao.add(premio);
                System.out.println("🏅 " + nome + " conquistou um prêmio! (" + (2 - premios.size()) + "/2)");
            }

            if (!oponente.banco.isEmpty()) {
                CartaPokemon novoAtivo = oponente.banco.remove(0);
                oponente.pokemonAtivo = novoAtivo;
                System.out.println("🔄 " + oponente.getNome() + " enviou " + novoAtivo.getNome() + " do Banco para o campo!");
            } else {
                System.out.println(oponente.getNome() + " não tem mais Pokémon no Banco!");
            }
        } else {
            System.out.println(alvo.getNome() + " ficou com " + alvo.getHpAtual() + "/" + alvo.getHpMaximo() + " HP.");
        }

        return true;
    }

    private boolean eSuperEfetivo(String tipoAtacante, String tipoDefensor) {
        return (tipoAtacante.equalsIgnoreCase("Água") && tipoDefensor.equalsIgnoreCase("Fogo"))
                || (tipoAtacante.equalsIgnoreCase("Fogo") && tipoDefensor.equalsIgnoreCase("Planta"))
                || (tipoAtacante.equalsIgnoreCase("Planta") && tipoDefensor.equalsIgnoreCase("Água"));
    }

    // Pacote-padrão (sem modificador) de propósito: precisa ser chamado pela
    // estratégia EfeitoTroca, que fica fora da classe Jogador.
    void trocarAtivoComBanco(int indiceBanco) {
        CartaPokemon antigoAtivo = pokemonAtivo;
        CartaPokemon novoAtivo = banco.remove(indiceBanco);
        banco.add(antigoAtivo);
        pokemonAtivo = novoAtivo;
        System.out.println("\n🔄 " + nome + " trocou " + antigoAtivo.getNome() + " e colocou " + pokemonAtivo.getNome() + " como novo Ativo!");
    }

    /**
     * Recua o Pokémon Ativo para o Banco (trocando por outro), pagando o
     * custo de 1 energia. Diferente da carta Troca, só pode ser feito 1 vez por turno.
     */
    public boolean recuar(int indiceBanco) {
        final int CUSTO_RECUO = 1;

        if (recuouNesteTurno) {
            System.out.println("⚠️ Você já recuou nesta rodada! Só é permitido 1 recuo por turno.");
            return false;
        }

        if (pokemonAtivo == null) {
            System.out.println("Você não tem Pokémon Ativo para recuar!");
            return false;
        }

        if (banco.isEmpty()) {
            System.out.println("Seu Banco está vazio, não há para quem trocar!");
            return false;
        }

        if (indiceBanco < 0 || indiceBanco >= banco.size()) {
            System.out.println("Posição inválida no Banco!");
            return false;
        }

        if (pokemonAtivo.getQuantidadeEnergias() < CUSTO_RECUO) {
            System.out.println("⚠️ " + pokemonAtivo.getNome() + " não tem energia suficiente pra pagar o custo de recuo ("
                    + CUSTO_RECUO + ")!");
            return false;
        }

        for (int i = 0; i < CUSTO_RECUO; i++) {
            pokemonAtivo.getEnergiasAnexadas().remove(0);
        }

        trocarAtivoComBanco(indiceBanco);
        recuouNesteTurno = true;
        return true;
    }

    /**
     * Atalho de compatibilidade para usar uma carta "Poção". Internamente
     * delega para {@link #usarCartaTreinador} e para a estratégia {@link EfeitoPocao}.
     */
    public boolean usarPocao(int indiceNaMao, CartaPokemon alvo) {
        ParametrosEfeito parametros = new ParametrosEfeito();
        parametros.setAlvo(alvo);
        return usarCartaTreinador(indiceNaMao, parametros);
    }

    /**
     * Atalho de compatibilidade para usar uma carta "Cura Total". Internamente
     * delega para {@link #usarCartaTreinador} e para a estratégia {@link EfeitoCuraTotal}.
     */
    public boolean usarCuraTotal(int indiceNaMao, CartaPokemon alvo) {
        ParametrosEfeito parametros = new ParametrosEfeito();
        parametros.setAlvo(alvo);
        return usarCartaTreinador(indiceNaMao, parametros);
    }

    /**
     * Atalho de compatibilidade para usar uma carta "Troca". Internamente
     * delega para {@link #usarCartaTreinador} e para a estratégia {@link EfeitoTroca}.
     */
    public boolean usarTroca(int indiceNaMao, int indiceBanco) {
        ParametrosEfeito parametros = new ParametrosEfeito();
        parametros.setIndiceBanco(indiceBanco);
        return usarCartaTreinador(indiceNaMao, parametros);
    }

    /** Reinicia os controles de "uma vez por rodada" (energia, ataque, recuo) ao fim do turno. */
    public void encerrarRodada() {
        energiaAnexadaNestaRodada = false;
        atacouNestaRodada = false;
        recuouNesteTurno = false;
        System.out.println("\n🔚 " + nome + " encerrou o turno.");
    }

    /** @return {@code true} se este jogador já anexou energia nesta rodada */
    public boolean isEnergiaAnexadaNestaRodada() { return energiaAnexadaNestaRodada; }
    /** @return {@code true} se este jogador já atacou nesta rodada */
    public boolean isAtacouNestaRodada() { return atacouNestaRodada; }
    /** @return {@code true} se este jogador já recuou nesta rodada */
    public boolean isRecuouNesteTurno() { return recuouNesteTurno; }

    /** Imprime no console um resumo do estado atual deste jogador (usado pelo protótipo em {@link App}). */
    public void mostrarTabuleiro() {
        System.out.println("\n================ TABULEIRO DE " + nome.toUpperCase() + " ================");
        System.out.println("🔴 POKÉMON ATIVO: " + (pokemonAtivo != null ? pokemonAtivo : "[Nenhum]"));

        System.out.print("🔵 BANCO DE RESERVAS: ");
        if (banco.isEmpty()) {
            System.out.println("[Vazio]");
        } else {
            for (int i = 0; i < banco.size(); i++) {
                CartaPokemon p = banco.get(i);
                System.out.print("[" + (i + 1) + " - " + p.getNome() + " HP:" + p.getHpAtual() + "] ");
            }
            System.out.println();
        }

        System.out.println("🃏 CARTAS NA MÃO: " + mao.size() + " cartas.");
        System.out.println("📚 BARALHO: " + getTamanhoBaralho() + " cartas restantes.");
        System.out.println("---- Limites do turno ----");
        System.out.println("⚡ Energia anexada: " + (energiaAnexadaNestaRodada ? "SIM (esgotado)" : "Disponível"));
        System.out.println("💥 Ataque usado: " + (atacouNestaRodada ? "SIM (esgotado — atacar encerra o turno)" : "Disponível"));
        System.out.println("🔄 Recuo usado: " + (recuouNesteTurno ? "SIM (esgotado)" : "Disponível"));
        System.out.println("====================================================\n");
    }

    /** @return {@code true} se já existe, em campo (ativo ou banco), um Pokémon com o nome informado */
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

    /** Imprime no console as cartas atualmente na mão deste jogador (usado pelo protótipo em {@link App}). */
    public void mostrarMao() {
        System.out.println("👋 CARTAS NA SUA MÃO:");
        if (mao.isEmpty()) {
            System.out.println("[Sua mão está vazia]");
            System.out.println();
            return;
        }

        System.out.println("  -- Pokémon --");
        boolean temPokemon = false;
        int evolucoesEscondidas = 0;
        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaPokemon) {
                CartaPokemon p = (CartaPokemon) mao.get(i);

                if (!p.isBasico() && !baseEmCampo(p.getEvoluiDe())) {
                    evolucoesEscondidas++;
                    continue;
                }

                String tag = !p.isBasico() ? " ✨(Pronto pra evoluir!)" : "";
                System.out.println("  " + (i + 1) + ". " + p + tag);
                temPokemon = true;
            }
        }
        if (!temPokemon) System.out.println("  [Nenhum]");
        if (evolucoesEscondidas > 0) {
            System.out.println("  🔒 " + evolucoesEscondidas + " evolução(ões) escondida(s) até você colocar a forma base em campo.");
        }

        System.out.println("  -- Treinador --");
        boolean temTreinador = false;
        for (int i = 0; i < mao.size(); i++) {
            if (mao.get(i) instanceof CartaTreinador) {
                System.out.println("  " + (i + 1) + ". " + mao.get(i));
                temTreinador = true;
            }
        }
        if (!temTreinador) System.out.println("  [Nenhuma]");

        System.out.println("  -- Energias --");
        System.out.println("  ♾️  Energia disponível: Ilimitada (basta anexar em qualquer Pokémon em campo).");
        System.out.println("      Cada Pokémon tem seu próprio limite (entre 6 e 8 energias).");

        System.out.println();
    }

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
}