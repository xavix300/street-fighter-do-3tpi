# Segundo lutador — apenas preparação do idle

Identificador temporário: `fighter_02`. Nome da personagem ainda por definir.
Está disponível no seletor e no combate, por autorização posterior do utilizador.
O idle aqui preparado é conservado; os outros previews foram importados em
`../fighter-02-pack/`. MP/stance ainda usam recursos visuais da própria personagem.

Folha fornecida pelo utilizador em Downloads:
`f06aa211-3fe3-4f19-ad36-34c783736bc1.png`, grelha de 4 × 3, lida da esquerda
para a direita e de cima para baixo.

O script remove manchas vermelhas/amarelas saturadas, isola a maior silhueta,
preserva o alpha e usa uma escala comum às doze poses. Não altera o original.
As poses mostram a personagem a levantar a guarda e voltar ao repouso; não são
animações de ataque nem substitutos para movimentos ainda inexistentes.

Recursos preparados, separados do primeiro lutador:

- `src/main/resources/assets/textures/fighters/fighter_02/idle/idle_001.png`
  até `idle_012.png`: telas de 128 × 112, chão em y=108.
- `frames-hd/`: as mesmas poses limpas em telas de 512 × 448, chão em y=432.
  São recortes redimensionados da folha, não imagens geradas com detalhe adicional.
- `preview.png`: pré-visualização das doze poses; números apenas para revisão.

Reproduzir a partir da raiz do projeto, com Python + Pillow:

```powershell
python -B output/fighter-02-idle/preparar_idle.py "$HOME/Downloads/f06aa211-3fe3-4f19-ad36-34c783736bc1.png" src/main/resources/assets/textures/fighters/fighter_02/idle
```

A escolha é independente para P1 e P2/CPU. No menu de pausa, a troca visual é
aplicada à ronda atual sem reiniciar a vida ou o estado. A personagem usa as
mesmas regras de combate existentes. Ver o pack provisório para os movimentos
adicionais e as respetivas limitações de qualidade.
