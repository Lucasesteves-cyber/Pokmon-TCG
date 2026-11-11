# Pokémon TCG — Java Edition

Um jogo de cartas colecionáveis (TCG) inspirado no card game oficial de Pokémon, feito inteiramente em **Java puro** (sem bibliotecas externas), com interface gráfica em **Swing** e integração em tempo real com a **PokeAPI**.

Projeto desenvolvido como exercício de Programação Orientada a Objetos, evoluindo de um protótipo em terminal (Scanner) até um jogo gráfico completo, com modo PvP e modo Solo contra um Bot.

---

## 📁 Estrutura de pastas (pacotes)

O código é organizado em 8 pacotes Java, por responsabilidade, e os testes ficam numa pasta separada com os mesmos pacotes:

```
Projeto - Teste/
├── modelo/        → Carta, CartaPokemon, CartaEnergia, CartaTreinador, Jogador, TabelaDeTipos
├── efeitos/       → EfeitoTreinador (Strategy) e as estratégias concretas, EfeitoTreinadorRegistro,
│                    TipoAlvo, ParametrosEfeito
├── eventos/       → OuvinteJogo (Observer), EventoJogo, TipoEvento, OuvinteConsole
├── excecoes/      → JogoException, JogadaInvalidaException, BaralhoVazioException,
│                    PokeApiException, PersistenciaException
├── fabrica/       → CartaPokemonFactory, BaralhosPadrao, TimeFixo
├── persistencia/  → HistoricoPartidas, ResultadoPartida (leitura e escrita de arquivo)
├── servicos/      → PokeApiCliente (acesso à PokeAPI)
├── ui/            → AppGUI, App, CartaVisual, SorteioMoeda
├── teste/         → testes JUnit 5 (teste/modelo, teste/efeitos, teste/fabrica, ...)
├── lib/           → junit-platform-console-standalone-1.11.4.jar (só para os testes)
├── rodar.bat          → compila e abre o jogo (Windows)
└── rodar-testes.bat   → compila e roda os testes (Windows)
```

## 🎮 Como rodar

Pré-requisitos: **JDK 17+** instalado. Rode tudo a partir da pasta `Projeto - Teste`.

**Jeito mais fácil (Windows):** dê dois cliques em `rodar.bat`.

**Pelo terminal:**

```bash
javac -encoding UTF-8 -d bin modelo/*.java efeitos/*.java eventos/*.java excecoes/*.java fabrica/*.java persistencia/*.java servicos/*.java ui/*.java
java -cp bin ui.AppGUI
```

O `-d bin` faz o `javac` colocar os arquivos compilados (`.class`) na pasta `bin`, separados do código-fonte. Por isso o `java` precisa do `-cp bin`, que diz onde procurar as classes. A pasta `bin` não vai para o GitHub.

> ⚠️ O `-encoding UTF-8` é obrigatório — sem ele, os acentos podem aparecer corrompidos dependendo da configuração do Windows.
>
> ⚠️ Sempre que alterar algum `.java`, compile de novo. Se o jogo continuar igual, apague a pasta `bin` (`Remove-Item -Recurse bin` no PowerShell) e compile de novo — quando a compilação falha, o `java` roda a versão velha que ficou compilada.

Também existe uma versão simplificada em **terminal** (`App.java`), mantida como o protótipo original. Ela usa as mesmas regras, exceções e eventos da versão gráfica:

```bash
java -cp bin ui.App
```

