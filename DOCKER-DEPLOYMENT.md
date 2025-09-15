# Burnbox Production-Ready Deployment Guide

This application is now fully containerized and ready for production deployment! 🚀

## 🚀 Quick Start Options

### Option 1: Development Setup
```bash
# Quick development start (minimal services)
./dev-start.sh

# Then run the app locally
mvn spring-boot:run
```

### Option 2: Full Dockerized Development
```bash
# Complete Docker environment
./build-and-run.sh
```

### Option 3: Production Deployment
```bash
# Prepare for production
./deploy.sh
```

## 📋 What's Been Added

### 🐳 **Docker Configuration**
- **Multi-stage Dockerfile** with security best practices
- **Docker Compose** with complete development stack
- **Health checks** for all services
- **Non-root user** for security
- **Optimized JVM settings** for containers

### 🔧 **Production Configuration**
- **application-prod.yml** with environment variables
- **Comprehensive logging** configuration
- **Database connection pooling** 
- **Kafka optimization** for production
- **Security headers** and session management

### 📦 **Complete Stack**
- **PostgreSQL 15** for main database
- **DynamoDB Local** for development
- **Kafka + Zookeeper** for event streaming
- **Redis** for caching (optional)
- **Kafka UI** for monitoring

### 🛠️ **Automation Scripts**
- **build-and-run.sh** - Complete development environment
- **dev-start.sh** - Quick development start
- **deploy.sh** - Production deployment preparation

### 🔒 **Security & Production Features**
- Environment variable configuration
- Non-root container execution
- Health checks and monitoring
- Resource limits and constraints
- Comprehensive logging

## 🎯 **Deployment Targets**

### Local Development
```bash
./dev-start.sh  # Start services only
mvn spring-boot:run  # Run app locally
```

### Docker Development
```bash
./build-and-run.sh  # Full containerized stack
```

### Production
```bash
./deploy.sh  # Generate production artifacts
# Follow generated DEPLOYMENT.md instructions
```

## 📊 **Monitoring Endpoints**

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics  
- **Info**: http://localhost:8080/actuator/info
- **Kafka UI**: http://localhost:8081

## 🌐 **Service Ports**

| Service | Port | Description |
|---------|------|-------------|
| Burnbox API | 8080 | Main application |
| PostgreSQL | 5432 | Database |
| DynamoDB | 8000 | NoSQL database |
| Kafka | 9092 | Message broker |
| Kafka UI | 8081 | Kafka monitoring |
| Redis | 6379 | Cache |

## 🔧 **Environment Configuration**

All configuration is externalized via environment variables:

```bash
# Copy template and customize
cp .env.example .env

# Required for OAuth2
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_secret

# Required for security
JWT_SECRET=your_256_bit_secret

# Database (auto-configured for Docker)
DATABASE_URL=jdbc:postgresql://localhost:5432/burnbox
```

## 🚨 **Pre-Production Checklist**

- [ ] Google OAuth2 credentials configured
- [ ] JWT secret generated (256-bit minimum)
- [ ] Database connection string updated
- [ ] AWS credentials configured (for SES/SNS)
- [ ] Domain DNS configured
- [ ] SSL certificates obtained
- [ ] Load balancer configured
- [ ] Monitoring setup (logs, metrics)
- [ ] Backup strategy implemented

## 🔄 **CI/CD Ready**

The application includes:
- Multi-stage Docker builds
- Environment-based configuration
- Health checks for orchestration
- Resource limits for Kubernetes
- Comprehensive logging

Perfect for deployment on:
- **Docker Swarm**
- **Kubernetes** 
- **AWS ECS/Fargate**
- **Google Cloud Run**
- **Azure Container Instances**

## 📈 **Scaling Considerations**

- **Database**: Use managed PostgreSQL (RDS, Cloud SQL)
- **Cache**: Use managed Redis (ElastiCache, MemoryStore) 
- **Messaging**: Use managed Kafka (MSK, Confluent Cloud)
- **Storage**: Use cloud object storage (S3, GCS, Blob)
- **Load Balancing**: Use cloud load balancers
- **Monitoring**: Use cloud monitoring (CloudWatch, Stackdriver)

Your application is now production-ready! 🎉