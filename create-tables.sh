#!/bin/bash

# Script to create DynamoDB tables for the disposable email service
# Make sure AWS CLI is configured with proper credentials

echo "Creating DynamoDB tables..."

# Create mailboxes table
aws dynamodb create-table \
    --table-name mailboxes \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
    --key-schema \
        AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region ap-south-1

# Create messages table  
aws dynamodb create-table \
    --table-name messages \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
    --key-schema \
        AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region ap-south-1

echo "Tables created successfully!"
echo "You can verify in the AWS Console: https://console.aws.amazon.com/dynamodb/"