O histórico de partidas é gravado em `dados/historico_partidas.csv`, dentro da pasta de onde o jogo foi aberto (a pasta é criada sozinha).

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
| `modelo/CartaTreinador.java` | Carta de Treinador — guarda a `EfeitoTreinador` (Strategy) do seu efeito |
| `modelo/Jogador.java` | Regras da partida (baralho, mão, campo, ataque, evolução, recuo). Lança exceções nas jogadas inválidas e avisa os ouvintes (Observer) a cada evento |
| `modelo/TabelaDeTipos.java` | Triângulo de fraquezas (Água → Fogo → Planta → Água) |
| `efeitos/EfeitoTreinador.java` | **Strategy** — interface do efeito de uma carta de treinador |
| `efeitos/EfeitoPocao.java`, `EfeitoCuraTotal.java`, `EfeitoTroca.java`, `EfeitoEvolucaoRapida.java`, `EfeitoNenhum.java` | Estratégias concretas de cada efeito |
| `efeitos/EfeitoTreinadorRegistro.java` | Resolve, pelo nome, qual estratégia pertence a cada `CartaTreinador` |
| `efeitos/TipoAlvo.java`, `ParametrosEfeito.java` | Que alvo cada efeito precisa e os alvos escolhidos pelo jogador |
| `eventos/OuvinteJogo.java` | **Observer** — interface de quem quer ser avisado dos eventos da partida |
| `eventos/EventoJogo.java`, `TipoEvento.java` | O evento (tipo + mensagem) enviado aos ouvintes |
| `eventos/OuvinteConsole.java` | Ouvinte que imprime os eventos no terminal (usado pelo `App`) |
| `excecoes/*.java` | Exceções próprias do jogo (ver seção abaixo) |
| `fabrica/CartaPokemonFactory.java` | Ponto único de criação de `CartaPokemon` (PokeAPI, times fixos e cópias) |
| `fabrica/BaralhosPadrao.java`, `TimeFixo.java` | Times prontos (Água, Fogo, Planta), cartas de treinador e prêmios |
| `persistencia/HistoricoPartidas.java` | Grava e lê o histórico de partidas em arquivo CSV |
| `servicos/PokeApiCliente.java` | Busca dados na PokeAPI e converte falhas em `PokeApiException` |
| `ui/AppGUI.java` | Versão gráfica (Swing): telas, tabuleiro e IA do Bot |
| `ui/App.java` | Versão em terminal (Scanner) — protótipo original |
| `ui/CartaVisual.java` | Desenho de uma carta na tela (herda de `JButton` e sobrescreve `paintComponent`) |
| `ui/SorteioMoeda.java` | Tela de Cara ou Coroa que decide quem começa a partida (herda de `JDialog`) |

Nenhuma biblioteca externa é usada no jogo — só a biblioteca padrão do Java. O JUnit (em `lib/`) é usado apenas para rodar os testes.

---

## 🏗️ Padrões de Projeto (GoF)

O projeto aplica **dois padrões do catálogo GoF**: Strategy e Observer. Os dois resolvem problemas que o código realmente tinha antes da refatoração.

### 1. Strategy — efeitos das cartas de Treinador

**Onde:** `efeitos/EfeitoTreinador.java` (interface) e `EfeitoPocao`, `EfeitoCuraTotal`, `EfeitoTroca`, `EfeitoEvolucaoRapida`, `EfeitoNenhum` (estratégias concretas). O *contexto* é a `CartaTreinador`, que guarda a sua estratégia, e o `Jogador.usarCartaTreinador(...)`, que a executa.

**Problema que resolve:** antes, o `AppGUI` decidia o que fazer com uma carta de treinador comparando o nome do efeito (`if (carta.getEfeito().equalsIgnoreCase("Poção")) ... else if ...`), e o `Jogador` tinha um método para cada efeito (`usarPocao`, `usarTroca`, ...), todos repetindo a mesma validação. Para criar uma carta nova era preciso mexer nas duas classes.

**Por que o Strategy é adequado:** cada carta é uma variação do *mesmo* comportamento ("aplicar um efeito"), escolhida em tempo de execução. Com o Strategy, cada efeito vira uma classe com o método `aplicar(...)`, e:
- `Jogador.usarCartaTreinador(...)` é o único ponto de entrada e só delega para `estrategia.aplicar(...)`;
- a interface descobre qual diálogo abrir por `efeito.getTipoAlvoNecessario()`, sem nenhuma comparação de String (tanto no `AppGUI` quanto no `App` de terminal);
- adicionar um efeito novo é só criar a classe e registrá-la em `EfeitoTreinadorRegistro`, sem tocar em `Jogador` ou `AppGUI` (princípio aberto/fechado).

O `EfeitoNenhum` é um *Null Object*: a carta Troféu tem uma estratégia que só explica que ela não tem efeito, e assim ninguém precisa testar `null`.

### 2. Observer — eventos da partida

**Onde:** `eventos/OuvinteJogo.java` (Observer), `modelo/Jogador.java` (Subject: `adicionarOuvinte`, `removerOuvinte`, `notificar`), `eventos/EventoJogo.java` (o que é enviado). Ouvintes concretos: a `AppGUI` (método `aoEventoDoJogo`), o `OuvinteConsole` do `App` de terminal e as listas usadas nos testes.

**Problema que resolve:** antes, as regras (`Jogador` e os efeitos) faziam `System.out.println(...)` para tudo, e a interface gráfica "sequestrava" o console com `System.setOut(...)` para transformar o texto em notificações. Isso deixava as regras presas ao console, impedia testar se um evento aconteceu e ainda **quebrava acentos e emojis**: o `PrintStream` falso convertia cada *byte* do UTF-8 num caractere separado.

