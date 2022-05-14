@echo off
cd /d identity
docker build -t crapi/crapi-identity .
cd /d .\..\
