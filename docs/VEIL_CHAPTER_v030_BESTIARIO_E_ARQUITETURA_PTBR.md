# Veil Chapter v0.3.0 — Bestiário e Arquitetura (PROPOSTA)
> Status: design aprovado parcialmente pelas 30 respostas de 2026-10-13; nenhuma das cinco fichas está implementada por este documento. A cena da árvore foi retirada da linha principal de desenvolvimento.
> Fonte de código: dev/no-tree-chapter1-v030; preservar versões anteriores em suas branches.

## Contrato das escolhas
1B 2C 3B 4D 5B 6D 7D 8D 9D 10D 11D 12D 13D 14D 15D 16D 17D 18D 19D 20D 21D 22D 23D 24C 25C 26A 27D 28D 29C 30D.
- Cinco entidades principais; origens parcialmente compartilhadas, parcialmente independentes; Watcher é independente, ambíguo.
- Guillhard pode coexistir como mitologia, sem compromisso de que cada criatura seja um aspecto.
- Pistas e documentos discretos; modelos, faces, tamanhos, animações, sentidos e reações próprios.
- Encontros por contexto, bioma, nível de profundidade, hora, nível de luz, história e cooldown.
- Perigos variáveis; defesa descoberta pela observação, não pela violência obrigatória.
- Eventos em cadeia de 2–3 ações, jamais efeitos ilimitados empilhados.
- Dia 7 permanece ativo até definição posterior da trama; SEM substituto automático à árvore.
- Primeira entrega funcional: três criaturas completas. Ordem: aperfeiçoar Watcher e Peeker, acrescentar Inquilino.
- Modelagem original; áudio espacial original; debug com comandos, logs, cena de testes e QA automatizada.

## Inventário de sistemas existentes (auditado na branch de base)
- EntityWatcher: lógica de observação / tentativas de aparecimento, distância de visão contextual, desvanecimento, aproximação invisível; revisar sincronização e multiplayer.
- EntityPeeker: renderização quadrilateral leve para olhos ou cabeça; 3 s de aparição, sem colisão.
- Presenças de cama: chance por tentativa, exige que jogador ainda durma após 20 ticks; no solo o salto da noite pode inviabilizar o evento.
- DreadSystem: sons 3D de sussurros, risos e respiração; efeitos do dia 6.
- AnomalySystem: bloco flutuante e mineração anômala; alterações destrutivas extras protegidas por configuração.
- StoryData: arquivo narrativo no Overworld, dias 1–7; sem reset de campanha ao migrar.
- LegacyClearingRecovery: dimensão 713 mantida estritamente para recuperar saves antigos.
- GUI/lógica ritual da árvore NÃO faz parte do jogo novo.

## Bestiário (nomes provisórios; arte 100% original)
### E01 — Watcher / O Observador
- Status atual: entidade existente, requer polimento real in-game.
- Introdução: dia 2, campo aberto, colina, linha de floresta; dia 4 pode observar casa sem penetrar.
- Silhueta: humanoide clássico, sombra quase sem detalhes; movimentos mínimos, ligeiro movimento de cabeça.
- Sentidos: distância, cone de visão do jogador, oclusão, situação da região.
- Máquina de estados: DORMANT > SEEK_VANTAGE > WATCH > SPOTTED > HIDE | VANISH; reposição só fora de vista.
- Fim: duração máxima, falha no posicionamento, mudança de dimensão, chunk descarregado, observação prolongada.
- Ameaça: nunca causa dano físico direto neste arco. Sons ocasionais, não telegráficos.
- Testes: ver realmente à frente, 180°/360° câmera, colinas, árvores, casas, dia/noite, multiplayer e render distance baixa.

