#!/bin/bash
LOG_DIR=~/projects/content-curator/dev-tools/logs
echo "=== Stopping all services ==="

for pid_file in $LOG_DIR/*.pid; do
  if [ -f "$pid_file" ]; then
    PID=$(cat $pid_file)
    SVC=$(basename $pid_file .pid)
    if kill -0 $PID 2>/dev/null; then
      kill $PID 2>/dev/null
      echo "  Stopped $SVC (PID $PID)"
    fi
    rm -f $pid_file
  fi
done

# Also kill by port as fallback
for port in 8081 8082 8083 8084 5001; do
  lsof -ti :$port | xargs kill -9 2>/dev/null
done

echo "All services stopped."
