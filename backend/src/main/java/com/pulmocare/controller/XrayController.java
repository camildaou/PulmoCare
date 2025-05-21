package com.pulmocare.controller;

import com.pulmocare.service.GeminiService;
import com.pulmocare.service.XrayClassifier;
import com.pulmocare.util.PreprocessingUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class XrayController {

    private final XrayClassifier classifier;
    private final GeminiService geminiService;

    @Autowired
    public XrayController(XrayClassifier classifier, GeminiService geminiService) {
        this.classifier = classifier;
        this.geminiService = geminiService;
    }
    
    @PostMapping("/xray/classify")
    public ResponseEntity<Map<String, Object>> classifyXray(@RequestParam("file") MultipartFile file) {
        try {
            // Debug
            System.out.println("Received file: " + file.getOriginalFilename() + " of size: " + file.getSize());

            // Preprocess the image
            float[] preprocessedImage = (float[]) PreprocessingUtils.prepareImage(file);
            
            // Double check the array type
            System.out.println("Preprocessed image class: " + preprocessedImage.getClass().getName());
            System.out.println("Preprocessed image length: " + preprocessedImage.length);
            
            // Force the correct array type if needed
            float[] inputTensor = preprocessedImage;
            if (!preprocessedImage.getClass().getName().equals("[F")) {
                System.out.println("Warning: Unexpected array type, forcing as 1D float array");
                
                if (preprocessedImage.getClass().getName().equals("[[F")) {
                    float[][] data2D = (float[][]) (Object) preprocessedImage;
                    float[] flattened = flattenArray(data2D);
                    inputTensor = flattened;
                    System.out.println("Camil Debug: "+ inputTensor.getClass().getName());
                }
            }

            // Classify the image
            String classification = classifier.classify(inputTensor);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("classification", classification);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to classify X-ray: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    private float[] flattenArray(float[][] data2D) {
        // Calculate total length
        int totalLength = 0;
        for (float[] row : data2D) {
            totalLength += row.length;
        }
        
        // Create flattened array
        float[] flattened = new float[totalLength];
        int pos = 0;
        for (float[] row : data2D) {
            for (float val : row) {
                flattened[pos++] = val;
            }
        }
        
        System.out.println("Flattened 2D array to 1D with length: " + flattened.length);
        return flattened;
    }
}