### E02 — A Fresta / O Espreitador
- Status atual: EntityPeeker existe com quads e olhos; precisa modelo apropriado, orientação e oclusão validados.
- Introdução: dia 3; preferências: paredes, árvores, pedras, topos de ravinas, vidraças com profundidade.
- Silhueta: duas pupilas pálidas e crânio parcial; não exibir cabeça inteira sempre.
- Sentidos: posicionamento geométrico e direção do observador.
- Estados: HIDDEN > RISE > PEEK > RETRACT > DESPAWN; algumas variantes só mostram olhos.
- Ameaça: psicológica; nunca colisão, ataque ou bloqueio da construção.
- Testes: 1-3 blocos de altura, transparência de vidro, LOD, câmera 1ª/3ª pessoa, parede diagonal, relogin.

### E03 — O Inquilino / O Intruso
- Status: inteiramente novo. Introdução: dia 4 à noite.
- Forma: humanoide muito estreito e alongado (2.1–2.5 blocos provisórios), cabeça torta; mãos quase imóveis.
- Habitat: casas com entradas/saídas e visibilidade válidas. Nunca entra em local protegido por falha de colisão.
- Sentidos: oclusão, interior/exterior, iluminação, janelas, sono, proximidade da cama, cooldown.
- Estados: WAIT_OUTSIDE > APPROACH_WINDOW | APPEAR_CORNER > STARE > LEAVE; raríssimo INDOOR_STILL.
- Ameaça: não persegue ativamente na versão inicial; pode assustar com respiração e deslocamentos sem dano.
- Defesa: aproximação deliberada, iluminação ou campo de visão contínuo faz retirar-se.
- Testes: casas sem janelas, vidro, portas, blocos decorativos, modded blocks, cama ausente, casas subterrâneas e multiplayer.

### E04 — O Eco / Manifestação Acústica
- Status: conceito, não implementado. Introdução: dia 5.
- Forma: presença distorcida de tamanho variável; por vezes sem corpo visível.
- Habitat: cavernas, túneis e estruturas fechadas; pode projetar sons em trajetos desconexos.
- Sentidos: eventos de mineração/movimento, oclusão e geometria de corredor; não registra microfone nem voz real.
- Estados: LISTEN > IMITATE > REPOSITION > MANIFEST > WITHDRAW.
- Audio: Foleys originais de passos, mineração, pequenos chamados sem reprodução de voz do jogador.
- Defesa: reconhecer padrões artificiais; a ausência de fonte visual é proposital.
- Ameaça: desorientação, efeitos temporários e encontros limitados; sem danos obrigatórios.
- Testes: audio mono, espacialização, mono/stereo QA, saída de cavernas, superfícies e multiplayer.

### E05 — O Arrastado / Predador de Túneis
- Status: conceito, não implementado. Introdução: dia 6, raramente no dia 7.
- Forma: membros dissimétricos, locomoção rastejante, silhueta quebrada; animação desacelerada fora de perseguição.
- Habitat: cavidades largas, caminhos subterrâneos com navegação comprovada.
- Sentidos: ruídos e linha de visão, regras de luz e bloqueios; alarmes sonoros diegéticos antes do perigo.
- Estados: REST > INVESTIGATE > REVEAL > STALK > CHASE > LOST_TARGET > RETREAT.
- Ameaça: dano físico configurável durante perseguições raras; habilidade de fugir sem cavar/destruir casas.
- Defesa: barreiras, linha de visão, iluminação ou espaço estreito, regras fixadas após protótipo.
- Testes: pathfinding e hitbox 1.12.2; redstone doors, slabs, água, escadas, túneis, TPS, agro cancel.

