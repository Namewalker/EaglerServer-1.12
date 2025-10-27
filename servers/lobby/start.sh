#!/usr/bin/env bash
cd "$(dirname "$0")"
JAVA="/usr/lib/jvm/java-17-openjdk-amd64/bin/java"
JAR="server.jar"
LOG="server.log"
if [ ! -x "$JAVA" ]; then
  echo "Java 17 not found at $JAVA"
  exit 1
fi
nohup "$JAVA" -Xms512M -Xmx1G -jar "$JAR" nogui > "$LOG" 2>&1 &
echo $! > server.pid
echo "Lobby started (PID $(cat server.pid))"