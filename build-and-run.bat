@echo off
rem Use this only if you have a JDK (javac). It recompiles the source, then starts the app.
cd /d "%~dp0"
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin src\sapp2p\*.java
if errorlevel 1 (
    echo.
    echo Compile failed. You may only have a JRE ^(no javac^). Use run.bat instead.
    pause
    exit /b 1
)
echo Compiled OK. Starting ...
echo Open  http://localhost:8080  in your browser.
java -cp bin sapp2p.Main
pause
