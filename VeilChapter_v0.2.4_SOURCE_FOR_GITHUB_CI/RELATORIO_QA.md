# QA — Veil Chapter v0.2.0-alpha

**Status: FONTE PRONTA PARA PRIMEIRA TENTATIVA DE BUILD, JAR NÃO VERIFICADO.**

## Evidências verificadas neste ambiente

- O arquivo inicial ForgeBase1122 foi inspecionado e a nova estrutura foi construída em diretório separado, sem sobrescrever o original.
- Código puro `StoryClock`, `ProximityGate`, `HorrorPacing`, `TreeMath` compilado com `javac --release 8` e executado: **235 verificações offline passaram**.
- PNG watcher 64×64 RGBA completamente preto criado, sem dados pessoais ou conteúdo de terceiros.
- `psst.ogg` criado localmente com áudio sintético; validação de formato em pipeline de QA.
- Gradle 1.12.2, Forge 14.23.5.2859, ForgeGradle 2.3 fork mantido, scripts Windows de bootstrap e compilação.
- Fonte Java analisado por `javac` sem dependências para erros de sintaxe aparentes. **Erros de símbolo não resolvido são esperados**, pois classes Minecraft/Forge não estão instaladas aqui; tal comando NÃO substitui compilação Forge.

## NÃO verificado

- Nenhum `gradlew build` bem-sucedido contra Minecraft/Forge, nenhuma execução `runClient`, nenhum JAR carregado em Minecraft.
- Nenhum teste de FPS/TPS, perfil de memória, animações, áudio espacial real, geometria renderizada, dimension travel e respawn em save.
- Nenhuma compatibilidade testada com Dynamic Lights, shaders, outros coremods, multiplayer ou mundos existentes.
- Módulos de risco off por padrão. São implementações provisórias e exigem validação antes de serem habilitados.

## Aceite mínimo antes de entregar versão beta em JAR

1. `gradlew clean build` produz JAR sem erro; `mcmod.info` carrega.
2. `gradlew runClient` abre mundo novo; primeira manhã sem eventos; primeiro despertar ativa dia 2.
3. `/veilstatus`, `/veiltest day 2`, `/veiltest psst` funcionam sem erros.
4. Figura preta renderiza a distância 40–70 atrás, desaparece após 30 ticks de observação, ataques a removem.
5. Cegueira 100 ticks, zona de rearme 12 blocos, não sobreposta a cada tick.
6. Zumbis neutros ignoram o jogador de forma consistente; agressivos continuam atacando.
7. Blocos flutuantes expiram sem tocar construções do jogador.
8. Flags de risco habilitadas apenas em mundo descartável de teste com backup.
9. `/veiltest day 7` + `/veiltest tree` entra na dimensão sem portal; capim e árvore crescem em 20–40s sem travar.
10. Deslocar-se >180 blocos retorna à área da árvore, sem teleporte letal.
11. Abrir baú retorna à cama/spawn, livro correto, alimento inalterado, horário aparente restaurado.
12. Sair durante crescimento + reabrir: continuidade; sair após clique do baú + reabrir: retorno idempotente.
13. Inventário cheio mantém livro pendente até haver vaga, sem duplicação.
14. Concluir capítulo remove somente zumbis extras e restaura aldeões/animais que foram suspensos.
15. Após completar, a figura aparece com maior frequência e pode abrir portas só se não observada.
16. Só depois dos itens 1–15, testar Dynamic Lights 1.12.2 separado (sem embutir).

## Principais riscos abertos

- Compilação com Maven Central/ForgeGradle legacy indisponível no container (sem DNS externo); ainda podemos encontrar incompatibilidades de API no primeiro build.
- Alteração global efetiva de skylight NÃO implementada, sob risco de corromper iluminação/spawns/saves.
- Dificuldade em reconhecer a casa, janela e abertura de forma generalizada.
- Temporização de árvore dependente de chunks e performance, possível crescimento incompleto quando render baixo.
- Preservar UUID/NBT de entidades após saída de chunks e outros mods ainda necessita matriz de testes.

**Conclusão:** teste offline positivo não significa mod jogável. O próximo passo técnico obrigatório é executar build e um playtest real em ambiente que consiga baixar o MDK/Forge. Se falhar, preservar código e enviar logs para corrigir o erro específico.
