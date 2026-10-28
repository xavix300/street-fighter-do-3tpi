# Proposta - Percurso C: Street Frame Fighter

**Estado:** pendente de aprovação do professor.

Pretendemos desenvolver um jogo de luta 2D local em Java com FXGL. O jogador combate um oponente controlado por IA numa arena única. Cada ação é composta por frames; em cada frame, as `pushboxes`, `hurtboxes` e `hitboxes` são lidas de um ficheiro de dados externo. O jogo terá os seis ataques clássicos (`LP`, `MP`, `HP`, `LK`, `MK`, `HK`), caminhada, salto e bloqueios alto/baixo.

## Entidades e modelo

- `ComponenteLutador`: vida, movimento, salto, bloqueio, animação e estado da ronda.
- `ComportamentoOponente` abstrato: comportamento comum da IA.
- `OponenteAgressivo`, `OponenteDefensivo` e `OponenteEquilibrado`: três subclasses com decisões distintas, usadas polimorficamente.
- `GestorCombate`: coordena a ronda, sobreposição das caixas e fim de jogo.
- `AcaoCombate`: enum com metadados das ações.
- `FrameCombate` e `CaixaFrame`: modelo de dados por frame.

## Cumprimento do Nível 1

| Requisito | Aplicação no jogo |
| --- | --- |
| R1/R2 | Hierarquia `ComportamentoOponente` com três IAs distintas, criada através de `Dificuldade`, sem testar o tipo do oponente. |
| R3 | Enum `AcaoCombate` e enum `Dificuldade`, ambos com atributos e métodos. |
| R4 | Record `Pontuacao(String nome, int pontos, LocalDate data)`. |
| R5 | `Map` para animações e lutadores; `List` para frames, caixas e recordes. |
| R6 | `DadosCombateException` e `RecordesException` para dados inválidos. |
| R7 | Top 10 persistente em `dados/recordes.csv`, codificação UTF-8. |
| R8 | Fim de ronda mostra vencedor, grava pontuação e reinicia o jogo. |
| R9 | A dificuldade progride de IA equilibrada para agressiva e defensiva após vitórias. |

## Nível 2 previsto

- R10: itens de arena como `VidaExtra`, `DanoExtra` e `Energia`, numa segunda hierarquia.
- R11: `dados/frames.csv` já permite alterar as regras de combate sem recompilar; será acrescentado conteúdo de arena externo.
- R12: menu, pausa e sons.
- R13: estratégia de decisão da IA separada da entidade de combate.
