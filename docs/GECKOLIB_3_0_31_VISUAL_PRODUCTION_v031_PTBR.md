# Veil Chapter 0.3.1 — Rigs e Animações Profissionais (BRANCH EXPERIMENTAL)

## Decisões técnicas
Minecraft 1.12.2 + Forge 14.23.5.2859 + **GeckoLib3 3.0.31**. O JAR GeckoLib deve ser instalado separadamente em `mods`; NÃO embutimos o software de terceiros nem infringimos licenças de outros mods.
Instalação exata: https://www.curseforge.com/minecraft/mc-mods/geckolib/files/4020277
A API e a dependência estão PINADAS; outras versões não foram validadas.
Fonte: https://github.com/Fallzz16/VeilChapter1122/tree/dev/geckolib-art-rigs-v031

## Arquitetura e assets
Quatro modelos bone-based originais no formato GeckoLib/Blockbench 1.12 JSON:
- watcher: 16 bones, 3 clips (idle, observe, bed)
- peeker: 9 bones, 3 clips (emerge, hold, retract), geometria alternativa peeker_eyes.geo.json
- tenant: 19 bones, 3 clips (idle, stare, retreat)
- dragged: 20 bones, 4 clips (idle, crawl, retreat, strike [reservada para sincronização com dano])
- echo: permanece invisível, entidade auditiva espacial.

Texturas novas: watcher_geo.png, peeker_geo.png, tenant_geo.png, dragged_geo.png, todas **128x128 RGBA**, paleta original, riscos discretos, veios, realces e atlas por material. Não são imagens de mods de terceiros.
Atlas e geometria ficam em assets/veilchapter/geo, assets/veilchapter/animations e assets/veilchapter/textures/entity.
Animações interpoladas por GeckoLib3; controladores fazem transição suave de estados por entidade.

## Testes
1. Mantenha cópia do mundo e use apenas um VeilChapter JAR.
2. Instale GeckoLib 3.0.31 FORGE 1.12.2 como dependência. Não use GeckoLib 4/5 ou outro Minecraft.
3. Use /veiltest day 4; /veiltest watcher; /veiltest peeker; /veiltest tenant e teste de costas/lado/frente, sombras, clipping, occlusion, transições.
4. Em caverna escura: /veiltest day 6, /veiltest dragged, /veiltest echo; observe crawl e sons.
5. Teste F5, chuva, luz de tochas, variações de render distance, tempos longos, desconectar, mundo salvo/reaberto e 2 jogadores.
6. /veilstatus. Dia 7 ainda não transporta o jogador para árvore; ramo de recuperação de dimension 713 segue exclusivamente para saves antigos.

## Limites e critérios
- Testes automáticos de asset/modelo garantem existência, dimensão, parentesco de ossos, keyframes e zip integridade; NÃO substituem client-side render.
- A arte é mais detalhada, mas NÃO é final sem validar câmera, textura UV, proporções e interpolação no jogo.
- Não há autoinstalador do GeckoLib. Download é pela página do projeto.
- Risco de performance da biblioteca deve ser medido no cliente antes de mesclar o branch em main.
- Assets e classes vanilla antigas continuam guardadas na branch anterior, com uma rota de rollback.
- Sons originais do próprio mod preservados; vozes de outras obras não foram copiadas.
