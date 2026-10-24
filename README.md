# Pokémon TCG — Java Edition

Um jogo de cartas colecionáveis (TCG) inspirado no card game oficial de Pokémon, feito inteiramente em **Java puro** (sem bibliotecas externas), com interface gráfica em **Swing** e integração em tempo real com a **PokeAPI**.

Projeto desenvolvido como exercício de Programação Orientada a Objetos, evoluindo de um protótipo em terminal (Scanner) até um jogo gráfico completo, com modo PvP e modo Solo contra um Bot.

---

## 📁 Estrutura de pastas (pacotes)

O código é organizado em 4 pacotes Java, por responsabilidade:

```
Projeto - Teste/
├── modelo/     → Carta, CartaPokemon, CartaEnergia, CartaTreinador, Jogador
├── efeitos/    → EfeitoTreinador, EfeitoPocao, EfeitoCuraTotal, EfeitoTroca,
│                 EfeitoEvolucaoRapida, EfeitoNenhum, EfeitoTreinadorRegistro,
│                 TipoAlvo, ParametrosEfeito
├── fabrica/    → CartaPokemonFactory
└── ui/         → AppGUI, App, CartaVisual, SorteioMoeda
```

## 🎮 Como rodar

Pré-requisitos: **JDK 17+** instalado. Rode os comandos a partir da pasta `Projeto - Teste` (a que contém as pastas `modelo/`, `efeitos/`, `fabrica/` e `ui/`).

```bash
javac -encoding UTF-8 -d bin modelo/*.java efeitos/*.java fabrica/*.java ui/*.java
java -cp bin ui.AppGUI
```

O `-d bin` faz o `javac` colocar os arquivos compilados (`.class`) na pasta `bin`, separados do código-fonte. Por isso o `java` precisa do `-cp bin`, que diz onde procurar as classes. A pasta `bin` não vai para o GitHub.

> ⚠️ O `-encoding UTF-8` é obrigatório — sem ele, os acentos podem aparecer corrompidos dependendo da configuração do Windows.
>
> ⚠️ Sempre que alterar algum `.java`, rode o `javac` de novo. Se o jogo continuar igual, apague a pasta `bin` (`Remove-Item -Recurse bin` no PowerShell) e compile de novo — quando a compilação falha, o `java` roda a versão velha que ficou compilada.
>
> ⚠️ Como as classes agora estão em pacotes, o comando para rodar mudou de `java AppGUI` para **`java ui.AppGUI`** (com o nome do pacote na frente).

Também existe uma versão simplificada em **terminal** (`App.java`), mantida como o protótipo original:

```bash
javac -encoding UTF-8 -d bin modelo/*.java efeitos/*.java fabrica/*.java ui/*.java
java -cp bin ui.App
```

A versão gráfica (`AppGUI`) é a recomendada — tem todas as funcionalidades mais recentes.

---

## 🕹️ Modos de jogo

- **Solo (vs Bot)** — você escolhe seu time, e um Bot monta o time dele automaticamente (sorteando Pokémon aleatórios da PokeAPI) e joga sozinho, seguindo uma lógica simples de decisão (bota Pokémon em campo, evolui, anexa energia, ataca).
- **PvP (2 Jogadores)** — modo "passa e joga", dois treinadores no mesmo computador, cada um escolhendo nome e time.

Em ambos os modos, ao fim da partida você pode **jogar novamente**: com a mesma equipe (revanche idêntica) ou trocando de time (o Bot também sorteia um time novo, se for o caso).

---

## 🃏 Montagem de time

Duas formas de montar seu time:

