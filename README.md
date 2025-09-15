# Burnbox - Secure Disposable Email Service

A secure, event-driven backend for disposable email service built with Spring Boot, featuring Google OAuth2 authentication, Kafka event streaming, and AWS integration.

## Features Implemented

### 🔐 Authentication & Security
- **Google OAuth2 Login**: Secure authentication using Google OAuth2
- **JWT Token Management**: Stateless session management with JWT tokens
- **User Management**: PostgreSQL-based user storage with automatic user creation/updates
- **Protected APIs**: All mailbox operations require authentication

### 📧 Mailbox Management
- **User-linked Mailboxes**: Each mailbox is linked to a specific user
- **TTL Support**: Mailboxes expire automatically based on configured lifespan
- **Burn After Read**: Optional feature to delete mailbox after first message read
- **Secure Access**: Users can only access their own mailboxes

### 📨 Message Handling
- **Message Retrieval**: Fetch messages with full content and attachment links
- **Attachment Management**: S3-based attachment storage with pre-signed URLs
- **Event Publishing**: All message access events are published to Kafka

### 🚀 Event-Driven Architecture
- **Kafka Integration**: Event publishing for all mailbox lifecycle events
- **Event Types**:
  - `MAILBOX_CREATED`: Published when a new mailbox is created
  - `MESSAGE_RECEIVED`: Published when a message is accessed
  - `MAILBOX_EXPIRED`: Published when a mailbox is deleted/expired
- **Scalable Design**: Ready for microservices integration and analytics

### 🗄️ Data Storage
- **PostgreSQL**: User data and authentication
- **DynamoDB**: Mailbox and message data with TTL
- **S3**: Attachment storage with secure access

## API Endpoints

### Authentication
- `GET /auth/google/login` - Initiate Google OAuth2 login
- `GET /auth/google/callback` - OAuth2 callback handler (returns JWT)
- `GET /auth/me` - Get current user information

### Mailbox Operations (All require JWT authentication)
- `POST /api/mailboxes/create` - Create new mailbox
- `GET /api/mailboxes/{id}/messages` - Get all messages for mailbox
- `GET /api/mailboxes/{id}/status` - Get mailbox status
- `DELETE /api/mailboxes/{id}` - Delete mailbox and all messages

### Message Operations (All require JWT authentication)
- `GET /messages/{mailboxId}/{messageId}` - Get full message with attachments
- `GET /messages/{mailboxId}/{messageId}/attachment/{attachmentId}` - Download attachment

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+
- AWS Account (for DynamoDB and S3)

## Setup Instructions

### 1. Database Setup

Run the database setup script:
```bash
./setup-database.sh
```

### 2. Environment Configuration

Update `src/main/resources/application.yml` with your configuration:

```yaml
# Google OAuth2 Configuration
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}

# JWT Configuration
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

# Kafka Configuration
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

# PostgreSQL Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/burnbox
    username: burnbox_user
    password: burnbox_password

# AWS Configuration
aws:
  access-key-id: ${AWS_ACCESS_KEY_ID}
  secret-access-key: ${AWS_SECRET_ACCESS_KEY}
  region: ${AWS_REGION:ap-south-1}
  s3:
    bucket-name: ${S3_BUCKET_NAME:burnbox-attachments}
```

### 3. Environment Variables

Set the following environment variables:

```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
export JWT_SECRET="your-jwt-secret-key"
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
export AWS_ACCESS_KEY_ID="your-aws-access-key"
export AWS_SECRET_ACCESS_KEY="your-aws-secret-key"
export AWS_REGION="ap-south-1"
export S3_BUCKET_NAME="burnbox-attachments"
```

### 4. Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Create OAuth2 credentials
5. Add authorized redirect URIs:
   - `http://localhost:8080/auth/google/callback` (for development)
   - `https://yourdomain.com/auth/google/callback` (for production)

### 5. AWS Setup

1. Create DynamoDB tables:
   - `mailboxes` (Primary Key: id)
   - `messages` (Primary Key: id, GSI: mailboxId)

