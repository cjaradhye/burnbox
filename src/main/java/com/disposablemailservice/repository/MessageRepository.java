// Repository for Message DynamoDB table.
// Handles CRUD operations for messages.
package com.disposablemailservice.repository;

import com.disposablemailservice.model.Message;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<Message> messageTable;

    @Autowired
    public MessageRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.messageTable = dynamoDbEnhancedClient.table("Message", software.amazon.awssdk.enhanced.dynamodb.TableSchema.fromBean(Message.class));
    }

    public void save(Message message) {
        messageTable.putItem(message);
    }

    public List<Message> findByMailboxId(String mailboxId) {
        // TODO: Implement actual query to DynamoDB for mailboxId
        return List.of();
    }

    public void deleteByMailboxId(String mailboxId) {
        // TODO: Implement actual delete logic for all messages with mailboxId
    }

    public void deleteById(String id) {
        messageTable.deleteItem(Key.builder().partitionValue(id).build());
    }
}