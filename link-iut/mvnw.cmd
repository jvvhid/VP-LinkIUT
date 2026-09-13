@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Begin all REM://
@echo off
@REM set title of command window
title %0
@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use another setlocal
@setlocal

@REM ==== START VALIDATION ====
set "JAVACMD=java.exe"
goto init
@REM ==== END VALIDATION ====

:init

@REM Find the project base dir, i.e. the directory that contains the folder ".mvn".
@REM Fallback to current working directory if not found.

set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir

set EXEC_DIR=%CD%
set WDIR=%EXEC_DIR%
:findBaseDir
IF EXIST "%WDIR%\.mvn" goto baseDirFound
cd ..
IF "%WDIR%"=="%CD%" goto baseDirNotFound
set WDIR=%CD%
goto findBaseDir

:baseDirFound
set MAVEN_PROJECTBASEDIR=%WDIR%
cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%EXEC_DIR%
cd "%EXEC_DIR%"

:endDetectBaseDir

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties" (
    echo Unable to find .mvn\wrapper\maven-wrapper.properties >&2
    goto error
)

@REM Extension: read distributionUrl from maven-wrapper.properties
set WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"
set DOWNLOAD_URL=
for /F "usebackq tokens=1,2 delims==" %%A in (%WRAPPER_PROPERTIES%) do (
    if "%%A"=="distributionUrl" set DOWNLOAD_URL=%%B
)

if "%DOWNLOAD_URL%"=="" (
    echo Could not read distributionUrl from %WRAPPER_PROPERTIES% >&2
    goto error
)

@REM Compute local repo path
set LOCAL_REPO=%USERPROFILE%\.m2\repository
set WRAPPER_DIR=%LOCAL_REPO%\org\apache\maven\apache-maven\3.9.9
set MAVEN_HOME=%WRAPPER_DIR%\apache-maven-3.9.9

if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runMaven

echo Downloading Maven 3.9.9 ...
mkdir "%WRAPPER_DIR%" 2>NUL

@REM Download using PowerShell
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%WRAPPER_DIR%\maven.zip' }"
if ERRORLEVEL 1 goto error

@REM Extract
powershell -Command "& { Expand-Archive -Path '%WRAPPER_DIR%\maven.zip' -DestinationPath '%WRAPPER_DIR%' -Force }"
if ERRORLEVEL 1 goto error

del "%WRAPPER_DIR%\maven.zip" 2>NUL

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Maven extraction failed >&2
    goto error
)

:runMaven
set MAVEN_CMD="%MAVEN_HOME%\bin\mvn.cmd"

%MAVEN_CMD% %* 
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

cmd /C exit /B %ERROR_CODE%
