#!/usr/bin/env bash
# stop.sh - stop bungee/Velocity safely
# Usage: ./stop.sh

set -euo pipefail
cd "$(dirname "$0")"
PIDFILE="bungee.pid"
JARNAME="bungee.jar"

# helper to check process existence
is_running() {
  local pid=$1
  if [ -z "$pid" ]; then return 1; fi
  if ps -p "$pid" > /dev/null 2>&1; then return 0; else return 1; fi
}

# find candidate pids: from pidfile, else pgrep -f bungee.jar
pids=()
if [ -f "$PIDFILE" ]; then
  pid=$(cat "$PIDFILE" 2>/dev/null || true)
  if is_running "$pid"; then
    pids+=("$pid")
  else
    echo "Found pidfile but process $pid not running — removing stale pidfile." >&2
    rm -f "$PIDFILE" || true
  fi
fi

if [ ${#pids[@]} -eq 0 ]; then
  # find java processes running the jar
  while IFS= read -r line; do
    # pgrep -f returns only pids, but we'll use ps to verify
    [ -n "$line" ] || continue
    pids+=("$line")
  done < <(pgrep -f "$JARNAME" || true)
fi

if [ ${#pids[@]} -eq 0 ]; then
  echo "No running bungee process found (no pidfile, no pgrep match)." >&2
  exit 0
fi

echo "Found process(es): ${pids[*]}" >&2

for pid in "${pids[@]}"; do
  if ! is_running "$pid"; then
    echo "PID $pid not running, skipping." >&2
    continue
  fi

  owner_uid=$(ps -o uid= -p "$pid" | tr -d ' ' || echo "")
  use_sudo=0
  if [ -n "$owner_uid" ] && [ "$owner_uid" -ne "$(id -u)" ]; then
    use_sudo=1
  fi

  echo "Stopping PID $pid (owned by UID=$owner_uid)" >&2
  if [ $use_sudo -eq 1 ]; then
    echo "Using sudo to send SIGTERM to $pid" >&2
    sudo kill -TERM "$pid" || true
  else
    kill -TERM "$pid" || true
  fi

  # wait up to 15s for process to exit
  timeout=15
  while [ $timeout -gt 0 ] && is_running "$pid"; do
    sleep 1
    timeout=$((timeout - 1))
  done

  if is_running "$pid"; then
    echo "PID $pid didn't exit, sending SIGKILL" >&2
    if [ $use_sudo -eq 1 ]; then
      sudo kill -KILL "$pid" || true
    else
      kill -KILL "$pid" || true
    fi
    # give it a moment
    sleep 1
  fi

  if is_running "$pid"; then
    echo "Failed to stop PID $pid" >&2
  else
    echo "Stopped PID $pid" >&2
  fi
done

# cleanup pidfile if the process we stopped matched it
if [ -f "$PIDFILE" ]; then
  pidfile_pid=$(cat "$PIDFILE" 2>/dev/null || true)
  still_running=0
  if is_running "$pidfile_pid"; then still_running=1; fi
  if [ $still_running -eq 0 ]; then
    rm -f "$PIDFILE" || true
    echo "Removed pidfile $PIDFILE" >&2
  else
    echo "Pidfile $PIDFILE still points to a running PID ($pidfile_pid) — leaving it." >&2
  fi
fi

# show ports status for visibility
echo "Ports listening (25577, 8081):" >&2
ss -ltn sport = :25577 || true
ss -ltn sport = :8081 || true

exit 0
