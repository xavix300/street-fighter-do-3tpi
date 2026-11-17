# Perfil mecânico: Ryu SF6, sem especiais nem supers

Alvo confirmado pelo utilizador em 17/11/2026: dano, comandos direcionais, normais,
saltos, tempos e movimento do Ryu SF6, mantendo esta personagem e as fotografias.
Não adicionar Hadoken, Shoryuken, Tatsumaki, outros especiais ou Super Arts.

## Fontes e confiança

- [Capcom: Ryu frame data](https://www.streetfighter.com/6/character/ryu/frame),
  consultada em 17/11/2026: danos base, startup/ativos/recuperação, vantagem,
  comandos dos cinco direcionais e recuperação de 3f após ataques aéreos.
- [Ultimate Frame Data: Ryu](https://ultimateframedata.com/sf6/ryu):
  resultados indexados indicam avanço 4,7, recuo 3,2, prejump 4f e Axe Kick
  400+400. A página retornou 404 no acesso direto; os valores secundários
  continuam sujeitos a confirmação em Training Mode.
- A duração 4+39+3 neutra / 4+38+3 diagonal e distância diagonal 190 vêm de
  resultados indexados de guias secundários, não da tabela oficial. São
  parâmetros de calibração, não medições verificadas nesta máquina.

## Implementado

- Normais de pé/agachados/aéreos já existentes conservam os valores de Capcom.
- Retirado o comando personalizado trás+MP; agora é MP normal, e baixo+trás+MP
  é MP agachado. Os dois enums antigos permanecem para compatibilidade com o CSV.
- Frente/trás são relativos ao adversário; baixo tem prioridade nas diagonais.
- Durante o salto, o botão continua a selecionar o normal aéreo correspondente.
- Avanço/recuo assimétricos: 235/160 px/s. `MovimentoRyu.PIXELS_POR_UNIDADE`
  converte 4,7/3,2 unidades por frame para esta arena (5/6 px por unidade).
- Prejump 4f, sem ataque aéreo nem deslocamento. Um impacto interrompe o prejump.
- Salto neutro 39f no ar, diagonal 38f; direção fixada ao saltar, sem air steering.
  Distância diagonal convertida: 158,333 px; altura adaptada: 144 px.
- Orientação não muda a meio do salto/golpe; volta a seguir o adversário no chão.
- Um relógio comum de 60 Hz, sem descartar janelas ativas em renderização a 30 FPS.
- Contactos simultâneos são capturados antes de aplicar stun: permite trades.
  O frame de contacto é avaliado antes de avançar a animação (LP acerta no 4.º passo).

| Comando | Movimento | Startup | Ativos (frames absolutos) | Recovery | Dano |
| --- | --- | ---: | --- | ---: | ---: |
| Frente+MP | Collarbone Breaker | 20 | 20–23 | 19 | 600 |
| Frente+HP | Solar Plexus Strike | 20 | 20–24 | 16 | 800 |
| Trás+HP | Short Uppercut | 7 | 7–10 | 26 | 800 |
| Trás+HK | Axe Kick | 10 | 10–14, 20–23 | 21 | 400+400 |
| Frente+HK | Whirlwind Kick | 16 | 16–19 | 20 | 800 |

Os direcionais usam fotografias e caixas de movimentos existentes, sem criar
assets do Ryu. Axe Kick regista cada grupo de contacto separadamente e não
aplica dano novamente em cada frame ativo ou em cada caixa sobreposta.

## Diferenças que ainda impedem fidelidade integral

- Collarbone Breaker está simplificado como um impacto overhead de 600.
  Capcom descreve dois ataques e overhead apenas no segundo quando o primeiro
  falha. Falta reproduzir essa distinção e obter a geometria/frames por impacto.
- As caixas, alturas e trajetórias espaciais são adaptadas. Não há deslocamentos
  particulares dos direcionais, cross-up hitboxes exatas ou ground bounce do Axe Kick.
- Hitstop continua a ser 5f e pushback continua a ser o do protótipo, não dados
  verificados por golpe. Não se apresentam estes valores como os do Ryu.
- Buffer de 7f e rapid cancels existentes ainda precisam de comparação direta.
  Target combos (HP→HK e MP→LK→HK) não foram implementados nesta alteração.
- Sweep ainda não derruba; faltam launch/juggle, crumple, scaling e reações
  específicas por golpe. Stun dos aéreos permanece o do protótipo.
- A barra de guarda/guard break é própria deste jogo, não o Drive/Burnout do SF6.
  Não há throws, dashes, parry, Drive Rush ou Drive Impact nesta alteração.
- Recuperação de aterragem continua 3f após usar um ataque aéreo; o cancelamento
  de aterragem de um salto vazio ainda precisa de calibração.
- CPU ainda toma decisões no update do motor, não por replay de inputs a 60 Hz.
- Não houve avaliação humana lado a lado com SF6 nesta sessão.

## Testes

Java 21; `mvn.cmd -B -ntp test-compile`. Os testes são programas `main`,
não testes descobertos automaticamente pelo Surefire.

Classpath Windows: `target/test-classes;target/classes;` seguido do conteúdo
de `target/classpath.txt` (gerado com `dependency:build-classpath`).

- `modelo.DirecionaisRyuTeste`: comandos dos dois lados, diagonais, aéreos,
  janelas dos cinco golpes, dano, dois grupos de Axe Kick e 60 passos/s a 30/60/144 FPS.
- `modelo.SaltoTeste`: prejump, topo, aterragem, distância independente do FPS,
  restrição de ataques e defesa.
- `RyuIntegracaoTeste`: abre brevemente o FXGL real e testa avanço/recuo,
  LP apenas no 4.º frame, acerto único, trade simultâneo, prejump e aterragem.
  Termina o seu próprio processo com código 0/1; executar num processo separado.
- As regressões `TemposRyuTeste`, `TipoAcertoTeste`, `PunicoesLutadorTeste` e
  os restantes testes funcionais continuam a passar.
- Falhas pré-existentes: `AnimacoesTeste` (PNG JUMP em falta) e `Ataques40Teste`
  (espera layout de 40 poses em MP, que tem outro pipeline de 20 imagens).
