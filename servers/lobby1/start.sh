#!/bin/sh
cd "$(dirname "$0")"
java -Xms1G -Xmx2G -jar server.jar nogui
