# Veil Chapter 0.2.3 — correção estrutural do build (Forge 1.12.2)

## O que o seu erro mostrou

A compilação da v0.2.2 parou **antes de compilar qualquer Java** porque o Maven Velocity respondeu HTTP **522** enquanto o Gradle buscava `Srg2Source:4.2.+`; a resolução de `com.amadornes:artifactural` foi interrompida depois.

## Estratégia nova

- ForgeGradle **3.0.140** em lugar de 3.0.20, evitando os dois componentes que dispararam nossos últimos erros: `Srg2Source:4.2.+` e `com.amadornes:artifactural`.
- `net.minecraftforge:artifactural:1.0.13` com versão **fixa**, hospedada no Maven do próprio Forge. **Não usa o servidor Velocity**.
- Gradle **4.9** fixado no wrapper, sem exigir instalação global. Java **JDK 8** é obrigatório.
- Comandos com diagnóstico, log completo e verificação real da existência do JAR.

Fontes para referência: [ForgeGradle 3.0.140](https://mvnrepository.com/artifact/net.minecraftforge.gradle/ForgeGradle/3.0.140/dependencies) e [Artifactural oficial](https://files.minecraftforge.net/net/minecraftforge/artifactural/index.html).

**Ainda não foi executado um build Forge real neste ambiente sem acesso à rede externa.** Portanto, isso corrige a causa conhecida, não é garantia de que o projeto completo compile no seu PC. Caso o build encontre erros Java, o log será preciso e podemos corrigi-los sem tocar na história.

## Para testar no Windows

1. Extraia **este ZIP inteiro** para uma pasta nova, de preferência `C:\ModsDev\VeilChapter`.
2. Execute `0_DIAGNOSTICAR_WINDOWS.bat`. Ele verifica **java.exe**, **javac.exe**, versões, wrapper e testa as URLs oficiais (os testes de rede são apenas informativos).
3. Execute `1_PREPARAR_WINDOWS.bat`. Baixa do MDK oficial **somente o Gradle Wrapper**, com verificação SHA-1, e fixa Gradle 4.9. O Gradle 4.9 que você baixou manualmente não precisa ser instalado no Windows.
4. Execute `3_COMPILAR_WINDOWS.bat` e espere. Se passar, mostrará o caminho do JAR em `build\libs`.
5. Se falhar, envie `build_log.txt`; se falhar a preparação, envie uma captura da janela.
6. `2_TESTAR_WINDOWS.bat` inicia o cliente de desenvolvimento e grava `test_client_log.txt`.

## Segurança

- **Não instale** ZIP de código-fonte em `mods`; somente o JAR gerado após build.
- Crie um perfil e mundo de teste. Os recursos experimentais continuam desativados por padrão.
- Este pacote não inclui binários de Minecraft, Forge ou Dynamic Lights.
- As regras de terror e código de gameplay foram preservados da v0.2.2; mudança concentrada no sistema de compilação.

## Erros que o diagnosticador tenta reconhecer

- Java/JDK incorreto (8 versus 17/21; ausência de `javac`).
- Wrapper ausente ou com versão incorreta.
- Falhas de rede HTTP 522/503, timeouts ou DNS.
- Dependências `Artifactural`, `Srg2Source`, `forge-userdev`.
- Falhas reais de compilação Java (`cannot find symbol`).
- Memória insuficiente; sucesso do Gradle sem JAR produzido.

O diagnosticador **explica** o erro; não pode consertar indisponibilidade de servidores externos ou incompatibilidades ainda desconhecidas sem logs.
