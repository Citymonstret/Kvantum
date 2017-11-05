#!/usr/bin/env bash

clear

echo
echo Exectuing gradlew build tasks!

./gradlew clean
./gradlew writeVersionToFile
./gradlew licenseFormat
./gradlew build
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
echo Copying built jar to ./bin/

cp -fr ./Implementation/build/libs/Implementation-${newVersion}-all.jar ./bin/

echo
echo Adding jar to git changelog
git add ./bin/Implementation-${newVersion}-all.jar -f

echo
echo Done!
