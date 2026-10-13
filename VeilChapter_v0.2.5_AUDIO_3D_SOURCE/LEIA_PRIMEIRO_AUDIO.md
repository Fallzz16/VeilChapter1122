# VEIL CHAPTER — ÁUDIO POSICIONAL v0.2.5 (CANDIDATO DE BUILD)

## O que mudou

- Integração de 14 clipes reais derivados dos 3 MP3 enviados: 5 psst, 4 hey, 3 trechos de risada e 2 respiratórios.
- Todos convertidos em OGG Vorbis MONO, 48 kHz. O Minecraft/OpenAL faz a espacialização no mundo, permitindo localizar origem com headphones. Não se utiliza estéreo pré-renderizado nem sons fixados ao player.
- Nos vocais, remoção de silêncios e transições curtas. Nos clipes longos, fades de 0,65–0,8 s na entrada e 1,05–1,65 s na saída; sem cortes secos.
- EQ leve para reduzir subgrave agressivo nas respirações e preservar a reverberação das risadas.
- Volume equilibrado; aleatoriedade das variações; probabilidade de eventos de voz cresce após o dia 4 e 6.
- O `psst` antigo foi substituído pela gravação fornecida. Incluído arquivo de compatibilidade `sounds/psst.ogg` para o pipeline legado.
- Comandos de teste direto de DIREÇÃO, sem alterar progresso do mundo.

## Como testar no jogo — mundo descartável, cheats ligados

`/veiltest audio psst left`
`/veiltest audio psst right`
`/veiltest audio hey behind`
`/veiltest audio laugh front`
`/veiltest audio breath right`
`/veiltest psst` (atalho que toca atrás do jogador)

Fique parado, use fones e gire a câmera. A direção percebida deve mudar; se o áudio parecer centralizado, confira opções de áudio do Minecraft, modo mono do Windows e sons ambientes.

Cada teste individual escolhe aleatoriamente UMA variante da família. São fontes 3D a 7 blocos para teste. Sons naturais surgem a 5–11 blocos em posições de ar carregadas, sem forçar o carregamento de chunks.

## Limites e segurança

ESTE ARQUIVO CONTÉM O CÓDIGO-FONTE E OS ASSETS, NÃO É UM JAR COMPILADO.
Para gerar o JAR, submeta a árvore inteira ao repositório GitHub (na raiz, sem pasta pai) e aguarde a execução de GitHub Actions. Não use o JAR antigo para tentar ouvir os novos sons.

Os recursos experimentais destrutivos permanecem desligados por padrão. O áudio foi testado como mídia, mas a execução com Forge e cliente Minecraft ainda depende do build e do playtest v0.2.5.

Nota: o Minecraft 1.12.2 padrão não realiza oclusão acústica real por paredes. A localização esquerda/direita é produzida pelo motor de áudio espacial, e isso não deve ser confundido com simulação acústica física detalhada.
