@echo off
cd /d workshop
cmd /c docker build -t crapi/crapi-workshop:%VERSION% .
cd /d .\..\..\
