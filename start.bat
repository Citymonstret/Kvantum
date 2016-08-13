@ECHO OFF

:: Configuration
SET version=1.2.0
SET minRam=256
SET maxRam=1024
:: End of configuration

mkdir .\.iserver\plugins
copy .\CrushEngine\CrushEngine-%version%.jar .\.iserver\plugins
echo Starting the server! Use "/stop" or CTRL+C to stop it
timeout 5 > nul
java -Xms%minRam%M -Xmx%maxRam%M -jar IntellectualServer-%version%-all.jar
