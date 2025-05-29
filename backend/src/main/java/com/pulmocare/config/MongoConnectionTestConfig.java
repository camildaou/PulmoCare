package com.pulmocare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MongoConnectionTestConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    
    @Value("${spring.data.mongodb.database}")
    private String mongoDatabase;
    
    @Bean
    public CommandLineRunner mongoConnectionTest(Environment env) {
        return args -> {
            System.out.println("\n=============================================");
            System.out.println("MongoDB Connection Test");
            System.out.println("MongoDB URI properly configured: " + 
                    (mongoUri != null && !mongoUri.equals("mongodb+srv://username:password@host/") ? "Yes" : "No"));
            System.out.println("MongoDB Database: " + mongoDatabase);
            System.out.println("=============================================\n");
        };
    }
}
