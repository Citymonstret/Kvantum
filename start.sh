#!/bin/sh
set +v

# Configuration
version=BETA-0.0.4
minRam=256
maxRam=1024
# End of configuration

mkdir ./server/plugins
echo Starting the server! Use /stop or CTRL+C to stop it
sleep 5
java -Xms${minRam}M -Xmx${maxRam}M -jar IntellectualServer-${version}-all.jar
