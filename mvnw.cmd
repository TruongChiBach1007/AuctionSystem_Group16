@echo off
setlocal

set "MAVEN_VERSION=3.9.9"
set "BASE_DIR=%~dp0"
set "WRAPPER_DIR=%BASE_DIR%.mvn\apache-maven"
set "MAVEN_HOME=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_ZIP=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Maven %MAVEN_VERSION% was not found locally. Downloading...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "New-Item -ItemType Directory -Force -Path '%WRAPPER_DIR%' | Out-Null; " ^
        "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%'; " ^
        "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%WRAPPER_DIR%' -Force"
    if errorlevel 1 (
        echo Failed to download Maven. Install Maven manually or check your network.
        exit /b 1
    )
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
