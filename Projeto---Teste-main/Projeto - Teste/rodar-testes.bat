@echo off
chcp 65001 > nul
REM Compila o jogo + os testes e roda todos os testes JUnit 5.
set JUNIT=lib\junit-platform-console-standalone-1.11.4.jar
if not exist %JUNIT% (
  echo Nao encontrei %JUNIT%. Veja a secao "Testes" do README.
  pause
  exit /b 1
)
if exist bin rmdir /s /q bin
javac -encoding UTF-8 -d bin -cp %JUNIT% modelo\*.java efeitos\*.java eventos\*.java excecoes\*.java fabrica\*.java persistencia\*.java servicos\*.java ui\*.java teste\modelo\*.java teste\efeitos\*.java teste\fabrica\*.java teste\persistencia\*.java teste\servicos\*.java
if errorlevel 1 (
  pause
  exit /b 1
)
java -jar %JUNIT% execute --class-path bin --scan-class-path
pause
