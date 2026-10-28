# Decisões de desenho

## 1. Dados por frame fora do código

Os tempos, `pushboxes`, `hurtboxes` e `hitboxes` vivem em `dados/frames.csv`. Isto permite corrigir alcance, dano e janelas de ataque sem alterar nem recompilar classes Java. O modo F3 mostra as caixas ativas para a afinação ser verificável.

## 2. IA através de polimorfismo

Oponente agressivo, defensivo e equilibrado são subclasses de `ComportamentoOponente`. A fábrica recebe uma `Dificuldade` que cria o componente certo; depois disso, o jogo chama sempre o mesmo método abstrato. Assim, acrescentar uma quarta IA não obriga a criar `if` ou `switch` espalhados.

## 3. Aplicação só coordena

`StreetFrameApp` configura o FXGL, apresenta a interface e liga teclas a métodos do `GestorCombate`. As regras de vida, hitboxes, bloqueio, derrota e recordes não ficam na classe da aplicação. Isto torna o código testável e fácil de explicar na defesa.
