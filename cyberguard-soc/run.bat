@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"
echo ======================================================================
echo   CYBERGUARD - SECURITY OPERATIONS CENTER (SOC) MONITORING SYSTEM
echo ======================================================================
echo   Starting JavaFX Application...
echo.

:: Detect Java 26 or installed JDK
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.1"
    ) else (
        for /d %%D in ("C:\Program Files\Java\jdk*") do (
            if exist "%%D\bin\java.exe" (
                set "JAVA_HOME=%%D"
            )
        )
    )
)

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_EXE%" (
    set "JAVA_EXE=java"
)

:: Run using target/libs and classes
"%JAVA_EXE%" --enable-native-access=javafx.graphics --module-path "target\libs" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "target\classes;src\main\resources;target\cyberguard-soc-1.0.0.jar" com.cyberguard.App

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [NOTICE] If build artifacts are missing, running Maven build...
    call mvnw.cmd compile
    "%JAVA_EXE%" --enable-native-access=javafx.graphics --module-path "target\libs" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "target\classes;src\main\resources;target\cyberguard-soc-1.0.0.jar" com.cyberguard.App
)
