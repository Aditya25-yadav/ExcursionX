@echo off
REM ============================================================
REM  ExcursionX — Run Script (Windows)
REM  Usage: Double-click this file OR run from command prompt
REM ============================================================

echo.
echo  ==========================================
echo   ExcursionX - Smart Excursion Management
echo  ==========================================
echo.

cd /d "%~dp0"

java -cp "src;lib/mysql-connector-j-9.6.0.jar" Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  [ERROR] Application failed to start.
    echo  Make sure MySQL is running and excursionx DB exists.
    echo  Run excursionx_schema.sql first if you haven't already.
    pause
)
