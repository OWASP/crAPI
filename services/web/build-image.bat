@echo off
cd /d web
docker build -t crapi/crapi-web .
cd /d .\..\
