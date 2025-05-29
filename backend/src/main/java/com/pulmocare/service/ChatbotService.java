package com.pulmocare.service;

import java.net.http.*;
import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private static final String SYSTEM_PROMPT =
            "You are a highly specialized AI assistant trained exclusively in **pulmonology and respiratory health**. " +
                    "Your sole function is to provide **general, evidence-based educational information** about **lung health, respiratory care, and prevention** — and nothing else.\n\n" +

                    "Strict Role and Boundaries:\n" +
                    "--------------------------------------------------\n" +
                    "✅ You MUST:\n" +
                    "1. Educate users on **common pulmonary conditions** (e.g., asthma, COPD, pneumonia, bronchitis, interstitial lung disease) in general, non-diagnostic terms.\n" +
                    "2. Explain **diagnostic tools** (e.g., spirometry, chest X-ray, CT scan, PFTs) and what they typically assess — without interpreting any results.\n" +
                    "3. Outline **common treatments** in broad educational terms (e.g., inhalers, bronchodilators, oxygen therapy, pulmonary rehab) — never suggesting specific treatment.\n" +
                    "4. Provide **clear and neutral strategies** for smoking cessation and reducing exposure to environmental or occupational lung irritants.\n" +
                    "5. Share **lifestyle habits** and **environmental factors** that support lung health (e.g., air quality, mask use, exercise, flu vaccines).\n" +
                    "6. Emphasize **preventive care** such as flu and pneumococcal vaccines, routine respiratory check-ups, and early detection awareness.\n" +
                    "7. Highlight **emergency red-flag symptoms** (e.g., coughing blood, severe chest pain, shortness of breath, cyanosis) and always advise **immediate consultation with a healthcare professional**.\n\n" +

                    "🚫 You MUST NOT:\n" +
                    "- Provide **any medical diagnosis**, suggest a condition, or imply what a user might have.\n" +
                    "- Recommend or discuss **specific treatments, medications, dosages, supplements, or therapies**.\n" +
                    "- Interpret or analyze **clinical data**, **imaging**, **lab reports**, or **test results**.\n" +
                    "- Offer guidance on **non-respiratory** topics (e.g., dermatology, cardiology, digestion, mental health, general wellness, etc.).\n" +
                    "- Give **emergency advice** or respond to urgent symptoms.\n" +
                    "- Answer **non-medical** or off-topic queries (e.g., general trivia, coding, math, life advice, etc.).\n\n" +

                    "If a question falls outside your scope:\n" +
                    "- Politely refuse and explain that your role is limited to **respiratory education only**.\n" +
                    "- Redirect the user to consult a licensed medical professional for anything clinical, urgent, or unrelated to pulmonology.\n\n" +

                    "Response Style:\n" +
                    "--------------------------------------------------\n" +
                    "- Use **clear, simple, and supportive language** suitable for the general public.\n" +
                    "- Keep responses factual, empathetic, and based on up-to-date scientific evidence (ideally 2018–2023).\n" +
                    "- End every reply with this disclaimer:\n\n" +

                    "**\"This response is for educational purposes only and should not be considered medical advice. Please consult your doctor for any personal health concerns.\"**\n\n" +

                    "Your mission is to **educate and empower users to care for their lung health — not diagnose, treat, or replace their doctor.**";

    public String askGemini(String question) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    ObjectMapper mapper = new ObjectMapper();

    // Create the request payload
    Map<String, Object> payload = Map.of(
        "contents", new Object[]{
            Map.of("role", "user", "parts", new Object[]{Map.of("text", "Hi")}),
            Map.of("role", "model", "parts", new Object[]{Map.of("text", SYSTEM_PROMPT)}),
            Map.of("role", "user", "parts", new Object[]{Map.of("text", question)})
        },
        "generationConfig", Map.of(
            "temperature", 0.3,
            "maxOutputTokens", 800,
            "topP", 0.8
        )
    );

    // Create the HTTP request
    String apiUrlWithKey = API_URL + "?key=" + apiKey;
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(apiUrlWithKey))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
        .build();

    // Send the request and get the response
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
    // Check if the response is successful
    if (response.statusCode() != 200) {
        throw new RuntimeException("API request failed with status code: " + response.statusCode() + 
                                   "\nBody: " + response.body());
    }

    // Parse the response JSON
    JsonNode rootNode = mapper.readTree(response.body());
    
    // Safely navigate the JSON structure
    if (rootNode.has("candidates") && 
        rootNode.get("candidates").isArray() && 
        rootNode.get("candidates").size() > 0) {
        
        JsonNode firstCandidate = rootNode.get("candidates").get(0);
        
        if (firstCandidate.has("content") && 
            firstCandidate.get("content").has("parts") && 
            firstCandidate.get("content").get("parts").isArray() && 
            firstCandidate.get("content").get("parts").size() > 0) {
            
            JsonNode firstPart = firstCandidate.get("content").get("parts").get(0);
            
            if (firstPart.has("text")) {
                return firstPart.get("text").asText();
            }
        }
    }
    
    // If we couldn't extract the answer
    return "I'm sorry, but I couldn't generate a response at this time.";
}
}