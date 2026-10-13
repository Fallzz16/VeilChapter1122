# Veil Chapter 0.3.0 Alpha 2 — Eco e Arrastado

## Alterações
Nova branch de expansão baseada em dev/entities-first-three-v030. Inclui cinco tipos de entidade registrados: Watcher 0, Fresta 1, Inquilino 2, Eco 3 e Arrastado 4. Preserva os dados narrativos e o ritual da árvore continua removido.

### O Eco — EntityEcho
Uma entidade não-física e propositalmente invisível. Sua única presença é uma fonte sonora 3D: sussurro, pedra abafada e respiração em sequência. Duração máxima aproximada de 5,4 s. Apenas em cavernas escuras e chunks já carregados depois do dia 5. Não agride, não interage com blocos e não tem colisão.

### O Arrastado — EntityDragged
Entidade subterrânea física com modelo autoral 3D assimétrico (posição agachada, braços longos) e textura autoral 64 × 64. Aparece raramente em cavernas a partir do dia 6. Procura caminho ao jogador com atualizações espaçadas, desiste diante de iluminação forte (12+) ou perda prolongada de visibilidade. Duração máxima ~16,5 s. Dano padrão = 1 HP/meio coração com intervalo de pelo menos 42 ticks. Sem dano ao mundo, loot ou teleporte.

## Configuração
enableEcho=true; echoAttemptChance=53;
enableDragged=true; draggedAttemptChance=20;
draggedAttackDamage=1.0 (pontos de HP). Para desativar o perigo físico, enableDragged=false, sem desativar os encontros psicológicos.

## Testes sugeridos
- Faça backup e instale só um JAR do VeilChapter (Forge 1.12.2/Java 8).
- /veiltest day 6, vá a caverna escura, digite /veiltest echo e depois /veiltest dragged.
- Verifique áudio posicional com fones, hitbox, perseguição, iluminação, teto baixo, fuga, dano, FPS e despawn.
- Tente esses comandos na superfície; nenhum deve invocar no exterior.
- Reabra o mundo e teste novamente; confira eventos próximos a dois jogadores.
- Teste /veiltest watcher, /veiltest peeker e /veiltest tenant.
- No dia 7, não existe árvore nem conclusão forçada.

## Origem de assets e limitações
- dragged.png é uma textura RGBA pixel-perfect original 64 × 64 feita para esta versão, sem sprites de terceiros.
- ModelDragged.java e RenderDragged.java usam apenas renderização nativa de Minecraft/Forge 1.12.2.
- O Eco utiliza os sons ambientais do próprio mod já existentes e efeitos de pedra vanilla, sem copiar gravações de outros mods.
- Esta branch é uma **candidata experimental**; compilação, servidor e testes estáticos não substituem teste visual/sonoro real no cliente.