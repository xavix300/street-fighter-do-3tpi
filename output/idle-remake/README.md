# Novo idle

Origem: `Folha de sprites de guarda de combate.png`, fornecida pelo utilizador
em 18/11/2026, grelha 6 × 2 com 2172 × 724 píxeis.

`preparar_idle.py` (Python + Pillow) remove elementos desligados da silhueta,
incluindo números e grelha, e conserva o alpha junto ao corpo. Aplica uma escala
única às doze poses, mantendo a compressão dos joelhos, o eixo horizontal da
célula e o chão em y=108. As telas finais têm 128 × 112 píxeis, compatíveis com
o renderizador existente. A imagem original em Downloads não é modificada.

Reproduzir a importação a partir da raiz do projeto:

```powershell
python output/idle-remake/preparar_idle.py "C:\caminho\Folha de sprites de guarda de combate.png" src/main/resources/assets/textures/fighter/idle
```

`preview.png` mostra as doze poses num fundo contrastante. Os números nesta
pré-visualização são apenas anotações; não existem nos PNGs do jogo.

O CSV usa as doze poses em ciclo, com 8 passos de simulação por pose após o
multiplicador de duração não ofensiva já existente (96 passos, 1,6 s por ciclo).
As caixas antigas das primeiras oito poses são preservadas; as quatro novas
reutilizam caixas de idle existentes, não uma recalibração anatómica por pose.
Nenhum ataque, dano, controlo ou outro estado visual foi alterado.

Teste dedicado: `pt.aepombal.tpi.ruinas.modelo.IdleSpritesTeste`.
