package com.pulmocare.controller;

import com.pulmocare.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/generate")
    public String generate(@RequestBody String prompt) throws Exception {
        return geminiService.generateResponse(prompt);
    }
}