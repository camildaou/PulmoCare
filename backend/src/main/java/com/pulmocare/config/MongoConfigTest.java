package com.pulmocare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfigTest {

    @Bean
    CommandLineRunner mongoConnectionDebug(@Value("${spring.data.mongodb.uri}") String mongoUri,
                                          @Value("${spring.data.mongodb.database}") String mongoDatabase) {
        return args -> {
            System.out.println("\n=============================================");
            System.out.println("MongoDB Connection Test");
            
            // Check if MongoDB URI is properly configured
            boolean isMongoUriValid = mongoUri != null && 
                (mongoUri.startsWith("mongodb://") || mongoUri.startsWith("mongodb+srv://"));
            
            System.out.println("MongoDB URI properly configured: " + (isMongoUriValid ? "Yes" : "No"));
            System.out.println("MongoDB Database: " + mongoDatabase);
            System.out.println("=============================================\n");
        };
    }
}
