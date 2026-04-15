#!/bin/zsh
BASE=~/projects/content-curator
LOG_DIR=$BASE/dev-tools/logs
mkdir -p $LOG_DIR

kill_ports() {
  for port in 8081 8082 8083 8084 5001 3000; do
    lsof -ti :$port | xargs kill -9 2>/dev/null
  done
  sleep 2
}

build() {
  echo "Building all modules..."
  cd $BASE
  mvn clean package -DskipTests -q && echo "Build OK" || { echo "Build FAILED"; exit 1; }
}

get_port() {
  case $1 in
    user-profile)      echo 8081 ;;
    content-ingestion) echo 8082 ;;
    recommendation)    echo 8083 ;;
    analytics)         echo 8084 ;;
  esac
}

start_ml() {
  echo "  Starting ml-service on :5001"
  cd $BASE/ml-service
  source venv/bin/activate
  nohup python3 main.py > $LOG_DIR/ml-service.log 2>&1 &
  echo $! > $LOG_DIR/ml-service.pid
}

start_svc() {
  local svc=$1
  local port=$(get_port $svc)
  echo "  Starting $svc on :$port"
  cd $BASE/$svc
  nohup mvn spring-boot:run > $LOG_DIR/$svc.log 2>&1 &
  echo $! > $LOG_DIR/$svc.pid
}

wait_healthy() {
  local svc=$1
  local port=$(get_port $svc)
  local max=60 i=0
  while [ $i -lt $max ]; do
    # Accept 200 (actuator ok) or 403 (running but secured) or 404 (running, no actuator)
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null)
    [[ "$STATUS" == "200" || "$STATUS" == "403" || "$STATUS" == "404" ]] && return 0
    sleep 2; i=$((i+2))
  done
  return 1
}

status() {
  echo "=== Service Status ==="
  for svc in user-profile content-ingestion recommendation analytics; do
    port=$(get_port $svc)
    HTTP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null)
    case $HTTP in
      200) echo "  $svc ($port): UP" ;;
      403|404) echo "  $svc ($port): UP (running, actuator secured)" ;;
      *) echo "  $svc ($port): DOWN (HTTP $HTTP)" ;;
    esac
  done
  ML=$(curl -s http://localhost:5001/health 2>/dev/null | python3 -c "import sys,json; print(json.load(sys.stdin).get('status'))" 2>/dev/null)
  echo "  ml-service (5001): ${ML:-DOWN}"
}

