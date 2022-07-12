@echo off
cd /d services
set "VERSION=latest"
for /F "delims=" %%a in ('dir /b build-image.bat /s') do call  "%%a"