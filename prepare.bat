@echo off &setlocal

echo.
echo Executing gradlew build tasks.

call gradlew clean
call gradlew writeVersionToFile
call gradlew licenseFormat
call gradlew build

echo.
echo Replacing version variables.

set /p oldVersion=< old-version.txt
set /p newVersion=< version.txt

echo.
echo Replacing README.md version
echo Don't forget to publish the github release
echo.
echo Old version: %oldVersion%
echo New version: %newVersion%

call:DoReplace "%oldVersion%" "%newVersion%" README.md README.md
call:DoReplace "%oldVersion%" "%newVersion%" start.bat start.bat
call:DoReplace "%oldVersion%" "%newVersion%" start.sh start.sh

echo.
echo Copying built jar to .\bin\

copy /Y .\ServerImplementation\build\libs\ServerImplementation-%newVersion%-all.jar .\bin\

echo.
echo Adding jar to git changelog
git add .\bin\ServerImplementation-%newVersion%-all.jar

echo.
echo Done!

exit /b

:DoReplace
echo ^(Get-Content "%3"^) ^| ForEach-Object { $_ -replace %1, %2 } ^| Set-Content %4>Rep.ps1
Powershell.exe -executionpolicy ByPass -File Rep.ps1
if exist Rep.ps1 del Rep.ps1
echo Successfully replaced text in "%4"!
