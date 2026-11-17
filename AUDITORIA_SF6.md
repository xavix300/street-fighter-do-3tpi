# Auditoria de fidelidade ao SF6 — 17/11/2026

## Referência e limites de verificação

Comparação com https://dood.gg/en/street-fighter-6/ryu/frame/ consultado nesta sessão.
A página oficial da Capcom não respondeu às duas tentativas de consulta.
A tabela secundária não identifica aqui um patch fixado: coincidência com os seus
valores não prova equivalência integral à versão atual de SF6.

O utilizador escolheu a versão mais recente, sem necessidade de instalação local.
Em 17/11/2026, o resultado indexado da página oficial
https://www.streetfighter.com/6/buckler/en/battle_change apresenta
**10.13.2026 update** (13/10/2026). Esta é a referência de balanceamento
mais recente encontrada nesta consulta, ainda provisória: a leitura direta
da página falhou. Não confundir a data do balanceamento com um número de build,
nem tratar o resultado indexado como confirmação de todos os dados individuais.
Antes de declarar equivalência, verificar as notas completas, eventuais hotfixes
posteriores e os valores detalhados de Ryu/sistemas universais.
Este documento é um diagnóstico; não declara as alterações abaixo implementadas.

## Ataques

Os 18 normais de pé, agachados e aéreos têm os mesmos valores base de startup,
ativos, recuperação e dano que a tabela consultada. A recuperação aérea termina
na aterragem. Isto não valida as hitboxes, reações, multi-hits, cancels ou trajetórias.

`ANTI_AIR_MP` e `ANTI_AIR_CROUCH_MP` são ataques personalizados: usam o timing do
2HP com dano 600, enquanto o 2HP da referência causa 800. Trás+MP não corresponde
a um command normal do Ryu. Não os apresentar como ataques oficiais.

### Direcionais reais ainda ausentes

Frente/trás são relativos ao adversário, não teclas absolutas.

| Comando | Movimento | Startup | Janela ativa indicada | Recuperação | Dano total |
| --- | --- | ---: | --- | ---: | ---: |
| Frente+MP | Collarbone Breaker | 20 | 20–23 | 19 | 600 |
| Frente+HP | Solar Plexus Strike | 20 | 20–24 | 16 | 800 |
| Trás+HP | Short Uppercut | 7 | 7–10 | 26 | 800 |
| Trás+HK | Axe Kick | 10 | 10–23 | 21 | 800 |
| Frente+HK | Whirlwind Kick | 16 | 16–19 | 20 | 800 |

As janelas da tabela-resumo não substituem os dados individuais de cada hit:
é necessário verificar intervalos, dano por hit, alturas, cancel windows,
reações e exceções antes de implementar como comportamento fiel.
Também faltam os target combos HP→HK e MP→LK→HK, se incluídos no âmbito.
Especiais e supers do Ryu foram explicitamente excluídos pelo utilizador.

## Diferenças encontradas na lógica

| Ficheiro/sistema | Estado atual e trabalho necessário |
| --- | --- |
| `modelo/TemposRyu.java` | Tempos base; stuns derivados de vantagens; sweep usa hit-stun em vez de knockdown; aéreos mantêm stuns locais. |
| `modelo/AcaoCombate.java` | Direcionais reais e target combos ausentes; trás+MP personalizado; sem distinção de hits individuais. |
| `modelo/TipoAcerto.java` | Bónus genéricos +20% e +2/+4f; faltam reações e exceções por golpe, interações com Drive e scaling. |
| `componentes/ComponenteLutador.java` | Guarda local 0–100, guard break e recuperação próprios; buffer de 7f; rapid cancel genérico sem janelas específicas. |
| `componentes/ComponenteLutador.java` | Cada lutador avança o seu relógio e resolve contactos separadamente; exige revisão para trades e simultaneidade determinística. |
| `componentes/ComponenteLutador.java` | Velocidades locais; falta dash/backdash, pre-jump, knockdown/wake-up, throw/tech, hit reactions e juggle completos. |
| `modelo/SaltoLutador.java` | Impulso -720 e gravidade 1800 locais; não verificados contra Ryu. |
| `logica/GestorCombate.java` | Um contacto por ataque; sem multi-hit real. Pushboxes separadas por deslocamento fixo; não reproduz pushback/corner de SF6. |
| `logica/ReacaoContacto.java` | Hitstop fixo 5f; recuo 90/150 durante 9f; parâmetros locais. |
| `modelo/RegraDefesa.java` | Alto/baixo básico, sem overheads direcionais específicos nem sistema completo de defesa SF6. |
| `dados/frames.csv` | Caixas aproximadas locais; não são coordenadas extraídas do SF6. |
| `modelo/RepositorioFrames.java` | Multiplica duração das animações não ofensivas por 1,5; não é uma regra de SF6. |
| `componentes/VistaLutador.java` | Troca direta de imagens/estados; não há transições específicas por pose de origem. |
| `componentes/Oponente*.java` | IA própria; não é IA de SF6. |
| Ronda/HUD | Ronda e score locais; sem implementação completa de timer, sets, double KO e sistemas de recursos SF6. |
| Sistemas ausentes | Drive Impact, Drive Parry/Perfect Parry, Drive Rush, Drive Reversal, Burnout, throws/tech, scaling, knockdown/wake-up e juggle completos. |

