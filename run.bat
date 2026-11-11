@echo off
rem Starts Acme Payroll Desktop. Builds the jar first if it is missing.
setlocal
cd /d "%~dp0"

if not exist target\payroll-swing.jar (
    echo Building payroll-swing.jar ...
    call mvn -q -B -DskipTests package
    if errorlevel 1 (
        echo Build failed.
        exit /b 1
    )
)

start "Acme Payroll" javaw -jar target\payroll-swing.jar %*
endlocal
