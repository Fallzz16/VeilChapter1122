# Veil Chapter — Texture Overhaul v0.3.2 (continuação)

> Status: **pacote artístico criado e auditado fora do GitHub**, na sessão de trabalho. Esta branch é o ponto de integração, baseada em `dev/geckolib-art-rigs-v031`. **Os PNGs e os JSONs UV específicos da v0.3.2 ainda não foram enviados para esta branch**. Não confundir os recursos da branch com o JAR artístico distribuído em conversa. Não substituir `main` sem revisão no cliente.

## Decisão
Nenhuma mudança de IA, histórias, som, classe compilada ou lógica de jogo. Removida Árvore da Vida continua removida. GeckoLib3 3.0.31 instalado separadamente.

## Quatro atlas pintados de novo
- Watcher: 512 x 512 RGBA; 16 bones, 108 faces UV únicas
- Peeker/Fresta: 256 x 256 RGBA; 9 bones, 54 faces; variante eyes-only sincronizada
- Inquilino/Tenant: 512 x 512 RGBA; 19 bones, 144 faces
- Arrastado/Dragged: 512 x 512 RGBA; 20 bones, 138 faces
- Total **444 faces**, sem regiões UV sobrepostas; pinturas personalizadas por rosto, corpo, membros e detalhes.

## Auditoria / resultados reais
- Auditoria estática v032: **1215 verificações aprovadas**, incluindo dados de geometria, atlas, cores, áreas UV e integridade ZIP.
- JAR derivado da v0.3.1: exatamente **9 recursos** alterados (4 geometrias, 4 texturas, 1 geometria eyes-only).
- **52 classes compiladas inalteradas** e **15 OGG preservados byte a byte**.
- Revisão adicional de *poses*: **10 clips**, 4 personagens, testes de matrizes de transformação finitas e **128 frames de vídeo** gerados via projeção independente.
- Exportação estática para GLB com atlas incorporados: 4 GLB auditados, permitindo abrir em Blender ou visualizador glTF.
- Fontes de pintura em camadas: 4 OpenRaster .ORA, comparados pixel a pixel com PNGs finais.
- As prévias e os vídeos são **renderizações de software**, não capturas de Minecraft.
- **Não existe aprovação visual no cliente Minecraft** nesta etapa: testar no Forge 1.12.2 + GeckoLib3 3.0.31 e revisar interpretação real de per-face UV.

## Reprodutibilidade
Os scripts e as fontes foram entregues com os arquivos em pacote separável:
- `build_textures.py`: pintura e organização UV determinísticas.
- `render_previews.py`: 4 ângulos e ambiente escuro.
- `qa_textures.py`: UV e integridade.
- `qa_animated_textures.py`: projeção de ossos em movimento; 10 sequências.
- `export_glb_textures.py`: exportação glTF de revisão, não formato de execução.
- `make_layered_sources.py`: arquivos .ORA.
- `package_textures.py`: empacotamento sem modificar .class nem áudio.

## Próxima condição para release
Abrir Minecraft cliente Forge 1.12.2 com GeckoLib 3.0.31. Invocar /veiltest watcher, /veiltest peeker, /veiltest tenant, /veiltest dragged em ambiente apropriado; capturar frente, perfil, costas, F5, noite e movimento; confirmar sem crash, texturas corretas e UV sem artefatos. Não marcar como definitivo antes disso.

## Preservação
A v0.3.1 original na branch `dev/geckolib-art-rigs-v031` permanece disponível para reversão. Ao subir os 9 arquivos finais da v0.3.2 para este ramo, comparar hashes e executar CI antes do merge.
