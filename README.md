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

### Comando Xbox (Windows)

Suporte nativo através do XInput do Windows e JNA, sem PowerShell nem programas de
remapeamento. Basta ligar o comando por USB, Bluetooth ou adaptador Xbox e iniciar
o combate. Também pode ser ligado ou desligado durante o jogo; o teclado continua disponível.

| Comando | Ação |
| --- | --- |
| Direcional / analógico esquerdo | Mover, saltar (cima), agachar (baixo) e defender (recuar) |
| X / Y / RB | LP / MP / HP (socos) |
| A / B / LB | LK / MK / HK (pontapés) |
| View / Back | Provocação |

O analógico tem uma zona morta para evitar movimentos involuntários. O jogo usa um
comando para o jogador 1, procurando nas quatro posições XInput, e mantém o comando
selecionado até ser desligado. Pausa e menus continuam a usar o teclado (`Esc`).
O script antigo `comando-xinput.ps1` já não é necessário para executar o jogo.
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

São danos base por acerto; os counters aplicam os bónus descritos abaixo.
O protótipo ainda não aplica redução de dano por combo. A barra de guarda mantém a escala 0–100.

Nos saltos, LP/LK causam 300, MP 700, MK 500 e HP/HK 800 de dano base,
segundo a mesma tabela oficial do Ryu. Cada salto permite um ataque aéreo;
o golpe conserva a trajetória e termina ao aterrar. Os ataques aéreos exigem
defesa em pé. A animação do salto acompanha a trajetória física na subida,
no topo e na queda, incluindo no regresso depois de um ataque no ar.

`dados/frames.csv` define as caixas e os danos. Cada linha descreve **uma caixa de um frame visual**:

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

As caixas atuais são uma base de jogo e devem ser afinadas com F3 depois de os sprites serem recortados para uma tela comum e com a mesma linha de chão. Alterar o CSV muda dano e caixas sem recompilar; os tempos de ataque vêm de `TemposRyu.java`.

### Tempos dos ataques — Ryu de Street Fighter 6

`modelo/TemposRyu.java` fixa os tempos segundo a [tabela oficial da Capcom](https://www.streetfighter.com/6/character/ryu/frame),
consultada em 04/11/2026. O combate avança em passos de **1/60 s**, independentemente
do FPS de desenho. Não se aplica o antigo multiplicador de 1,5 aos ataques.
Arranque indica o primeiro frame ativo: um golpe de 4f tem 3f de preparação.

| Normal | Arranque | Ativos | Recuperação |
| --- | ---: | ---: | ---: |
| LP / LK | 4 / 5 | 3 / 3 | 7 / 11 |
| MP / MK | 6 / 9 | 4 / 3 | 11 / 18 |
| HP / HK | 10 / 12 | 5 / 4 | 18 / 20 |
| Agachado LP / LK | 4 / 5 | 2 / 2 | 9 / 10 |
| Agachado MP / MK | 6 / 8 | 4 / 3 | 13 / 19 |
| Agachado HP / HK | 9 / 9 | 6 / 3 | 22 / 23 |
| Aéreo LP / LK | 4 / 6 | 10 / 10 | Até aterrar + 3f |
| Aéreo MP / MK | 8 / 7 | 5 / 6 | Até aterrar + 3f |
| Aéreo HP / HK | 9 / 10 | 6 / 8 | Até aterrar + 3f |

MP em pé e agachado recuperam mais 2f quando falham. As poses existentes são
redistribuídas pelas fases; ter 40 fotografias não significa que o golpe dure 40f.
As vantagens de hit/block dos normais de chão também ajustam o stun, sem o antigo
mínimo artificial de block-stun. A cadeia universal leve → médio → forte foi retirada;
mantém-se rapid cancel dos leves compatíveis, sem adicionar especiais.

Limites: as caixas, hitstop, trajetória de salto e sistema de guarda continuam a ser
os deste protótipo, não uma reprodução integral de SF6. O HK agachado ainda usa hit-stun,
não o knockdown do original; os stuns aéreos continuam próprios do jogo. Os dois
anti-airs personalizados usam os tempos do HP agachado, preservando o dano de 600.

O tratamento de contacto é implementado em Java no próprio projeto:
`GestorCombate` resolve o primeiro contacto de cada ataque e `ReacaoContacto`
define a pausa de 5 frames e o recuo do defensor (90 ao bloquear, 150 ao sofrer
um golpe, durante 9 frames). O motor gráfico e de jogo usado é o FXGL.

### Animação visual do MP

O MP usa agora 20 poses da spritesheet fornecida pelo utilizador: 5 de preparação,
4 ativas e 11 de recuperação. A animação normal ocupa 20 frames a 60 passos/s,
sem alterar o dano, as hitboxes ou os frames extra ao falhar.
O hitstop continua a congelar ambos os lutadores no contacto.
Os PNGs e `animacao.properties` estão em
`src/main/resources/assets/textures/fighter/attacks/mp/`.
Cada golpe pode definir a sua própria quantidade de poses por fase, sem exigir
40 imagens. Os golpes ainda não convertidos mantêm as sequências anteriores.
O original, o script de recorte e a imagem de revisão estão em `output/mp-fluid/`.

### Counter Hit, Punish Counter e Whiff Punish

Não exigem botões especiais: dependem do estado do defensor no momento do contacto.
As mesmas regras aplicam-se ao jogador e à CPU.

| Situação do defensor | Resultado | Dano | Hit-stun |
| --- | --- | --- | --- |
| Livre ou já em hit-stun | Acerto normal | Base | Base |
| Preparação ou fase ativa de um ataque | COUNTER HIT | Base × 1,2 | Base + 2f |
| Recuperação de um ataque que acertou ou foi bloqueado | PUNISH COUNTER | Base × 1,2 | Base + 4f |
| Recuperação de um ataque que não tocou no adversário | WHIFF PUNISH / PUNISH COUNTER | Base × 1,2 | Base + 4f |

O whiff punish é um Punish Counter identificado como punição de um golpe falhado;
os dois bónus não acumulam. Antes de terminar a fase ativa, um golpe ainda pode
acertar e não é considerado whiff. Os frames extra de recuperação de MP/cr.MP
falhados e de aterragem após ataque também impedem defesa e podem ser punidos.
O HUD mostra o tipo de acerto até ao contacto seguinte e a vantagem usa o stun efetivo.
Bloqueios e guard break não recebem os bónus de counter.

São regras comuns inspiradas no SF6, não uma reprodução integral: ainda não há
reações específicas por golpe (crumple, launch ou knockdown de Punish Counter),
Drive, throws ou scaling de combos. O startup, os frames ativos, a recuperação
e as animações dos ataques não foram alongados.

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
