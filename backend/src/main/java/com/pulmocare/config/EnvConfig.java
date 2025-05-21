package com.pulmocare.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EnvConfig {

    @Bean
    public Dotenv dotenv(ConfigurableEnvironment environment) {
        // Load .env file from project root
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();
        
        // Add all environment variables from .env to Spring environment
        Map<String, Object> envVars = new HashMap<>();
        dotenv.entries().forEach(entry -> envVars.put(entry.getKey(), entry.getValue()));
        
        // Add property source with higher precedence
        MapPropertySource propertySource = new MapPropertySource("dotenvProperties", envVars);
        environment.getPropertySources().addFirst(propertySource);
        
        return dotenv;
    }
}
