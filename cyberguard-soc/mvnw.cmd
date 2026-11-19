@echo off
setlocal enabledelayedexpansion

set "DIR=%~dp0"
cd /d "%DIR%"

:: Ensure JAVA_HOME points to installed JDK
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

set "MAVEN_VERSION=3.9.6"
set "MAVEN_CMD=%DIR%.mvn\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd"

if exist "%MAVEN_CMD%" (
    call "%MAVEN_CMD%" %*
    exit /b %ERRORLEVEL%
)

echo [CyberGuard Maven Wrapper] Maven is not yet downloaded.
echo [CyberGuard Maven Wrapper] Downloading Apache Maven %MAVEN_VERSION%...

set "MAVEN_ZIP=%DIR%.mvn\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference = 'SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%MAVEN_URL%', '%MAVEN_ZIP%'); Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%DIR%.mvn' -Force; Remove-Item '%MAVEN_ZIP%'"

if not exist "%MAVEN_CMD%" (
    echo [ERROR] Failed to download or unpack Maven. Please check your internet connection.
    exit /b 1
)

echo [CyberGuard Maven Wrapper] Maven %MAVEN_VERSION% ready.
call "%MAVEN_CMD%" %*
exit /b %ERRORLEVEL%
