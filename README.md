# Disposable Email Service

## Overview
The Disposable Email Service is a Spring Boot application that allows users to create temporary email mailboxes. Users can generate mailboxes with a specified lifespan, receive emails via AWS SES, and retrieve messages associated with their mailboxes. The service uses DynamoDB for storage and provides RESTful APIs for interaction.

## Features
- Create disposable mailboxes with configurable lifespans (5 minutes, 10 minutes, 1 hour).
- Retrieve messages associated with a specific mailbox.
- Manually delete mailboxes and their associated messages.
- Integration with AWS SES for receiving emails.
- Scheduled cleanup of expired mailboxes.

## Technologies Used
- Spring Boot 3
- Spring Web
- Spring Data DynamoDB (via AWS SDK v2)
- AWS SDK for SES (Simple Email Service)
- Lombok
- Spring Boot Actuator
- Spring Scheduler

## Project Structure
```
disposable-email-service
├── src
│   └── main
│       ├── java
│       │   └── com
│       │       └── disposablemailservice
│       │           ├── DisposableEmailServiceApplication.java
│       │           ├── controller
│       │           │   └── MailboxController.java
│       │           ├── service
│       │           │   ├── MailboxService.java
│       │           │   └── EmailService.java
│       │           ├── model
│       │           │   ├── Mailbox.java
│       │           │   └── Message.java
│       │           └── repository
│       │               ├── MailboxRepository.java
│       │               └── MessageRepository.java
│       └── resources
│           └── application.yml
├── build.gradle
└── README.md
```

## Setup Instructions
1. Clone the repository:
   ```
   git clone <repository-url>
   cd disposable-email-service
   ```

2. Update the `application.yml` file with your AWS credentials, DynamoDB table names, and SES region.

3. Build the project using Gradle:
   ```
   ./gradlew build
   ```

4. Run the application:
   ```
   ./gradlew bootRun
   ```

## API Endpoints
- **POST /api/mailboxes**: Create a mailbox with a specified lifespan.
- **GET /api/mailboxes/{id}/messages**: Retrieve all messages for a specific mailbox.
- **DELETE /api/mailboxes/{id}**: Manually delete a mailbox and its messages.

## CORS Support
The application is configured to allow CORS requests from frontend applications.

## License
This project is licensed under the MIT License.