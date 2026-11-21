@echo off
cd /d "%~dp0"
javac -encoding UTF-8 -d out -cp "lib\mysql-connector-j-8.4.0.jar" src\main\java\*.java || (pause & exit /b 1)
java -cp "out;lib\mysql-connector-j-8.4.0.jar" Main