// Service for processing incoming emails from AWS SES.
// Parses and stores emails in the Message table.
package com.disposablemailservice.service;
import com.disposablemailservice.model.Message;
import com.disposablemailservice.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.io.IOException;

@Service
public class EmailService {

    private final MessageRepository messageRepository;

    @Autowired
    public EmailService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void handleIncomingEmail(MimeMessage mimeMessage, String mailboxId) throws IOException, jakarta.mail.MessagingException {
        String subject = mimeMessage.getSubject();
        String sender = mimeMessage.getFrom()[0].toString();
        String body = mimeMessage.getContent().toString();
        
        Message message = new Message();
        message.setMailboxId(mailboxId);
        message.setSubject(subject);
        message.setFrom(sender);
        message.setBody(body);
    
        messageRepository.save(message);
    }
}