# Veil Chapter v0.2.6 — Tree Ritual (QA e playtest)

## Estado

**ALPHA PARA TESTES.** Implementada sobre a v0.2.5 de áudio 3D; sem remover as mecânicas normais do mod. Compilação Forge e início do servidor de produção foram verificados. A sequência completa com jogador, FPS real e visibilidade não foram comprovados automaticamente.

## Mudanças implementadas

1. **150 segundos**, com a progressão planejada: 0–20s silêncio e preparo discreto de chunks; 20–45s raízes; 45–85s tronco e galhos; 85–125s contorno de copa; 125–150s refinamento. O baú só aparece depois de todos os blocos serem processados.
2. **24 operações de bloco por tick**, em vez do limite antigo de 220. Pré-carregamento em lotes de até um chunk a cada dez ticks durante o silêncio inicial, e no máximo um chunk faltante por tick durante a construção.
3. **Geometria mais leve e folhas persistentes**: remove madeira e folhas invisíveis dentro da estrutura, preserva o formato externo do tronco e da copa, inclui galhos e raízes radiais; protege o corredor de entrada ao sul. As folhas da copa recebem estado com decaimento desativado e o CI rejeita folhas que possam desaparecer naturalmente.
4. Ponto inicial da dimensão deslocado para perto do tronco, olhando para a entrada. Jogador livre para andar; sem câmera obrigatória.
5. Três aparições **não hostis** e identificadas do Watcher: uma no começo, outra durante o crescimento e a última junto à entrada depois da conclusão; cada uma desaparece automaticamente.
6. Mensagens na actionbar em português: **“Você demorou.”**, **“Tudo começa abaixo de nós.”**, **“Observe.”**, **“Mesmo aquilo que foi esquecido continua crescendo.”**, **“Três dias.”**, e ao final **“Agora, entre.”**
7. O áudio da dimensão continua silencioso, exceto pelos novos sons **posicionais roteirizados do próprio mod**.
8. Estado de aparições persistente, impedindo repetição ao retomar o mundo. Mantidas as proteções contra quebra/dano, o baú, o livro **“3 Dias”** e o retorno ao Overworld.

## Blocos reais validados

| Fase | Operações |
| --- | ---: |
| Raízes e base | 5.855 |
| Tronco e galhos | 10.376 |
| Copa/silhueta | 11.176 |
| Detalhes da copa | 9.213 |
| **Total** | **36.620** |

O planejamento anterior tinha aproximadamente **69.396** operações. A redução estimada é de **47,2%**. O verificador no servidor de produção comprovou limites por etapa, ausência de posições duplicadas e corredor de passagem livre (largura 5, altura 4). A redução teórica de trabalho **não equivale a um benchmark de FPS**.

## Testes reproduzíveis

- [GitHub Actions: compilação e auditoria do JAR](https://github.com/Fallzz16/VeilChapter1122/actions/workflows/forge-1122-ci.yml)
- [GitHub Actions: Forge de produção e marcador TREE_RITUAL_PLAN_OK](https://github.com/Fallzz16/VeilChapter1122/actions/workflows/forge-production-smoke.yml)

A automação não possui um jogador real, portanto não testa o movimento, a orientação da câmera, o carregamento de chunks pelo cliente nem o baú aberto pelo jogador.

## Como testar

1. Use **Minecraft Java 1.12.2 + Forge 14.23.5.2859** em um perfil separado.
2. Remova o arquivo JAR anterior, instale o **v0.2.6** e crie **um mundo descartável**, com cheats ativados. **Faça backup antes de ativar a dimensão**.
3. Em `config/veilchapter.cfg`, configure `B:enableTreeSequence=true`, salve com o Minecraft fechado e reinicie.
4. No Overworld, execute `/veiltest day 7` e `/veiltest tree`.
5. Use `/veilstatus` para observar `arvoreEtapa`, `blocosConstruidos` e `aparicoesRitual` (deverão chegar a 2, 36620 e 3 respectivamente).
6. Teste a sequência de ponta a ponta: ficar no chão, olhar a copa, caminhar até a entrada, abrir o baú, voltar à cama/spawn e receber **“3 Dias”**. Teste também sair e reabrir o jogo durante o crescimento.

**Importante sobre visibilidade:** como a copa supera 100 blocos de altura, distâncias de renderização pequenas podem ocultá-la mesmo após construída. Experimente 4, 6 e 8 chunks e anote o resultado. O mod não modifica automaticamente suas opções de renderização.

## Limitações e diagnóstico

- Não há benchmark real de TPS/FPS no cliente; o novo limite de 24/tick deve ser avaliado no jogo.
- A silhueta completa não pode ser garantida com qualquer distância de renderização.
- Ainda não há teste automatizado de colisões durante a animação, retomada após crash, streaming de chunks visuais ou textura da entidade em cada uma das três posições.
- Se algum problema ocorrer, envie `logs/latest.log` ou o arquivo em `crash-reports` e informe `/veilstatus`, distância de renderização e momento da cena.
