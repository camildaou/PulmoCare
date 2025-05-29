package com.pulmocare.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class EnvConfig {
    
    @Bean
    public Dotenv dotenv(ConfigurableEnvironment environment) {
        // Create a special Dotenv that only loads from the .env file
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();
        
        // Add only the variables from .env file to Spring environment (not system env vars)
        Map<String, Object> envVars = new HashMap<>();
        
        // Read the .env file directly to get only variables defined there
        File envFile = new File("./.env");
        if (!envFile.exists()) {
            envFile = new File("../.env");
        }
        if (!envFile.exists()) {
            envFile = new File("../../.env");
        }
        
        if (envFile.exists()) {
            System.out.println("Loading variables from .env file at: " + envFile.getAbsolutePath());
            
            // The dotenv library mixes system env vars with .env vars, so we need to read the file directly
            try {
                java.nio.file.Files.lines(envFile.toPath()).forEach(line -> {
                    // Skip comments and empty lines
                    if (line.trim().isEmpty() || line.trim().startsWith("#") || line.trim().startsWith("//")) {
                        return;
                    }
                    
                    // Parse the line to get key=value
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();
                        
                        // Add to our map
                        envVars.put(key, value);
                        
                        // Don't log sensitive information
                        if (key.contains("URI") || key.contains("KEY") || 
                            key.contains("PASSWORD") || key.contains("SECRET")) {
                            System.out.println("Loaded " + key);
                        } else {
                            System.out.println("Loaded " + key + ": " + value);
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error reading .env file: " + e.getMessage());
            }
        } else {
            System.out.println("No .env file found at any of the checked locations");
        }
        
        // Add property source with higher precedence
        MapPropertySource propertySource = new MapPropertySource("dotenvProperties", envVars);
        environment.getPropertySources().addFirst(propertySource);
        
        return dotenv;
    }
}
