# Movimento e guarda — folhas de 18/11/2026

Importação reproduzível com Python + Pillow, a partir da raiz do projeto:

```powershell
python -B output/movement-remake/preparar_movimento.py "$HOME/Downloads" src/main/resources/assets/textures/fighter
```

O script reutiliza o isolamento de silhueta de `../idle-remake/preparar_idle.py`.
Não altera os ficheiros originais.

| Folha em Downloads | Grelha | Utilização |
| --- | --- | --- |
| `walk_forward_contact_sheet.png` | 8 × 2 | 16 poses de avanço |
| `walk_backward_contact_sheet.png` | 4 × 4 | 16 poses de recuo |
| `Folha de sprites_ guarda de boxe.png` | 6 × 2 | Poses 4–8 para GUARD e BLOCK |
| `Folha de Sprites_ Crouch de Combate.png` | 6 × 2 | Poses 7–12 para CROUCH |

Todas as folhas têm alpha, incluindo a de agachamento. O recorte respeita esse
alpha; cores invisíveis de fundo não são tornadas opacas. Remove elementos
desligados da silhueta e manchas vermelhas saturadas junto às mãos na guarda.
Os quatro `*_preview.png` mostram **todas** as poses preparadas com números
apenas para revisão; os PNGs do jogo não têm números.

As telas finais são 128 × 112, chão em y=108 e escala única dentro de cada
folha, calibrada pela pose mais alta para 100 píxeis. Não se esticam as poses
agachadas até à altura de uma pessoa em pé.

A guarda usa apenas o trecho com os braços levantados. O agachamento usa apenas
o trecho já agachado para não repetir a transição de pé enquanto baixo é mantido.
A animação LOW_BLOCK existente não é substituída por imagens sem guarda baixa.
Não foram adicionadas transições de entrada/saída ao motor de estados.

O CSV passa a percorrer as novas contagens. Os ciclos de andar continuam a durar
80 passos (16 × 5 em vez de 10 × 8). As velocidades, defesa, dano, stun e tempos
dos ataques não são alterados. Conservam-se as caixas das poses de movimento
existentes; as novas reutilizam essas caixas, não geometria recalibrada.

Teste: `pt.aepombal.tpi.ruinas.modelo.MovimentoSpritesTeste`.
