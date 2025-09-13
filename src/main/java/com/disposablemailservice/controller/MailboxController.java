// REST API controller for mailbox operations.
// Endpoints: create mailbox, fetch messages, delete mailbox.
package com.disposablemailservice.controller;

import com.disposablemailservice.model.Mailbox;
import com.disposablemailservice.model.Message;
import com.disposablemailservice.service.MailboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mailboxes")
public class MailboxController {

    private final MailboxService mailboxService;

    public MailboxController(MailboxService mailboxService) {
        this.mailboxService = mailboxService;
    }

    @PostMapping
    public ResponseEntity<Mailbox> createMailbox(@RequestParam("lifespan") String lifespan) {
        Mailbox mailbox = mailboxService.createMailbox(lifespan);
        return ResponseEntity.ok(mailbox);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String id) {
        List<Message> messages = mailboxService.getMessagesByMailboxId(id);
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMailbox(@PathVariable String id) {
        mailboxService.deleteMailbox(id);
        return ResponseEntity.noContent().build();
    }

    // 2. Mailbox Status
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getMailboxStatus(@PathVariable("id") String mailboxId) {
        return mailboxService.getMailbox(mailboxId)
            .map(mailbox -> ResponseEntity.ok(
                java.util.Map.of(
                    "active", true,
                    "expiration", mailbox.getExpiryTime(),
                    "burnAfterRead", false // TODO: add burnAfterRead field to Mailbox
                )
            ))
            .orElse(ResponseEntity.status(404).body(java.util.Map.of("error", "Mailbox not found or expired")));
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

    // 7. Health Check
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(java.util.Map.of("status", "UP"));
    }
}