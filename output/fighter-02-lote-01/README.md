# Lutador 02 — folhas independentes, lote 01

**Atualização:** o lote 02 substitui cinco dos previews restantes. As contagens
abaixo documentam esta etapa; o total atual é de 507 poses nos 37 movimentos,
mais 12 no idle. Ver [`../fighter-02-lote-02/README.md`](../fighter-02-lote-02/README.md).

Fonte: `Downloads/fighter_lote_01_10_imagens.zip`. Os originais não são alterados.
Cada folha é verificada com o SHA-256 do manifesto antes do recorte.

| Sequência | Grelha | Poses |
| --- | --- | ---: |
| idle, guard, crouch, block, low_block | 4 × 3 | 12 cada |
| walk_forward, walk_backward | 4 × 4 | 16 cada |
| jump, jump_side | 6 × 4 | 24 cada |
| lp | 5 × 4 | 20 |

São **160 poses**, sem duplicação/interpolação para atingir contagens fictícias.
Estas dez sequências substituem as versões anteriores; os restantes 28 movimentos
do pack de previews mantêm-se. MP continua a usar a guarda própria e stance o idle.
Total no jogo: 483 poses nos 37 movimentos, mais 12 no idle.

## Processamento

- Remove artefactos vermelhos/amarelos e componentes exteriores à silhueta.
- `walk_forward` usa o alpha fornecido: fundo abaixo de 128 é removido.
- `jump_side` exige segmentação GrabCut: o alpha original deixava todo o fundo branco visível.
- Usa escala comum por ação, referência horizontal fixa e pés na linha 108.
  Agachar não aumenta artificialmente o corpo; saltos não repetem a trajetória
  desenhada, pois o deslocamento é aplicado pela física do jogo.
- Os PNGs do jogo conservam a tela lógica 128 × 112. `frames-hd/` conserva
  os recortes de trabalho em 512 × 448 e `*_preview.png` permite revisão.
- LP usa as poses 1–8 na preparação, 9–10 nos ativos e 11–20 na recuperação.
  É alinhamento visual aproximado; não altera startup, dano, hitboxes ou física.

Ainda pode haver halos, pequenas variações de pose e detalhes cortados no
original, especialmente o punho de LP junto à borda. A limpeza não inventa
partes em falta nem substitui revisão artística manual.

## Reproduzir

Requer Python, Pillow, NumPy e OpenCV. Nesta máquina, OpenCV está isolado em
`~/.local/share/fighter-image-tools`, sem alterações aos pacotes globais:

```powershell
python -m pip install --target "$HOME/.local/share/fighter-image-tools" --no-deps opencv-python-headless
python -B output/fighter-02-lote-01/importar_lote.py "$HOME/Downloads/fighter_lote_01_10_imagens.zip" src/main/resources/assets/textures/fighters/fighter_02
```

Para reconstruir tudo, executar o importador de `fighter-02-pack` **antes**
deste. O lote 01 tem prioridade sobre os previews antigos. O idle deste lote
também tem prioridade sobre `fighter-02-idle`.

`importacao.json` regista fontes, grelhas, contagens, escala, referência e limpeza.
Testes: `LotePrimeiroSpritesTeste`, `PackSegundoLutadorTeste`,
`NovoLutadorSpritesTeste`, `PersonagensCombateTeste` e `SeletorPersonagensTeste`.
