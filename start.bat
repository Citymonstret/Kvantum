@ECHO OFF

:: Configuration
SET version=1.3-SNAPSHOT
SET minRam=256
SET maxRam=1024
:: End of configuration

mkdir .\.kvantum\plugins
echo Starting the server! Use "/stop" or CTRL+C to stop it
timeout 5 > nul
java -Xms%minRam%M -Xmx%maxRam%M -jar Kvantum-%version%-all.jar
