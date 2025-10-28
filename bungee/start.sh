#!/usr/bin/env bash
# start.sh - start bungee/Velocity with Java 17 explicitly
# Usage: ./start.sh [--foreground]

set -euo pipefail
cd "$(dirname "$0")"
## Resolve Java 17 binary. Priority:
## 1) env override $JAVA17
## 2) $JAVA_HOME/bin/java
## 3) `java` on PATH
## 4) common /usr/lib/jvm/*java-17*/bin/java
JAVA17="${JAVA17:-}"
if [ -n "$JAVA17" ] && [ ! -x "$JAVA17" ]; then
  echo "Note: JAVA17 is set to '$JAVA17' but is not executable. Ignoring override."
  JAVA17=""
fi

if [ -z "$JAVA17" ]; then
  if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
    JAVA17="${JAVA_HOME}/bin/java"
  fi
fi

if [ -z "$JAVA17" ]; then
  if command -v java >/dev/null 2>&1; then
    JAVA17="$(command -v java)"
  fi
fi

if [ -z "$JAVA17" ]; then
  # try common jvm locations for java 17
  for cand in /usr/lib/jvm/*java-17*/bin/java /usr/lib/jvm/java-17*/bin/java; do
    if [ -x "$cand" ]; then
      JAVA17="$cand"
      break
    fi
  done
fi

## function to extract major version from `java -version` output
get_java_major() {
  local java_bin="$1"
  if [ -z "$java_bin" ]; then
    echo "0"
    return
  fi
  local ver
  ver="$($java_bin -version 2>&1 | head -n1)"
  # extract quoted part: "17.0.1" or "1.8.0_XXX"
  local quoted="$(echo "$ver" | sed -n 's/.*"\([^"]*\)".*/\1/p')"
  if [ -z "$quoted" ]; then
    # fallback: extract first number token
    quoted="$($java_bin -version 2>&1 | awk '{print $3}' | head -n1)"
  fi
  # if starts with 1., major is the second part
  if echo "$quoted" | grep -q '^1\.'; then
    echo "$quoted" | awk -F. '{print $2}'
  else
    echo "$quoted" | awk -F. '{print $1}'
  fi
}
JAR="bungee.jar"
LOG="bungee.log"
PIDFILE="bungee.pid"

PORTS=(25577 8081)

# check java binary presence and version
if [ -z "$JAVA17" ] || [ ! -x "$JAVA17" ]; then
  echo "ERROR: No java executable for Java 17 found. Tried JAVA17 env, JAVA_HOME, PATH, and common JVM locations."
  echo "Install a Java 17 JRE (e.g. 'sudo apt install openjdk-17-jre-headless') or set JAVA17 to the java 17 binary path."
  exit 1
fi

major=$(get_java_major "$JAVA17" 2>/dev/null || echo 0)
if ! [[ "$major" =~ ^[0-9]+$ ]]; then
  major=0
fi
if [ "$major" -lt 17 ]; then
  echo "ERROR: Java found at '$JAVA17' reports major version $major (needs 17+)."
  echo "You can install openjdk-17-jre-headless or change JAVA17 (export JAVA17=/path/to/java) to a Java 17 binary."
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
