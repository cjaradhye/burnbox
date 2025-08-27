
// Service for mailbox business logic.
// Handles mailbox creation, deletion, and message retrieval.
package com.disposablemailservice.service;

import java.util.List;

import com.disposablemailservice.model.Mailbox;
import com.disposablemailservice.model.Message;
import com.disposablemailservice.repository.MailboxRepository;
import com.disposablemailservice.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class MailboxService {

    private final MailboxRepository mailboxRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public MailboxService(MailboxRepository mailboxRepository, MessageRepository messageRepository) {
        this.mailboxRepository = mailboxRepository;
        this.messageRepository = messageRepository;
    }

    public Mailbox createMailbox(String lifespan) {
        Mailbox mailbox = new Mailbox();
        mailbox.setAddress(generateUniqueAddress());
        mailbox.setCreatedAt(Instant.now());
        mailbox.setExpiryTime(calculateExpiryTime(lifespan));
        return mailboxRepository.save(mailbox);
    }

    public Optional<Mailbox> getMailbox(String id) {
        Mailbox mailbox = mailboxRepository.findById(id);
        return Optional.ofNullable(mailbox);
    }

    public List<Message> getMessagesByMailboxId(String mailboxId) {
        return messageRepository.findByMailboxId(mailboxId);
    }

    public void deleteMailbox(String id) {
        mailboxRepository.deleteById(id);
        messageRepository.deleteByMailboxId(id);
    }

    private String generateUniqueAddress() {
        // Logic to generate a unique email address
        return "unique-address@example.com";
    }

    private Instant calculateExpiryTime(String lifespan) {
        // Logic to calculate expiry time based on lifespan
        return Instant.now().plusSeconds(300); // Default to 5 minutes for now
    }
}