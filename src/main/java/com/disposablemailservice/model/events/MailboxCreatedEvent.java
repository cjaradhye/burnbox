package com.disposablemailservice.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailboxCreatedEvent {
    
    @JsonProperty("mailboxId")
    private String mailboxId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("expiryTime")
    private Instant expiryTime;
    
    @JsonProperty("burnAfterRead")
    private boolean burnAfterRead;
    
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @JsonProperty("eventType")
    private String eventType = "MAILBOX_CREATED";
}
