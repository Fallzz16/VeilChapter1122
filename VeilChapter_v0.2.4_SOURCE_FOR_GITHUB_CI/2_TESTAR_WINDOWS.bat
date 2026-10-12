@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\diagnostico_ambiente.ps1"
if errorlevel 1 (echo Verifique o JDK 8 antes de iniciar o cliente.& pause& exit /b 2)
if not exist gradlew.bat (echo Execute 1_PREPARAR_WINDOWS.bat primeiro.& pause& exit /b 1)
call gradlew.bat --no-daemon runClient > test_client_log.txt 2>&1
set RESULT=%ERRORLEVEL%
type test_client_log.txt
if not "%RESULT%"=="0" echo Teste falhou. Envie test_client_log.txt.
pause
exit /b %RESULT%
