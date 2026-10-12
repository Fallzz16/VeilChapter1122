# Auditoria do build v0.2.3

## Verificações estruturais

- O `build.gradle` usa ForgeGradle 3.0.140 (em vez de 3.0.20).
- Não faz uso do Maven Velocity nem de `com.amadornes:artifactural`.
- O plugin usa `net.minecraftforge:artifactural:1.0.13`, em versão fixa (evitando metadata `1.0.+`).
- `tools/preparar_mdk.ps1` compara SHA-1 do MDK 2859 contra o valor oficial conhecido antes de copiar scripts.
- O wrapper é configurado para Gradle 4.9, após a instalação.
- O diagnóstico exige `java.exe` e `javac.exe` do Java 8.
- O build gera log e só anuncia sucesso se o Gradle terminar sem erro e existir um JAR com tamanho não trivial em `build/libs`.
- Código Java de gameplay e recursos da v0.2.2 intocados.

**Limitação:** o sandbox não tem acesso a Maven/Forge para executar a compilação completa. O resultado precisa do log do Windows.
