# VEIL CHAPTER 0.2.9 — Observer / Peekers / Cinematic angle

## Mudanças desta branch
- Corrigida câmera da árvore: removido override de CameraSetup que forçava rotação de renderização independente do yaw do jogador. Ângulo vanilla 180° continua voltado ao norte, para a árvore em z=0.
- Aparições extras do Watcher nos dias 2 a 7, com intervalos e tentativas espaçados. Procura à frente do jogador, somente em chunks já carregados e em linha de visão.
- Observador gira na direção do jogador, some após um curto período observado e não ataca.
- Aparições rápidas (~3 s) com somente **olhos** ou parte de **cabeça preta com olhos claros** sobre blocos sólidos.
- Render simples por quads, sem shaders, sem textura adicional, sem colisões.
- Chance configurável (24% padrão) de Watcher aparecer 20 ticks após deitar, somente se o jogador ainda estiver dormindo. Sai imediatamente ao levantar; não afeta sono quando evento não pode acontecer.
- Configs novas: enableDistantWatcher, enablePeeking, enableBedWatcher, bedWatcherChance, watcherHorizonBlocks.
- Corrigido /veilstatus para contar os 5 trechos do ritual, não apenas 3.

## Limite conhecido sobre camas
Em mundo solo, Minecraft 1.12.2 normalmente avança a noite automaticamente antes de 20 ticks. Esta primeira implementação é segura, mas não garante aparição em todo sono; não altera o salto da noite nem prende o jogador. Um modo de pesadelo com permanência obrigatória na cama exige mudança separada e teste em cliente.

## Testar
1. Mundo descartável e cheats: /veiltest day 3, observe o horizonte ao explorar.
2. Teste olhos/cabeça em morros e perto de paredes durante dias 3-5.
3. Teste a cama à noite com e sem outros jogadores dormindo.
4. /veiltest day 7 e /veiltest tree: a árvore deve aparecer à frente durante a fala; entrada abre somente após as cinco vozes.
5. Saia e volte ao jogo para verificar entidades transitórias e persistência.

## QA
Distinguir build Java, bootstrap de servidor, QA estático e playtest real.
Não instalar duas versões do mod simultaneamente.
A base GitHub permanece sem os 22 OGG aprovados. Artefatos de CI sem áudios **não** são releases.
