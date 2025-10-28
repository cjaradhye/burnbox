#!/bin/bash

# Test Burnbox with Docker Compose (includes PostgreSQL)
echo "🐳 Testing Burnbox with Docker Compose (PostgreSQL included)"

# Stop any existing containers
echo "🛑 Stopping existing containers..."
docker-compose -f docker-compose.test.yml down

# Build and start services
echo "🔨 Building and starting services..."
docker-compose -f docker-compose.test.yml up --build -d

echo "⏳ Waiting for services to be ready..."
sleep 30

# Check health
echo "🏥 Checking service health..."
echo "PostgreSQL Health:"
docker-compose -f docker-compose.test.yml exec postgres-test pg_isready -U burnbox_user -d burnbox

echo "Application Health:"
curl -f http://localhost:8080/health || echo "❌ Health check failed"

echo ""
echo "📋 Service Status:"
docker-compose -f docker-compose.test.yml ps

echo ""
echo "🎯 Test endpoints:"
echo "  Health: http://localhost:8080/health"
echo "  Actuator: http://localhost:8080/actuator/health"
echo "  API: http://localhost:8080/api/mailboxes"

echo ""
echo "📜 View logs:"
echo "  docker-compose -f docker-compose.test.yml logs burnbox-app"
echo "  docker-compose -f docker-compose.test.yml logs postgres-test"

echo ""
echo "🛑 Stop services:"
echo "  docker-compose -f docker-compose.test.yml down"