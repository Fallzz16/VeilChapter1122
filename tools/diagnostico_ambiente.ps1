param([switch]$Online)
$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent $PSScriptRoot
$ok = $true
Write-Host '==== VEIL CHAPTER - DIAGNOSTICO DE AMBIENTE ===='
Write-Host ('Pasta: ' + $root)
if ($root.Length -gt 140) { Write-Warning 'Caminho longo. Extraia em C:\ModsDev\VeilChapter para reduzir riscos do Gradle legado.' }
foreach ($exe in @('java.exe','javac.exe')) {
    $cmd=Get-Command $exe -ErrorAction SilentlyContinue
    if ($null -eq $cmd) {
        Write-Host "[ERRO] $exe nao encontrado no PATH. Instale JDK 8 (nao somente JRE)." -ForegroundColor Red
        $ok=$false; continue
    }
    $ver=(& $exe -version 2>&1 | Select-Object -First 1 | Out-String).Trim()
    Write-Host "[$exe] $ver | $($cmd.Source)"
    if ($ver -notmatch '(1\.8\.|\b8\.)') {
        Write-Host "[ERRO] $exe nao esta usando Java 8. Defina JAVA_HOME para o JDK 8 e coloque %JAVA_HOME%\bin primeiro no PATH." -ForegroundColor Red
        $ok=$false
    }
}
if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    Write-Warning 'JAVA_HOME nao definido. Pode funcionar via PATH, mas o JDK 8 e recomendado.'
} else {
    Write-Host ('JAVA_HOME: ' + $env:JAVA_HOME)
    if (-not (Test-Path (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) {
        Write-Host '[ERRO] JAVA_HOME nao aponta para um JDK completo com javac.exe.' -ForegroundColor Red
        $ok=$false
    }
}
$wrapperBat=Join-Path $root 'gradlew.bat'
$wrapperJar=Join-Path $root 'gradle/wrapper/gradle-wrapper.jar'
if (-not ((Test-Path $wrapperBat) -and (Test-Path $wrapperJar))) {
    Write-Warning 'Wrapper ainda nao instalado. Execute 1_PREPARAR_WINDOWS.bat.'
} else {
    Write-Host '[OK] Wrapper presente.'
    $props=Join-Path $root 'gradle/wrapper/gradle-wrapper.properties'
    if ((Test-Path $props) -and ((Get-Content $props -Raw) -notmatch 'gradle-7\.3\.3-bin\.zip')) {
        Write-Host '[ERRO] Wrapper nao configurado para Gradle 7.3.3. Rode a etapa de preparacao.' -ForegroundColor Red
        $ok=$false
    }
}
if ($Online) {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    foreach($endpoint in @(
       'https://maven.minecraftforge.net/net/minecraftforge/gradle/ForgeGradle/5.1.74/ForgeGradle-5.1.74.pom',
       'https://maven.minecraftforge.net/net/minecraftforge/forge/1.12.2-14.23.5.2859/forge-1.12.2-14.23.5.2859-userdev3.jar',
       'https://repo.maven.apache.org/maven2/org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.pom')) {
        try {
            $res=Invoke-WebRequest -Uri $endpoint -Method Head -TimeoutSec 12 -UseBasicParsing
            Write-Host ('[REDE OK] ' + $res.StatusCode + ' ' + $endpoint)
        } catch {
            Write-Warning ('[REDE] Falhou ' + $endpoint + ': ' + $_.Exception.Message)
        }
    }
}
if ($ok) { Write-Host '[OK] Verificacoes locais passaram.' -ForegroundColor Green; exit 0 }
Write-Host '[FALHA] Corrija o JDK 8 / PATH antes de compilar.' -ForegroundColor Red
exit 2
