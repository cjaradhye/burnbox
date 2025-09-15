package com.disposablemailservice.service;

import com.disposablemailservice.model.Mailbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class EventPublisherService {
    
    private static final Logger log = LoggerFactory.getLogger(EventPublisherService.class);
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;
    
    private static final String MAILBOX_EVENTS_TOPIC = "mailbox-events";
    private static final String MESSAGE_EVENTS_TOPIC = "message-events";
    
    public void publishMailboxCreated(Mailbox mailbox, String userId) {
        log.info("Publishing MAILBOX_CREATED event for mailbox {} and user {} - Kafka enabled: {}", 
                mailbox.getMailboxId(), userId, kafkaEnabled);
        
        if (!kafkaEnabled) {
            log.info("Kafka is disabled, skipping event publishing for mailbox {}", mailbox.getMailboxId());
            return;
        }
        
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate is not available, skipping event publishing for mailbox {}", mailbox.getMailboxId());
            return;
        }
        
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("mailboxId", mailbox.getMailboxId());
            event.put("userId", userId);
            event.put("address", mailbox.getAddress());
            event.put("expiryTime", mailbox.getExpiryTime());
            event.put("burnAfterRead", mailbox.isBurnAfterRead());
            event.put("createdAt", mailbox.getCreatedAt());
            event.put("eventType", "MAILBOX_CREATED");
            
            kafkaTemplate.send(MAILBOX_EVENTS_TOPIC, mailbox.getMailboxId(), event);
            log.info("Successfully published MAILBOX_CREATED event for mailbox {} and user {}", mailbox.getMailboxId(), userId);
            
        } catch (Exception e) {
            log.error("Failed to publish MAILBOX_CREATED event for mailbox {}: {}", mailbox.getMailboxId(), e.getMessage());
        }
    }
    
    public void publishMessageReceived(String messageId, String mailboxId, String userId, String from, String subject, boolean hasAttachments) {
        log.info("Publishing MESSAGE_RECEIVED event for message {} in mailbox {} - Kafka enabled: {}", 
                messageId, mailboxId, kafkaEnabled);
        
        if (!kafkaEnabled) {
            log.info("Kafka is disabled, skipping event publishing for message {}", messageId);
            return;
        }
        
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate is not available, skipping event publishing for message {}", messageId);
            return;
        }
        
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("messageId", messageId);
            event.put("mailboxId", mailboxId);
            event.put("userId", userId);
            event.put("from", from);
            event.put("subject", subject);
            event.put("receivedAt", Instant.now());
            event.put("hasAttachments", hasAttachments);
            event.put("eventType", "MESSAGE_RECEIVED");
            
            kafkaTemplate.send(MESSAGE_EVENTS_TOPIC, messageId, event);
            log.info("Successfully published MESSAGE_RECEIVED event for message {} in mailbox {}", messageId, mailboxId);
            
        } catch (Exception e) {
            log.error("Failed to publish MESSAGE_RECEIVED event for message {}: {}", messageId, e.getMessage());
        }
    }
    
    public void publishMailboxExpired(Mailbox mailbox, String userId, int messagesDeleted, int attachmentsDeleted) {
        log.info("Publishing MAILBOX_EXPIRED event for mailbox {} - Kafka enabled: {}", 
                mailbox.getMailboxId(), kafkaEnabled);
        
        if (!kafkaEnabled) {
            log.info("Kafka is disabled, skipping event publishing for expired mailbox {}", mailbox.getMailboxId());
            return;
        }
        
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate is not available, skipping event publishing for expired mailbox {}", mailbox.getMailboxId());
            return;
        }
        
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("mailboxId", mailbox.getMailboxId());
            event.put("userId", userId);
            event.put("address", mailbox.getAddress());
            event.put("expiryTime", mailbox.getExpiryTime());
            event.put("deletedAt", Instant.now());
            event.put("messagesDeleted", messagesDeleted);
            event.put("attachmentsDeleted", attachmentsDeleted);
            event.put("eventType", "MAILBOX_EXPIRED");
            
            kafkaTemplate.send(MAILBOX_EVENTS_TOPIC, mailbox.getMailboxId(), event);
            log.info("Successfully published MAILBOX_EXPIRED event for mailbox {} with {} messages and {} attachments deleted", 
                    mailbox.getMailboxId(), messagesDeleted, attachmentsDeleted);
            
        } catch (Exception e) {
            log.error("Failed to publish MAILBOX_EXPIRED event for mailbox {}: {}", mailbox.getMailboxId(), e.getMessage());
        }
    }
}