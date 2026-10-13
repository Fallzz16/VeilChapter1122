# Veil Chapter — v0.3.0 Primeiro Pacote de Entidades (Alpha)
## Situação e escopo
Esta etapa IMPLEMENTA o HorrorDirector inicial e a terceira entidade, O Inquilino, com modelo humanoide original, textura 64x64 autoral e animações procedurais. Também refina Watcher e Fresta.
A biblioteca GeckoLib foi pesquisada para Forge 1.12.2, mas deliberadamente NÃO é uma dependência do pacote: primeiro testamos uma solução vanilla compatível, mais simples e sem exigir instalação adicional.
O Eco e o Arrastado continuam fichas de design, não entidades falsas nem placeholders registradas no jogo.

## Proteções
- Nenhuma entidade nova remove blocos, força chunk loading ou altera inventário.
- O Inquilino não é agressivo e não causa dano; desaparece por proximidade, observação continuada ou depois de ~12 segundos.
- A Fresta usa quads sem colisão; desaparece rapidamente.
- O Watcher permanece entidade passiva, sem ataques e sem drops.
- O máximo de Inquilinos é limitado por proximidade, independentemente da quantidade de jogadores.
- A dimensão 713 é legado exclusivo para recuperação de saves antigos. A Árvore da Vida continua excluída.

## Testes manuais recomendados
1. Forge 1.12.2-14.23.5.2859, Java 8, somente um JAR veilchapter no diretório mods.
2. Faça BACKUP de qualquer mundo antigo.
3. Mundo de teste: /veiltest day 4 e /veiltest tenant em campo aberto; observe pose, braços, pernas, rosto e aparição/desaparecimento.
4. Dia 3: explore campos, morros e vegetação para Watcher/Fresta. Teste F5, chuva, noite, render distance 4 e 12 chunks.
5. Dia 4 à noite: cama marcada, ficar dentro de 35 blocos da base; aguarde tentativas naturais (cadência 4–8 min e chance 42% padrão).
6. Testar casa com 2 blocos de altura (Inquilino NÃO deve atravessar), telhado baixo, cavernas e região sem chunks carregados.
7. Testar desconexão, volta ao mundo, teleporte, morte, multiplayer e eventos próximos de 2 jogadores.
8. /veilstatus confirma o dia e mostra que árvore foi removida.
9. /veiltest day 7 e receber dano comum NÃO deve teleportar para dimensão da árvore.
10. Medir FPS/TPS comparando com v0.2.9 na mesma seed e render distance.

## Limites honestos
- Sem testes de cliente Minecraft automáticos; modelo e eventos visuais exigem playtest real.
- A detecção de "casa" é inicialmente por cama, não por mapa arquitetônico da construção.
- Os novos sons exclusivos do Inquilino NÃO foram ainda produzidos; usa passos discretos vanilla em baixa intensidade. Áudios existentes de terror permanecem.
- A progressão narrativa completa de cinco entidades é futura.
- O Inquilino tem uma única aparência; variações faciais e de silhueta estão na etapa de direção artística posterior.

## Proveniência de assets
- assets/veilchapter/textures/entity/tenant.png: textura autoral 64×64 RGBA, gerada programaticamente para esta branch com paleta 2–4 tons preto/acinzentado, sem uso de material externo.
- ModelTenant.java, ModelWatcher.java: modelos autorais por geometria de caixas de Minecraft, editáveis no código e sem dependências extras.
- assets/veilchapter/textures/entity/watcher.png: pré-existente no projeto; preservada byte a byte.
- sfx psst/hey/breath/laugh: 14 OGG pré-existentes de origem do próprio mod; preservados.
- Nenhum asset do From The Fog, Cave Dweller ou The Man From The Fog foi redistribuído.
