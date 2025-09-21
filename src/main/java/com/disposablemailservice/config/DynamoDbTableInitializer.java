package com.disposablemailservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Component
@Profile("aws")
public class DynamoDbTableInitializer implements CommandLineRunner {

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Override
    public void run(String... args) throws Exception {
        createTableIfNotExists("mailboxes", "id");
        createTableIfNotExists("messages", "mailboxId", "id");
    }

    private void createTableIfNotExists(String tableName, String partitionKey) {
        createTableIfNotExists(tableName, partitionKey, null);
    }

    private void createTableIfNotExists(String tableName, String partitionKey, String sortKey) {
        try {
            // Check if table exists
            DescribeTableRequest describeRequest = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();
            
            try {
                dynamoDbClient.describeTable(describeRequest);
                System.out.println("Table " + tableName + " already exists");
                return;
            } catch (ResourceNotFoundException e) {
                System.out.println("Table " + tableName + " does not exist, creating...");
            }

            // Create table
            CreateTableRequest.Builder requestBuilder = CreateTableRequest.builder()
                    .tableName(tableName)
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName(partitionKey)
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName(partitionKey)
                                    .keyType(KeyType.HASH)
                                    .build()
                    );

            // Add sort key if provided
            if (sortKey != null) {
                requestBuilder.attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName(partitionKey)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(sortKey)
                                .attributeType(ScalarAttributeType.S)
                                .build()
                );
                requestBuilder.keySchema(
                        KeySchemaElement.builder()
                                .attributeName(partitionKey)
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName(sortKey)
                                .keyType(KeyType.RANGE)
                                .build()
                );
            }

            CreateTableRequest request = requestBuilder.build();
            dynamoDbClient.createTable(request);
            
            System.out.println("Table " + tableName + " creation initiated successfully");
            
            // Simple wait for table to become active
            Thread.sleep(5000); // Wait 5 seconds for table to be ready
            
            System.out.println("Table " + tableName + " should be ready");
            
        } catch (Exception e) {
            System.err.println("Error creating table " + tableName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}