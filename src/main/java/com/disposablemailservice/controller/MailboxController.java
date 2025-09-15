package com.disposablemailservice.controller;

import com.disposablemailservice.model.Mailbox;
import com.disposablemailservice.model.Message;
import com.disposablemailservice.model.User;
import com.disposablemailservice.service.MailboxService;
import com.disposablemailservice.model.MailboxRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Collections;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/mailboxes")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class MailboxController {

    private static final Logger log = LoggerFactory.getLogger(MailboxController.class);
    private final MailboxService mailboxService;
    
    public MailboxController(MailboxService mailboxService) {
        this.mailboxService = mailboxService;
    }

    @PostMapping("/create")
    public ResponseEntity<Mailbox> createMailbox(@RequestBody MailboxRequest request, @AuthenticationPrincipal User user, HttpServletRequest httpRequest) {
        log.info("🚀 [STEP 1] Received POST /api/mailboxes/create");
        log.info("📋 [STEP 2] Request body: {}", request);
        log.info("🔑 [STEP 3] Authorization header: {}", httpRequest.getHeader("Authorization"));
        log.info("📊 [STEP 4] All headers: {}", Collections.list(httpRequest.getHeaderNames()));
        log.info("👤 [STEP 5] Authenticated user: {}", (user != null ? user.getUserId() : "null"));
        log.info("🌐 [STEP 6] Remote address: {}", httpRequest.getRemoteAddr());
        log.info("🔧 [STEP 7] Request method: {}", httpRequest.getMethod());
        log.info("🎯 [STEP 8] Request URI: {}", httpRequest.getRequestURI());
        
        try {
            log.info("✅ [STEP 9] Starting request processing...");
            
            if (user == null) {
                log.warn("❌ [STEP 10] Authentication failed - user is null, returning 401");
                return ResponseEntity.status(401).build();
            }
            
            log.info("👍 [STEP 11] User authenticated successfully: {}", user.getUserId());
            log.info("📝 [STEP 12] Calling mailboxService.createMailbox with lifespan={} days, burnAfterRead={}, emailName={}", 
                    request.getLifespan(), request.isBurnAfterRead(), request.getEmailName());
            
            Mailbox mailbox = mailboxService.createMailbox(user.getUserId(), request.getLifespan(), request.isBurnAfterRead(), request.getEmailName());
            
            log.info("🎉 [STEP 13] Mailbox created successfully:");
            log.info("   📧 Mailbox ID: {}", mailbox.getId());
            log.info("   📮 Email address: {}", mailbox.getAddress());
            log.info("   ⏰ Expiry time: {}", mailbox.getExpiryTime());
            log.info("   🔥 Burn after read: {}", mailbox.isBurnAfterRead());
            log.info("✅ [STEP 14] Returning successful response");
            
            return ResponseEntity.ok(mailbox);
        } catch (Exception e) {
            log.error("💥 [STEP ERROR] Exception occurred: {}", e.getMessage());
            log.error("📍 [STEP ERROR] Stack trace:", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String id, @AuthenticationPrincipal User user) {
        log.info("📥 [STEP 1] Received GET /api/mailboxes/{}/messages", id);
        log.info("👤 [STEP 2] Authenticated user: {}", (user != null ? user.getUserId() : "null"));
        
        try {
            if (user == null) {
                log.warn("❌ [STEP 3] Authentication failed - user is null, returning 401");
                return ResponseEntity.status(401).build();
            }
            
            log.info("✅ [STEP 4] User authenticated successfully: {}", user.getUserId());
            log.info("🔍 [STEP 5] Looking up mailbox with ID: {}", id);
            
            Optional<Mailbox> mailbox = mailboxService.getMailbox(id);
            
            if (mailbox.isPresent()) {
                Mailbox mb = mailbox.get();
                log.info("📬 [STEP 6] Mailbox found:");
                log.info("   📧 Address: {}", mb.getAddress());
                log.info("   👤 Owner: {}", mb.getUserId());
                log.info("   ⏰ Expiry: {}", mb.getExpiryTime());
                
                // Verify the mailbox belongs to the authenticated user
                if (!mb.getUserId().equals(user.getUserId())) {
                    log.warn("⛔ [STEP 7] Access denied - mailbox {} belongs to user {}, but request from user {}", 
                            id, mb.getUserId(), user.getUserId());
                    return ResponseEntity.status(403).build();
                }
                
                log.info("✅ [STEP 8] Access granted - user owns this mailbox");
                log.info("📨 [STEP 9] Retrieving messages for mailbox {}", id);
                
                List<Message> messages = mailboxService.getMessagesByMailboxId(id, mb.getUserId());
                
                log.info("📊 [STEP 10] Found {} messages in mailbox {}", messages.size(), id);
                for (int i = 0; i < messages.size(); i++) {
                    Message msg = messages.get(i);
                    log.info("   📧 Message {}: ID={}, From={}, Subject={}", 
                            i + 1, msg.getId(), msg.getFrom(), msg.getSubject());
                }
                
                log.info("✅ [STEP 11] Returning {} messages", messages.size());
                return ResponseEntity.ok(messages);
            } else {
                log.warn("📭 [STEP 6] Mailbox not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("💥 [STEP ERROR] Exception in getMessages: {}", e.getMessage());
            log.error("📍 [STEP ERROR] Stack trace:", e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMailbox(@PathVariable String id, @AuthenticationPrincipal User user) {
        log.info("🗑️ [STEP 1] Received DELETE /api/mailboxes/{}", id);
        log.info("👤 [STEP 2] Authenticated user: {}", (user != null ? user.getUserId() : "null"));
        
        try {
            if (user == null) {
                log.warn("❌ [STEP 3] Authentication failed - user is null, returning 401");
                return ResponseEntity.status(401).build();
            }
            
            log.info("✅ [STEP 4] User authenticated successfully: {}", user.getUserId());
            log.info("🔍 [STEP 5] Looking up mailbox with ID: {}", id);
            
            Optional<Mailbox> mailbox = mailboxService.getMailbox(id);
            
            if (mailbox.isPresent()) {
                Mailbox mb = mailbox.get();
                log.info("📬 [STEP 6] Mailbox found:");
                log.info("   📧 Address: {}", mb.getAddress());
                log.info("   👤 Owner: {}", mb.getUserId());
                
                // Verify the mailbox belongs to the authenticated user
                if (!mb.getUserId().equals(user.getUserId())) {
                    log.warn("⛔ [STEP 7] Access denied - mailbox {} belongs to user {}, but delete request from user {}", 
                            id, mb.getUserId(), user.getUserId());
                    return ResponseEntity.status(403).build();
                }
                
                log.info("✅ [STEP 8] Access granted - user owns this mailbox");
                log.info("🗑️ [STEP 9] Calling mailboxService.deleteMailbox for mailbox {}", id);
                
                mailboxService.deleteMailbox(id, mb.getUserId());
                
                log.info("🎉 [STEP 10] Mailbox {} deleted successfully", id);
                log.info("✅ [STEP 11] Returning 204 No Content");
                return ResponseEntity.noContent().build();
            } else {
                log.warn("📭 [STEP 6] Mailbox not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("💥 [STEP ERROR] Exception in deleteMailbox: {}", e.getMessage());
            log.error("📍 [STEP ERROR] Stack trace:", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getMailboxStatus(@PathVariable("id") String mailboxId, @AuthenticationPrincipal User user) {
        log.info("📊 [STEP 1] Received GET /api/mailboxes/{}/status", mailboxId);
        log.info("👤 [STEP 2] Authenticated user: {}", (user != null ? user.getUserId() : "null"));
        
        try {
            if (user == null) {
                log.warn("❌ [STEP 3] Authentication failed - user is null, returning 401");
                return ResponseEntity.status(401).build();
            }
            
            log.info("✅ [STEP 4] User authenticated successfully: {}", user.getUserId());
            log.info("🔍 [STEP 5] Looking up mailbox with ID: {}", mailboxId);
            
            Optional<Mailbox> mailbox = mailboxService.getMailbox(mailboxId);
            
            if (mailbox.isPresent()) {
                Mailbox mb = mailbox.get();
                log.info("📬 [STEP 6] Mailbox found:");
                log.info("   📧 Address: {}", mb.getAddress());
                log.info("   👤 Owner: {}", mb.getUserId());
                log.info("   ⏰ Expiry: {}", mb.getExpiryTime());
                
                // Verify the mailbox belongs to the authenticated user
                if (!mb.getUserId().equals(user.getUserId())) {
                    log.warn("⛔ [STEP 7] Access denied - mailbox {} belongs to user {}, but status request from user {}", 
                            mailboxId, mb.getUserId(), user.getUserId());
                    return ResponseEntity.status(403).build();
                }
                
                log.info("✅ [STEP 8] Access granted - user owns this mailbox");
                log.info("📊 [STEP 9] Counting messages for mailbox {}", mailboxId);
                
                List<Message> messages = mailboxService.getMessagesByMailboxId(mailboxId, mb.getUserId());
                int messageCount = messages.size();
                
                log.info("📈 [STEP 10] Found {} messages in mailbox", messageCount);
                
                Map<String, Object> status = Map.of(
                    "id", mb.getId(),
                    "address", mb.getAddress(),
                    "expiryTime", mb.getExpiryTime(),
                    "messageCount", messageCount
                );
                
                log.info("📊 [STEP 11] Returning status: {}", status);
                return ResponseEntity.ok(status);
            } else {
                log.warn("📭 [STEP 6] Mailbox not found with ID: {}", mailboxId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("💥 [STEP ERROR] Exception in getMailboxStatus: {}", e.getMessage());
            log.error("📍 [STEP ERROR] Stack trace:", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.info("❤️ [STEP 1] Received GET /api/mailboxes/health");
        log.info("✅ [STEP 2] Returning health status: UP");
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping
    public ResponseEntity<List<Mailbox>> getAllMailboxes(@AuthenticationPrincipal User user) {
        log.info("📋 [STEP 1] Received GET /api/mailboxes");
        log.info("👤 [STEP 2] Authenticated user: {}", (user != null ? user.getUserId() : "null"));
        
        try {
            if (user == null) {
                log.warn("❌ [STEP 3] Authentication failed - user is null, returning 401");
                return ResponseEntity.status(401).build();
            }
            
            log.info("✅ [STEP 4] User authenticated successfully: {}", user.getUserId());
            log.info("📋 [STEP 5] Retrieving all mailboxes for user: {}", user.getUserId());
            
            List<Mailbox> mailboxes = mailboxService.getAllMailboxesForUser(user.getUserId());
            
            log.info("📊 [STEP 6] Found {} mailboxes for user {}", mailboxes.size(), user.getUserId());
            for (int i = 0; i < mailboxes.size(); i++) {
                Mailbox mb = mailboxes.get(i);
                log.info("   📧 Mailbox {}: ID={}, Address={}, Expires={}", 
                        i + 1, mb.getId(), mb.getAddress(), mb.getExpiryTime());
            }
            
            log.info("✅ [STEP 7] Returning {} mailboxes for user", mailboxes.size());
            return ResponseEntity.ok(mailboxes);
        } catch (Exception e) {
            log.error("💥 [STEP ERROR] Exception in getAllMailboxes: {}", e.getMessage());
            log.error("📍 [STEP ERROR] Stack trace:", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 5. Read Single Message
    @GetMapping("/{mailboxId}/messages/{messageId}")
    public ResponseEntity<?> readSingleMessage(@PathVariable String mailboxId, @PathVariable String messageId) {
        // TODO: fetch message, check burnAfterRead, delete mailbox if needed
        return ResponseEntity.status(501).body(java.util.Map.of("error", "Not implemented"));
    }

    // 6. Download Attachment
    @GetMapping("/{mailboxId}/messages/{messageId}/attachment/{attachmentId}")
    public ResponseEntity<?> downloadAttachment(@PathVariable String mailboxId, @PathVariable String messageId, @PathVariable String attachmentId) {
        // TODO: generate pre-signed S3 URL for attachment
        return ResponseEntity.status(501).body(java.util.Map.of("error", "Not implemented"));
    }
}