package com.pulmocare.controller;

import com.pulmocare.model.Chatbot;
import com.pulmocare.service.ChatbotService;
import com.pulmocare.util.HealthTips;
import com.pulmocare.util.RedFlagChecker;
import com.pulmocare.service.ChatbotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/ask")
    public String ask(@RequestBody Chatbot request) throws Exception {
        String q = request.getQuestion();
        if (RedFlagChecker.contains(q)) {
            return "⚠️ This may be a medical emergency. Please consult a doctor immediately.\n\n**This response is for educational purposes only.**";
        }
        return chatbotService.askGemini(q);
    }

    @GetMapping("/tip")
    public String getTip() {
        return HealthTips.getTipOfTheDay();
    }
}
