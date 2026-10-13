# Veil Chapter v0.2.7 — Watcher Spatial Voice (playtest)

## Situação real

**Código da atualização preparado em branch isolada, ainda sem JAR validado.** A v0.2.6 da branch main continua intacta. Precisamos enviar **15 OGG** (5 falas × campo aberto / abafado / interior) para esta branch antes da compilação. Nada de música foi integrado.

## Como enviar os áudios (uma única vez)

1. Baixe o ZIP `VeilChapter_v0.2.7_Watcher_Spatial_15_OGG_UPLOAD_BRANCH.zip` fornecido no chat.
2. Extraia no Windows. Nele há `src/main/resources/assets/veilchapter/sounds/watcher/` com **15 arquivos .ogg**.
3. No GitHub, selecione a branch **`dev/watcher-dual-voice-tree`**, NÃO `main`.
4. Navegue até **`src/main/resources/assets/veilchapter/sounds/watcher/`**.
5. Clique em **Add file → Upload files** e selecione **somente os 15 OGG** de dentro dessa pasta extraída. Não envie uma pasta extra com o nome do ZIP.
6. Clique em **Commit changes** na branch de desenvolvimento.
7. O workflow da branch deverá conferir formato mono 48 kHz, SHA-256, 15 eventos e conteúdo do JAR. Se falhar, confira os arquivos e o log; não instale o JAR da execução reprovada.

O arquivo `WATCHER_SPATIAL_ASSETS_QA.json` JÁ está no GitHub e precisa corresponder aos OGG deste ZIP. Não é necessário enviá-lo novamente.

## Cena do dia 7

A árvore continua com crescimento de ~150 segundos, sem alteração em sua geometria. As cinco falas completas têm início previsto em:
- 00:12 — chegada
- 00:34 — confiança
- 00:59 — tragédia da vila (13,99 s, preservada inteira)
- 01:35 — desesperança
- 02:17 — convite final

Cada fala é disparada apenas uma vez e pertence a uma aparição do Watcher. A posição da fonte de som é a posição da própria entidade no momento de falar; ele some depois de terminar, com pausas entre as aparições. Reentrar no save não repete automaticamente falas antigas. O baú só abre ao término da árvore.

## Acústica aproximada

- `open`: voz original já mesclada, rádio intacto.
- `muffled`: versão de voz com filtro passa-baixa, para obstrução detectada por dois raios entre o Watcher e o jogador.
- `indoor`: reflexões discretas se o ponto de origem ou o jogador estiver sob teto baixo e cercado por paredes.
- Direcionalidade esquerda/direita/frente/trás produzida pelo OpenAL do Minecraft com áudio OGG **mono**.
- Não há DSP contínuo; se a pessoa atravessar uma parede **durante** uma fala, o timbre não mudará até a próxima fala. Isso evita processamento desnecessário.

O áudio narrativo fica na categoria **Vozes** das opções de som. Os 14 efeitos da v0.2.5 permanecem como antes. Nenhuma música foi inserida.

## Comandos propostos e implementados na branch

```text
/veiltest watcher voice 1 left
/veiltest watcher voice 2 right
/veiltest watcher voice 3 behind
/veiltest watcher voice 4 front
/veiltest watcher voice 5 left
/veiltest day 7
/veiltest tree
/veilstatus
```

As cinco falas do teste direcional precisam ser reproduzidas em um mundo descartável no Overworld com cheats ativados. Para ouvir a versão abafada, faça uma parede sólida entre sua posição e a origem do som. Para ouvir a versão interior, use uma pequena sala de paredes e teto. O comando informa no chat OPEN/MUFFLED/INDOOR.

**Cuidado:** `/veiltest tree` modifica o estado narrativo. Use um MUNDO NOVO. A configuração `B:enableTreeSequence=true` precisa estar ativada com o jogo fechado e carregada após reiniciar.

## O que NÃO foi validado

- Compilação / geração de JAR com os novos 15 OGG, até recebermos os arquivos na branch.
- FPS do cliente, balanceamento de volume percebido em fones, fidelidade do som ambiente e aparições reais.
- Áudio reagindo dinamicamente durante a reprodução: a acústica é escolhida no início de cada fala.

Documentação de referência: Forge/Minecraft 1.12.2 usa `World.playSound`, `SoundCategory.VOICE` e `World.rayTraceBlocks`.
