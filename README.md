# Street Frame Fighter

Protótipo de um jogo de luta 2D em Java/FXGL, desenvolvido como proposta de Percurso C para a UFCD 0816. É inspirado em jogos de luta por frames, mas usa personagens, fotografias e dados próprios.

## Executar

1. Abrir a pasta que contém o `pom.xml` no IntelliJ IDEA.
2. Definir o Project SDK e Language level para **JDK 21**.
3. Esperar pela sincronização Maven.
4. Executar `StreetFrameApp.main()` ou correr:

   ```text
   mvn clean compile exec:java
   ```

Não criar `module-info.java`: o projeto usa o classpath do FXGL 21.1.

## Controlos

| Tecla | Ação |
| --- | --- |
| A / D | Mover sempre para a esquerda / direita |
| W | Salto vertical |
| A + W / D + W | Salto diagonal para a esquerda / direita |
| Recuar (A ou D) | Bloqueio alto |
| S + recuar (A ou D) | Bloqueio baixo |
| T | Provocação (taunt) |
| U / I / O | LP / MP / HP |
| J / K / L | LK / MK / HK |
| S + U / I / O | LP / MP / HP agachados |
| S + J / K / L | LK / MK / HK agachados |
| Trás + I | Anti-air em pé (MP, 600 de dano) |
| S + trás + I | Anti-air agachado (MP, 600 de dano) |
| W, depois U / I / O | LP / MP / HP no ar |
| W, depois J / K / L | LK / MK / HK no ar |
| A + W / D + W, depois ataque | Soco ou pontapé durante o salto lateral |
| F3 ou H | Mostrar ou ocultar pushboxes, hurtboxes e hitboxes |
| Esc | Pausa / menu do FXGL |

No Windows, comandos compatíveis com XInput podem usar o direcional ou analógico esquerdo;
X/Y/RB são LP/MP/HP, A/B/LB são LK/MK/HK e View/Back é taunt. O suporte usa PowerShell e XInput do Windows,
sem novas dependências Maven; o ficheiro `comando-xinput.ps1` deve permanecer junto ao `pom.xml`.
Outros sistemas e comandos que não exponham XInput não são suportados diretamente.

## Sistema de frames e caixas

Ambos os lutadores começam com **10 000 de vida**. Os ataques usam os danos base
dos normais do [Ryu em Street Fighter 6](https://www.streetfighter.com/6/ja-jp/character/ryu/frame):

| Ataque | Em pé | Agachado |
| --- | ---: | ---: |
| LP | 300 | 300 |
| MP | 600 | 600 |
| HP | 800 | 800 |
| LK | 300 | 200 |
| MK | 700 | 500 |
| HK | 900 | 900 |

São danos base por acerto; o protótipo não aplica os modificadores de counter ou
de redução de combo do SF6. A barra de guarda mantém a escala 0–100.

Nos saltos, LP/LK causam 300, MP 700, MK 500 e HP/HK 800 de dano base,
segundo a mesma tabela oficial do Ryu. Cada salto permite um ataque aéreo;
o golpe conserva a trajetória e termina ao aterrar. Os ataques aéreos exigem
defesa em pé. A animação do salto acompanha a trajetória física na subida,
no topo e na queda, incluindo no regresso depois de um ataque no ar.

`dados/frames.csv` é a fonte de verdade para o combate. Cada linha descreve **uma caixa de um frame visual**:

```text
animation;frame;duration;kind;id;x;y;width;height;damage;hitStun;blockStun
HP;7;3;HIT;attack_1;52;22;45;24;35;16;11
```

- `PUSH`: impede que os dois lutadores se atravessem.
- `HURT`: zona vulnerável do corpo.
- `HIT`: zona que aplica dano, hit-stun e block-stun.

Os lutadores viram-se automaticamente para o adversário, mas os controlos nunca
trocam: `A` é sempre esquerda e `D` é sempre direita. A direção que estiver a afastar
o jogador do adversário conta como recuo e bloqueia golpes altos e médios, como nos
jogos de luta tradicionais; golpes baixos continuam a exigir `S`. Cada bloqueio reduz
a barra `BLOCK`, que recupera quando o lutador deixa de defender. Se chegar a zero
ocorre `GUARD BREAK`. O bloqueio baixo exige `S` e a direção contrária ao adversário; `S` sozinho não defende.
Os seis ataques em pé são golpes médios, defendidos tanto em pé como em baixo.
Os seis ataques agachados usam S com o botão de ataque; os pontapés agachados
exigem bloqueio baixo. S sozinho agacha, mas não defende.

Depois de cada contacto, o centro do HUD apresenta o **frame advantage** do atacante.
Por exemplo, `P1 +5 ON HIT` significa que o jogador recupera cinco frames antes do
oponente; `CPU -11 ON BLOCK` significa que a CPU fica vulnerável durante onze frames.
O valor é calculado a partir do hit-stun/block-stun do CSV menos os frames de
recuperação que ainda faltam na animação do ataque.

Os valores atuais são uma base de jogo e devem ser afinados com F3 depois de os sprites serem recortados para uma tela comum e com a mesma linha de chão. Alterar o CSV muda tempos, dano e caixas sem recompilar.

Os ataques com uma única pose no CSV são divididos pelo carregador em arranque,
frame ativo e recuperação. As sequências com vários frames são prolongadas pelo
carregador em cerca de 50%, incluindo as suas hitboxes e recuperação. O tratamento de impacto foi adaptado a partir do
[Sakuga Engine](https://github.com/NoisyChain/Sakuga-Engine); licença em `SAKUGA_LICENSE.txt`.

## Estrutura principal

```text
src/main/java/pt/aepombal/tpi/ruinas/
├── StreetFrameApp.java       configuração, UI e controlos
├── FabricaEntidades.java     criação da arena e lutadores
├── logica/GestorCombate.java regras de ronda, hitboxes e recordes
├── componentes/              lutador e três comportamentos de IA
└── modelo/                   frames, caixas, enum, record e repositórios
dados/frames.csv              dados de animação e combate por frame
dados/recordes.csv            criado pelo jogo com o top 10
```

## Estado do protótipo

Já há movimento e orientação dinâmica, salto vertical e diagonal, back-to-block, barra de guarda com guard break, frame advantage, bloqueio alto/baixo, seis ataques, vida, hit-stun, block-stun, pushboxes, CPU com três estilos polimórficos, persistência do top 10 e uma camada de depuração das caixas. O protótipo usa sprites fotográficos com recorte automático; as silhuetas vetoriais servem de alternativa se os ficheiros faltarem.

Os recortes e o alinhamento são provisórios, e as caixas ainda precisam de ser afinadas com F3. Alguns golpes têm apenas uma fotografia e não uma sequência de animação completa.
