@echo off
cd /d identity
cmd /c docker build -t crapi/crapi-chatbot:%VERSION% .
cd /d .\..\
