#!/bin/bash
echo "=== Packaging Lambda function ==="
cd digest-function

# Install dependencies
pip install -r requirements.txt -t ./package/ --quiet

# Copy handler
cp handler.py package/

# Create zip
cd package
zip -r ../digest-function.zip . --quiet
cd ..
rm -rf package

echo "Lambda package created: digest-function/digest-function.zip"
echo "Deploy with:"
echo "  aws lambda create-function \\"
echo "    --function-name cc-digest \\"
echo "    --runtime python3.11 \\"
echo "    --handler handler.lambda_handler \\"
echo "    --zip-file fileb://digest-function/digest-function.zip \\"
echo "    --role arn:aws:iam::ACCOUNT_ID:role/lambda-execution-role \\"
echo "    --environment Variables={SENDER_EMAIL=noreply@contentcurator.ai,AWS_REGION=us-east-1} \\"
echo "    --region us-east-1"
