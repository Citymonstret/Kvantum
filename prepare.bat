@echo off &setlocal

call gradlew :licenseFormatMain
call gradlew :build
call gradlew :shadowJar

set /p oldVersion=< old-version.txt
set /p newVersion=< version.txt

echo Replacing README.md version
echo Don't forget to publish the github release
echo.
echo Old version: %oldVersion%
echo New version: %newVersion%

call:DoReplace "%oldVersion%" "%newVersion%" README.md README.md

copy /Y .\build\libs\IntellectualServer-%newVersion%-all.jar .\

git add IntellectualServer-%newVersion%-all.jar

exit /b

:DoReplace
echo ^(Get-Content "%3"^) ^| ForEach-Object { $_ -replace %1, %2 } ^| Set-Content %4>Rep.ps1
Powershell.exe -executionpolicy ByPass -File Rep.ps1
if exist Rep.ps1 del Rep.ps1
echo Done
pause
