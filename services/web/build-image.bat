@echo off
cd /d web
docker build -t crapi-web .
cd /d .\..\