@echo off
echo %cd%
cd /d services
for /F "delims=" %%a in ('dir /b build-image.bat /s') do call  "%%a"

@REM git clone https://github.com/OWASP/crAPI.git --config core.autocrlf=input
@REM call "%cd%\deploy\docker\build-all.bat"