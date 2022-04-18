@echo off
cd /d services
for /F "delims=" %%a in ('dir /b build-image.bat /s') do call  "%%a"