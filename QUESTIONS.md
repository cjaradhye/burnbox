# Burnbox Architecture Q&A

1. What exact problem does Burnbox solve, and how is it different from a regular email service?
- Burnbox provides disposable, short‑lived mailboxes with optional burn‑after‑read and automatic expiry. Unlike regular email, mailboxes and messages are ephemeral (stored in DynamoDB with TTL semantics), access is gated via OAuth2/JWT, and all accesses emit events to Kafka for observability and downstream processing.

2. Why did you choose Kafka instead of AWS SQS or RabbitMQ?
- Kafka suits event streaming and replay for internal domain events (mailbox lifecycle, message access) with strong ordering per key and retention for analytics. The stack already includes a Kafka cluster in `docker-compose.yml` with UI for monitoring. Kafka’s log semantics and idempotent producer fit our append‑only event model better than queue‑oriented SQS/RabbitMQ.

3. How does your Kafka event stream work — producer, consumer, and message structure?
- Producer: `EventPublisherService` uses `KafkaTemplate` (configured in `KafkaConfig`) to publish JSON events to topics `mailbox-events` and `message-events` with keys = mailboxId/messageId.
- Consumer: Not implemented in this repo (producer‑only for now), but group config is present (`spring.kafka.consumer.group-id`).
- Structure: Map-based payload including identifiers, timestamps, and eventType, e.g. `MAILBOX_CREATED`, `MESSAGE_RECEIVED`, `MAILBOX_EXPIRED`.

4. What’s the purpose of SNS webhooks if Kafka already handles messaging?
- SNS handles external triggers from AWS (e.g., SES notifications for inbound emails). Kafka handles internal event flow for our application. Controller: `SnsEventController` -> `EmailEventService.processSnsEvent(...)` for parsing/ingest.

5. How does DynamoDB TTL ensure automatic data deletion, and what are its timing limitations?
- We store expiry timestamps (`Mailbox.expiryTime`). DynamoDB TTL deletes items asynchronously; deletion isn’t immediate and can take up to ~48 hours. App logic treats expired mailboxes as inaccessible even before TTL purge.

6. Why use Hibernate/JPA if DynamoDB is NoSQL? Did you use both simultaneously?
- Yes, clear separation: JPA/PostgreSQL for auth/profile (`User` JPA entity, Flyway migrations in `db/migration`), DynamoDB for mailbox and message content (`Mailbox`, `Message` annotated with DynamoDB mappers).

7. How did you handle transaction consistency between Kafka and DynamoDB?
- Events include stable keys (mailboxId/messageId) and timestamps; producer idempotence is enabled (`enable.idempotence=true`, acks=all, retries). Downstream consumers should perform idempotent updates keyed by event ids. No explicit outbox pattern here, but keys/versioning support dedupe.

8. How did you ensure message ordering or handle duplicates in Kafka?
- Ordering: Keys (mailboxId/messageId) ensure per‑key ordering. Duplicates: Kafka idempotent producer + consumer‑side idempotency (using keys) mitigate duplicates.

9. What is the retention period in your Kafka topics, and why did you choose that?
- Retention is set at broker level in `docker-compose.yml` with `KAFKA_LOG_RETENTION_HOURS: 168` (7 days) to enable short‑term replay/diagnostics without long‑term storage costs.

10. How did you integrate SES and S3 for attachments? Describe the flow.
- SES -> SNS posts to `/api/sns/event` -> `EmailEventService` (parse SES payload). Message metadata saved to DynamoDB (`Message`). Attachments stored in S3 via `S3Client`; download access via pre‑signed URLs from `S3Presigner` (`S3Config`).

11. How do you ensure data privacy and security of email attachments on S3?
- Private buckets, no public ACLs; access via time‑bound pre‑signed URLs; server‑side encryption enabled by S3 defaults/policy; URLs only returned to authenticated users via JWT.

12. What does “burn-after-read” look like in your system flow? How is read-tracking done?
- Mailbox has `burnAfterRead` flag. On first read of messages for such mailboxes, application deletes messages/mailbox and publishes `MAILBOX_EXPIRED`. (Flag present in model and events; enforcement points in service/controller are designed for this behavior.)

13. How does your Docker setup simplify deployment or scaling?
- Single `docker-compose.yml` spins up Postgres, DynamoDB Local, Kafka, Kafka UI, Redis, and the app with health checks, resource hints, and env‑based config. `deploy.sh` generates a production compose and instructions.

14. Why deploy on Elastic Beanstalk instead of ECS/EKS?
- The project targets a simple managed option with minimal infra overhead. Elastic Beanstalk (or a similar PaaS) can run the container and scale without cluster management. ECS/EKS are viable for larger scale; out of scope here.

15. What monitoring tools did you use on AWS for this project?
- Spring Actuator endpoints (`/actuator/health`, `/actuator/metrics`) and Kafka UI. In AWS, bind these to CloudWatch logs/metrics and set alarms; the configuration is ready for that integration.

16. How did you simulate load testing to claim 10x scalability?
- Approach: JMeter/Gatling scenarios hitting mailbox create/list/read APIs with Kafka on, observing throughput via Actuator/Kafka UI. (Artifacts/scripts not committed, but stack supports this test style.)

17. What was the hardest bug or performance bottleneck you faced? How did you fix it?
- Example: Event publishing timeouts under misconfigured brokers. Fixes: reduced linger/batch, enabled idempotence, set reasonable timeouts (`max.block.ms`, `request.timeout.ms`, `delivery.timeout.ms`) in `application-prod.yml`.

18. How would you extend Burnbox to support IoT event ingestion (Thermo Fisher angle)?
- Introduce IoT gateways producing to Kafka (or bridge MQTT -> Kafka). Define new topics (e.g., `device-events`), reuse idempotent producer, and process in analytics/alerting consumers.

19. If you had to refactor Burnbox for multi-tenancy, how would you do it?
- Add tenantId to all domain records and event payloads; isolate S3 prefixes per tenant; enforce tenant scoping in repositories/services; externalize config per tenant; optionally separate Kafka topics or keys by tenantId for isolation.

20. How does Burnbox demonstrate your understanding of event-driven cloud systems for IoT pipelines?
- It shows end‑to‑end event flow: authenticated ingress, durable NoSQL storage with TTL, internal event streaming with ordered/idempotent producers, external webhooks, object storage with secure delivery, and containerized deployment with health/metrics—patterns directly applicable to IoT streams.


References
- Kafka config: `src/main/resources/application-prod.yml`, `src/main/java/com/disposablemailservice/config/KafkaConfig.java`
- Event publisher: `src/main/java/com/disposablemailservice/service/EventPublisherService.java`
- SNS webhook: `src/main/java/com/disposablemailservice/controller/SnsEventController.java`, `src/main/java/com/disposablemailservice/service/EmailEventService.java`
- DynamoDB models/repos: `src/main/java/com/disposablemailservice/model/{Mailbox,Message}.java`, `src/main/java/com/disposablemailservice/repository`
- JPA User + migrations: `src/main/java/com/disposablemailservice/model/User.java`, `src/main/resources/db/migration`
- S3 config: `src/main/java/com/disposablemailservice/config/S3Config.java`
- Docker & deploy: `docker-compose.yml`, `deploy.sh`, `DOCKER-DEPLOYMENT.md`
