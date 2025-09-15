#!/bin/bash

# Production Deployment Script for Burnbox Application
# This script prepares and deploys the application to production

set -e  # Exit on any error

echo "🚀 Deploying Burnbox to Production..."
echo "====================================="

# Configuration
REGISTRY_URL=${REGISTRY_URL:-"your-registry.com"}
APP_NAME="burnbox"
VERSION=${VERSION:-$(date +%Y%m%d-%H%M%S)}
PROD_TAG="${REGISTRY_URL}/${APP_NAME}:${VERSION}"
LATEST_TAG="${REGISTRY_URL}/${APP_NAME}:latest"

# Check if production environment file exists
if [ ! -f .env.prod ]; then
    echo "❌ .env.prod file not found!"
    echo "📝 Please create .env.prod with production configuration."
    echo "💡 Use .env.example as a template."
    exit 1
fi

# Load production environment variables
source .env.prod

# Validate required environment variables
required_vars=(
    "DATABASE_URL"
    "GOOGLE_CLIENT_ID" 
    "GOOGLE_CLIENT_SECRET"
    "JWT_SECRET"
    "BASE_URL"
    "FRONTEND_URL"
)

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "❌ Required environment variable $var is not set in .env.prod"
        exit 1
    fi
done

echo "✅ Environment validation passed"

# Build production image
echo ""
echo "🔨 Building production Docker image..."
docker build \
    --target production \
    --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
    --build-arg VERSION=${VERSION} \
    -t ${APP_NAME}:${VERSION} \
    -t ${APP_NAME}:latest \
    .

if [ $? -ne 0 ]; then
    echo "❌ Production build failed!"
    exit 1
fi

echo "✅ Production build successful"

# Tag for registry
echo ""
echo "🏷️  Tagging images for registry..."
docker tag ${APP_NAME}:${VERSION} ${PROD_TAG}
docker tag ${APP_NAME}:latest ${LATEST_TAG}

# Push to registry (uncomment when registry is configured)
echo ""
echo "📤 Pushing to registry..."
echo "⚠️  Registry push disabled. Uncomment lines in script to enable."
# docker push ${PROD_TAG}
# docker push ${LATEST_TAG}

# Generate production docker-compose file
echo ""
echo "📝 Generating production docker-compose..."
cat > docker-compose.prod.yml << EOF
version: '3.8'

services:
  burnbox-app:
    image: ${PROD_TAG}
    container_name: burnbox-prod
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: \${DATABASE_URL}
      DATABASE_USERNAME: \${DATABASE_USERNAME}
      DATABASE_PASSWORD: \${DATABASE_PASSWORD}
      GOOGLE_CLIENT_ID: \${GOOGLE_CLIENT_ID}
      GOOGLE_CLIENT_SECRET: \${GOOGLE_CLIENT_SECRET}
      JWT_SECRET: \${JWT_SECRET}
      BASE_URL: \${BASE_URL}
      FRONTEND_URL: \${FRONTEND_URL}
      AWS_REGION: \${AWS_REGION}
      AWS_S3_BUCKET: \${AWS_S3_BUCKET}
      AWS_SNS_TOPIC: \${AWS_SNS_TOPIC}
      KAFKA_BOOTSTRAP_SERVERS: \${KAFKA_BOOTSTRAP_SERVERS}
      KAFKA_ENABLED: \${KAFKA_ENABLED:-true}
      LOG_LEVEL: \${LOG_LEVEL:-WARN}
    ports:
      - "8080:8080"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'
EOF

# Generate deployment instructions
echo ""
echo "📋 Generating deployment instructions..."
cat > DEPLOYMENT.md << EOF
# Burnbox Production Deployment

## Quick Deploy

\`\`\`bash
# 1. Copy files to production server
scp .env.prod docker-compose.prod.yml user@your-server:/opt/burnbox/

# 2. SSH to production server
ssh user@your-server

# 3. Navigate to application directory
cd /opt/burnbox

# 4. Start the application
docker-compose -f docker-compose.prod.yml up -d

# 5. Check health
curl http://localhost:8080/actuator/health
\`\`\`

## Environment Setup

Make sure these are configured in your production environment:

### Database
- PostgreSQL instance (AWS RDS recommended)
- Connection details in DATABASE_URL

### AWS Services
- SES configured for email receiving
- SNS topic created
- S3 bucket for attachments
- DynamoDB tables created

### External Services  
- Google OAuth2 credentials
- Domain DNS configured
- SSL certificates

## Monitoring

- Health: http://your-domain/actuator/health
- Metrics: http://your-domain/actuator/metrics
- Logs: \`docker-compose logs -f burnbox-app\`

## Rollback

\`\`\`bash
# Stop current version
docker-compose -f docker-compose.prod.yml down

# Deploy previous version
docker-compose -f docker-compose.prod.yml up -d
\`\`\`
EOF

echo ""
echo "✅ Production deployment prepared!"
echo ""
echo "📂 Generated files:"
echo "   📄 docker-compose.prod.yml - Production compose file"
echo "   📖 DEPLOYMENT.md - Deployment instructions"
echo ""
echo "🎯 Next steps:"
echo "   1. Review docker-compose.prod.yml"
echo "   2. Copy files to production server"
echo "   3. Follow instructions in DEPLOYMENT.md"
echo ""
echo "🔧 Registry deployment:"
echo "   📦 Image tagged: ${PROD_TAG}"
echo "   📦 Latest tagged: ${LATEST_TAG}"
echo "   💡 Uncomment push commands to deploy to registry"
echo ""

# Save deployment info
echo "Deployment Info:" > deployment-info.txt
echo "Version: ${VERSION}" >> deployment-info.txt
echo "Build Date: $(date)" >> deployment-info.txt
echo "Image: ${PROD_TAG}" >> deployment-info.txt
echo "Environment: Production" >> deployment-info.txt

echo "📝 Deployment info saved to deployment-info.txt"
echo ""
echo "🎉 Production deployment ready!"