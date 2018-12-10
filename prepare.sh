#!/usr/bin/env bash

clear

echo
echo Exectuing gradlew build tasks!

./gradlew clean
./gradlew writeVersionToFile
./gradlew licenseFormat
./gradlew -Dorg.gradle.project.sign=true -Dorg.gradle.project.full=true build
./gradlew -Dorg.gradle.project.sign=true -Dorg.gradle.project.full=true signArchives
./gradlew shadowJar

echo
echo Replacing version variables

export oldVersion=$(<old-version.txt)
export newVersion=$(<version.txt)

echo
echo Replacing README.md version
echo Do not forget to publish the github release
echo
echo Old version: ${oldVersion}
echo New version: ${newVersion}

sed -i 's/'"${oldVersion}"'/'"${newVersion}"'/g' README.md
sed -i 's/'"${oldVersion}"'/'"${newVersion}"'/g' start.bat
sed -i 's/'"${oldVersion}"'/'"${newVersion}"'/g' start.sh

echo
echo Done!