**Por que o Observer é adequado:** é uma relação um-para-muitos (um jogador, vários interessados) em que o jogador não deve saber *quem* está ouvindo. Agora o `Jogador` só chama `notificar(TipoEvento.ATAQUE, "...")`, e cada ouvinte decide o que fazer:
- a interface gráfica mostra a notificação e grava no histórico da partida (e esconde do humano qual carta o Bot comprou, usando o `TipoEvento.COMPRA`);
- o terminal imprime a linha;
- os testes guardam os eventos numa lista e conferem a ordem.

Nenhuma dessas mudanças exige alterar o `Jogador`.

### Recurso complementar (não-GoF): `CartaPokemonFactory`

`fabrica/CartaPokemonFactory` centraliza a criação de `CartaPokemon` (dados da PokeAPI com valores padrão, times fixos e cópias), eliminando 4 blocos de código idênticos que existiam antes. Ela é uma **Simple Factory** (classe com métodos estáticos), e **não** o *Factory Method* do GoF, que exige uma hierarquia de criadores com subclasses. Por isso ela não é contada como um dos padrões GoF do projeto.

---

## 🚨 Tratamento de exceções

As regras do jogo não imprimem mensagens nem devolvem `false` quando algo dá errado: elas **lançam exceções específicas**, com mensagens pensadas para aparecer na tela.

| Exceção | Tipo | Quando acontece | Como é tratada |
|---|---|---|---|
| `JogoException` | não verificada (`RuntimeException`) | Base de todas as violações de regra | — |
| `JogadaInvalidaException` | não verificada | Jogada proibida: atacar no Turno 1, 2ª energia na rodada, evolução errada, Banco cheio, posição inexistente... | A interface mostra a mensagem num único ponto (`AppGUI.executarJogada`). Para o Bot, a jogada é só registrada no histórico (`tentarJogadaDoBot`) |
| `BaralhoVazioException` | não verificada | O jogador precisa comprar e o baralho acabou | Encerra a partida com derrota de quem ficou sem cartas |
| `PokeApiException` | **verificada** (`Exception`) | Sem internet, tempo esgotado, Pokémon inexistente, resposta inesperada | Mostra "verifique sua internet" no montador de time; no sorteio do Bot, o Pokémon é pulado |
| `PersistenciaException` | **verificada** | Erro ao ler/gravar o histórico | Mostra o aviso na tela de fim de jogo, sem impedir de jogar de novo |

**Por que umas são verificadas e outras não?** Jogada inválida é erro do jogador e pode acontecer em qualquer ação, então obrigar `throws` em todo método só poluiria o código: a interface trata tudo num lugar só. Já rede e disco são falhas externas e previsíveis, e o compilador obriga quem chama a decidir o que fazer.

Exemplos de mensagens:
- `Charmeleon não evolui de Squirtle! Ele evolui de Charmander.`
- `Você já anexou uma Energia nesta rodada! Só é permitida 1 por rodada.`
- `Não foi possível falar com a PokeAPI. Verifique sua conexão com a internet.`

Os `catch (Exception e) { }` vazios que existiam na interface foram trocados por exceções específicas (`IOException`, `PokeApiException`, ...), e cada um explica num comentário por que a falha pode ser ignorada (por exemplo: sem internet, o jogo segue sem as imagens). No terminal, letras digitadas onde se espera número (`InputMismatchException`) pedem a resposta de novo em vez de fechar o programa.

---

## 🧺 Coleções utilizadas

| Coleção | Onde | Por quê |
|---|---|---|
| `List<Carta>` / `ArrayList` | Baralho, mão, Banco, zona morta e prêmios do `Jogador` | Ordem importa (topo do baralho, posição no Banco) e o acesso é por índice |
| `List<OuvinteJogo>` | Ouvintes do `Jogador` | Ordem de inscrição; a lista é copiada antes de notificar, para um ouvinte poder se remover durante o aviso |
| `Map<String, EfeitoTreinador>` / `HashMap` | `EfeitoTreinadorRegistro` | Busca da estratégia pelo nome do efeito |
| `Map<String, String>` (`Map.of`) | `TabelaDeTipos`, tradução de tipos na `PokeApiCliente` | Tabelas fixas consultadas por chave, sem cadeias de `if` |
| `LinkedHashMap<String, Integer>` | Placar em `HistoricoPartidas.vitoriasPorJogador()` | Conta vitórias por nome e mantém a ordem do ranking |
| `Set<TimeFixo>` (`EnumSet`) | Times ainda disponíveis na escolha | Cada time só pode ser escolhido uma vez; `remove` já diz se estava disponível |
| `Set<Integer>` (`HashSet`) | `PokeApiCliente.buscarLinhaEvolutiva` | Evita laço infinito caso a API devolva uma cadeia circular |
| `Map`/`Set` de cache | Sprites e tipos na `AppGUI` | Não baixar duas vezes a mesma imagem |

