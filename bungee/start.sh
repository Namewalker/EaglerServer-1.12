#!/usr/bin/env bash
# start.sh - start bungee/Velocity with Java 17 explicitly
# Usage: ./start.sh [--foreground]

set -euo pipefail
cd "$(dirname "$0")"
JAVA17="/usr/lib/jvm/java-17-openjdk-amd64/bin/java"
JAR="bungee.jar"
LOG="bungee.log"
PIDFILE="bungee.pid"

PORTS=(25577 8081)

# check java binary
if [ ! -x "$JAVA17" ]; then
  echo "ERROR: Java 17 binary not found at $JAVA17"
  echo "You can install openjdk-17-jre-headless or change JAVA17 in this script."
  exit 1
fi

# check ports
for p in "${PORTS[@]}"; do
  if ss -ltn "sport = :$p" 2>/dev/null | grep -q LISTEN; then
    echo "Port $p already in use. Is another instance running? Aborting to avoid port conflicts."
    exit 2
  fi
done

if [ "${1:-}" = "--foreground" ]; then
  exec "$JAVA17" -jar "$JAR"
else
  nohup "$JAVA17" -jar "$JAR" > "$LOG" 2>&1 &
  echo $! > "$PIDFILE"
  echo "Started $JAR with PID $(cat $PIDFILE). Logging to $LOG"
fi
