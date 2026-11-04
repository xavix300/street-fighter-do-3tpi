# Hitboxes e dano — Street Fighter e aplicação neste projeto

## Fontes oficiais

- [Capcom, SF Seminar, Hour 7: The Basics of Boxes](https://game.capcom.com/cfn/sfv/column/131422?lang=en): distingue caixas ofensivas e vulneráveis; explica que não coincidem necessariamente com o desenho e que os ataques podem expor também hurtboxes.
- [Capcom, SF Seminar, Hour 9: The Basics of Attack Composition](https://game.capcom.com/cfn/sfv/column/131432?lang=en): a janela em que o golpe tem hitbox corresponde aos frames ativos. Arranque e recuperação são fases distintas.
- [Capcom, SF Seminar, Hour 11: Damage Part II](https://game.capcom.com/cfn/sfv/column/131545?lang=en): valores de dano e parâmetros de reação estão associados às hitboxes.
- [Capcom, alterações de Guile em SFV CE202102](https://game.capcom.com/cfn/sfv/adjust/ce202102/gul?lang=en): exemplos concretos de alterações da hurtbox do tronco e da hitbox a partir de determinado frame.

Estas referências documentam Street Fighter V e os princípios da série; não são uma especificação interna do motor de SF6. Os resultados indexados das páginas oficiais estavam disponíveis, mas a abertura direta das páginas do seminário e dos dados de SF6 devolveu HTTP 403 nesta sessão. Não se copiaram coordenadas ou fórmulas internas de SF6.

## Relação com o jogo

O projeto já distingue `HIT` (ataque), `HURT` (zona vulnerável) e `PUSH` (colisão dos corpos). Um contacto ofensivo resulta da interseção HIT × HURT; a sobreposição das imagens ou das PUSH não determina dano. As caixas são espelhadas quando o lutador muda de direção. Cada execução de um ataque normal só pode registar um acerto, mesmo que tenha vários frames ativos.

Uma hitbox não precisa de seguir cada píxel do punho ou do pé. Deve transmitir o alcance e o momento do golpe, com tolerâncias ajustadas à jogabilidade. Os membros estendidos podem precisar de hurtboxes próprias, para permitir punir golpes que falharam. As nossas caixas continuam aproximadas; não se fez nesta alteração uma recalibração anatómica de todas as fotografias.

## Correção do dano

O problema confirmado pelo utilizador era perda excessiva de vida. Na versão inicial, a vida máxima era 100; o MK causava 35 pontos, e HP/HK tinham valores distintos entre os seus frames ativos. As variantes agachadas fortes tinham ainda herdado o dano do primeiro frame ativo das referências, tornando alguns fortes mais fracos do que médios.

A pedido do utilizador, a versão atual usa **10 000 de vida** para ambos os lutadores e os danos base dos doze ataques normais do Ryu em Street Fighter 6:

| Ataque | Em pé | Agachado |
| --- | ---: | ---: |
| LP | 300 | 300 |
| MP | 600 | 600 |
| HP | 800 | 800 |
| LK | 300 | 200 |
| MK | 700 | 500 |
| HK | 900 | 900 |

Fonte: [tabela oficial da Capcom, Ryu SF6](https://www.streetfighter.com/6/ja-jp/character/ryu/frame), consultada em 2026-11-03. Nesta consulta foi possível obter a página oficial por HTTP 200 com PowerShell e ler as linhas da tabela HTML, apesar do erro 403 no leitor web. Os valores substituem a tabela provisória 4/7/10. Não se introduziram modificadores de counter, punish counter ou redução de combo: correspondem ao dano base de cada normal, não a uma reprodução completa das regras de dano de SF6.

Todos os frames ativos de um mesmo normal mantêm o mesmo dano. A barra de vida e o número apresentado usam a nova escala. A guarda permanece em 0–100; o dano é convertido para essa escala antes de calcular o desgaste, evitando quebra imediata causada apenas pela mudança de unidades.

Os controlos, alcance, duração dos ataques, hit-stun, block-stun e bloqueio foram mantidos. O bloqueio continua sem retirar vida; consome a barra de guarda. `AnimacoesTeste` verifica o dano de todas as hitboxes dos doze ataques e deteta diferenças indevidas entre frames ativos.
