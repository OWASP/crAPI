@echo off
cd /d identity
cmd /c docker build -t crapi/api-external:%VERSION% .
cd /d .\..\
