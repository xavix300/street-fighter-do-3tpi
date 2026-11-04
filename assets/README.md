# Frames originais

As novas sequências de LP, MP, LK, MK e dos seis ataques agachados estão ativas em
`src/main/resources/assets/textures/fighter/sequences/`. Os originais desta pasta
foram preservados. Segurar S ao atacar seleciona a variante agachada.

Esta pasta contém os 115 frames WebP organizados pelas ações de combate (`lp`, `mp`, `hp`, `lk`, `mk`, `hk`, `block`, `low_block`, caminhada, salto, etc.).

Os originais mantêm o fundo. As versões PNG com fundo removido e alinhamento aproximado estão em `src/main/resources/assets/textures/fighter/` e são carregadas pelo jogo. Os recortes são automáticos e devem ser revistos manualmente, sobretudo nas mãos e nas margens.

Próximos passos: corrigir os recortes, melhorar a estabilidade entre frames e afinar `dados/frames.csv` com F3 visível.

As hitboxes atuais continuam aproximadas; não foram recalibradas para os novos sprites.
