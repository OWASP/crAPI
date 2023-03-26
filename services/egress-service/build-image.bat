@echo off
cd /d identity
cmd /c docker build -t crapi/egress-service:%VERSION% .
cd /d .\..\
