@echo off
title Java Web App Server
echo Starting Tomcat Server...
echo Access http://localhost:8080/ after startup.
rem サーバー起動までの待ち時間（5秒）を置いてからブラウザを開く
start "" powershell -NoProfile -Command "Start-Sleep -s 5; Start-Process 'http://localhost:8080/'"
call mvn clean compile exec:java -Dexec.mainClass="App"
pause