@echo off
setlocal
cd /d "%~dp0"
set "CITY_JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "CITY_JAVA=%JAVA_HOME%\bin\java.exe"
"%CITY_JAVA%" tools\Build.java %*
if errorlevel 1 (
 echo Build gagal. Pastikan JDK 17 atau lebih baru terpasang dan JAVA_HOME/PATH benar.
 exit /b 1
)
exit /b 0
