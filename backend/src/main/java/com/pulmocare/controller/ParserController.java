package com.pulmocare.controller;

import com.pulmocare.service.ParserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/parser")
public class ParserController {

    @Autowired
    private ParserService parserService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> analyzePdf(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
        
        String result = parserService.callGeminiWithPdf(file);
        return ResponseEntity.ok(result);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
        }
        
        return new ResponseEntity<>(errorResponse, status);
    }
}

