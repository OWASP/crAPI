@echo off
cd /d community
docker build -t crapi/crapi-community .
cd /d .\..\