Menus, fotografias, recordes e apresentação são próprios do projeto e não
precisam de imitar SF6 para que as regras de combate possam ser fiéis.

## Inventário visual

| Grupo | Existente | Em falta / necessário rever |
| --- | --- | --- |
| LP | 40 poses alternativas | Continuidade com idle/movimento e alinhamento. |
| MP | 20 poses novas (5/4/11) | A guarda inicial/final não coincide exatamente com todos os frames do idle. |
| HP / HK | 14 / 12 poses | Continuidade com estados de origem/destino. |
| LK / MK | 6 poses cada | Poses intermédias e transições. |
| Seis agachados | 6 poses por golpe | Transições, recuperação do sweep e knockdown do alvo. |
| Seis aéreos | 6 poses por golpe | Entrada a partir da fase de salto, regresso e aterragem. |
| Salto | 12 PNGs originais em `fighter/jump` | O código aponta para `sequences/jump_clean`, pasta ausente. |
| Idle / andar | 8 / 10 poses por direção | Transições de ataque para cada postura relevante. |
| Agachar / guarda | Uma pose de crouch, block, guard e low_block | Descer/subir, entrada/saída de defesa e reações. |
| Hit / KO | Uma pose cada | Reações por força/altura, queda, chão, levantar e lançamento aéreo. |
| Cinco direcionais | Nenhuma sequência dedicada | Preparação, hits e recuperação completos. |
| Target combos | Nenhuma sequência dedicada | Seguimentos, caso incluídos. |
| Sistemas universais | Sem sequências dedicadas | Dash/backdash, throws/tech, parry, Drive e respetivas reações, caso incluídos. |

Não existe um número universal de sprites obrigatório por golpe.
O plano visual deve caber nos tempos lógicos, sem alongar o startup.
Para transições contínuas com sprites fotográficos, são necessárias poses
compatíveis ou transições por estado/pose de origem. Crossfade cria duas
silhuetas e não equivale à interpolação de uma animação esquelética de SF6.
Cancels, impactos, bloqueios, aterragem e KO podem exigir interrupção imediata;
não se deve atrasar a resposta de combate para terminar uma transição cosmética.

## Âmbito confirmado pelo utilizador

Inclui sistemas universais completos: Drive Impact, Parry/Perfect Parry,
Drive Rush, Drive Reversal, Burnout, throws/tech, dash/backdash,
knockdown/wake-up, counters, cancels, scaling, juggle e respetivas interações.
Inclui os cinco direcionais e os target combos do Ryu, além dos normais.
A personagem e os recursos visuais próprios são mantidos.
Especiais e supers do Ryu continuam excluídos.

Esta confirmação define o objetivo; não significa que os sistemas já existam.
Nenhuma implementação aproximada deve ser apresentada como equivalência completa.

## Dependências ainda pendentes

1. Validar a referência mais recente encontrada (13/10/2026) contra as notas
   completas e eventuais hotfixes, sem exigir ao utilizador uma instalação do SF6.
2. Obter dados detalhados verificáveis, incluindo exceções, janelas por hit,
   propriedades de defesa e reações de cada ataque.
3. Obter os recursos visuais em falta; transições cosméticas não podem acrescentar
   startup nem atrasar interrupções de combate.
