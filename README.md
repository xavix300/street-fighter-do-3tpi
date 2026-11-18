# Street Frame Fighter

Jogo de luta 2D em Java/FXGL, desenvolvido como proposta de Percurso C para a UFCD 0816.
O alvo mecânico do lutador é **Ryu de Street Fighter 6, sem especiais nem supers**,
mantendo a personagem e as fotografias próprias. O objetivo é reproduzir o feeling
de movimento e combate, não apenas a inspiração visual. A implementação ainda não
é uma reprodução integral: ver [perfil, fontes e diferenças conhecidas](PERFIL_RYU.md).

## Executar

1. Abrir a pasta que contém o `pom.xml` no IntelliJ IDEA.
2. Definir o Project SDK e Language level para **JDK 21**.
3. Esperar pela sincronização Maven.
4. Executar `StreetFrameApp.main()` ou correr:

   ```text
   mvn clean compile exec:java
   ```

Não criar `module-info.java`: o projeto usa o classpath do FXGL 21.1.
O jogo arranca em **ecrã inteiro**. Pode mudar para janela em Opções → Vídeo;
`Esc` abre a pausa sem sair do ecrã inteiro.

## Personagens

No menu **Personagens**, escolhe primeiro **P1** ou **P2/CPU** e confirma o
lutador com clique, Enter ou A/Cruz. A escolha aplica-se ao próximo combate.
Durante uma partida, **Esc / Start / Options → Personagens** permite trocar
imediatamente sem reiniciar a vida, a posição ou o estado da ronda.

O **Lutador 02** usa 10 folhas independentes do lote 01 para idle, guarda,
caminhadas, agachar, bloqueios, saltos e LP. O lote 02 acrescenta folhas próprias
para HP, LK, MK, crouch LP e crouch LK. Os restantes 23 movimentos importados
ainda usam previews de detalhe reduzido. MP usa a guarda própria como recurso;
stance usa o idle. As regras de combate mantêm-se e nunca usa imagens do Lutador 01.
Ver `output/fighter-02-lote-01/README.md`, `output/fighter-02-lote-02/README.md`
e `output/fighter-02-pack/README.md`
para as fontes, limitações e ordem de importação.
As escolhas são mantidas durante a execução do jogo, não guardadas em disco.

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
| Frente + I | Collarbone Breaker (600) |
| Frente + O | Solar Plexus Strike (800) |
| Trás + O | Short Uppercut (800) |
| Trás + L | Axe Kick (400 + 400) |
| Frente + L | Whirlwind Kick (800) |
| Trás + I / S + trás + I | MP normal / MP agachado, sem anti-air personalizado |
| W, depois U / I / O | LP / MP / HP no ar |
| W, depois J / K / L | LK / MK / HK no ar |
| A + W / D + W, depois ataque | Soco ou pontapé durante o salto lateral |
| F3 ou H | Mostrar ou ocultar pushboxes, hurtboxes e hitboxes |
| Esc | Pausa / menu do FXGL |

Frente/trás são relativos ao adversário; A/D continuam a indicar esquerda/direita.
Baixo com qualquer diagonal escolhe o normal agachado. No ar, os seis botões escolhem
os normais aéreos, não os direcionais de chão. Manter W/cima permite repetir o salto
quando o lutador volta a poder agir; cima+baixo é neutralizado.

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

O analógico tem uma zona morta para evitar movimentos involuntários. O jogo procura
nas quatro posições XInput e mantém cada comando selecionado até ser desligado.
São suportados até dois comandos, com atribuições independentes.
O script antigo `comando-xinput.ps1` já não é necessário para executar o jogo.
Outros sistemas não são suportados diretamente.

### Comando PS5 DualSense (Windows)

Ligar por USB ou emparelhar por Bluetooth nas definições do Windows. O jogo lê o
DualSense através do WinMM/JNA, sem precisar de DS4Windows. O comando tem de estar
visível ao Windows como dispositivo de jogo (ver `joy.cpl`); ferramentas que ocultam
o dispositivo físico podem impedir a deteção. A deteção usa os identificadores Sony
do DualSense/DualSense Edge ou o nome «DualSense» / «Wireless Controller».

