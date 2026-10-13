# Veil Chapter v0.2.8 — Cinematic Gate (teste experimental)

Checkpoint imutável original: 27226819829b74c010adb73e03c62b6e11edf7b3
Branch isolada: dev/tree-sealed-watcher-cinematic-v028

## O que mudou
- Árvore original de até 30 blocos preservada.
- Construção prévia da árvore na dimensão de destino; até 128 blocos e um chunk novo por tick.
- Porta inicial 5x4 (x=-2..2, y=5..8, z=3) bloqueia o corredor do tronco.
- O jogador observa a árvore pronto na posição (0.5,4,45.5); rotação 180°, -14°.
- Bloqueio de WASD, salto, câmera e posição no cliente e no servidor enquanto dura a fala.
- Watcher perto do observador, sem ataques, presente ao longo das cinco falas.
- Todas as cinco gravações originais reproduzidas consecutivamente. Primeiro diálogo aos 2 segundos; fim da cena ~41 segundos.
- Apenas ao fim da fala 5 a porta abre e a movimentação volta.
- Mantidos baú, livro 3 Dias, retorno e gravações originais, com áudio ambiente atenuado.
- Ao reconectar durante a cena selada, o monólogo reinicia para preservar os cinco trechos completos.

## Testar
Em mundo descartável, enableTreeSequence=true, cheats ativos:
 /veiltest day 7
 /veiltest tree
Verificar prebuild antes do teleporte, FPS, controle/câmera, ordem dos cinco trechos,
ausência de cortes/sobreposição, porta abrindo ao fim, inventário, baú e retorno.

A branch original ainda não possuía os 22 OGG binários aprovados. A compilação no GitHub pode resultar em artefato incompleto até que os sons recuperados sejam reintegrados. Não usar o JAR incompleto de Actions como release.
