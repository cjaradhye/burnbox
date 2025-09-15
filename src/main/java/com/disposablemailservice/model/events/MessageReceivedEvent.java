package com.disposablemailservice.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReceivedEvent {
    
    @JsonProperty("messageId")
    private String messageId;
    
    @JsonProperty("mailboxId")
    private String mailboxId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("from")
    private String from;
    
    @JsonProperty("subject")
    private String subject;
    
    @JsonProperty("receivedAt")
    private Instant receivedAt;
    
    @JsonProperty("hasAttachments")
    private boolean hasAttachments;
    
    @JsonProperty("eventType")
    private String eventType = "MESSAGE_RECEIVED";
}
