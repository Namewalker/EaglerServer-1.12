#!/usr/bin/env bash
# restart.sh - stop then start the bungee/Velocity proxy
# Usage: ./restart.sh [--foreground]

set -euo pipefail
cd "$(dirname "$0")"

echo "Running stop.sh..."
./stop.sh || true

echo "Running start.sh..."
# forward any args (e.g. --foreground) to start.sh
./start.sh "${@:-}"

echo "Restart complete. Check bungee.log for details."
