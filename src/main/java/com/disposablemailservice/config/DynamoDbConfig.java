package com.disposablemailservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;

@Configuration
public class DynamoDbConfig {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbConfig.class);

    @Configuration
    @Profile("postgres")
    public static class PostgresOnlyConfig {
        
        @PostConstruct
        public void init() {
            log.info("🐘 [CONFIG] PostgreSQL-only mode activated - DynamoDB disabled");
        }
    }

    @Configuration
    @Profile("aws")
    public static class AwsHybridConfig {
        
        @Value("${aws.access-key-id}")
        private String accessKeyId;
        
        @Value("${aws.secret-access-key}")
        private String secretAccessKey;
        
        @Value("${aws.region}")
        private String region;

        @PostConstruct
        public void validateAwsCredentials() {
            log.info("☁️ [CONFIG] AWS + PostgreSQL hybrid mode activated");
            
            if (accessKeyId == null || accessKeyId.trim().isEmpty()) {
                throw new IllegalStateException("AWS profile is active but AWS_ACCESS_KEY_ID is missing! " +
                    "Please set the AWS_ACCESS_KEY_ID environment variable.");
            }
            
            if (secretAccessKey == null || secretAccessKey.trim().isEmpty()) {
                throw new IllegalStateException("AWS profile is active but AWS_SECRET_ACCESS_KEY is missing! " +
                    "Please set the AWS_SECRET_ACCESS_KEY environment variable.");
            }
            
            log.info("✅ [CONFIG] AWS credentials validated successfully");
            log.info("🔧 [CONFIG] Region: {}", region);
            log.info("🔑 [CONFIG] Access Key: {}****", accessKeyId.substring(0, Math.min(4, accessKeyId.length())));
        }

        @Bean
        public DynamoDbClient dynamoDbClient() {
            log.info("🚀 [CONFIG] Creating DynamoDB client...");
            
            try {
                DynamoDbClient client = DynamoDbClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                    .build();
                
                log.info("✅ [CONFIG] DynamoDB client created successfully");
                return client;
                
            } catch (Exception e) {
                log.error("❌ [CONFIG] Failed to create DynamoDB client: {}", e.getMessage());
                throw new IllegalStateException("Failed to initialize DynamoDB client with provided credentials", e);
            }
        }

        @Bean
        public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
            log.info("🚀 [CONFIG] Creating DynamoDB Enhanced client...");
            
            try {
                DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(dynamoDbClient)
                    .build();
                
                log.info("✅ [CONFIG] DynamoDB Enhanced client created successfully");
                return enhancedClient;
                
            } catch (Exception e) {
                log.error("❌ [CONFIG] Failed to create DynamoDB Enhanced client: {}", e.getMessage());
                throw new IllegalStateException("Failed to initialize DynamoDB Enhanced client", e);
            }
        }
    }
}