@echo off
cd /d identity
cmd /c docker build -t crapi/gateway-service:%VERSION% .
cd /d .\..\
