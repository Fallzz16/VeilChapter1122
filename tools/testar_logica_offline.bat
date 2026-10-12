@echo off
setlocal
cd /d "%~dp0\.."
if not exist build\offline-classes mkdir build\offline-classes
javac -source 8 -target 8 -d build\offline-classes src\main\java\br\veilchapter\core\*.java src\test\java\br\veilchapter\core\*.java
if errorlevel 1 (echo FALHA na compilacao offline.& pause& exit /b 1)
java -cp build\offline-classes br.veilchapter.core.OfflineCoreTests
pause
