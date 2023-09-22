@echo off
cd /d community
cmd /c docker build -t crapi/crapi-community:%VERSION% .
cd /d .\..\
