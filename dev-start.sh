#!/bin/bash

# Development Quick Start Script
# Sets up the development environment with minimal configuration

set -e

echo "🚀 Burnbox Development Quick Start"
echo "=================================="

# Create .env from template if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp .env.example .env
    
    # Generate a random JWT secret
    JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
    
    # Update .env with generated values
    sed -i.bak "s|JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" .env
    
    echo "✅ .env file created with random JWT secret"
    echo "⚠️  You still need to set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET"
fi

# Check if Google OAuth is configured
if grep -q "your_google_client_id_here" .env; then
    echo ""
    echo "⚠️  Google OAuth2 not configured!"
    echo "📋 To set up Google OAuth2:"
    echo "   1. Go to https://console.developers.google.com/"
    echo "   2. Create a new project or select existing"
    echo "   3. Enable Google+ API"
    echo "   4. Create OAuth2 credentials"
    echo "   5. Add redirect URI: http://localhost:8080/login/oauth2/code/google"
    echo "   6. Update GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET in .env"
    echo ""
    echo "🔄 For now, we'll start with Google OAuth disabled..."
fi

# Start only essential services for development
echo ""
echo "🔨 Starting essential development services..."
docker-compose up -d postgres dynamodb redis

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 5

# Check if PostgreSQL is ready
until docker-compose exec -T postgres pg_isready -U burnbox -d burnbox; do
    echo "⏳ Waiting for PostgreSQL..."
    sleep 2
done

echo ""
echo "✅ Essential services are ready!"
echo ""
echo "🎯 Next steps:"
echo "   1. Configure Google OAuth2 in .env file"
echo "   2. Run: mvn spring-boot:run"
echo "   3. Access: http://localhost:8080/actuator/health"
echo ""
echo "📂 Available services:"
echo "   🗄️  PostgreSQL: localhost:5432 (burnbox/password)"
echo "   📦 DynamoDB:   localhost:8000"  
echo "   💾 Redis:      localhost:6379"
echo ""
echo "🛑 To stop: docker-compose down"