1. **Times fixos** — Água, Fogo ou Planta, cada um com 4 linhas evolutivas completas + 1 lendário, prontos pra jogar.
2. **Time Customizado** — escolha **qualquer Pokémon da 1ª à 6ª geração** (nº 1 ao 721), buscado em tempo real da [PokeAPI](https://pokeapi.co/). Ao escolher uma evolução, o sistema completa a linha evolutiva inteira sozinho (ex: escolher o Charizard também traz o Charmander e o Charmeleon).

O baralho segue a regra oficial de cópias: até **4 cópias** de um Pokémon Básico e **2 cópias** de uma evolução, num limite de **40 cartas** por baralho (+ 9 cartas de Treinador padrão, sempre as mesmas).

---

## ⚔️ Mecânicas implementadas

O jogo segue (com algumas simplificações) a estrutura oficial de turno do TCG.

**Quem começa:** antes de cada partida, um **cara ou coroa** decide quem joga o primeiro turno, como no TCG oficial. O Jogador 1 escolhe o lado da moeda; se acertar, ele começa, se errar, o adversário (ou o Bot) começa. Quem abre a partida não pode atacar nem evoluir no primeiro turno (as duas ações só valem a partir do Turno 2 da partida). Nenhum dos dois jogadores compra carta no seu primeiro turno, porque ambos já começam com 6 cartas na mão.

Em cada turno:

1. **Compra obrigatória** — 1 carta por turno; baralho vazio = derrota imediata.
2. **Ações livres** (sem limite de vezes, exceto onde indicado):
   - Baixar Pokémon Básicos no Banco
   - Evoluir Pokémon em campo (1x por Pokémon por turno, só a partir do Turno 2)
   - Anexar Energia (1x por turno — energia é **ilimitada**, cada Pokémon tem seu próprio limite de 6 a 8)
   - Usar cartas de Treinador (Poção, Cura Total, Troca, Evolução Rápida)
   - Recuar o Pokémon Ativo (1x por turno, custa 1 energia)
3. **Atacar** — encerra o turno automaticamente. Aplica um triângulo de fraquezas simplificado (Água ⟶ Fogo ⟶ Planta ⟶ Água, dano dobrado).

**Cartas de Treinador:**
| Carta | Efeito |
|---|---|
| 💊 Poção | Cura 30 HP |
| 💖 Cura Total | Restaura o HP por completo |
| 💨 Troca | Recua o Ativo de graça, sem gastar energia |
| ⚡ Evolução Rápida | Evolui na hora, ignorando as restrições de turno |

**Condições de vitória:** o adversário ficar sem cartas para comprar, ou sem nenhum Pokémon em campo.

---

## 🎨 Interface

- Janela em **tela cheia**, com tabuleiro dividido em Zona Ativa, Banco, Baralho e Zona Morta (cemitério dos nocauteados).
- **Cartas com cara de carta**: em vez de botões quadrados, cada carta da mão, do Ativo e do Banco é desenhada pela classe `CartaVisual`:
  - cantos arredondados, sombra e moldura na cor do tipo (Água azul, Fogo laranja, Planta verde, etc.);
  - cabeçalho com nome e HP, **barra de vida** que muda de cor (verde → amarelo → vermelho);
  - ilustração do Pokémon, etiqueta do tipo e etiqueta "Evolução" quando não é Básico;
  - linha do ataque com o dano e **bolinhas de energia** (cheias = anexadas, vazias = espaço livre até o limite);
  - a carta "levanta" quando o mouse passa por cima, e fica apagada quando não pode ser usada;
  - cartas de Treinador e Energia têm um layout próprio, e os espaços vazios do Banco aparecem tracejados.
- Ilustrações reais dos Pokémon, carregadas da PokeAPI (com cache e carregamento em segundo plano, sem travar a tela).
- Notificações do tipo "toast" no canto da tela pra cada evento do jogo (substituem o log fixo, que agora fica disponível sob demanda no botão "Ver Histórico").
- Efeito visual de flash ao atacar.
- **Tela de Cara ou Coroa** antes de cada partida, com uma moeda animada que gira e pula (Pokébola de um lado, estrela dourada do outro), desenhada pela classe `SorteioMoeda`.
- Telas de abertura estilizadas: seleção de modo, nome do treinador e escolha de time, todas com uma Pokébola desenhada vetorialmente (sem depender de imagem externa) e Pokémon desfocados ao fundo.

---

## 🗂️ Estrutura do código

| Arquivo | Responsabilidade |
|---|---|
| `modelo/Carta.java` | Classe abstrata base de qualquer carta |
| `modelo/CartaPokemon.java` | Carta de Pokémon (HP, dano, energia, evolução) |
| `modelo/CartaEnergia.java` | Carta de Energia |
| `modelo/CartaTreinador.java` | Carta de Treinador — guarda a `EfeitoTreinador` (Strategy) correspondente ao seu efeito |
| `modelo/Jogador.java` | Toda a lógica de jogo: baralho, mão, campo, ataque, evolução, regras de turno |
| `ui/App.java` | Versão em terminal (Scanner) — protótipo original |
| `ui/AppGUI.java` | Versão gráfica (Swing) — interface completa, integração com a PokeAPI, IA do Bot |
| `ui/CartaVisual.java` | Desenho de uma carta na tela (herda de `JButton` e sobrescreve `paintComponent`) |
| `ui/SorteioMoeda.java` | Tela de Cara ou Coroa que decide quem começa a partida (herda de `JDialog`) |
| `fabrica/CartaPokemonFactory.java` | **Factory Method** — ponto único de criação de `CartaPokemon` (PokeAPI, times fixos e clonagem) |
| `efeitos/EfeitoTreinador.java` | **Strategy** — interface do efeito de uma carta de treinador |
| `efeitos/EfeitoPocao.java`, `EfeitoCuraTotal.java`, `EfeitoTroca.java`, `EfeitoEvolucaoRapida.java`, `EfeitoNenhum.java` | Estratégias concretas de cada efeito de treinador |
| `efeitos/EfeitoTreinadorRegistro.java` | Resolve, pelo nome, qual `EfeitoTreinador` pertence a cada `CartaTreinador` |
| `efeitos/TipoAlvo.java` | Enum que descreve que tipo de alvo cada efeito precisa (usado pela interface gráfica) |
| `efeitos/ParametrosEfeito.java` | Agrupa os parâmetros (alvo, índice de banco, etc.) coletados pela interface antes de aplicar um efeito |

Nenhuma biblioteca externa é usada — só a biblioteca padrão do Java (`javax.swing`, `java.awt`, `java.net`, `java.io`, `java.util`).

---

## 🏗️ Padrões de Projeto (GoF)

### Strategy — efeitos das cartas de Treinador

Antes: `AppGUI` decidia o que fazer com uma carta de treinador comparando o nome do efeito (`if (carta.getEfeito().equalsIgnoreCase("Poção")) ...`), e `Jogador` tinha um método próprio, com validação duplicada, para cada efeito.

Agora, cada efeito (Poção, Cura Total, Troca, Evolução Rápida) é uma classe que implementa `EfeitoTreinador`. `CartaTreinador` guarda a estratégia correspondente, `Jogador.usarCartaTreinador(...)` virou o único ponto de entrada (que delega para `estrategia.aplicar(...)`), e `AppGUI` decide qual diálogo mostrar consultando `efeito.getTipoAlvoNecessario()` — sem nenhuma comparação de String. Isso torna trivial adicionar um novo efeito de treinador no futuro: basta criar a classe e registrá-la em `EfeitoTreinadorRegistro`, sem tocar em `Jogador` ou `AppGUI`.

### Factory Method — criação de `CartaPokemon`

Antes: `new CartaPokemon(...)` aparecia espalhado por `AppGUI` e `App`, com 4 blocos de código **idênticos** para clonar uma carta (copiar nome, tipo, HP, dano, pré-evolução e número da Pokédex), e a lógica de aplicar HP/dano padrão quando a PokeAPI não retornava esses dados.

Agora, `CartaPokemonFactory` centraliza essa criação em três métodos (`criarDaPokeAPI`, `criarFixo` e `clonar`), eliminando a duplicação e concentrando num único lugar as regras de o que fazer quando os dados vêm incompletos.

---

## 🖌️ Como a `CartaVisual` funciona

`CartaVisual` é uma subclasse de `JButton` que **sobrescreve `paintComponent`** e desenha a carta inteira com `Graphics2D` (formas, gradiente, textos e a imagem do Pokémon), sem usar HTML nem imagens de fundo.

- **Herança**: por ser um `JButton`, ela continua aceitando `addActionListener`, `setEnabled` e `setToolTipText`. Por isso o `AppGUI` só precisou trocar `new JButton(...)` por `new CartaVisual(carta, sprite)` — os cliques e as regras do jogo não mudaram.
- **Polimorfismo**: a mesma classe desenha layouts diferentes conforme o tipo da carta (`CartaPokemon`, `CartaTreinador` ou `CartaEnergia`), usando `instanceof`.
- **Separação de responsabilidades**: todo o código de desenho fica na `CartaVisual`, então o `AppGUI` não cresceu (ficou até menor, porque as linhas de estilização dos botões antigos saíram).
- Os sprites da PokeAPI têm muita margem transparente; a classe recorta essa margem (com cache) e amplia a imagem sem borrar, para a pixel art ficar nítida.

---

## 🪙 Como o `SorteioMoeda` funciona

`SorteioMoeda` é uma subclasse de `JDialog` com um único método público, `sortearQuemComeca(nome1, nome2)`, que abre a tela e devolve `true` se o Jogador 1 começa.

- **Onde é chamado**: no `iniciarPartida` do `AppGUI`, logo antes do primeiro turno. O resultado define quem é o `jogadorAtual` e quem é o `adversario`. Como o "Jogar novamente" também passa pelo `iniciarPartida`, toda revanche tem um sorteio novo.
- **Sorteio**: `Random.nextBoolean()` decide se deu cara ou coroa. O Jogador 1 começa se o lado escolhido for igual ao sorteado.
- **Animação**: um `javax.swing.Timer` redesenha a moeda a cada 16 ms (cerca de 60 quadros por segundo) por 1,8 s. Em cada quadro, a largura da moeda é multiplicada por `|cos(ângulo)|`, o que dá a impressão de que ela gira; o sinal do cosseno decide qual face aparece. O ângulo final é calculado para a moeda parar exatamente na face sorteada, e uma curva de desaceleração faz ela girar rápido no começo e devagar no fim.
- **Diálogo modal**: o `setVisible(true)` de um `JDialog` modal só retorna quando a tela é fechada (`dispose()`), então o `AppGUI` simplesmente espera o resultado, sem precisar de callbacks.
- **Encapsulamento**: o construtor é `private`; o único jeito de usar a classe é pelo método estático, e o `AppGUI` só recebe um `boolean`, sem saber nada da animação.
- As duas faces são desenhadas com `Graphics2D` (sem imagens nem emojis), para funcionar igual em qualquer computador.

---

## 🌐 Integração com a PokeAPI

O jogo consome a [PokeAPI](https://pokeapi.co/) para:
- Buscar nome, tipo, HP, ataque e linha evolutiva de qualquer Pokémon (Gen 1-6)
- Buscar as ilustrações oficiais (via [PokeAPI/sprites](https://github.com/PokeAPI/sprites) no GitHub)

Como não usamos nenhuma biblioteca de JSON, a extração dos dados é feita com expressões regulares simples, escritas à mão.

> Requer conexão com a internet para o modo Customizado, o modo Solo (o Bot busca o time dele também) e para as ilustrações. Sem internet, o jogo continua funcionando normalmente nos times fixos, só sem as imagens.

---

## 📌 Status

Projeto em desenvolvimento ativo — feito em conversas incrementais, adicionando uma funcionalidade de cada vez.
