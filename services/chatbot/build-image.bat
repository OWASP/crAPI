@echo off
cd /d chatbot
cmd /c docker build -t crapi/crapi-chatbot:%VERSION% .
cd /d .\..\
