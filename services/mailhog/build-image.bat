@echo off
cd /d mailhog
docker build -t crapi/crapi-mailhog .
cd /d .\..\
