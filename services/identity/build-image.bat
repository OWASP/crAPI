@echo off
cd /d identity
cmd /c docker build -t crapi/crapi-identity:%VERSION% .
cd /d .\..\
