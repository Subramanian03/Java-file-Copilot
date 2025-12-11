@echo off
REM Lightweight mvnw.cmd that delegates to installed Maven (Windows)
set WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar
if exist %WRAPPER_JAR% (
  java -jar %WRAPPER_JAR% %*
  exit /b %errorlevel%
)

where mvn >nul 2>&1
if %errorlevel%==0 (
  mvn %*
) else (
  echo Maven (mvn) not found on PATH and no wrapper jar present. Generate wrapper locally with:
  echo   mvn -N io.takari:maven:wrapper
  exit /b 1
)
