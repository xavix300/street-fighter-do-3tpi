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
| Q | Bloqueio alto |
| S | Bloqueio baixo |
| U / I / O | LP / MP / HP |
| J / K / L | LK / MK / HK |
| F3 | Mostrar ou ocultar pushboxes, hurtboxes e hitboxes |
| Esc | Pausa / menu do FXGL |

## Sistema de frames e caixas

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
ocorre `GUARD BREAK`.

Depois de cada contacto, o centro do HUD apresenta o **frame advantage** do atacante.
Por exemplo, `P1 +5 ON HIT` significa que o jogador recupera cinco frames antes do
oponente; `CPU -11 ON BLOCK` significa que a CPU fica vulnerável durante onze frames.
O valor é calculado a partir do hit-stun/block-stun do CSV menos os frames de
recuperação que ainda faltam na animação do ataque.

Os valores atuais são uma base de jogo e devem ser afinados com F3 depois de os sprites serem recortados para uma tela comum e com a mesma linha de chão. Alterar o CSV muda tempos, dano e caixas sem recompilar.

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

Já há movimento e orientação dinâmica, salto vertical e diagonal, back-to-block, barra de guarda com guard break, frame advantage, bloqueio alto/baixo, seis ataques, vida, hit-stun, block-stun, pushboxes, CPU com três estilos polimórficos, persistência do top 10 e uma camada de depuração das caixas. O protótipo usa silhuetas vetoriais com poses diferentes para tornar cada ação legível enquanto os sprites fotográficos ainda não estão recortados.

Antes de substituir os retângulos pelos frames fotografados, é necessário remover os fundos e normalizar cada sprite para o mesmo canvas. Só assim as caixas de cada frame podem ficar rigorosamente alinhadas com o corpo e com o golpe.
