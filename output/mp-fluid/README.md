# MP — animação de 20 poses

Origem: spritesheet fornecida pelo utilizador em
`Downloads/Imagem ChatGPT 17_11_2026, 12_21_53.png`.
O original é preservado em `mp-atlas-original.png`; não foi alterado nos Downloads.
A tentativa anterior de geração integrada falhou e não foi usada.

`prepare-mp.ps1` divide a imagem em cinco colunas e quatro linhas, mantém a ordem
cronológica e escreve PNGs transparentes de 128 × 112 em
`src/main/resources/assets/textures/fighter/attacks/mp/`.
Usa uma escala comum baseada na altura mediana das poses e na guarda antiga.
Alinha a linha do chão e o centro dos apoios; não centra pelas mãos, não deforma
os membros, não mistura imagens e não substitui os sprites antigos.
`mp-review.png` apresenta os recortes sobre fundo cinzento para revisão.

O plano `animacao.properties` define 5 poses de preparação, 4 ativas e 11 de
recuperação. Neste MP cada pose ocupa um passo de 1/60 s: 20 frames no total.
Hitstop continua a pausar a animação quando há contacto; whiff recovery extra
continua a ser controlada pelo combate, fora destes 20 frames.
As hitboxes e o dano continuam nos dados lógicos existentes.

As poses foram geradas externamente e têm pequenas diferenças de postura/roupa
relativamente às fotografias originais. O alinhamento automático não corrige
anatomia nem garante que os pés coincidam exatamente entre todas as poses.
É uma primeira versão para testar em movimento, não uma captura fotográfica real.

Para repetir o processamento:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File output/mp-fluid/prepare-mp.ps1 -Source "caminho/para/spritesheet.png"
```