| Comando PS5 | Ação |
| --- | --- |
| Direcional / analógico esquerdo | Mover, saltar, agachar e defender |
| Quadrado / Triângulo / R1 | LP / MP / HP (socos) |
| Cruz / Círculo / L1 | LK / MK / HK (pontapés) |
| Create | Provocação |

Pode ligar/desligar o comando durante o combate (deteção até cerca de um segundo).
O analógico tem zona morta. Na deteção inicial, os dispositivos XInput têm prioridade;
os restantes lugares podem usar DualSense. Um comando já atribuído não troca de lugar
só porque se liga outro. Desativar a emulação XInput no Steam Input/DS4Windows para
evitar que um DualSense apareça também como um segundo comando virtual.
Vibração, touchpad e gatilhos
adaptativos não estão implementados.

### Menus com comando

| Comando Xbox / PS5 | Ação |
| --- | --- |
| Direcional / analógico esquerdo | Navegar (manter a direção repete) |
| A / Cruz | Confirmar, ativar botões e opções |
| B / Círculo | Voltar / fechar painel |
| Start / Options | Abrir pausa no combate; retomar no menu de pausa |
| Esquerda / direita sobre o volume | Ajustar áudio |

Qualquer um dos dois comandos pode navegar no menu principal, definições,
confirmações e pausa. Teclado: setas, `Enter` / espaço e `Esc`.

### Dois jogadores — versus local

Em **Jogar**, mudar **MODO** para **2 JOGADORES** e selecionar as fontes de
**P1** e **P2** antes de iniciar. São permitidas todas as combinações de fontes
distintas: dois comandos (Xbox, PS5 ou um de cada), teclado + comando, ou dois
esquemas de teclado. O menu impede iniciar com um comando escolhido mas desligado.

| Fonte | Movimento | Socos LP / MP / HP | Pontapés LK / MK / HK | Provocação |
| --- | --- | --- | --- | --- |
| Teclado 1 | W A S D | U I O | J K L | T |
| Teclado 2 | Setas | Num 7 / 8 / 9 | Num 4 / 5 / 6 | Num 0 |

Ativar **Num Lock** para o teclado 2. Pode usar um teclado partilhado ou dois
teclados físicos: o Windows junta as teclas de ambos, pelo que cada jogador tem
de usar o respetivo esquema. Alguns teclados limitam quantas teclas simultâneas
conseguem detetar (ghosting); comandos evitam essa limitação.

No versus, ambos controlam um lutador completo, sem IA. O HUD identifica P1/P2 e
os controlos escolhidos. Os resultados locais não alteram a dificuldade nem os
recordes do modo contra CPU. Depois do K.O., confirmar inicia uma nova partida
com os mesmos controlos; a pausa permite regressar ao menu.
O modo **1 JOGADOR** mantém teclado 1 + comando 1 contra a CPU.

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

`dados/frames.csv` define as caixas e os danos dos movimentos originais.
`NormaisDirecionaisRyu` acrescenta os cinco direcionais, com caixas adaptadas dos
movimentos existentes e dados próprios. Cada linha do CSV descreve **uma caixa de um frame visual**:

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

O movimento distingue avanço (235 px/s) e recuo (160 px/s), conservando a proporção
4,7:3,2 da referência e uma conversão espacial explícita para esta arena.
O salto tem 4f de preparação, 39f no ar em neutro ou 38f na diagonal. Altura e
geometria são adaptações às fotografias, não medidas exatas do SF6.
Os dois anti-airs antigos permanecem como dados de compatibilidade, mas já não têm
comandos do jogador; o anti-air direcional passa a ser trás+HP.

O tratamento de contacto é implementado em Java no próprio projeto:
`GestorCombate` usa um relógio comum de 60 Hz para ambos os lutadores e captura os
contactos antes das reações, permitindo trades. Cada golpe só acerta uma vez;
Axe Kick tem dois grupos de impacto separados. `ReacaoContacto`
define a pausa de 5 frames e o recuo do defensor (90 ao bloquear, 150 ao sofrer
um golpe, durante 9 frames). O motor gráfico e de jogo usado é o FXGL.

Ver `PERFIL_RYU.md` para os direcionais, limites de fidelidade e testes executáveis.

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
