@echo off
cd /d "%~dp0"
echo Starting SAP MM-inspired Purchase-to-Pay ...
echo When you see the "running" message, open  http://localhost:8080  in your browser.
echo.
java -cp bin sapp2p.Main
echo.
echo The program stopped. If you saw an error above, check README.md.
pause
