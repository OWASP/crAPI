@echo off
cd /d mailhog
cmd /c docker build -t crapi/mailhog:%VERSION% .
cd /d .\..\
