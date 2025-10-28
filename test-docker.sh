#!/bin/bash

# Test Docker Container with Database Connection
echo "🐳 Testing Burnbox Docker Container with Database Connection"

# Check if PostgreSQL is running locally
if ! pg_isready -h localhost -p 5432; then
    echo "❌ PostgreSQL is not running on localhost:5432"
    echo "Please start PostgreSQL first:"
    echo "  brew services start postgresql"
    echo "  # OR using Docker:"
    echo "  docker run --name burnbox-postgres -e POSTGRES_DB=burnbox -e POSTGRES_USER=burnbox_user -e POSTGRES_PASSWORD=burnbox_password -p 5432:5432 -d postgres:14"
    exit 1
fi

echo "✅ PostgreSQL is running"

# Build the Docker image
echo "🔨 Building Docker image..."
docker build -t burnbox-backend .

if [ $? -ne 0 ]; then
    echo "❌ Docker build failed"
    exit 1
fi

echo "✅ Docker image built successfully"

# Run the container with proper network configuration
echo "🚀 Starting Burnbox container..."
docker run --rm \
    --name burnbox-test \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=docker \
    -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/burnbox \
    -e DB_USERNAME=burnbox_user \
    -e DB_PASSWORD=burnbox_password \
    -e JWT_SECRET=my-super-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm \
    burnbox-backend

echo "🎉 Container started! Check http://localhost:8080/health"