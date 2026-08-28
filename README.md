# Pokémon TCG — Java Edition

Um jogo de cartas colecionáveis (TCG) inspirado no card game oficial de Pokémon, feito inteiramente em **Java puro** (sem bibliotecas externas), com interface gráfica em **Swing** e integração em tempo real com a **PokeAPI**.

Projeto desenvolvido como exercício de Programação Orientada a Objetos, evoluindo de um protótipo em terminal (Scanner) até um jogo gráfico completo, com modo PvP e modo Solo contra um Bot.

---

## 🎮 Como rodar

Pré-requisitos: **JDK 17+** instalado.

```bash
javac -encoding UTF-8 *.java
java AppGUI
```

> ⚠️ O `-encoding UTF-8` é obrigatório — sem ele, os emojis e acentos podem aparecer corrompidos dependendo da configuração do Windows.

Também existe uma versão simplificada em **terminal** (`App.java`), mantida como o protótipo original:

```bash
javac -encoding UTF-8 *.java
java App
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

O jogo segue (com algumas simplificações) a estrutura oficial de turno do TCG:

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
- Ilustrações reais dos Pokémon, carregadas da PokeAPI (com cache e carregamento em segundo plano, sem travar a tela).
- Notificações do tipo "toast" no canto da tela pra cada evento do jogo (substituem o log fixo, que agora fica disponível sob demanda no botão "Ver Histórico").
- Efeito visual de flash ao atacar.
- Telas de abertura estilizadas: seleção de modo, nome do treinador e escolha de time, todas com uma Pokébola desenhada vetorialmente (sem depender de imagem externa) e Pokémon desfocados ao fundo.

---

## 🗂️ Estrutura do código

| Arquivo | Responsabilidade |
|---|---|
| `Carta.java` | Classe abstrata base de qualquer carta |
| `CartaPokemon.java` | Carta de Pokémon (HP, dano, energia, evolução) |
| `CartaEnergia.java` | Carta de Energia |
| `CartaTreinador.java` | Carta de Treinador (Poção, Troca, etc) |
| `Jogador.java` | Toda a lógica de jogo: baralho, mão, campo, ataque, evolução, regras de turno |
| `App.java` | Versão em terminal (Scanner) — protótipo original |
| `AppGUI.java` | Versão gráfica (Swing) — interface completa, integração com a PokeAPI, IA do Bot |

Nenhuma biblioteca externa é usada — só a biblioteca padrão do Java (`javax.swing`, `java.awt`, `java.net`, `java.io`, `java.util`).

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
