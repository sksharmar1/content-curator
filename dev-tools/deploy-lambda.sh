#!/bin/bash
BASE=~/projects/content-curator
echo "Deploying Lambda cc-digest..."

cd $BASE/lambda
pip install boto3 -t digest-function/package/ --quiet 2>/dev/null
cp digest-function/handler.py digest-function/package/
cd digest-function/package && zip -r ../digest-function.zip . --quiet && cd ../..

aws --endpoint-url=http://localhost:4566 lambda delete-function \
  --function-name cc-digest --region us-east-1 2>/dev/null || true

aws --endpoint-url=http://localhost:4566 lambda create-function \
  --function-name cc-digest \
  --runtime python3.11 \
  --handler handler.lambda_handler \
  --zip-file fileb://digest-function/digest-function.zip \
  --role arn:aws:iam::000000000000:role/lambda-execution-role \
  --environment "Variables={SENDER_EMAIL=noreply@contentcurator.ai,AWS_REGION=us-east-1,RECOMMENDATION_SERVICE_URL=http://host.docker.internal:8083}" \
  --region us-east-1 > /dev/null 2>&1 && echo "Lambda deployed OK" || echo "Lambda deploy failed"
