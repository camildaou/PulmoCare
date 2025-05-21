package com.pulmocare.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

// @Configuration - disabled temporarily
public class EnvConfig {

    // @Bean - disabled temporarily
    public Dotenv dotenv(ConfigurableEnvironment environment) {
        // Load .env file from backend directory
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();
        
        // Add all environment variables from .env to Spring environment
        Map<String, Object> envVars = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            envVars.put(entry.getKey(), entry.getValue());
            // Don't log sensitive information
            if (entry.getKey().contains("URI") || entry.getKey().contains("KEY") || 
                entry.getKey().contains("PASSWORD") || entry.getKey().contains("SECRET")) {
                System.out.println("Loaded " + entry.getKey());
            } else {
                System.out.println("Loaded " + entry.getKey() + ": " + entry.getValue());
            }
        });
        
        // Add property source with higher precedence
        MapPropertySource propertySource = new MapPropertySource("dotenvProperties", envVars);
        environment.getPropertySources().addFirst(propertySource);
        
        return dotenv;
    }
}