## Arquitetura de produção
- ChapterCoordinator — integra eventos Forge e não altera o relógio já salvo sem motivo.
- HorrorDirector — mantém por jogador próximo evento narrativo, seleção contextual, intensidade, cooldown, orçamento.
- EntityEventRegistry — tabela versionada de descritores para observações, espies, casa, cavernas, som e encontros maiores.
- SpawnValidator — checa chunks já carregados, colisão, visão, iluminação, ambiente e compatibilidade.
- HorrorEntityBase — subclasses/estratégias com estado explícito, max lifetime, tracking e cancelamento.
- BehaviorControllers — regras individuais por criatura; não misturar AI de cada monstro no coordenador de eventos.
- AnimationBridge — opção vanilla ModelBase para entidades leves, GeckoLib3 apenas após teste de integração.
- SoundscapeDirector — sonidos por evento e entidade, volume e fonte 3D; silêncio é recurso deliberado.
- WorldEffectLedger — alterações persistentes temporárias com registro, restauração segura e respeito a edições do jogador.
- DebugHarness — comandos /veiltest entity <id>, /veiltest event <id>, /veiltest ai <id>, /veiltest perf.
- StoryData v2 — manter chave original; adicionar campos novos opcionais com valores default; estados transitórios por jogador quando necessário.
- ClientSide — somente render/modelos/texturas; pacotes não acessam mundo em thread de rede, agendar thread principal.

### Regras anti-bug
1. Eventos não geram chunks fora da visão, jamais calculam pathfinding em regiões descarregadas.
2. Limite provisório: uma cadeia principal ativa por jogador, no máximo 2–3 ações; sinais secundários limitados por cooldown.
3. Qualquer entidade transitória tem tempo de vida e cleanup em logout, morte, mudança de dimensão e servidor finalizado.
4. Proteger saves, construções, tile entities e blocos colocados pelo jogador; risco de mundo persistente precisa ser opt-in.
5. Não registrar IDs de entidades usando valores já publicados; 0=watcher e 1=peeker permanecem reservados.
6. Todas as ações autoritativas no servidor; renderização no cliente; validar comportamento em multiplayer.
7. Evitar bloqueio de controles sem mecanismo de liberação verificado; cama deve respeitar salto de noite do vanilla.

## Plano de produção, sem simular status de entrega
P0: corrigir e confirmar build da branch sem árvore; migrar saves antigos com teste em cliente.
P1: HorrorDirector + EventRegistry + estado/cooldown/seleção determinística e QA offline.
P2: Watcher final e Fresta com modelos originais, rig, efeitos e geometria; 1ª entrega.
P3: Inquilino completo com comportamento doméstico, hitbox, áudio e cenários de teste; marco de 3 entidades completas.
P4: Eco e Arrastado em separado; modelos próprios, áudio, navegação, agressão e contra-medidas.
P5: narrativa ambiental dias 2–7, pistas, dupla/trio de eventos, relações entre entidades; sem final compulsório.
P6: benchmark FPS/TPS, redução de repetições, áudio 3D, testes cliente+servidor, integração saves e pacote QA.

## Ferramentas e referências verificadas
- [GeckoLib 3.0.31 Forge 1.12.2](https://www.curseforge.com/minecraft/mc-mods/geckolib/files/4020277) existe como build, mas 1.12.2 é versão legada sem suporte ativo: teste de integração antes de torná-la dependência.
- [Blockbench+GeckoLib plugin](https://github.com/JannisX11/blockbench-plugins/blob/master/plugins/geckolib/about.md) para fontes editáveis .bbmodel, modelos e animações.
- [Forge 1.12 networking](https://docs.minecraftforge.net/en/1.12.x/networking/simpleimpl/) ressalta thread principal e segurança de chunks.
- [Forge 1.12 WorldSavedData](https://docs.minecraftforge.net/en/1.12.x/datastorage/worldsaveddata/) garante persistência com markDirty().
- [From The Fog](https://modrinth.com/datapack/from-the-fog/version/i5klIEP6) demonstra mecânicas de observação, espreitamento e pesadelo; usar somente princípios, jamais copiar ativos.
- [The Man From The Fog](https://www.curseforge.com/minecraft/mc-mods/the-man-from-the-fog) usa observação/ameaça; não reproduzir assets ou código sob licença própria.
- [Cave Dweller reimagined](https://modrinth.com/mod/reimagined-cave-dweller) fornece referência de criatura de caverna original e linguagem de animação; não clonar modelo.
