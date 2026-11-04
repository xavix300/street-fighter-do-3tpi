# Frames complementares

PNG individuais com transparência real, tela de 128 × 112 píxeis, personagem virada para a direita e linha de apoio em y=107. As gerações em resolução alta estão nas subpastas `generated-originals`; os PNGs na raiz de cada ação são os frames alinhados para revisão à escala do jogo.

## Sequências

Cada pasta tem seis frames: `01` posição inicial, `02` início, `03` preparação, `04` impacto, `05` recuperação e `06` regresso à posição inicial. Os extremos coincidem para fechar a sequência sem salto entre estados.

| Pasta | Referência de impacto/base | Novos frames |
| --- | --- | --- |
| lp | fighter/lp/lp_001.png | 02, 03, 05 |
| mp | fighter/mp/mp_001.png | 02, 03, 05 |
| lk | fighter/lk/lk_001.png | 02, 03, 05 |
| mk | fighter/hk/hk_001.png | 02, 03, 05 |
| crouch_lp | low_block + LP | 02–05 |
| crouch_mp | low_block + MP | 02–05 |
| crouch_hp | low_block + HP | 02–05 |
| crouch_lk | low_block + LK | 02–05 |
| crouch_mk | low_block + MK | 02–05 |
| crouch_hk | low_block + HK | 02–05 |

Nas quatro ações em pé, os frames 01/06 são cópias exatas do sprite `idle_001.png` e 04 é uma cópia exata do impacto existente. Nenhuma fotografia ou sprite original foi substituído. Nas variantes agachadas, 01/06 reutilizam a imagem existente de defesa baixa, redimensionada proporcionalmente para 70 píxeis de altura e alinhada à mesma linha de chão. Os quatro frames de ataque de cada variante são novas interpretações, autorizadas pelo utilizador, dos golpes existentes; não representam fotografias de ataques agachados presentes no projeto.

## Auditoria das referências

LP, MP e LK tinham apenas uma pose. A ação MK também usa uma única pose: `VistaLutador.carregarSprites()` associa MK à pasta `hk` e HK à pasta `mk`. A nomenclatura desta entrega segue as **ações do jogo**, preservando essa associação atual. Na integração, o carregador passou a usar as novas sequências e o CSV foi normalizado para esses nomes de ação.

HP tem 14 frames; HK utiliza os 12 da pasta `mk`; caminhada para a frente e para trás têm 10 cada; salto tem 12; provocação tem 10; vitória tem 14. Estas sequências foram revistas visualmente e preservadas. Não se acrescentaram poses especulativas a sequências já completas. `existing-review.png` mostra amostras distribuídas ao longo dessas ações. Não foram encontrados vídeos no repositório.

## Geração e verificação

Ferramenta utilizada: `image_gen` integrada, com fundo transparente. Os conjuntos de prompts estão em `prompts.json` e `crouch-prompts.json`. O primeiro frame gerado (`lp_02`) usou os sprites idle/LP como referências, pedindo o primeiro quarto do trajeto do jab, com o cotovelo a começar a estender e a mão de trás junto ao peito. Os restantes usam as fotografias WebP originais como referência de identidade e movimento.

`prepare-frames.ps1` guarda os PNGs gerados antes de os redimensionar proporcionalmente e alinhar. Não aplica deformação, mistura de fotografias nem efeito de arrasto. `review-frames.ps1` verifica a presença dos 60 frames, dimensões e transparência, escreve os hashes SHA-256 em `manifest.json` e monta `sequences-review.png` para revisão visual.

Os PNGs estão agora integrados em `src/main/resources/assets/textures/fighter/sequences/`.
O jogo carrega as quatro sequências em pé e as seis agachadas. S + ataque seleciona
a variante agachada; S sozinho mantém a pose baixa sem bloquear. As hitboxes
ofensivas dos novos ataques só ficam ativas no frame 04. Os tempos totais dos
quatro ataques em pé foram preservados e o CSV passou a identificar MK/HK pelas
ações do jogo, sem a troca antiga no leitor. As fotografias de origem continuam
intactas. Os pontapés agachados exigem defesa baixa.

A integração foi verificada por compilação Maven, `AnimacoesTeste` (recursos de
todas as ações, seis fases, impacto, tempos e defesa baixa) e `RegraDefesaTeste`.