---

## 💾 Leitura e escrita de arquivos

Ao fim de cada partida, o resultado é **gravado** em `dados/historico_partidas.csv`:

```
data_hora;vencedor;perdedor;modo;turnos;motivo
2026-11-11T10:42:13;Renato;Bot;Solo;14;Conquistou os 2 prêmios!
```

Em seguida o arquivo é **lido** de volta para mostrar o placar geral na tela de fim de jogo (`📊 Placar geral (5 partidas salvas): Renato 3 vitória(s) • Bot 2 vitória(s)`).

Detalhes da implementação (`persistencia/HistoricoPartidas.java`):
- usa `java.nio.file.Files` com `BufferedWriter`/`BufferedReader` em **UTF-8** e *try-with-resources* (o arquivo sempre é fechado);
- cria a pasta `dados/` e o cabeçalho na primeira gravação, e depois só acrescenta linhas (`APPEND`);
- linhas corrompidas (alguém editou o arquivo à mão) são **puladas e contadas**, sem derrubar o jogo;
- `;` e quebras de linha nos nomes são removidos para não estragar o CSV;
- qualquer `IOException` vira `PersistenciaException` com o caminho do arquivo na mensagem.

O projeto também lê dados da internet (PokeAPI) com `BufferedReader` + `InputStreamReader`, em `servicos/PokeApiCliente.java`.

---

## ✅ Testes (JUnit 5)

São **55 testes** em `teste/`, organizados nos mesmos pacotes do código. Cada classe e cada teste têm um `@DisplayName` em português explicando o que é verificado.

| Classe de teste | O que cobre |
|---|---|
| `modelo/JogadorTest` | Colocar Pokémon em campo, limites do Banco e de energia, ataque e super efetividade, nocaute e prêmio, evolução (energia e dano mantidos), recuo, baralho vazio e os eventos do Observer |
| `modelo/CartaPokemonTest` | Dano, cura, nocaute, limite de energias e validação do construtor |
| `modelo/TabelaDeTiposTest` | Triângulo de fraquezas |
| `efeitos/EfeitosTreinadorTest` | Cada estratégia do Strategy (Poção, Cura Total, Troca, Evolução Rápida, Troféu) e o registro |
| `fabrica/FabricaEBaralhosTest` | Valores padrão da fábrica, cópia independente, coerência dos 3 times fixos (40 cartas e toda evolução com sua pré-evolução) |
| `persistencia/HistoricoPartidasTest` | Gravar e ler, cabeçalho único, linha corrompida, placar e erro de disco (usa uma pasta temporária, não mexe no histórico real) |
| `servicos/PokeApiClienteTest` | Leitura do JSON da PokeAPI sem acessar a internet e validação do número da Pokédex |

**Como rodar os testes (Windows):** dê dois cliques em `rodar-testes.bat`.

**Pelo terminal** (na pasta `Projeto - Teste`; no Linux/Mac troque `\` por `/`):

```bash
javac -encoding UTF-8 -d bin -cp lib\junit-platform-console-standalone-1.11.4.jar modelo\*.java efeitos\*.java eventos\*.java excecoes\*.java fabrica\*.java persistencia\*.java servicos\*.java ui\*.java teste\modelo\*.java teste\efeitos\*.java teste\fabrica\*.java teste\persistencia\*.java teste\servicos\*.java
java -jar lib\junit-platform-console-standalone-1.11.4.jar execute --class-path bin --scan-class-path
```

> O arquivo `lib/junit-platform-console-standalone-1.11.4.jar` já vem no repositório. Se ele não estiver lá, baixe em
> https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar
> e salve na pasta `lib/`.

---

## 📚 Javadoc

Todas as classes e métodos públicos têm Javadoc. Para gerar a documentação em HTML (na pasta `docs/`, que não vai para o GitHub):

```bash
javadoc -encoding UTF-8 -charset UTF-8 -d docs -sourcepath . -subpackages modelo:efeitos:eventos:excecoes:fabrica:persistencia:servicos:ui
```

Depois é só abrir `docs/index.html` no navegador.

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

Todo o acesso fica em `servicos/PokeApiCliente.java`. Como não usamos nenhuma biblioteca de JSON, a extração dos dados é feita com expressões regulares simples, escritas à mão (e testadas em `PokeApiClienteTest`).

> Requer conexão com a internet para o modo Customizado, o modo Solo (o Bot busca o time dele também) e para as ilustrações. Sem internet, o jogo continua funcionando normalmente nos times fixos, só sem as imagens.