case "$1" in
  start)
    echo "=== Starting Content Curator ==="
    build
    kill_ports
    start_ml
    for svc in user-profile content-ingestion recommendation analytics; do
      start_svc $svc
    done
    echo "Waiting for services (up to 60s)..."
    for svc in user-profile content-ingestion recommendation analytics; do
      wait_healthy $svc && echo "  $svc: ready" || echo "  $svc: timeout — check: curator logs $svc"
    done
    status
    $BASE/dev-tools/quickstart.sh
    source $BASE/dev-tools/.token
    echo ""
    # Deploy Lambda
    echo "Deploying Lambda..."
    cd $BASE/lambda
    pip install boto3 -t digest-function/package/ --quiet 2>/dev/null
    cp digest-function/handler.py digest-function/package/
    cd digest-function/package && zip -r ../digest-function.zip . --quiet && cd ../..
    aws --endpoint-url=http://localhost:4566 lambda delete-function --function-name cc-digest --region us-east-1 2>/dev/null || true
    aws --endpoint-url=http://localhost:4566 lambda create-function \\
      --function-name cc-digest \\
      --runtime python3.11 \\
      --handler handler.lambda_handler \\
      --zip-file fileb://digest-function/digest-function.zip \\
      --role arn:aws:iam::000000000000:role/lambda-execution-role \\
      --environment "Variables={SENDER_EMAIL=noreply@contentcurator.ai,AWS_REGION=us-east-1}" \\
      --region us-east-1 >/dev/null 2>&1 && echo "Lambda deployed" || echo "Lambda deploy failed"
    echo "Ready. Token loaded."
    echo "Frontend: cd ~/projects/content-curator/frontend && npm start"
    ;;

  restart)
    SVC=${2:-all}
    if [ "$SVC" = "all" ]; then
      $0 stop && $0 start
    else
      echo "Rebuilding $SVC..."
      cd $BASE
      mvn clean package -DskipTests -q -pl $SVC -am || { echo "Build FAILED"; exit 1; }
      PID_FILE=$LOG_DIR/$SVC.pid
      [ -f $PID_FILE ] && kill $(cat $PID_FILE) 2>/dev/null && rm $PID_FILE
      PORT=$(get_port $SVC)
      lsof -ti :$PORT | xargs kill -9 2>/dev/null
      sleep 2
      start_svc $SVC
      wait_healthy $SVC && echo "$SVC restarted OK" || echo "$SVC failed — check: curator logs $SVC"
    fi
    ;;

  stop)
    echo "=== Stopping all services ==="
    kill_ports
    rm -f $LOG_DIR/*.pid
    echo "Done"
    ;;

  status)
    status
    ;;

  logs)
    SVC=${2:-user-profile}
    [ -f $LOG_DIR/$SVC.log ] && tail -f $LOG_DIR/$SVC.log || echo "No log found for $SVC"
    ;;

  test)
    source $BASE/dev-tools/.token
    echo "=== Smoke Tests ==="
    echo -n "  Login:           "
    curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST http://localhost:8081/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"email":"sk@test.com","password":"password123"}'
    echo -n "  Profile:         "
    curl -s -o /dev/null -w "HTTP %{http_code}\n" http://localhost:8081/api/users/me \
      -H "Authorization: Bearer $TOKEN"
    echo -n "  Feed poll:       "
    curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST http://localhost:8082/api/feeds/poll \
      -H "Authorization: Bearer $TOKEN"
    sleep 8
    echo -n "  Recommendations: "
    curl -s -o /dev/null -w "HTTP %{http_code}\n" \
      "http://localhost:8083/api/recommendations?limit=5" \
      -H "Authorization: Bearer $TOKEN"
    echo -n "  Dashboard:       "
    curl -s -o /dev/null -w "HTTP %{http_code}\n" \
      http://localhost:8084/api/analytics/dashboard \
      -H "Authorization: Bearer $TOKEN"
    echo -n "  ML service:      "
    curl -s http://localhost:5001/health | python3 -c "import sys,json; print(json.load(sys.stdin)['status'])"
    echo -n "  S3 articles:     "
    aws --endpoint-url=http://localhost:4566 s3 ls \
      s3://content-curator-articles/ --recursive 2>/dev/null | wc -l | xargs echo "files"
    echo -n "  Lambda:          "
    PAYLOAD='{"Records":[{"body":"{\"userId\":\"a09b1130-866b-452f-834e-987b3a2f08a4\",\"email\":\"sk@test.com\",\"displayName\":\"SK Sharma\"}"}]}'
    aws --endpoint-url=http://localhost:4566 lambda invoke \
      --function-name cc-digest \
      --payload "$PAYLOAD" \
       \
      --region us-east-1 \
      /tmp/lambda-response.json > /dev/null 2>&1 && cat /tmp/lambda-response.json || echo "Lambda not deployed"
    ;;

  *)
    echo "Usage: curator {start|stop|restart [svc]|status|logs [svc]|test}"
    echo ""
    echo "  curator start                     # build + start all"
    echo "  curator stop                      # stop all"
    echo "  curator restart                   # rebuild + restart all"
    echo "  curator restart content-ingestion # rebuild + restart one"
    echo "  curator status                    # check all ports"
    echo "  curator logs analytics            # tail a log"
    echo "  curator test                      # smoke tests"
    ;;
esac
