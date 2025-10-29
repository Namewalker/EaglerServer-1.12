#!/usr/bin/env bash
cd "$(dirname "$0")"
if [ -f server.pid ]; then
  pid=$(cat server.pid)
  if ps -p "$pid" > /dev/null 2>&1; then
    kill "$pid" && echo "Stopped lobby PID $pid"
    rm -f server.pid
    exit 0
  else
    echo "PID $pid not running; removing stale pidfile"
    rm -f server.pid
    exit 0
  fi
else
  echo "No server.pid found; looking for java process..."
  pgrep -f "server.jar" | xargs -r -n1 kill && echo "Sent kill to matching server.jar processes"
fi