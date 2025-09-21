// Service for processing incoming emails from AWS SES.
// Parses and stores emails in the Message table.
package com.disposablemailservice.service;
import com.disposablemailservice.model.Message;
import com.disposablemailservice.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.io.IOException;

@Service
@Profile("aws")
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final MessageRepository messageRepository;

    public EmailService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void handleIncomingEmail(MimeMessage mimeMessage, String mailboxId) throws IOException, jakarta.mail.MessagingException {
        log.info("📧 Processing incoming email for mailbox: {}", mailboxId);
        
        String subject = mimeMessage.getSubject();
        String sender = mimeMessage.getFrom()[0].toString();
        String body = mimeMessage.getContent().toString();
        
        log.info("📬 Email details - Subject: {}, From: {}", subject, sender);
        
        Message message = new Message();
        message.setMailboxId(mailboxId);
        message.setSubject(subject);
        message.setFrom(sender);
        message.setBody(body);
    
        messageRepository.save(message);
        log.info("✅ Email saved successfully for mailbox: {}", mailboxId);
    }
}