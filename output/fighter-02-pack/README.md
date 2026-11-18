# Pack provisório do segundo lutador

**Atualização:** nove destes movimentos e o idle foram substituídos por folhas
independentes do lote 01. Este diretório documenta a importação inicial; os seus
442 recortes não representam a contagem atual. Após os lotes 01 e 02, o jogo tem
507 poses nos 37 movimentos e mais 12 no idle. Outros 23 movimentos continuam provisórios.
Ver [`../fighter-02-lote-01/README.md`](../fighter-02-lote-01/README.md).
Ao reconstruir os recursos, importar este pack **primeiro** e o lote 01 **depois**,
para não voltar a substituir as novas folhas pelos previews antigos. O
[`lote 02`](../fighter-02-lote-02/README.md) deve ser importado por último.

Fonte: `Downloads/fighter_partial_pack.zip`, que contém `overview_preview.png`
e recortes RGB de baixa resolução. O utilizador autorizou explicitamente
continuar com estes previews apesar das limitações e da ausência de MP/stance.

Importar, a partir da raiz do projeto (Python + Pillow):

```powershell
python -B output/fighter-02-pack/importar_pack.py "$HOME/Downloads/fighter_partial_pack.zip" src/main/resources/assets/textures/fighters/fighter_02
```

O importador lê apenas o manifesto e os PNGs indicados; não executa conteúdos
do ZIP. Remove cabeçalhos, contornos e fundo axadrezado, identifica poses pela
posição da cabeça e separa-as em PNGs transparentes de 128 × 112. A queda usa
silhuetas separadas e conserva só a última pose do trecho com corpos sobrepostos.
As poses que se tocam na imagem original podem perder detalhes nas separações.
Não há interpolação nem criação de poses para atingir os números dos títulos.

`importacao.json` regista as contagens **efetivamente recortadas** e a escala:
37 movimentos, 442 poses, além das 12 poses do idle anteriormente integrado.
`preview.png` mostra 12 amostras por movimento para revisão, podendo repetir
poses ou omitir amostras intermédias se a sequência tiver outra contagem.
Essa pré-visualização não é a sequência usada no jogo.

O idle anterior de 12 poses é preservado. São importados os outros 37
movimentos disponíveis. MP usa a guarda própria como imagem de recurso;
stance usa o idle próprio. Nunca se usam imagens do primeiro lutador.

As sequências são ampliadas a partir de miniaturas: continuam com detalhe
reduzido, possíveis halos e imperfeições anatómicas. Não são o pack final de
512 × 512 nem uma reprodução visual exata de Ryu. A limpeza automática
também não substitui revisão artística manual.

A apresentação é remapeada para os frames lógicos já existentes. Ataques
usam uma divisão visual aproximada em preparação/ativos/recuperação. Os
tempos, dano, hitboxes, hitstop e física não são alterados pelo pack. A
cronologia de apresentação avança só quando a simulação deixa de estar congelada.
Os saltos usam poses relativas ao corpo; a trajetória é aplicada pela física,
não repetida a partir da altura desenhada na miniatura.

Testes: `PackSegundoLutadorTeste`, `PersonagensCombateTeste`,
`SeletorPersonagensTeste`, `RyuIntegracaoTeste`.
