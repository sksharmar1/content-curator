#!/bin/bash
echo "=== Starting Content Curator ==="

# Infrastructure
echo "Starting Docker services..."
cd ~/projects/content-curator/docker
docker compose up -d
sleep 15

# SQS queues
echo "Creating SQS queues..."
aws --endpoint-url=http://localhost:4566 sqs create-queue \
  --queue-name user-activity --region us-east-1 2>/dev/null || true
aws --endpoint-url=http://localhost:4566 sqs create-queue \
  --queue-name content-ingested --region us-east-1 2>/dev/null || true
aws --endpoint-url=http://localhost:4566 sqs create-queue \
  --queue-name digest-scheduled --region us-east-1 2>/dev/null || true

echo ""
echo "=== Infrastructure ready ==="
echo ""
echo "Now start these in separate terminals:"
echo ""
echo "Terminal 1: cd ~/projects/content-curator/user-profile && mvn spring-boot:run"
echo "Terminal 2: cd ~/projects/content-curator/content-ingestion && mvn spring-boot:run"
echo "Terminal 3: cd ~/projects/content-curator/recommendation && mvn spring-boot:run"
echo "Terminal 4: cd ~/projects/content-curator/analytics && mvn spring-boot:run"
echo "Terminal 5: cd ~/projects/content-curator/ml-service && source venv/bin/activate && python3 main.py"
echo ""
echo "Then run: ~/projects/content-curator/dev-tools/quickstart.sh"
echo "Then run: source ~/projects/content-curator/dev-tools/.token"