2. Create S3 bucket for attachments:
   - Bucket name: `burnbox-attachments`
   - Enable versioning and lifecycle policies

3. Configure IAM permissions for your AWS credentials

### 6. Kafka Setup

Start Kafka locally or use a managed service:

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties

# Create topics
bin/kafka-topics.sh --create --topic MAILBOX_CREATED --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic MESSAGE_RECEIVED --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic MAILBOX_EXPIRED --bootstrap-server localhost:9092
```

### 7. Build and Run

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/ephemail-0.0.1-SNAPSHOT.jar
```

## Usage

### 1. Authentication Flow

1. Navigate to `/auth/google/login` to initiate OAuth2 flow
2. Complete Google authentication
3. Receive JWT token from `/auth/google/callback`
4. Use JWT token in Authorization header: `Bearer <token>`

### 2. Create Mailbox

```bash
curl -X POST http://localhost:8080/api/mailboxes/create \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lifespan": 60,
    "burnAfterRead": false
  }'
```

### 3. Get Messages

```bash
curl -X GET http://localhost:8080/api/mailboxes/{mailboxId}/messages \
  -H "Authorization: Bearer <your-jwt-token>"
```

### 4. Get Single Message

```bash
curl -X GET http://localhost:8080/messages/{mailboxId}/{messageId} \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot   │    │   PostgreSQL    │
│   (React/Vue)   │◄──►│   Backend       │◄──►│   (Users)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Apache Kafka  │
                       │   (Events)      │
                       └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   DynamoDB      │    │   S3 Storage    │
                       │ (Mailboxes/     │    │ (Attachments)   │
                       │  Messages)      │    │                 │
                       └─────────────────┘    └─────────────────┘
```

## Event Schema

### MAILBOX_CREATED
```json
{
  "mailboxId": "mailbox_1234567890_123",
  "userId": "google_user_123",
  "address": "mailbox_1234567890_123@nahneedpfft.com",
  "expiryTime": "2024-01-01T12:00:00Z",
  "burnAfterRead": false,
  "createdAt": "2024-01-01T10:00:00Z",
  "eventType": "MAILBOX_CREATED"
}
```

### MESSAGE_RECEIVED
```json
{
  "messageId": "msg_1234567890",
  "mailboxId": "mailbox_1234567890_123",
  "userId": "google_user_123",
  "from": "sender@example.com",
  "subject": "Test Message",
  "receivedAt": "2024-01-01T11:00:00Z",
  "hasAttachments": true,
  "eventType": "MESSAGE_RECEIVED"
}
```

### MAILBOX_EXPIRED
```json
{
  "mailboxId": "mailbox_1234567890_123",
  "userId": "google_user_123",
  "address": "mailbox_1234567890_123@nahneedpfft.com",
  "expiryTime": "2024-01-01T12:00:00Z",
  "deletedAt": "2024-01-01T12:05:00Z",
  "messagesDeleted": 5,
  "attachmentsDeleted": 3,
  "eventType": "MAILBOX_EXPIRED"
}
```

## Security Features

- **OAuth2 Authentication**: Secure Google-based authentication
- **JWT Tokens**: Stateless session management
- **User Isolation**: Users can only access their own data
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Input Validation**: Comprehensive request validation
- **Error Handling**: Secure error responses without information leakage

## Monitoring & Observability

- **Actuator Endpoints**: Health checks and metrics
- **Structured Logging**: Comprehensive logging with correlation IDs
- **Event Tracking**: All user actions are tracked via Kafka events
- **Performance Metrics**: Built-in Spring Boot metrics

## Deployment

### AWS Elastic Beanstalk

1. Build the application: `mvn clean package`
2. Deploy to Elastic Beanstalk
3. Configure environment variables in EB console
4. Set up RDS for PostgreSQL
5. Configure MSK for Kafka (or use Confluent Cloud)
6. Set up S3 bucket for attachments

### Docker

```dockerfile
FROM openjdk:17-jre-slim
COPY target/ephemail-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.