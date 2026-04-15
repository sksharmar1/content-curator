#!/bin/bash
# Usage: ./logs-services.sh [service-name]
# e.g.:  ./logs-services.sh user-profile
#        ./logs-services.sh  (shows all)
LOG_DIR=~/projects/content-curator/dev-tools/logs

if [ -n "$1" ]; then
  tail -f $LOG_DIR/$1.log
else
  echo "Available logs:"
  ls $LOG_DIR/*.log 2>/dev/null | xargs -I{} basename {}
  echo ""
  echo "Usage: $0 <service-name>"
  echo "e.g.:  $0 user-profile"
fi
