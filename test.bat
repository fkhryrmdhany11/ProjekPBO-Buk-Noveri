@echo off
cd /d "%~dp0"
call build.bat --test
set "CITY_TEST_EXIT=%errorlevel%"
pause
exit /b %CITY_TEST_EXIT%
