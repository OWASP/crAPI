@echo off
cd /d identity
docker build -t crapi-identity .
cd /d .\..\