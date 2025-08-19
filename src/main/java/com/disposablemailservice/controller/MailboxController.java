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
}