// Repository for Mailbox DynamoDB table.
// Handles CRUD operations for mailboxes.
package com.disposablemailservice.repository;

import com.disposablemailservice.model.Mailbox;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import org.springframework.stereotype.Repository;

@Repository
public class MailboxRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Mailbox> mailboxTable;

    public MailboxRepository(DynamoDbClient dynamoDbClient) {
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.mailboxTable = enhancedClient.table("mailboxes", TableSchema.fromBean(Mailbox.class));
    }

    public Mailbox save(Mailbox mailbox) {
        mailboxTable.putItem(mailbox);
        return mailbox;
    }

    public Mailbox findById(String id) {
        return mailboxTable.getItem(Key.builder().partitionValue(id).build());
    }

    public void deleteById(String id) {
        mailboxTable.deleteItem(Key.builder().partitionValue(id).build());
    }
}