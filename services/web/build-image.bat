@echo off
cd /d web
cmd /c docker build -t crapi/crapi-web:%VERSION% .
cd /d .\..\
