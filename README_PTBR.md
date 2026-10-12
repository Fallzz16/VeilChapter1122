# IMPORTANTE: HOTFIX v0.2.3

Leia [HOTFIX_BUILD_0.2.3_LEIA_PRIMEIRO.md](HOTFIX_BUILD_0.2.3_LEIA_PRIMEIRO.md). O novo compilador utiliza ForgeGradle 3.0.140 + Artifactural 1.0.13 (Forge oficial), sem Maven Velocity.

# IMPORTANTE: CORRECAO DO BUILD 0.2.1

Esta versão usa ForgeGradle 3.

# Veil Chapter — Terror Psicológico (Minecraft Forge 1.12.2)

**VERSÃO: v0.2.0-alpha de código-fonte. ESTADO: pré-compilação / pré-playtest.**

> **IMPORTANTE:** Este ZIP é o PROJETO FONTE e **não é um mod `.jar` instalável**. Não houve compilação completa com Forge 1.12.2 nem validação dentro do Minecraft neste ambiente, porque as dependências antigas do ForgeGradle não puderam ser baixadas. Scripts para compilar e testar em Windows estão incluídos. Faça **backup do mundo** antes de instalar qualquer JAR que resultar da compilação.

## O que foi preparado

- Um projeto **Java 8 / Forge 1.12.2 (14.23.5.2859)** com código original organizado e scripts para compilar.
- Relógio narrativo: dia 1 sem eventos, primeiro despertar ativa dia 2; avanço por dias subsequentes; morte não reinicia.
- Entidade própria do **Steve completamente preto**, sem IA de combate, dano ou drops, com renderizador e textura dedicados.
- Tentativas de aparição atrás do jogador a 40–70 blocos; aparições próximas da cama a partir do dia 4; observação com prazo de 1,5 s e, após a árvore, de 3–6 s; ataque faz entidade desaparecer.
- Distância de cegueira: `<10` blocos por 5 segundos; rearma apenas ao se afastar até `>=12`; reposicionamento durante cegueira.
- Passos/sons posicionais e áudio experimental **psst** em OGG original; sons ilusórios de portas sem mudança de blocos a partir do dia 4.
- Blocos flutuantes reais de pedra/terra que expiram em 2–5 minutos se permanecerem intactos.
- Mineração ocasionalmente lenta; quebra isolada de folhas e teleporte discreto, ambos protegidos por **flags experimentais**, sendo destruição desligada por padrão.
- Zumbis divididos individualmente entre passivos e normais; os passivos têm ataques anulados e seus alvos são limpos periodicamente (seguir/perseguir ainda precisa de testes in-game).
- Dia 6: spawns adicionais de zumbis na região da cama, marcação dos extras para remoção posterior; sons mais frequentes; fog/sky tint cliente experimental.
- Rotina NBT experimental de suspensão e restauração de animais/aldeões, **desligada por padrão**.
- Incêndios temporários e contidos ao redor do foco, **desligados por padrão**, pois ainda requerem validação.
- Dimensão especial superflat com grama, tempo de espera de 8s, crescimento em até 30s de árvore ancestral com altura aproximada de 110 blocos, entrada natural, interior oco de 4 blocos de altura e baú com o livro "3 Dias".
- Estado persistido via `WorldSavedData`; retorno à cama/ao spawn seguro, hora restaurada e livro pendente se inventário cheio.
- Comandos de diagnóstico `/veilstatus` e `/veiltest` (o segundo exige cheats/op).
- Core **testado offline** em Java puro; arquivos de fontes e recursos auditados estaticamente.

## Funcionalidades NÃO confirmadas / lacunas conhecidas

**Não foram realizadas compilação Forge, execução de Minecraft, medição de TPS/FPS, testes de salvamento reais, testes de compatibilidade ou QA visual.**

1. **Escuridão natural efetiva do dia 6**: por segurança, NÃO modifica globalmente os cálculos vanilla de skylight. O código muda apenas o tom da névoa/céu e cria mobs extras perto da cama. O requisito completo, que afeta nível de luz e spawns globalmente, **não está implementado**.
2. **Silêncio da dimensão:** sons não classificados como PLAYER são bloqueados no cliente. Ainda exige validar se todos os passos vanilla continuam audíveis.
3. **Zumbis passivos:** ataques são cancelados e alvos limpos, mas IA que tenta perseguir pode precisar de ajuste.
4. **Aparições domésticas:** usa cama e espaço caminhável como heurística; reconhecimento perfeito de janelas/entradas ainda não existe.
5. **Teleporte discreto:** compara blocos, luz e orientação em posições de chunks já carregados, mas não garante equivalência visual (flag `enableSeamlessTeleports=false`).
6. **Incêndios/reversão de entidades:** apesar de conterem mecanismos de salvamento, podem interagir com mods de terceiros; opt-in e backups exigidos.
7. **Crescimento:** a dimensão depende dos chunks carregados e do desempenho; com renderização extremamente baixa pode parar antes de concluir. O tamanho e geometria exigem playtest.
8. **Loop espacial:** retorno por teleporte a uma área central ao afastar-se além de ~180 blocos; pode ser percebido, ainda não corresponde a loop visual perfeito.
9. **Mineração/entidades:** apenas tipos vanilla específicos receberam tratamento; máquinas/blocos de outros mods são intencionalmente excluídos de intervenções.
10. **Multiplayer:** ainda não suportado. A infraestrutura foi escrita visando apenas singleplayer.
11. **Sons:** o 'psst' é sintetizado proceduralmente, não uma gravação humana. Palavras e vozes faladas ainda estão pendentes.
12. **Teste de dimensão e transferência:** APIs do Forge consultadas, mas a transferência real de dimensões e o livro não foram validados.

