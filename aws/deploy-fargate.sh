#!/bin/bash
# Deploy ML service to AWS Fargate
# Usage: ./deploy-fargate.sh <account-id> <subnet-id> <security-group-id>

ACCOUNT_ID=$1
SUBNET_ID=$2
SG_ID=$3
REGION="us-east-1"
CLUSTER="content-curator-cluster"

if [ -z "$ACCOUNT_ID" ]; then
  echo "Usage: ./deploy-fargate.sh <account-id> <subnet-id> <security-group-id>"
  exit 1
fi

echo "=== Deploying ML Service to Fargate ==="

# Replace placeholder account ID in task definition
sed "s/ACCOUNT_ID/$ACCOUNT_ID/g" fargate-task-definition.json > /tmp/task-def.json

# Create ECS cluster if it doesn't exist
aws ecs create-cluster \
  --cluster-name $CLUSTER \
  --region $REGION 2>/dev/null || true

# Register task definition
TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json file:///tmp/task-def.json \
  --region $REGION \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)

echo "Task definition registered: $TASK_DEF_ARN"

# Create or update service
aws ecs create-service \
  --cluster $CLUSTER \
  --service-name ml-service \
  --task-definition content-curator-ml-service \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_ID],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
  --region $REGION 2>/dev/null || \
aws ecs update-service \
  --cluster $CLUSTER \
  --service ml-service \
  --task-definition content-curator-ml-service \
  --desired-count 1 \
  --region $REGION

echo "=== Fargate deployment complete ==="
echo "Service URL will be the public IP of the running task"
echo "Check: aws ecs list-tasks --cluster $CLUSTER --region $REGION"
