# Somente baixa o wrapper do MDK Forge oficial e verifica o SHA-1 publicado.
# Nao altera configuracoes de seguranca ou instala componentes no Windows.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$version = '1.12.2-14.23.5.2859'
$url = "https://maven.minecraftforge.net/net/minecraftforge/forge/$version/forge-$version-mdk.zip"
$sha1 = '347338fc96359640c3d9673438db26742bd658d5'
$archive = Join-Path $env:TEMP ("veil_mdk_" + [Guid]::NewGuid().ToString('N') + '.zip')
$tmp = Join-Path $env:TEMP ("veil_mdk_" + [Guid]::NewGuid().ToString('N'))
$wrapper = Join-Path $root 'gradle/wrapper/gradle-wrapper.properties'
try {
    if (-not ((Test-Path (Join-Path $root 'gradlew.bat')) -and (Test-Path (Join-Path $root 'gradle/wrapper/gradle-wrapper.jar')))) {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Write-Host 'Baixando wrapper do MDK oficial Forge 1.12.2...'
        Invoke-WebRequest -Uri $url -OutFile $archive -UseBasicParsing -TimeoutSec 90
        $actual = (Get-FileHash -Path $archive -Algorithm SHA1).Hash.ToLowerInvariant()
        if ($actual -ne $sha1) { throw "Hash divergente! Download rejeitado. $actual" }
        New-Item -ItemType Directory -Path $tmp -Force | Out-Null
        Expand-Archive -Path $archive -DestinationPath $tmp -Force
        foreach ($relative in @('gradlew','gradlew.bat','gradle/wrapper/gradle-wrapper.jar','gradle/wrapper/gradle-wrapper.properties')) {
            $src=Join-Path $tmp $relative
            if (-not (Test-Path $src)) { throw "Arquivo ausente no MDK: $relative" }
            $dest=Join-Path $root $relative
            New-Item -ItemType Directory -Path (Split-Path $dest -Parent) -Force | Out-Null
            Copy-Item $src $dest -Force
        }
    } else { Write-Host 'Wrapper existente reaproveitado.' }
    # ForgeGradle 5.1.74 usa Gradle 7.3.3; wrapper original do MDK sera atualizado.
    $properties = @'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-7.3.3-bin.zip
'@
    [System.IO.File]::WriteAllText($wrapper,$properties,[System.Text.Encoding]::ASCII)
    Write-Host 'Wrapper configurado para Gradle 7.3.3.'
} catch {
    Write-Host ('FALHA: ' + $_.Exception.Message) -ForegroundColor Red
    Write-Host 'Baixe o MDK oficial 2859 em files.minecraftforge.net se o servidor estiver indisponivel.'
    exit 1
} finally {
    if (Test-Path $tmp) { Remove-Item $tmp -Force -Recurse }
    if (Test-Path $archive) { Remove-Item $archive -Force }
}
