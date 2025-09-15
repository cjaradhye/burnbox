// DynamoDB model for an email message.
// Fields: id, mailboxId, subject, from, body, attachments, receivedAt.
package com.disposablemailservice.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.util.List;

@Data
@DynamoDbBean
public class Message {
    private String id;
    private String mailboxId;
    private String subject;
    private String from;
    private String body;
    private List<String> attachments;
    private Instant receivedAt;

    @DynamoDbPartitionKey
    public String getMailboxId() {
        return mailboxId;
    }

    public void setMailboxId(String mailboxId) {
        this.mailboxId = mailboxId;
    }

    @DynamoDbSortKey
    public String getMessageId() {
        return id;
    }

    public void setMessageId(String messageId) {
        this.id = messageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}