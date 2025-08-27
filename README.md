# Disposable Email Service

## Overview
The Disposable Email Service is a Spring Boot application that allows users to create temporary email mailboxes. Users can generate mailboxes, receive emails via AWS SES, and retrieve messages associated with their mailboxes. The service uses DynamoDB for storage and provides RESTful APIs for interaction.

## Features
- Create disposable mailboxes
- Retrieve messages associated with a specific mailbox
- Manually delete mailboxes and their associated messages
- Integration with AWS SES for receiving emails

## Technologies Used
- Spring Boot 3
- Spring Web
- AWS SDK v2 (DynamoDB, DynamoDB Enhanced, SES)
- Lombok
- Jakarta Mail

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
├── pom.xml
├── connect
└── README.md
```

## Setup Instructions
1. Clone the repository:
   ```
   git clone <repository-url>
   cd disposable-email-service
   ```

2. Update the `application.yml` file with your AWS credentials, DynamoDB table names, and SES region.

3. Build the project using Maven:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   ./connect
   ```

## API Endpoints
- **POST /api/mailboxes**: Create a mailbox.
- **GET /api/mailboxes/{id}/messages**: Retrieve all messages for a specific mailbox.
- **DELETE /api/mailboxes/{id}**: Delete a mailbox and its messages.

## Additional Notes
- The project was migrated from Gradle to Maven. All Gradle files were removed.
- AWS DynamoDB and DynamoDbEnhancedClient beans are configured automatically.
- A shell script `connect` is provided to start the backend easily.
- If you see a login page, it is due to default Spring Security. The backend only exposes REST endpoints and does not serve a web UI.

## License
This project is licensed under the MIT License.