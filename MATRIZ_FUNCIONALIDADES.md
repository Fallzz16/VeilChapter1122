# Matriz de cobertura — Veil Chapter v0.2.0-alpha

Legenda: **CÓDIGO** = fonte escrita, mas não compilada/testada no Minecraft; **PARCIAL** = incompleto; **RISCO** = desabilitado por segurança; **PENDENTE** = não implementado.

| Requisito acordado | Estado | Arquivo principal | Observações |
|---|---|---|---|
| Dia 1 vanilla + primeiro despertar | CÓDIGO | `GameEvents`, `StoryClock` | teste offline passou |
| Dias 2–7 sem regressão em mortes | CÓDIGO | `StoryClock`, `StoryData` | no Minecraft ainda não testado |
| Figura preta modelo Steve | CÓDIGO | `EntityWatcher`, `ClientProxy`, `watcher.png` | renderer não testado |
| Atrás a 40–70 blocos | CÓDIGO | `PresenceSystem` | cancela quando local inválido |
| 1,5s pré-árvore; 3–6s pós | CÓDIGO | `PresenceSystem` | visão por cone e opacidade aproximada |
| Aparições em janelas/portas/casa | PARCIAL | `PresenceSystem` | heurística de cama e posições caminháveis |
| Reposicionamento quando invisível | PARCIAL | `PresenceSystem` | pequenos teletransportes; pathwalking real pendente |
| Cegueira 5s, raio <10, rearme >=12 | CÓDIGO | `ProximityGate`, `PresenceSystem` | 235 asserts offline incluem histerese |
| Ataque faz figura desaparecer | CÓDIGO | `EntityWatcher` | sem drops |
| Abrir/fechar portas reais pós-árvore | CÓDIGO | `PresenceSystem` | só enquanto fora da visão; portas de madeira |
| Sons falsos de portas/psst/passos | CÓDIGO | `DreadSystem`, OGG, sounds.json | vozes faladas pendentes |
| Zumbis atacam ou ignoram | PARCIAL | `GameEvents` | cancelar ataque + limpar alvo; IA precisa teste |
| Blocos flutuantes reais e expiração | CÓDIGO | `AnomalySystem`, `StoryData` | apenas pedra/terra, 2–5min |
| Mineração desacelerada rara | CÓDIGO | `AnomalySystem` | apenas blocos vanilla selecionados |
| Blocos quebram sozinhos | RISCO | `AnomalySystem` | apenas folhas, flag off |
| Teleporte discreto <=200b | RISCO | `AnomalySystem` | flag off; similaridade aproximada |
| Escuridão efetiva luz natural/spawns | PENDENTE | `ClientEvents`, `DreadSystem` | só fog color + mobs extras perto cama |
| Aldeões e animais somem/retornam | RISCO | `DreadSystem`, `StoryData` | flag off; só entidades carregadas e sem nome |
| Incêndios reais com propagação limitada | RISCO | `DreadSystem` | flag off; contenção temporária precisa teste |
| Região da cama mais hostil | CÓDIGO | `DreadSystem` | mobs extras marcados |
| Gatilho primeiro dano na superfície 7+ | RISCO | `GameEvents`, `TreeScene` | flag off; superfície heurística |
| Dimensão plana isolada ensolarada | RISCO | `ClearingProvider`, `TreeScene` | sem compile/playtest |
| 5–10s espera, crescimento 20–40s | CÓDIGO | `TreeScene`, `TreeMath` | usa 8s + alvo 30s |
| Árvore 80–120, tronco e folhas | CÓDIGO | `TreeMath`, `TreeScene` | ~110b; carga real por tick desconhecida |
| Vegetação simultânea | CÓDIGO | `TreeScene` | capim intercalado na construção |
| Interior oco, 4b de altura, baú | CÓDIGO | `TreeMath`, `TreeScene` | precisa QA geométrico visual |
| Loop espacial de retorno ao redor árvore | PARCIAL | `TreeScene` | teleporte visível possível |
| Proteção de blocos, dano, fome, inventário | PARCIAL | `GameEvents`, `TreeScene` | protege formas comuns; inventário GUI não totalmente bloqueado |
| Retorno seguro e livro "3 Dias" | CÓDIGO | `TreeScene`, `StoryData` | entrega anti-duplicação; não testado in-game |
| Horário aparente igual, total time não revertido | CÓDIGO | `TreeScene` | `setWorldTime(savedTime)` |
| Mundo normal pós-árvore, cicatrizes permanecem | PARCIAL | `GameEvents`, `DreadSystem` | fog/day6 para, entidades registradas voltam |
| Figura mais frequente após árvore | PARCIAL | `GameEvents`, `PresenceSystem` | probabilidade/cooldown ainda necessita ajuste |
| Singleplayer | ALVO | arquitetura | multiplayer não testado/suportado |
| Compatibilidade Dynamic Lights | PENDENTE | - | externo e opcional; sem integração binária |

Nunca interpretar 'CÓDIGO' como mod funcional: compilar e testar dentro do Minecraft ainda é obrigatório.
