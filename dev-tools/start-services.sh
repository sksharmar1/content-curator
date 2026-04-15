#!/bin/bash
BASE=~/projects/content-curator
LOG_DIR=$BASE/dev-tools/logs
mkdir -p $LOG_DIR

echo "=== Content Curator — Starting all services ==="

# Kill anything already running
for port in 8081 8082 8083 8084 5001; do
  lsof -ti :$port | xargs kill -9 2>/dev/null
done
sleep 2

# Start ML service
echo "Starting ml-service (port 5001)..."
cd $BASE/ml-service
source venv/bin/activate
nohup python3 main.py > $LOG_DIR/ml-service.log 2>&1 &
echo $! > $LOG_DIR/ml-service.pid

# Start Spring Boot services
for svc in user-profile content-ingestion recommendation analytics; do
  echo "Starting $svc..."
  cd $BASE/$svc
  nohup mvn spring-boot:run > $LOG_DIR/$svc.log 2>&1 &
  echo $! > $LOG_DIR/$svc.pid
done

echo ""
echo "All services starting in background."
echo "Waiting 35 seconds for startup..."
sleep 35

# Health check
echo ""
echo "=== Health Check ==="
declare -A PORTS=(
  [user-profile]=8081
  [content-ingestion]=8082
  [recommendation]=8083
  [analytics]=8084
)

ALL_OK=true
for svc in "${!PORTS[@]}"; do
  port=${PORTS[$svc]}
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null)
  if [ "$STATUS" = "200" ]; then
    echo "  $svc (port $port): UP"
  else
    echo "  $svc (port $port): NOT READY (HTTP $STATUS) — check $LOG_DIR/$svc.log"
    ALL_OK=false
  fi
done

ML=$(curl -s http://localhost:5001/health 2>/dev/null | python3 -c "import sys,json; print(json.load(sys.stdin).get('status','DOWN'))" 2>/dev/null)
echo "  ml-service (port 5001): ${ML:-DOWN}"

echo ""
if $ALL_OK; then
  echo "All services UP. Run: source ~/projects/content-curator/dev-tools/.token"
  # Auto-get token
  ~/projects/content-curator/dev-tools/quickstart.sh
  source ~/projects/content-curator/dev-tools/.token
else
  echo "Some services not ready. Check logs in $LOG_DIR/"
fi