## Requisitos

- Windows 10/11, Minecraft **Java Edition 1.12.2**, **JDK 8** instalado (não apenas JRE).
- Acesso à internet para Gradle, ForgeGradle e dependências legadas.
- Forge 14.23.5.2859 recomendado; use uma pasta de jogo de teste, jamais seu save principal.
- Memória recomendada para dev: 4–6 GB para Gradle; ao menos 2–4 GB para o cliente Minecraft.

## Passo a passo no Windows

1. Extraia o ZIP para uma pasta sem nomes muito longos, por exemplo `C:\Projetos\VeilChapter`.
2. Verifique `java -version` e `javac -version` no Prompt: ambos devem apontar para um **JDK 8**.
3. Execute **`1_PREPARAR_WINDOWS.bat`**. O script baixa apenas o wrapper do MDK oficial (verificando SHA-1).
4. Execute **`3_COMPILAR_WINDOWS.bat`**. Pode levar vários minutos na primeira execução. Se der erro, envie `build_log.txt`.
5. Depois da compilação bem-sucedida, o JAR de mod deverá estar em `build\libs\VeilChapter1122-0.2.0-alpha.jar`.
6. Instale esse JAR em `mods` de um perfil isolado com Forge 1.12.2, **somente depois de backup**, ou use `2_TESTAR_WINDOWS.bat` para iniciar o cliente de desenvolvimento.

**Não coloque o ZIP do código-fonte diretamente na pasta `mods`.**

### Problemas comuns

- `UnsupportedClassVersionError` ou Gradle não inicia: selecione **JDK 8** com `JAVA_HOME` correto.
- `Could not resolve com.anatawa12.forge:ForgeGradle`: conexão ao Maven Central indisponível. Não tente soluções que baixam binários de sites não verificados.
- `gradlew.bat not found`: execute a etapa 1, verifique internet e SHA-1 do MDK.
- `setupDecompWorkspace`/`runClient` falha: salve o log completo e nos envie; não altere arquivos ao acaso.
- Travamento ao entrar na dimensão: feche o jogo, guarde o log e restaure o backup; nunca insista no único save.

## Configuração `config/veilchapter.cfg`

Gerada pelo Forge no primeiro início bem-sucedido. Por segurança, são **false** por padrão:

- `allowWorldDamage` — quebras espontâneas de folhas + fogo (risco de alterações duradouras).
- `enableEntityStasis` — esconder e restaurar animais/aldeões.
- `enableSeamlessTeleports` — teleporte discreto de até 200 blocos.
- `enableTreeSequence` — gatilho do primeiro dano na superfície a partir do dia 7 + dimensão da árvore.

`enableAnomalies`, `enableWatcher` e `enableDaySix` vêm **true**. **Habilite flags perigosas SOMENTE em uma cópia de teste**. A restauração ambiental completa ainda não é garantida.

## Comandos de diagnóstico

- `/veilstatus`: mostra dia narrativo, evento da árvore, livro pendente e quantidade de criaturas em espera.
- `/veiltest day 2`, `/veiltest day 4`, `/veiltest day 6`, `/veiltest day 7`: acelera o teste manual da fase, exigindo cheats/op.
- `/veiltest psst`: reproduz o som experimental.
- `/veiltest tree`: precisa `enableTreeSequence=true` e dia >=7, solicita teleporte à dimensão especial.

Comandos de teste **alteram o progresso salvo**, portanto não use em um mundo que pretende continuar normalmente.

## Dependências externas e licenças

O `.jar` do **Dynamic Lights** enviado anteriormente **NÃO está incluso** e não foi modificado, copiado ou redistribuído. Seu uso com o capítulo 6 ainda precisa ser testado. Instale separadamente apenas se quiser experimentar e se a licença permitir; o nosso mod não depende dele.

O código original desta base está sob licença MIT no arquivo `LICENSE`. Recursos de terceiros (Minecraft/Forge/Dynamic Lights) **não estão inclusos no pacote**, exceto chamadas às suas APIs públicas em código-fonte. Os assets próprios `watcher.png` e `psst.ogg` são gerados localmente.

## Documentos

- `RELATORIO_QA.md`: o que foi verificado e o que ficou pendente.
- `MATRIZ_FUNCIONALIDADES.md`: rastreabilidade da especificação v1.5 para o código.
- `tools/testar_logica_offline.sh` e `tools/testar_logica_offline.bat`: testes sem Minecraft/Forge.

**Recomendação:** primeiro compilar a base, testar um mundo novo e nos enviar o primeiro `build_log.txt`. Essa é a informação indispensável para corrigir eventuais erros reais de compatibilidade antes de iniciar um playtest de sete dias.


## STATUS DE VALIDACAO EM OUTUBRO DE 2026

Esta pasta contem um candidato fonte de desenvolvimento, **NAO** um JAR compilado ou liberacao validada em gameplay. Os testes offline passaram, mas o projeto necessita build no GitHub Actions e teste dentro do Minecraft antes de uso em mundos de verdade.

Repositorio: https://github.com/Fallzz16/VeilChapter1122

Nao inclua builds ou caches no GitHub. A pasta `.github/workflows` contem a automacao de compilacao.
