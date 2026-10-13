@echo off
setlocal
cd /d "%~dp0"
echo === Etapa 1: ambiente Java 8 ===
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\diagnostico_ambiente.ps1"
if errorlevel 1 (echo Falha no JDK. Corrija e tente novamente.& pause& exit /b 2)
echo === Etapa 2: preparar wrapper verificado ===
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\preparar_mdk.ps1"
if errorlevel 1 (echo Falha no download/validacao do wrapper.& pause& exit /b 3)
echo === Etapa 3: verificar Gradle 7.3.3 ===
call gradlew.bat --version
if errorlevel 1 (echo Falha no Gradle. Verifique sua conexao.& pause& exit /b 4)
echo Ambiente preparado. Agora execute 3_COMPILAR_WINDOWS.bat
pause
