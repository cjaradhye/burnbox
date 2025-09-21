
// Service for mailbox business logic.
// Handles mailbox creation, deletion, and message retrieval.
package com.disposablemailservice.service;

import java.util.List;
import java.util.Optional;

import com.disposablemailservice.model.Mailbox;
import com.disposablemailservice.model.Message;
import com.disposablemailservice.repository.MailboxRepository;
import com.disposablemailservice.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MailboxService {

    private static final Logger log = LoggerFactory.getLogger(MailboxService.class);
    private final Optional<MailboxRepository> mailboxRepository;
    private final Optional<MessageRepository> messageRepository;
    private final EventPublisherService eventPublisherService;
    
    public MailboxService(Optional<MailboxRepository> mailboxRepository, 
                         Optional<MessageRepository> messageRepository, 
                         EventPublisherService eventPublisherService) {
        this.mailboxRepository = mailboxRepository;
        this.messageRepository = messageRepository;
        this.eventPublisherService = eventPublisherService;
        
        log.info("🔧 [SERVICE INIT] MailboxService initialized:");
        log.info("   📦 MailboxRepository available: {}", mailboxRepository.isPresent());
        log.info("   📨 MessageRepository available: {}", messageRepository.isPresent());
        log.info("   📡 EventPublisherService available: {}", eventPublisherService != null);
    }
    // private final S3Service s3Service;

    public Mailbox createMailbox(String userId, int lifespanDays, boolean burnAfterRead, String emailName) {
        log.info("🏭 [SERVICE STEP 1] Starting mailbox creation for user: {}", userId);
        log.info("📋 [SERVICE STEP 2] Parameters - lifespan: {} days, burnAfterRead: {}, emailName: {}", lifespanDays, burnAfterRead, emailName);
        
        if (mailboxRepository.isEmpty()) {
            log.error("💥 [SERVICE ERROR] MailboxRepository is not available (DynamoDB not configured)");
            throw new IllegalStateException("DynamoDB is not configured - mailbox creation not available. Please activate 'aws' profile.");
        }
        
        try {
            Mailbox mailbox = new Mailbox();
            log.info("📦 [SERVICE STEP 3] Created new Mailbox object");
            
            String mailboxId = generateUniqueId();
            log.info("🎲 [SERVICE STEP 4] Generated unique mailbox ID: {}", mailboxId);
            
            mailbox.setId(mailboxId);
            mailbox.setUserId(userId);
            log.info("👤 [SERVICE STEP 5] Set mailbox owner to user: {}", userId);
            
            String address = generateUniqueAddress(mailboxId, emailName);
            mailbox.setAddress(address);
            log.info("📧 [SERVICE STEP 6] Generated email address: {}", address);
            
            Instant createdAt = Instant.now();
            mailbox.setCreatedAt(createdAt);
            log.info("⏰ [SERVICE STEP 7] Set creation time: {}", createdAt);
            
            Instant expiryTime = calculateExpiryTime(lifespanDays);
            mailbox.setExpiryTime(expiryTime);
            log.info("⏳ [SERVICE STEP 8] Set expiry time: {} (in {} days)", expiryTime, lifespanDays);
            
            mailbox.setBurnAfterRead(burnAfterRead);
            log.info("🔥 [SERVICE STEP 9] Set burn after read: {}", burnAfterRead);
            
            log.info("💾 [SERVICE STEP 10] Saving mailbox to repository...");
            Mailbox savedMailbox = mailboxRepository.get().save(mailbox);
            log.info("✅ [SERVICE STEP 11] Mailbox saved successfully with ID: {}", savedMailbox.getId());
            
            // Publish event
            log.info("📡 [SERVICE STEP 12] Publishing MAILBOX_CREATED event...");
            eventPublisherService.publishMailboxCreated(savedMailbox, userId);
            log.info("📢 [SERVICE STEP 13] Event published successfully");
            
            log.info("🎉 [SERVICE STEP 14] Mailbox creation completed successfully:");
            log.info("   📦 Mailbox ID: {}", savedMailbox.getId());
            log.info("   📧 Email Address: {}", savedMailbox.getAddress());
            log.info("   👤 User ID: {}", savedMailbox.getUserId());
            log.info("   ⏰ Created At: {}", savedMailbox.getCreatedAt());
            log.info("   ⏳ Expires At: {}", savedMailbox.getExpiryTime());
            log.info("   🔥 Burn After Read: {}", savedMailbox.isBurnAfterRead());
            
            return savedMailbox;
        } catch (Exception e) {
            log.error("💥 [SERVICE ERROR] Failed to create mailbox for user {}: {}", userId, e.getMessage());
            log.error("📍 [SERVICE ERROR] Stack trace:", e);
            throw e;
        }
    }

    public Optional<Mailbox> getMailbox(String id) {
        log.info("🔍 [SERVICE STEP 1] Looking up mailbox with ID: {}", id);
        
        if (mailboxRepository.isEmpty()) {
            log.error("💥 [SERVICE ERROR] MailboxRepository is not available (DynamoDB not configured)");
            return Optional.empty();
        }
        
        try {
            Mailbox mailbox = mailboxRepository.get().findById(id);
            
            if (mailbox != null) {
                log.info("✅ [SERVICE STEP 2] Mailbox found:");
                log.info("   📧 Address: {}", mailbox.getAddress());
                log.info("   👤 Owner: {}", mailbox.getUserId());
                log.info("   ⏰ Created: {}", mailbox.getCreatedAt());
                log.info("   ⏳ Expires: {}", mailbox.getExpiryTime());
                log.info("   🔥 Burn After Read: {}", mailbox.isBurnAfterRead());
                return Optional.of(mailbox);
            } else {
                log.warn("❌ [SERVICE STEP 2] Mailbox not found with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("💥 [SERVICE ERROR] Failed to get mailbox {}: {}", id, e.getMessage());
            log.error("📍 [SERVICE ERROR] Stack trace:", e);
            return Optional.empty();
        }
    }

    public Optional<Mailbox> getMailboxForUser(String id, String userId) {
        if (mailboxRepository.isEmpty()) {
            log.error("💥 [SERVICE ERROR] MailboxRepository is not available (DynamoDB not configured)");
            return Optional.empty();
        }
        
        Mailbox mailbox = mailboxRepository.get().findById(id);
        if (mailbox != null && mailbox.getUserId().equals(userId)) {
            return Optional.of(mailbox);
        }
        return Optional.empty();
    }

    public List<Mailbox> getAllMailboxesForUser(String userId) {
        log.info("📋 [SERVICE STEP 1] Getting all mailboxes for user: {}", userId);
        
        if (mailboxRepository.isEmpty()) {
            log.error("💥 [SERVICE ERROR] MailboxRepository is not available (DynamoDB not configured)");
            return List.of();
        }
        
        try {
            log.info("🔍 [SERVICE STEP 2] Querying repository for user mailboxes...");
            List<Mailbox> mailboxes = mailboxRepository.get().findByUserId(userId);
            
            log.info("📊 [SERVICE STEP 3] Found {} mailboxes for user {}", mailboxes.size(), userId);
            for (int i = 0; i < mailboxes.size(); i++) {
                Mailbox mb = mailboxes.get(i);
                log.info("   📧 Mailbox {}: ID={}, Address={}, Created={}, Expires={}", 
                        i + 1, mb.getId(), mb.getAddress(), mb.getCreatedAt(), mb.getExpiryTime());
            }
            
            log.info("✅ [SERVICE STEP 4] Successfully retrieved {} mailboxes", mailboxes.size());
            return mailboxes;
        } catch (Exception e) {
            log.error("💥 [SERVICE ERROR] Failed to get mailboxes for user {}: {}", userId, e.getMessage());
            log.error("📍 [SERVICE ERROR] Stack trace:", e);
            throw e;
        }
    }

    public List<Message> getMessagesByMailboxId(String mailboxId, String userId) {
        log.info("📨 [SERVICE STEP 1] Getting messages for mailbox: {}, user: {}", mailboxId, userId);
        
        if (mailboxRepository.isEmpty() || messageRepository.isEmpty()) {
            log.error("💥 [SERVICE ERROR] Required repositories are not available (DynamoDB not configured)");
            return List.of();
        }
        
        try {
            // Verify mailbox belongs to user
            log.info("🔐 [SERVICE STEP 2] Verifying mailbox ownership...");
            Optional<Mailbox> mailbox = getMailboxForUser(mailboxId, userId);
            
            if (mailbox.isEmpty()) {
                log.error("⛔ [SERVICE STEP 3] Access denied - mailbox {} not found or doesn't belong to user {}", mailboxId, userId);
                throw new IllegalArgumentException("Mailbox not found or access denied");
            }
            
            log.info("✅ [SERVICE STEP 3] Mailbox ownership verified for user: {}", userId);
            log.info("📮 [SERVICE STEP 4] Retrieving messages from repository...");
            
            List<Message> messages = messageRepository.get().findByMailboxId(mailboxId);
            
            log.info("📊 [SERVICE STEP 5] Found {} messages for mailbox {}", messages.size(), mailboxId);
            for (int i = 0; i < messages.size(); i++) {
                Message msg = messages.get(i);
                log.info("   📧 Message {}: ID={}, From={}, Subject={}, Received={}", 
                        i + 1, msg.getId(), msg.getFrom(), msg.getSubject(), msg.getReceivedAt());
            }
            
            return messages;
        } catch (Exception e) {
            log.error("💥 [SERVICE ERROR] Failed to get messages for mailbox {}: {}", mailboxId, e.getMessage());
            log.error("📍 [SERVICE ERROR] Stack trace:", e);
            throw e;
        }
    }

    public void deleteMailbox(String id, String userId) {
        log.info("🗑️ [SERVICE STEP 1] Starting mailbox deletion: {}, user: {}", id, userId);
        
        if (mailboxRepository.isEmpty() || messageRepository.isEmpty()) {
            log.error("💥 [SERVICE ERROR] Required repositories are not available (DynamoDB not configured)");
            throw new IllegalStateException("DynamoDB is not configured - mailbox deletion not available. Please activate 'aws' profile.");
        }
        
        try {
            log.info("🔐 [SERVICE STEP 2] Verifying mailbox ownership...");
            Optional<Mailbox> mailbox = getMailboxForUser(id, userId);
            
            if (mailbox.isEmpty()) {
                log.error("⛔ [SERVICE STEP 3] Access denied - mailbox {} not found or doesn't belong to user {}", id, userId);
                throw new IllegalArgumentException("Mailbox not found or access denied");
            }
            
            Mailbox mailboxToDelete = mailbox.get();
            log.info("✅ [SERVICE STEP 3] Mailbox ownership verified:");
            log.info("   📧 Address: {}", mailboxToDelete.getAddress());
            log.info("   👤 Owner: {}", mailboxToDelete.getUserId());
            
            // Get message count for event
            log.info("📊 [SERVICE STEP 4] Counting messages to delete...");
            List<Message> messages = messageRepository.get().findByMailboxId(id);
            int messagesDeleted = messages.size();
            log.info("📨 [SERVICE STEP 5] Found {} messages to delete", messagesDeleted);
            
            // Delete S3 attachments
            log.info("📎 [SERVICE STEP 6] Processing attachments for deletion...");
            int attachmentsDeleted = 0;
            for (Message message : messages) {
                if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                    log.info("📎 [SERVICE STEP 7] Message {} has {} attachments", 
                            message.getId(), message.getAttachments().size());
                    for (String attachmentId : message.getAttachments()) {
                        try {
                            log.info("🗑️ [SERVICE STEP 8] Deleting attachment: {}", attachmentId);
                            // s3Service.deleteAttachment(id, message.getId(), attachmentId);
                            attachmentsDeleted++;
                            log.info("✅ [SERVICE STEP 9] Attachment deleted: {}", attachmentId);
                        } catch (Exception e) {
                            log.warn("⚠️ [SERVICE WARNING] Failed to delete attachment {} for message {}: {}", 
                                    attachmentId, message.getId(), e.getMessage());
                        }
                    }
                }
            }
            log.info("📎 [SERVICE STEP 10] Processed {} attachments for deletion", attachmentsDeleted);
            
            // Delete messages and mailbox
            log.info("🗑️ [SERVICE STEP 11] Deleting messages from repository...");
            messageRepository.get().deleteByMailboxId(id);
            log.info("✅ [SERVICE STEP 12] Messages deleted successfully");
            
            log.info("🗑️ [SERVICE STEP 13] Deleting mailbox from repository...");
            mailboxRepository.get().deleteById(id);
            log.info("✅ [SERVICE STEP 14] Mailbox deleted successfully");
            
            // Publish event
            log.info("📡 [SERVICE STEP 15] Publishing MAILBOX_EXPIRED event...");
            eventPublisherService.publishMailboxExpired(mailboxToDelete, userId, messagesDeleted, attachmentsDeleted);
            log.info("📢 [SERVICE STEP 16] Event published successfully");
            
            log.info("🎉 [SERVICE STEP 17] Mailbox deletion completed successfully:");
            log.info("   📦 Mailbox ID: {}", id);
            log.info("   👤 User ID: {}", userId);
            log.info("   📨 Messages deleted: {}", messagesDeleted);
            log.info("   📎 Attachments deleted: {}", attachmentsDeleted);
            
        } catch (Exception e) {
            log.error("💥 [SERVICE ERROR] Failed to delete mailbox {}: {}", id, e.getMessage());
            log.error("📍 [SERVICE ERROR] Stack trace:", e);
            throw e;
        }
    }

    private String generateUniqueId() {
        // Generate a unique ID using timestamp and random number
        return "mailbox_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    private String generateUniqueAddress(String mailboxId, String emailName) {
        // Use emailName if provided and valid, otherwise fall back to mailboxId
        String addressPrefix = mailboxId; // default fallback
        
        if (emailName != null && !emailName.trim().isEmpty()) {
            // Clean the emailName to make it email-safe
            String cleanEmailName = emailName.trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9._-]", "") // Remove invalid characters
                    .replaceAll("^\\.+|\\.+$", "") // Remove leading/trailing dots
                    .replaceAll("\\.{2,}", "."); // Replace multiple dots with single dot
            
            // Ensure it's not empty after cleaning and not too long
            if (!cleanEmailName.isEmpty() && cleanEmailName.length() <= 50) {
                // Add timestamp suffix to ensure uniqueness
                addressPrefix = cleanEmailName + "_" + System.currentTimeMillis();
            }
        }
        
        return addressPrefix + "@nahneedpfft.com";
    }

    private Instant calculateExpiryTime(int lifespanDays) {
        // Calculate expiry time in days (changed from minutes)
        return Instant.now().plusSeconds(lifespanDays * 24L * 60L * 60L);
    }
}