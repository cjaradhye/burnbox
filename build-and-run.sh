#!/bin/bash

# Build and Run Script for Burnbox Application
# This script builds the Docker image and starts the entire stack

set -e  # Exit on any error

echo "🏗️  Building Burnbox Application..."
echo "=================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found. Creating from template..."
    cp .env.example .env
    echo "📝 Please edit .env file with your configuration before running again."
    echo "📋 Required: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, JWT_SECRET"
    exit 1
fi

# Build the Docker image
echo "🔨 Building Docker image..."
docker build -t burnbox:latest .

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "🚀 Starting application stack with Docker Compose..."
    echo "======================================================"
    
    # Start all services
    docker-compose up -d
    
    echo ""
    echo "🎉 Application stack started successfully!"
    echo ""
    echo "📍 Services:"
    echo "   🔗 Backend API:     http://localhost:8080"
    echo "   ❤️  Health Check:   http://localhost:8080/actuator/health"
    echo "   📊 Kafka UI:        http://localhost:8081"
    echo "   🗄️  PostgreSQL:     localhost:5432"
    echo "   📦 DynamoDB Local:  localhost:8000"
    echo "   🔄 Kafka:           localhost:9092"
    echo "   💾 Redis:           localhost:6379"
    echo ""
    echo "📋 Management Commands:"
    echo "   📊 View logs:       docker-compose logs -f burnbox-app"
    echo "   🛑 Stop all:        docker-compose down"
    echo "   🔄 Restart app:     docker-compose restart burnbox-app"
    echo "   🧹 Clean volumes:   docker-compose down -v"
    echo ""
    echo "🧪 Test the API:"
    echo "   curl http://localhost:8080/actuator/health"
    echo ""
    
    # Wait for services to be ready
    echo "⏳ Waiting for services to be ready..."
    sleep 10
    
    # Check health
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Application is healthy and ready!"
    else
        echo "⚠️  Application may still be starting. Check logs with:"
        echo "   docker-compose logs -f burnbox-app"
    fi
    
else
    echo "❌ Build failed!"
    echo "📋 Check the build output above for errors."
    exit 1
fi