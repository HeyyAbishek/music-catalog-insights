@echo off
setlocal

set "MAVEN_VERSION=3.9.9"
set "BASE_DIR=%~dp0"
set "MVN_HOME=%BASE_DIR%.maven\apache-maven-%MAVEN_VERSION%"
set "MVN_CMD=%MVN_HOME%\bin\mvn.cmd"
set "ZIP_PATH=%BASE_DIR%.maven\apache-maven-%MAVEN_VERSION%-bin.zip"

if not exist "%MVN_CMD%" (
  if not exist "%BASE_DIR%.maven" mkdir "%BASE_DIR%.maven"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop';" ^
    "$url='https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip';" ^
    "Invoke-WebRequest -Uri $url -OutFile '%ZIP_PATH%';" ^
    "Expand-Archive -Path '%ZIP_PATH%' -DestinationPath '%BASE_DIR%.maven' -Force;"
  if errorlevel 1 exit /b %errorlevel%
)

call "%MVN_CMD%" %*
