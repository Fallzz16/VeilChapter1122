# Veil Chapter — Minecraft Forge 1.12.2

**Status em 12/10/2026: ALPHA COMPILADA E TESTADA NA INICIALIZAÇÃO DE SERVIDOR.**
**NÃO é um capítulo completo nem foi aprovada em teste visual/gameplay.**

## O que foi validado

- GitHub Actions com JDK 8, ForgeGradle 5.1.74 e Gradle 7.3.3 compilou e reobfuscou o código Java.
- A inspeção do JAR confirmou `br/veilchapter/VeilMod.class`, `mcmod.info`, a textura de Steve preto e o áudio OGG.
- Testes de lógica independentes e checagens de estrutura/recursos passaram.
- O teste de produção instalou o Forge oficial **1.12.2-14.23.5.2859** em um ambiente descartável, colocou o JAR real em `mods`, e o servidor iniciou com `veilchapter` identificado e mensagem `Done`.

Registros de auditoria:
- [Build aprovado](https://github.com/Fallzz16/VeilChapter1122/actions/runs/38321346638)
- [Servidor de produção aprovado](https://github.com/Fallzz16/VeilChapter1122/actions/runs/38321978158)

**Ainda não validado:** cliente gráfico, efeito de cegueira, renderização da figura, interação com portas, teleporte da árvore e testes longos de salvamento/recuperação. Evite mundos importantes.

## Instalação do JAR compilado (não confundir com ZIP de código-fonte)

1. Faça backup de seus mundos e crie um **perfil isolado** para Minecraft Java **1.12.2**, com Forge **14.23.5.2859** e Java 8.
2. Baixe o artefato `veilchapter-1122-COMPILED-NOT-GAMEPLAY-TESTED` no [build aprovado](https://github.com/Fallzz16/VeilChapter1122/actions/runs/38321346638). Extraia o JAR do ZIP.
3. Copie **somente** `VeilChapter1122-0.2.4-alpha-internal.jar` para a pasta `mods` do perfil Forge 1.12.2.
4. Inicie o Minecraft e crie um **mundo de teste com Cheats ativados**.
5. Confirme o mod na tela de mods e teste no chat.

Comandos de teste:
- `/veilstatus` — mostra fase atual, árvore e livro pendente.
- `/veiltest day 2`, `/veiltest day 4`, `/veiltest day 6`, `/veiltest day 7` — força fases narrativas.
- `/veiltest psst` — reproduz o som do mod.
- `/veiltest tree` — solicita a Árvore da Vida, **se** `enableTreeSequence=true` e o dia narrativo for 7 ou superior.

O comando `/veiltest` altera o estado do mundo: não use no seu save principal.

## Recursos presentes e limitações

Implementados em nível de código: relógio narrativo, observador preto, detecção aproximada de visão, efeito de cegueira, som “psst”, anomalias de blocos e mineração, zumbis com comportamento variável, dia 6 parcial, dimensão isolada, árvore procedural, livro “3 Dias” e salvamento.

**Recursos experimentais desabilitados por padrão no arquivo `config/veilchapter.cfg`:**
- `B:allowWorldDamage=false` — incêndios e quebras espontâneas.
- `B:enableEntityStasis=false` — remoção/restauração temporária de animais e aldeões.
- `B:enableSeamlessTeleports=false` — teleporte ambiental.
- `B:enableTreeSequence=false` — sequência e dimensão da árvore.

Para ensaiar a árvore, altere `B:enableTreeSequence=true` **apenas em mundo novo de teste**, reinicie o jogo e use `/veiltest day 7` seguido de `/veiltest tree`. A proteção e o retorno ainda não foram confirmados em gameplay. Faça cópia do mundo antes.

**Funcionalidades ainda incompletas:** escurecimento real dos níveis de skylight do dia 6 (somente névoa/cor e mobs extras estão implementados), reconhecimento preciso de janelas/casas, todas as vozes humanas, IA completa dos zumbis, comparação visual perfeita em teleportes e multiplayer.

`Dynamic Lights` NÃO foi incorporado nem redistribuído; será testado opcionalmente no futuro.

## Compilar a partir do código

- JDK 8.
- Windows: `1_PREPARAR_WINDOWS.bat` e `3_COMPILAR_WINDOWS.bat`.
- Linux/CI: `bash tools/preparar_mdk.sh && ./gradlew --no-daemon clean build`.
- Saída: `build/libs/VeilChapter1122-0.2.4-alpha-internal.jar`.
- O wrapper MDK oficial tem SHA-1 verificado; dependências vêm dos repositórios configurados no `build.gradle`.

A inicialização de desenvolvimento via `gradlew runServer` apresentou uma incompatibilidade de classpath de bibliotecas internas do Forge antigo com ForgeGradle 5. O teste com o **servidor de produção** passou; por isso o build e o ambiente de desenvolvimento devem ser avaliados separadamente.

## Se der erro dentro do jogo

Envie `logs/latest.log`, ou `crash-reports/*.txt` se houver crash, mencionando o comando e o dia narrativo. Não tente salvar repetidamente uma dimensão experimental em um único mundo sem backup.

**Esta é uma build ALPHA de teste, não uma release final de gameplay.**
