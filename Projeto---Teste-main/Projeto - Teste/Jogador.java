import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Jogador {
    private String nome;
    private List<Carta> baralho;
    private List<Carta> mao;
    private CartaPokemon pokemonAtivo;
    private List<CartaPokemon> banco;
    private List<CartaPokemon> zonaMorta;

    private boolean energiaAnexadaNestaRodada;
    private boolean atacouNestaRodada;
    private boolean recuouNesteTurno;

    public Jogador(String nome) {
        this.nome = nome;
        this.baralho = new ArrayList<>();
        this.mao = new ArrayList<>();
        this.banco = new ArrayList<>();
        this.zonaMorta = new ArrayList<>();
        this.pokemonAtivo = null;
        this.energiaAnexadaNestaRodada = false;
        this.atacouNestaRodada = false;
        this.recuouNesteTurno = false;
    }

    public void adicionarAoBaralho(Carta carta) {
        this.baralho.add(carta);
    }

    public void verificarBaralho() {
        Collections.shuffle(this.baralho);
    }

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

    public int getTamanhoBaralho() {
        return baralho.size();
    }

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

    public CartaPokemon getPokemonDoCampoPorIndice(int indice) {
        if (indice == 0) {
            return pokemonAtivo;
        } else if (indice >= 1 && indice <= banco.size()) {
            return banco.get(indice - 1);
        }
        return null;
    }

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

    private boolean executarEvolucao(int indiceNaMao, CartaPokemon alvoEmCampo) {
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

    public boolean usarEvolucaoRapida(int indiceCartaTreinador, int indiceCartaEvolucao, CartaPokemon alvoEmCampo) {
        if (indiceCartaTreinador < 0 || indiceCartaTreinador >= mao.size()) {
            System.out.println("Posição inválida na mão!");
            return false;
        }

        Carta cartaTreinador = mao.get(indiceCartaTreinador);
        if (!(cartaTreinador instanceof CartaTreinador) || !((CartaTreinador) cartaTreinador).getEfeito().equalsIgnoreCase("Evolução Rápida")) {
            System.out.println("Essa carta não é Evolução Rápida!");
            return false;
        }

        boolean sucesso = executarEvolucao(indiceCartaEvolucao, alvoEmCampo);
        if (!sucesso) {
            return false;
        }

        int indiceTreinadorAtualizado = mao.indexOf(cartaTreinador);
        if (indiceTreinadorAtualizado != -1) {
            mao.remove(indiceTreinadorAtualizado);
        }

        System.out.println("⚡ " + nome + " usou Evolução Rápida — sem esperar turno, sem limite de vezes!");
        return true;
    }

    public void resetarEvolucoesDoTurno() {
        if (pokemonAtivo != null) pokemonAtivo.setEvoluiuNesteTurno(false);
        for (CartaPokemon p : banco) p.setEvoluiuNesteTurno(false);
    }

    public boolean atacar(Jogador oponente) {
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

    private void trocarAtivoComBanco(int indiceBanco) {
        CartaPokemon antigoAtivo = pokemonAtivo;
        CartaPokemon novoAtivo = banco.remove(indiceBanco);
        banco.add(antigoAtivo);
        pokemonAtivo = novoAtivo;
        System.out.println("\n🔄 " + nome + " trocou " + antigoAtivo.getNome() + " e colocou " + pokemonAtivo.getNome() + " como novo Ativo!");
    }

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

    public boolean usarPocao(int indiceNaMao, CartaPokemon alvo) {
        if (indiceNaMao < 0 || indiceNaMao >= mao.size()) {
            System.out.println("Posição inválida na mão!");
            return false;
        }

        Carta carta = mao.get(indiceNaMao);
        if (!(carta instanceof CartaTreinador) || !((CartaTreinador) carta).getEfeito().equalsIgnoreCase("Poção")) {
            System.out.println("Essa carta não é uma Poção!");
            return false;
        }

        if (alvo == null) {
            System.out.println("Escolha um Pokémon válido em campo para curar!");
            return false;
        }

        final int CURA = 30;
        alvo.curar(CURA);
        mao.remove(indiceNaMao);

        System.out.println("\n💊 " + nome + " usou Poção em " + alvo.getNome() + "! Curou " + CURA + " HP. ("
                + alvo.getHpAtual() + "/" + alvo.getHpMaximo() + ")");
        return true;
    }

    public boolean usarCuraTotal(int indiceNaMao, CartaPokemon alvo) {
        if (indiceNaMao < 0 || indiceNaMao >= mao.size()) {
            System.out.println("Posição inválida na mão!");
            return false;
        }

        Carta carta = mao.get(indiceNaMao);
        if (!(carta instanceof CartaTreinador) || !((CartaTreinador) carta).getEfeito().equalsIgnoreCase("Cura Total")) {
            System.out.println("Essa carta não é uma Cura Total!");
            return false;
        }

        if (alvo == null) {
            System.out.println("Escolha um Pokémon válido em campo para curar!");
            return false;
        }

        alvo.curar(alvo.getHpMaximo());
        mao.remove(indiceNaMao);

        System.out.println("\n💖 " + nome + " usou Cura Total em " + alvo.getNome() + "! HP restaurado por completo. ("
                + alvo.getHpAtual() + "/" + alvo.getHpMaximo() + ")");
        return true;
    }

    public boolean usarTroca(int indiceNaMao, int indiceBanco) {
        if (indiceNaMao < 0 || indiceNaMao >= mao.size()) {
            System.out.println("Posição inválida na mão!");
            return false;
        }

        Carta carta = mao.get(indiceNaMao);
        if (!(carta instanceof CartaTreinador) || !((CartaTreinador) carta).getEfeito().equalsIgnoreCase("Troca")) {
            System.out.println("Essa carta não é uma Troca!");
            return false;
        }

        if (pokemonAtivo == null) {
            System.out.println("Você não tem Pokémon Ativo para trocar!");
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

        trocarAtivoComBanco(indiceBanco);
        mao.remove(indiceNaMao);

        System.out.println("💨 " + nome + " usou a carta Troca — recuo grátis, sem gastar energia!");
        return true;
    }

    public void encerrarRodada() {
        energiaAnexadaNestaRodada = false;
        atacouNestaRodada = false;
        recuouNesteTurno = false;
        System.out.println("\n🔚 " + nome + " encerrou o turno.");
    }

    public boolean isEnergiaAnexadaNestaRodada() { return energiaAnexadaNestaRodada; }
    public boolean isAtacouNestaRodada() { return atacouNestaRodada; }
    public boolean isRecuouNesteTurno() { return recuouNesteTurno; }

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

    public String getNome() { return nome; }
    public List<Carta> getMao() { return mao; }
    public CartaPokemon getPokemonAtivo() { return pokemonAtivo; }
    public List<CartaPokemon> getBanco() { return banco; }
    public List<CartaPokemon> getZonaMorta() { return zonaMorta; }
}
