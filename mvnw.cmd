@echo off
setlocal
set MAVEN_VERSION=3.9.14
set BASE_DIR=%~dp0
set DIST_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MAVEN_HOME=%DIST_DIR%\apache-maven-%MAVEN_VERSION%
set ARCHIVE=%DIST_DIR%\apache-maven-%MAVEN_VERSION%-bin.zip
set URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
  if not exist "%ARCHIVE%" powershell -NoProfile -Command "Invoke-WebRequest -Uri '%URL%' -OutFile '%ARCHIVE%'"
  powershell -NoProfile -Command "Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%DIST_DIR%' -Force"
)
call "%MAVEN_HOME%\bin\mvn.cmd" %*
