@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\diagnostico_ambiente.ps1" -Online
set STATUS=%ERRORLEVEL%
echo.
if not "%STATUS%"=="0" echo DIAGNOSTICO local encontrou problemas.
pause
exit /b %STATUS%
