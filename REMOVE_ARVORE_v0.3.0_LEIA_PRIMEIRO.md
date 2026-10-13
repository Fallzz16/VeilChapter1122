# Veil Chapter — Árvore da Vida excluída (base v0.3.0-dev)
## Retirado
- Evento da árvore no dia 7, gatilho ao receber dano, transporte automático, ritual, trava de câmera, voz/cutscene, porta, baú e livro do ritual.
- Comandos /veiltest tree e /veiltest watcher voice.
- SoundEvents e áudio exclusivas da árvore. As 14 faixas originais de terror ambiental continuam.
- Classes TreeScene, TreeMath, TreeRitualPacing, TreeCinematicTimeline, TreeRitualAudio e os handlers de cena.
## Preservado
- Dias 1–7 (o dia 7 fica ativo; não há novo final por enquanto).
- Watcher, espreitadores, cama, sons de terror, anomalias, efeitos do dia 6.
- Dados originais do mundo: chaves NBT existentes são lidas e os indicadores órfãos do ritual são limpos.
- DIM 713 continua registrada **apenas para recuperar saves antigos**. Jogadores que estiverem nela são transportados para o Overworld; a dimensão não pode mais ser acessada pelo mod.
- Mundos concluídos em versões antigas permanecem marcados como concluídos. Não reiniciamos uma campanha sem permissão.
- Arquivos e commits históricos continuam na branch antiga para backup. Não apague manualmente DIM713 de um save existente.
## QA/uso
- Faça backup do mundo antes de trocar o JAR, especialmente se já entrou na árvore.
- Substitua o JAR antigo; não instale ambas as versões.
- Os arquivos OGG de ritual da v0.2.9 não são mais necessários.
- Testar /veiltest day 7, sofrer dano e confirmar que não há teleporte.
- Testar mundo antigo dentro da dimensão 713 e confirmar recuperação.
- Testar dias 2-7, cama, olhos, sons, anomalias e FPS.
- O Capítulo 1 **não tem final novo** definido até o usuário escolher o futuro roteiro.
