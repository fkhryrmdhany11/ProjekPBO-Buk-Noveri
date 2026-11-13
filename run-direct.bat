@echo off
cd /d "%~dp0"
if not exist "dist\ADayInMyCity.jar" (
 call build.bat
 if errorlevel 1 (
  pause
  exit /b 1
 )
)
java -jar "dist\ADayInMyCity.jar" %*
if errorlevel 1 pause
