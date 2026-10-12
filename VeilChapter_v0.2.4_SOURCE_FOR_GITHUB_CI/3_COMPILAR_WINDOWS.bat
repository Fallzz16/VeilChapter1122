@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\compilar_seguro.ps1"
set RESULT=%ERRORLEVEL%
if not "%RESULT%"=="0" (echo Falha. Envie build_log.txt. & pause & exit /b %RESULT%)
echo Compilacao confirmada pelo Gradle. Teste o JAR em um perfil separado.
pause
