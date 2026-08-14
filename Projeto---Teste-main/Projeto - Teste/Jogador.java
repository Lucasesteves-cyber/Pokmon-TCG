import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Jogador {
    private String nome;
    private List<Carta> baralho;
    private List<Carta> mao;
    private CartaPokemon pokemonAtivo;
    private List<CartaPokemon> banco;

    public Jogador(String nome) {
        this.nome = nome;
        this.baralho = new ArrayList<>();
        this.mao = new ArrayList<>();
        this.banco = new ArrayList<>();
        this.pokemonAtivo = null;
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

    public void mostrarTabuleiro() {
        System.out.println("\n================ TABULEIRO DE " + nome.toUpperCase() + " ================");
        System.out.println("🔴 POKÉMON ATIVO: " + (pokemonAtivo != null ? pokemonAtivo : "[Nenhum]"));
        
        System.out.print("🔵 BANCO DE RESERVAS: ");
        if (banco.isEmpty()) {
            System.out.println("[Vazio]");
        } else {
            for (CartaPokemon p : banco) {
                System.out.print("[" + p.getNome() + " HP:" + p.getHpAtual() + "] ");
            }
            System.out.println();
        }
        
        System.out.println("🃏 CARTAS NA MÃO: " + mao.size() + " cartas.");
        System.out.println("====================================================\n");
    }

    // O método que estava dando falta agora está aqui com certeza:
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
}
