# Frames originais

Esta pasta contém os 115 frames WebP organizados pelas ações de combate (`lp`, `mp`, `hp`, `lk`, `mk`, `hk`, `block`, `low_block`, caminhada, salto, etc.).

Ainda não são carregados diretamente pelo FXGL porque mantêm o fundo das fotografias e não estão todos recortados para o mesmo canvas. A próxima etapa gráfica é:

1. remover o fundo;
2. recortar cada pose com transparência;
3. alinhar os pés numa linha de chão comum;
4. exportar PNG para `src/main/resources/assets/textures/`;
5. afinar `dados/frames.csv` com F3 visível.

Só depois disso as hitboxes podem ficar alinhadas com precisão à arte final.
