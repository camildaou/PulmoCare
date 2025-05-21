package com.pulmocare.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vertexai.VertexAI;
//import com.google.cloud.vertexai.api.Generation;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ContentMaker;
//import com.google.cloud.vertexai.generativeai.GenerationConfig;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@Service
public class GeminiService {

    private final String project = "avid-catalyst-459709-i5";
    private final String location = "us-central1";
    private final String model = "gemini-1.5-pro-preview-0409";

    public String generateResponse(String prompt) throws Exception {
        InputStream keyStream = getClass().getClassLoader().getResourceAsStream("vertex-key.json");

        // Create credentials from the service account key
        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(keyStream);

        // Create VertexAI client with the updated constructor
        try (VertexAI vertexAI = new VertexAI.Builder()
                .setProjectId(project)
                .setLocation(location)
                .setCredentials(credentials)
                .build()) {            // Create and configure the generative model
            GenerativeModel genModel = new GenerativeModel(model, vertexAI);

            // Generate content using the correct method for the current version of the library
            return genModel.generateContent(ContentMaker.fromString(prompt))
                    .getCandidates(0).getContent().getParts(0).getText();
        }
    }
}