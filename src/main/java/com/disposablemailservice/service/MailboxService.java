
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

    public Mailbox createMailbox(int lifespan, boolean burnAfterRead) {
        Mailbox mailbox = new Mailbox();
        String mailboxId = generateUniqueId();
        mailbox.setId(mailboxId);
        mailbox.setAddress(generateUniqueAddress(mailboxId));
        mailbox.setCreatedAt(Instant.now());
        mailbox.setExpiryTime(calculateExpiryTime(lifespan));
        mailbox.setBurnAfterRead(burnAfterRead);
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

    private String generateUniqueId() {
        // Generate a unique ID using timestamp and random number
        return "mailbox_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    private String generateUniqueAddress(String mailboxId) {
        // Use mailboxId and SES domain to generate unique email address
        return mailboxId + "@nahneedpfft.com";
    }

    private Instant calculateExpiryTime(int lifespan) {
        // Calculate expiry time in minutes
        return Instant.now().plusSeconds(lifespan * 60L);
    }
}