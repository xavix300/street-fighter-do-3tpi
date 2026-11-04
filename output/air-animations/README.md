# Saltos e ataques no ar

Integrados no jogo:

- W: salto vertical; A/D + W: salto lateral.
- Durante qualquer salto, U/I/O executam LP/MP/HP e J/K/L executam LK/MK/HK.
- Um ataque por salto; ao terminar no ar, volta à fase atual do salto. Ao tocar no chão, o ataque e as suas hitboxes terminam.
- A trajetória lateral mantém-se durante o golpe. Trás/baixo no ar não selecionam o anti-air nem ataques agachados.
- Ataques no ar exigem defesa em pé; não há bloqueio aéreo.

A fase visual é calculada pela trajetória física, em vez de reproduzir uma animação de 1,6 segundos durante um salto de 0,8 segundos. O salto lateral reutiliza as poses do vertical com inclinação na direção do movimento. O movimento vertical foi tornado independente da frequência de atualização. Hitstop pausa a trajetória juntamente com o combate.

## Imagens

Geração com `image_gen` integrada, PNG com transparência. Seis novas poses de impacto foram criadas a partir da fotografia de salto e dos golpes de referência. `prompts.json` contém os prompts exatos. Os originais em alta resolução são `air_*-impact.png`.

As fotografias 005–012 do salto foram recortadas novamente com a mesma ferramenta, preservando as poses e removendo fragmentos do cenário. `jump-clean/` contém os resultados e os respetivos prompts. Os recursos originais continuam intactos.

`prepare-air.ps1` normaliza os PNGs e escreve os recursos ativos em `src/main/resources/assets/textures/fighter/sequences/air_*/` e `sequences/jump_clean/`. Cada ataque tem seis fases visuais; a pose de impacto é antecipada na fase 03 e a hitbox fica ativa apenas na fase 04. Os frames de preparação e recuperação reutilizam poses limpas do salto; não foram geradas 36 poses distintas. A cabeça é alinhada em y=8; o corpo fica mais compacto nas poses com os joelhos recolhidos, sem esticar o recorte para preencher toda a tela.

`air-sequences-review.png` permite comparar os seis ataques.

## Dano e verificação

Os danos base dos aéreos seguem as linhas de salto da [tabela oficial do Ryu SF6](https://www.streetfighter.com/6/ja-jp/character/ryu/frame), consultada nesta sessão: LP/LK 300, MP 700, MK 500, HP/HK 800. Não se reproduziram todos os timings, cancels ou modificadores de SF6.

Verificação: compilação limpa Maven; `AnimacoesTeste`, `RegraDefesaTeste` e `SaltoTeste`. Este último cobre salto vertical e lateral nos dois sentidos, topo/aterragem, fases a 30/60/144 atualizações por segundo, um ataque por salto, variantes aéreas e defesa alta/baixa.
