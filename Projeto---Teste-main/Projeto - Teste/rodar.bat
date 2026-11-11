@echo off
chcp 65001 > nul
REM Compila o jogo e abre a versao grafica. Rode a partir desta pasta.
if exist bin rmdir /s /q bin
javac -encoding UTF-8 -d bin modelo\*.java efeitos\*.java eventos\*.java excecoes\*.java fabrica\*.java persistencia\*.java servicos\*.java ui\*.java
if errorlevel 1 (
  echo.
  echo Erro de compilacao. Veja as mensagens acima.
  pause
  exit /b 1
)
java -cp bin ui.AppGUI
