$ErrorActionPreference = 'Continue'
$root=Split-Path -Parent $PSScriptRoot
Set-Location $root
$log=Join-Path $root 'build_log.txt'
Write-Host 'Etapa 1/3: verificando JDK, wrapper e versoes...'
& powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'tools/diagnostico_ambiente.ps1')
if ($LASTEXITCODE -ne 0) { Write-Host '[PARADO] Ambiente invalido. Nenhuma compilacao iniciada.' -ForegroundColor Red; exit 2 }
Write-Host 'Etapa 2/3: compilando e gerando log completo (pode demorar)...'
$command='call gradlew.bat --no-daemon clean build --stacktrace > "build_log.txt" 2>&1'
& cmd.exe /D /S /C $command
$code=$LASTEXITCODE
Write-Host ('Log salvo: ' + $log)
if ($code -ne 0) {
    $contents= if (Test-Path $log) {[IO.File]::ReadAllText($log)} else {''}
    Write-Host '[BUILD FAILED] Diagnostico automatizado:' -ForegroundColor Red
    if ($contents -match '522|503|502|504|Read timed out|Connect timed out|Connection reset') {
        Write-Host '- Servidor Maven indisponivel/instavel. Nao e falha do codigo Java.'
        Write-Host '- Reexecute mais tarde; a configuracao nao depende de Velocity.'
    }
    if ($contents -match 'artifactural|Srg2Source|forge-userdev') {
        Write-Host '- Dependencia nao resolvida. Verifique ForgeGradle 5.1.74 e o userdev3 do Forge 2859.'
    }
    if ($contents -match 'UnsupportedClassVersionError|Unsupported major.minor|Could not determine java version') {
        Write-Host '- JDK incorreto: selecione Java 8 (nao Java 17/21).'
    }
    if ($contents -match 'Could not resolve|Could not GET|Could not find') {
        Write-Host '- Resolucao de dependencia falhou. Leia a secao FAILURE no build_log.txt.'
    }
    if ($contents -match 'Compilation failed|error: cannot find symbol|error: package .* does not exist') {
        Write-Host '- Erros de compilacao Java encontrados. Envie build_log.txt para correcao do codigo.'
    }
    if ($contents -match 'OutOfMemoryError|Java heap space') {
        Write-Host '- Memoria insuficiente. Ajuste org.gradle.jvmargs em gradle.properties.'
    }
    Write-Host 'Ultimas linhas do log:'
    ($contents -split "`r?`n" | Select-Object -Last 35) | ForEach-Object {Write-Host $_}
    exit $code
}
Write-Host 'Etapa 3/3: verificando JAR gerado...'
$jar=Get-ChildItem (Join-Path $root 'build/libs') -Filter '*.jar' -ErrorAction SilentlyContinue |
     Where-Object { $_.Name -notmatch '(sources|javadoc)' -and $_.Length -gt 1024 } |
     Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($null -eq $jar) {
    Write-Host '[ERRO] Gradle concluiu, mas nao foi encontrado JAR instalavel em build/libs.' -ForegroundColor Red
    exit 3
}
$jarTool = Get-Command jar.exe -ErrorAction SilentlyContinue
if ($null -eq $jarTool) {
    Write-Host '[ERRO] jar.exe nao encontrado. Instale JDK 8 completo.' -ForegroundColor Red
    exit 4
}
$entries = & $jarTool.Source tf $jar.FullName 2>&1
if ($LASTEXITCODE -ne 0 -or -not ($entries -match 'mcmod.info') -or -not ($entries -match 'br/veilchapter/VeilMod.class')) {
    Write-Host '[ERRO] JAR nao contem classes esperadas do mod ou nao pode ser aberto.' -ForegroundColor Red
    exit 4
}
Write-Host ('[BUILD SUCCESSFUL] JAR verificado: ' + $jar.FullName) -ForegroundColor Green
Write-Host 'ATENCAO: compile completo nao substitui teste dentro do Minecraft. Use mundo de teste.'
exit 0
