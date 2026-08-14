import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Jogador {
    private String nome;
    private List<Carta> baralho;
    private List<Carta> mao;
    private CartaPokemon pokemonAtivo;
    private List<CartaPokemon> banco;

    // Controle de limite de jogadas por rodada
    private boolean energiaAnexadaNestaRodada;
    private boolean atacouNestaRodada;

    public Jogador(String nome) {
        this.nome = nome;
        this.baralho = new ArrayList<>();
        this.mao = new ArrayList<>();
        this.banco = new ArrayList<>();
        this.pokemonAtivo = null;
        this.energiaAnexadaNestaRodada = false;
        this.atacouNestaRodada = false;
    }

    public void adicionarAoBaralho(Carta carta) {
        this.baralho.add(carta);
    }

    public void verificarBaralho() {
        Collections.shuffle(this.baralho);
    }

    public void comprarCarta() {
        if (!baralho.isEmpty()) {
            Carta cartaComprada = baralho.remove(0);
            mao.add(cartaComprada);
            System.out.println(nome + " comprou: " + cartaComprada.getNome());
        } else {
            System.out.println("O baralho de " + nome + " acabou!");
        }
    }

    public void colocarPokemonEmCampo(int indiceNaMao) {
        if (indiceNaMao < 0 || indiceNaMao >= mao.size()) {
            System.out.println("Posição inválida na mão!");
            return;
        }

        Carta carta = mao.get(indiceNaMao);
        
        if (carta instanceof CartaPokemon) {
            if (pokemonAtivo == null) {
                pokemonAtivo = (CartaPokemon) carta;
                mao.remove(indiceNaMao);
                System.out.println("\n🔥 " + nome + " colocou " + pokemonAtivo.getNome() + " como seu Pokémon Ativo!");
            } else if (banco.size() < 5) {
                banco.add((CartaPokemon) carta);
                mao.remove(indiceNaMao);
                System.out.println("\n💤 " + nome + " colocou " + carta.getNome() + " no Banco de Reservas.");
            } else {
                System.out.println("O Banco de Reservas está cheio!");
            }
        } else {
            System.out.println("Essa carta não é um Pokémon!");
        }
    }

    // ---------- NOVO: ANEXAR ENERGIA ----------

    /**
     * Anexa uma carta de energia (que está na mão) a um Pokémon em campo.
     * @param indiceNaMao índice da carta de energia na mão
     * @param destino Pokémon que vai receber a energia (ativo ou do banco)
     * @return true se a energia foi anexada com sucesso
     */
    public boolean anexarEnergia(int indiceNaMao, CartaPokemon destino) {
        if (energiaAnexadaNestaRodada) {
            System.out.println("⚠️ Você já anexou uma Energia nesta rodada! Só é permitida 1 por rodada.");
            return false;
        }

        if (indiceNaMao < 0 || indiceNaMao >= mao.size()) {
            System.out.println("Posição inválida na mão!");
            return false;
        }

        Carta carta = mao.get(indiceNaMao);

        if (!(carta instanceof CartaEnergia)) {
            System.out.println("Essa carta não é uma Energia!");
            return false;
        }

        if (destino == null) {
            System.out.println("Não há Pokémon para receber a energia!");
            return false;
        }

        CartaEnergia energia = (CartaEnergia) carta;
        destino.anexarEnergia(energia);
        mao.remove(indiceNaMao);
        energiaAnexadaNestaRodada = true;

        System.out.println("\n⚡ " + nome + " anexou " + energia.getNome() + " em " + destino.getNome() + "!");
        return true;
    }

    /**
     * Retorna um Pokémon do campo (ativo ou banco) a partir de um índice escolhido no menu.
     * indice 0 = ativo; indice 1..5 = banco (posição 1 a 5)
     */
    public CartaPokemon getPokemonDoCampoPorIndice(int indice) {
        if (indice == 0) {
            return pokemonAtivo;
        } else if (indice >= 1 && indice <= banco.size()) {
            return banco.get(indice - 1);
        }
        return null;
    }

    // ---------- NOVO: ATAQUE ----------

    /**
     * O Pokémon ativo deste jogador ataca o Pokémon ativo do oponente.
     * Regra simples: precisa de pelo menos 1 energia anexada para atacar.
     */
    public boolean atacar(Jogador oponente) {
        if (atacouNestaRodada) {
            System.out.println("⚠️ Você já atacou nesta rodada! Só é permitido 1 ataque por rodada.");
            return false;
        }

        if (pokemonAtivo == null) {
            System.out.println(nome + " não tem Pokémon Ativo para atacar!");
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

        System.out.println("\n💥 " + pokemonAtivo.getNome() + " atacou " + alvo.getNome() + " causando " + dano + " de dano!");
        alvo.receberDano(dano);
        atacouNestaRodada = true;

        if (alvo.isNocauteado()) {
            System.out.println("☠️ " + alvo.getNome() + " foi Nocauteado!");
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

    /**
     * Encerra a rodada deste jogador, liberando novamente a Energia e o Ataque para a próxima rodada.
     */
    public void encerrarRodada() {
        energiaAnexadaNestaRodada = false;
        atacouNestaRodada = false;
        System.out.println("\n🔚 " + nome + " encerrou a rodada.");
    }

    public boolean isEnergiaAnexadaNestaRodada() { return energiaAnexadaNestaRodada; }
    public boolean isAtacouNestaRodada() { return atacouNestaRodada; }

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
        System.out.println("---- Limites da rodada ----");
        System.out.println("⚡ Energia anexada: " + (energiaAnexadaNestaRodada ? "SIM (esgotado)" : "Disponível"));
        System.out.println("💥 Ataque usado: " + (atacouNestaRodada ? "SIM (esgotado)" : "Disponível"));
        System.out.println("====================================================\n");
    }

    public void mostrarMao() {
        System.out.println("👋 CARTAS NA SUA MÃO:");
        if (mao.isEmpty()) {
            System.out.println("[Sua mão está vazia]");
        } else {
            for (int i = 0; i < mao.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + mao.get(i));
            }
        }
        System.out.println();
    }

    public String getNome() { return nome; }
    public List<Carta> getMao() { return mao; }
    public CartaPokemon getPokemonAtivo() { return pokemonAtivo; }
    public List<CartaPokemon> getBanco() { return banco; }
}
