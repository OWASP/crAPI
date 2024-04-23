@echo off
cd /d chatbot
m
xcopy .\..\..\docs\ retrieval\docs\ /E /Y
cmd /c docker build -t crapi/crapi-chatbot:%VERSION% .
cd /d .\..\
