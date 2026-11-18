# Lutador 02 — cinco folhas aprovadas do lote 02

Fonte: `Downloads/fighter_lote_02_10_imagens.zip`. O utilizador escolheu integrar
**apenas cinco** folhas após revisão do manifesto e das imagens:

| Movimento | Disposição de poses | Poses | Preparação / ativos / recuperação |
| --- | --- | ---: | --- |
| HP | 9 × 2 | 18 | 6 / 4 / 8 |
| LK | 5 × 2 | 10 | 4 / 2 / 4 |
| MK | 5 × 4 | 20 | 7 / 3 / 10 |
| crouch LP | 10 × 2 | 20 | 7 / 4 / 9 |
| crouch LK | 8 × 2 | 16 | 6 / 3 / 7 |

São 84 poses reais. **HK, crouch MP, crouch HP, crouch MK e crouch HK não são
importados**: as folhas misturam pixel art com fotografia e/ou mostram ataques
em pé em vez de agachados. As cinco animações anteriores são preservadas.
MP e stance continuam sem folhas próprias.

O jogo passa a ter 507 poses em 37 movimentos, mais as 12 poses de idle.
Quinze sequências usam folhas independentes; 23 movimentos continuam derivados
dos previews anteriores. O lote 01 não é alterado.

## Limpeza e limites

O importador verifica os SHA-256 do manifesto, remove os halos vermelhos/amarelos
e o fundo branco de HP. As colunas não são uniformes: são separadas por cabeças
detetadas, e crouch LK usa componentes completos da silhueta para não cortar
pés que atravessam as colunas. Não há criação ou interpolação de poses.

A escala é comum por movimento. Crouch LP usa uma referência de corpo em pé
para não ampliar artificialmente o agachamento. As silhuetas são centradas:
faltam âncoras autorais, pelo que o alinhamento horizontal e as fases visuais
são aproximados, não medições de SF6. Halos e imperfeições originais podem
subsistir. Não se alteram dano, tempos, hitboxes ou física.

O jogo usa PNGs transparentes de 128 × 112, chão na linha 108.
`frames-hd/` guarda os recortes de trabalho em 512 × 448;
`*_preview.png` permite a revisão. Os dois PNGs excedentes do LK antigo
são removidos apenas da pasta dessa sequência.

`importacao.json` regista a origem, recortes, escala, contagem e fases. Inclui
também o hash agregado de **todos** os ficheiros fora das cinco pastas aprovadas,
comparado antes e depois da importação.

## Reproduzir

Usa Pillow, NumPy e a instalação isolada de OpenCV descrita no lote 01.
A ordem de reconstrução é: **pack de previews → lote 01 → lote 02**.

```powershell
python -B output/fighter-02-lote-02/importar_lote.py "$HOME/Downloads/fighter_lote_02_10_imagens.zip" src/main/resources/assets/textures/fighters/fighter_02
```

Testes: `LoteSegundoSpritesTeste` verifica as cinco novas contagens e os hashes
das cinco animações excluídas; `PackSegundoLutadorTeste` verifica todos os PNGs.
