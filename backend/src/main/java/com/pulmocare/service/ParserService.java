package com.pulmocare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParserService {    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    private String getEndpoint() {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
    }
    
    public String callGeminiWithPdf(MultipartFile file) throws IOException, InterruptedException {
        String base64Pdf = Base64.getEncoder().encodeToString(file.getBytes());        
        String prompt = "Here is a medical test PDF report (could be blood test, urinalysis, or other lab test). Please parse it and return the following:\n\n"
                + "A valid JSON with:\n"
                + "- \"metadata\": includes:\n"
                + "  - patient_name\n"
                + "  - age\n"
                + "  - gender\n"
                + "  - date\n"
                + "  - physician\n"
                + "- \"tests\": a list of tests, each with:\n"
                + "  - test_name (the name of the test)\n"
                + "  - result_value (just the numeric value if available, or null if it's qualitative like 'Negative')\n"
                + "  - unit (the unit of measurement, or null if not applicable)\n"
                + "  - normal_range (format as 'min-max' e.g. '0.6-1.2', or null if not available)\n"
                + "\nFor non-numeric results like 'Negative', 'Positive', 'Clear', etc., set result_value to null and keep the qualitative result in the test_name.\n"
                + "If a value is not available, use null instead of empty string.\n";
                String bodyJson = "{\n"
                + "  \"contents\": [\n"
                + "    {\n"
                + "      \"parts\": [\n"
                + "        {\n"
                + "          \"text\": \"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"\n"
                + "        },\n"
                + "        {\n"
                + "          \"inlineData\": {\n"
                + "            \"mimeType\": \"application/pdf\",\n"
                + "            \"data\": \"" + base64Pdf + "\"\n"
                + "          }\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "  ],\n"
                + "  \"generationConfig\": {\n"
                + "    \"temperature\": 0.1,\n"
                + "    \"topK\": 32,\n"
                + "    \"topP\": 0.95,\n"
                + "    \"maxOutputTokens\": 2048\n"
                + "  }\n"
                + "}";          HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getEndpoint()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
          // Check if response is successful (2xx status code)
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return addAbnormalFlags(response.body());
        } else {
            throw new IOException("API request failed with status code: " + response.statusCode() + 
                                 " and response: " + response.body());
        }
    }
      /**
     * Post-processes the response from Gemini to add abnormal flags based on result values and normal ranges.
     * This method adds a "flag" field to each test indicating whether the result is "Normal" or "Abnormal".
     */    private String addAbnormalFlags(String responseBody) {
        try {
            // Find the tests array in the JSON
            int testsStartIndex = responseBody.indexOf("\"tests\":");
            if (testsStartIndex == -1) {
                // If tests array not found, return the original response
                System.out.println("No tests array found in the response, returning original");
                return responseBody;
            }
            
            // Process each test to add the flag
            StringBuilder processedJson = new StringBuilder(responseBody);
            int currentIndex = testsStartIndex;
            int testsArrayOpenBracket = processedJson.indexOf("[", currentIndex);
            int testsArrayCloseBracket = findMatchingCloseBracket(processedJson.toString(), testsArrayOpenBracket);
            
            if (testsArrayOpenBracket == -1 || testsArrayCloseBracket == -1) {
                System.out.println("Could not find the bounds of the tests array, returning original");
                return responseBody;
            }
            
            // Process until end of tests array
            while (currentIndex < testsArrayCloseBracket) {
                int testStartIndex = processedJson.indexOf("{", currentIndex);
                if (testStartIndex == -1 || testStartIndex >= testsArrayCloseBracket) {
                    break; // No more tests or reached the end of the array
                }
                
                int testEndIndex = findMatchingCloseBracket(processedJson.toString(), testStartIndex);
                if (testEndIndex == -1 || testEndIndex > testsArrayCloseBracket) {
                    // Invalid JSON structure, move to next open brace
                    currentIndex = testStartIndex + 1;
                    continue;
                }
                
                // Extract the test object as a string
                String testObject = processedJson.substring(testStartIndex, testEndIndex + 1);
                
                // Extract test_name, result_value, unit, and normal_range
                String testName = extractField(testObject, "test_name");
                String resultValue = extractField(testObject, "result_value");
                String unit = extractField(testObject, "unit");
                String normalRange = extractField(testObject, "normal_range");
                
                // Skip adding a flag if test name is empty
                if (testName.isEmpty()) {
                    System.out.println("Warning: Empty test name found, skipping flag addition");
                    currentIndex = testEndIndex + 1;
                    continue;
                }
                
                // Determine if the result is normal or abnormal
                boolean isNormal = isResultNormal(testName, resultValue, unit, normalRange);
                String flag = isNormal ? "Normal" : "Abnormal";
                
                System.out.println("Test '" + testName + "' with result '" + resultValue + 
                                  "' " + unit + " (range: " + normalRange + ") is " + flag);
                
                // Check if the test object already has a flag field
                if (testObject.contains("\"flag\":")) {
                    System.out.println("Test already has a flag field, skipping");
                    currentIndex = testEndIndex + 1;
                    continue;
                }
                
                // Add the flag field to the test object
                String flaggedTest = testObject.substring(0, testObject.length() - 1) + 
                                     (testObject.charAt(testObject.length() - 2) == ',' ? " " : ", ") +
                                     "\"flag\": \"" + flag + "\"}";
                
                // Replace the original test object with the flagged one
                processedJson.replace(testStartIndex, testEndIndex + 1, flaggedTest);
                
                // Adjust currentIndex and testsArrayCloseBracket to account for the change in length
                int lengthDiff = flaggedTest.length() - testObject.length();
                testsArrayCloseBracket += lengthDiff;
                currentIndex = testStartIndex + flaggedTest.length();
            }
            
            return processedJson.toString();
        } catch (Exception e) {
            // If any error occurs during processing, return the original response
            System.err.println("Error adding abnormal flags: " + e.getMessage());
            e.printStackTrace();
            return responseBody;
        }
    }
    
    /**
     * Helper method to find the matching closing bracket (} or ]) for an opening bracket ({ or [).
     * Returns the index of the matching closing bracket, or -1 if not found.
     */
    private int findMatchingCloseBracket(String json, int openBracketIndex) {
        if (openBracketIndex < 0 || openBracketIndex >= json.length()) {
            return -1;
        }
        
        char openBracket = json.charAt(openBracketIndex);
        char closeBracket;
        if (openBracket == '{') {
            closeBracket = '}';
        } else if (openBracket == '[') {
            closeBracket = ']';
        } else {
            return -1; // Not an opening bracket
        }
        
        int nestLevel = 1;
        for (int i = openBracketIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            
            // Skip string literals
            if (c == '"') {
                i++;
                while (i < json.length()) {
                    if (json.charAt(i) == '\\') {
                        i++; // Skip escape character and the next character
                    } else if (json.charAt(i) == '"') {
                        break;
                    }
                    i++;
                }
                if (i >= json.length()) {
                    return -1; // Unclosed string literal
                }
                continue;
            }
            
            if (c == openBracket) {
                nestLevel++;
            } else if (c == closeBracket) {
                nestLevel--;
                if (nestLevel == 0) {
                    return i; // Found matching close bracket
                }
            }
        }
        
        return -1; // No matching close bracket found
    }
      /**
     * Extracts a field value from a JSON object string.
     * Handles various value types (string, number, null)
     */    private String extractField(String jsonObject, String fieldName) {
        int fieldIndex = jsonObject.indexOf("\"" + fieldName + "\":");
        if (fieldIndex == -1) {
            System.out.println("Field '" + fieldName + "' not found in JSON object");
            return "";
        }
        
        // Find the start of the value after the field name and colon
        int valueStartPos = fieldIndex + fieldName.length() + 2;
        while (valueStartPos < jsonObject.length() && Character.isWhitespace(jsonObject.charAt(valueStartPos))) {
            valueStartPos++;
        }
        
        // Check for null value
        if (valueStartPos + 3 <= jsonObject.length() && 
            jsonObject.substring(valueStartPos, valueStartPos + 4).equals("null")) {
            System.out.println("Found null value for field: " + fieldName);
            return "";  // Return empty string for null values
        }
        
        // Check if it's a string (starts with quote)
        if (jsonObject.charAt(valueStartPos) == '"') {
            int valueStartIndex = valueStartPos;
            // Find the end quote, handling escaped quotes
            int valueEndIndex = valueStartIndex + 1;
            boolean escaped = false;
            
            while (valueEndIndex < jsonObject.length()) {
                char c = jsonObject.charAt(valueEndIndex);
                if (c == '\\') {
                    escaped = !escaped;
                } else if (c == '"' && !escaped) {
                    break;
                } else {
                    escaped = false;
                }
                valueEndIndex++;
            }
            
            if (valueEndIndex == jsonObject.length()) {
                System.out.println("Warning: Unclosed string for field: " + fieldName);
                return "";
            }
            
            String value = jsonObject.substring(valueStartIndex + 1, valueEndIndex);
            System.out.println("Extracted string value for field '" + fieldName + "': " + value);
            return value;
        }
        
        // If not a string, it's likely a number or boolean - find the next comma or closing brace
        int valueEndPos = jsonObject.indexOf(",", valueStartPos);
        if (valueEndPos == -1) {
            // If no comma found, look for the closing brace
            valueEndPos = jsonObject.indexOf("}", valueStartPos);
            if (valueEndPos == -1) {
                System.out.println("Warning: Could not find end of value for field: " + fieldName);
                return "";
            }
        }
        
        // Extract and trim the value
        String value = jsonObject.substring(valueStartPos, valueEndPos).trim();
        System.out.println("Extracted non-string value for field '" + fieldName + "': " + value);
        return value;
    }
      /**
     * Determines if a test result is within the normal range.
     * Handles special case for Triglycerides and other tests.
     */    private boolean isResultNormal(String testName, String resultValue, String unit, String normalRange) {
        try {
            // Log the test information for debugging
            System.out.println("Checking test: " + testName + ", value: " + resultValue + 
                              ", unit: " + unit + ", range: " + normalRange);
            
            // If the result value is empty or null, we can't determine abnormality
            if (resultValue == null || resultValue.isEmpty()) {
                System.out.println("Result value is null or empty for test: " + testName + ", assuming Normal");
                return true;
            }
            
            // If the normal range is empty or null, we can't determine abnormality
            if (normalRange == null || normalRange.isEmpty()) {
                System.out.println("Normal range is null or empty for test: " + testName + ", assuming Normal");
                return true;
            }
            
            // Check for qualitative values (e.g., "Negative", "Positive", "Clear")
            if (resultValue.equalsIgnoreCase("negative") || 
                resultValue.equalsIgnoreCase("normal") ||
                resultValue.equalsIgnoreCase("clear") ||
                resultValue.equalsIgnoreCase("absent") ||
                resultValue.equalsIgnoreCase("nil")) {
                
                // For these values, check if they match the expected normal values
                if (normalRange.toLowerCase().contains("negative") ||
                    normalRange.toLowerCase().contains("normal") ||
                    normalRange.toLowerCase().contains("clear") ||
                    normalRange.toLowerCase().contains("absent") ||
                    normalRange.toLowerCase().contains("nil")) {
                    System.out.println("Qualitative result '" + resultValue + "' is Normal");
                    return true;
                }
            }
            
            if (resultValue.equalsIgnoreCase("positive") || 
                resultValue.equalsIgnoreCase("abnormal") ||
                resultValue.equalsIgnoreCase("present") ||
                resultValue.equalsIgnoreCase("detected")) {
                
                // For these abnormal values, they're normal only if the expected range includes them
                if (normalRange.toLowerCase().contains("positive") ||
                    normalRange.toLowerCase().contains("abnormal") ||
                    normalRange.toLowerCase().contains("present") ||
                    normalRange.toLowerCase().contains("detected")) {
                    System.out.println("Qualitative result '" + resultValue + "' matches expected normal range");
                    return true;
                } else {
                    System.out.println("Qualitative result '" + resultValue + "' is Abnormal");
                    return false;
                }
            }
            
            // Check for trace values which are often considered borderline normal
            if (resultValue.equalsIgnoreCase("trace") || 
                resultValue.equalsIgnoreCase("few") || 
                resultValue.equalsIgnoreCase("rare")) {
                
                if (normalRange.toLowerCase().contains("trace") ||
                    normalRange.toLowerCase().contains("few") ||
                    normalRange.toLowerCase().contains("rare") ||
                    normalRange.toLowerCase().contains("negative") ||
                    normalRange.toLowerCase().contains("normal")) {
                    System.out.println("Trace/few/rare result '" + resultValue + "' is within normal range");
                    return true;
                } else {
                    System.out.println("Trace/few/rare result '" + resultValue + "' is considered borderline abnormal");
                    return false;
                }
            }
            
            // Check for color/appearance descriptors
            if (isAppearanceOrColorDescriptor(resultValue)) {
                if (normalRange.toLowerCase().contains(resultValue.toLowerCase()) ||
                    (resultValue.equalsIgnoreCase("yellow") && normalRange.toLowerCase().contains("pale yellow")) ||
                    (resultValue.equalsIgnoreCase("straw") && normalRange.toLowerCase().contains("pale yellow"))) {
                    System.out.println("Appearance/color '" + resultValue + "' is within normal range");
                    return true;
                } else {
                    // For appearance/color, return true if we can't determine
                    System.out.println("Appearance/color '" + resultValue + "' not found in normal range, assuming Normal");
                    return true;
                }
            }
            
            // Try to parse as numeric value if not qualitative
            String cleanResult = resultValue.replaceAll("[^0-9.-]", "");
            if (cleanResult.isEmpty()) {
                System.out.println("Warning: Empty result value after cleanup for test: " + testName + ", assuming Normal");
                return true;
            }
            
            // Try to parse the result as a double, return true (normal) if it fails
            double result;
            try {
                result = Double.parseDouble(cleanResult);
            } catch (NumberFormatException e) {
                System.out.println("Warning: Could not parse result value as number for test: " + testName + ", assuming Normal");
                return true;
            }
            
            // Handle specific test types
            if (testName.equalsIgnoreCase("Triglycerides") || testName.contains("Triglyceride") || testName.contains("TG")) {
                System.out.println("Processing Triglycerides test");
                // Most labs use 35-135 mg/dL or 0.4-1.5 mmol/L as reference range for Triglycerides
                if (unit.contains("mg") || unit.contains("mg%") || unit.contains("mg/dl")) {
                    // Handle mg/dL units
                    if (normalRange.contains("-")) {
                        String[] parts = normalRange.split("-");
                        double min = Double.parseDouble(parts[0].replaceAll("[^0-9.]", ""));
                        double max = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                        return result >= min && result <= max;
                    } else if (normalRange.contains("<")) {
                        double max = Double.parseDouble(normalRange.replaceAll("[^0-9.]", ""));
                        return result < max;
                    }
                }
            } 
            // Continue with rest of the method for WBC, RBC, etc.
            else if (testName.equalsIgnoreCase("WBC's") || testName.contains("WBC") || testName.contains("White Blood Cell")) {
                // Existing WBC handling code...
                System.out.println("Processing WBC test");
                // Check if units are in /cumm, and range is in thousands
                double valueInThousands = result;
                
                // If result has 3 or more digits, convert to thousands
                if (result >= 1000) {
                    valueInThousands = result / 1000.0;
                    System.out.println("WBC test: Converting large value " + result + " to " + valueInThousands + " thousand");
                }
                
                System.out.println("WBC value in thousands: " + valueInThousands);
                
                if (normalRange.contains("-")) {
                    String[] parts = normalRange.split("-");
                    double min = Double.parseDouble(parts[0].replaceAll("[^0-9.]", ""));
                    double max = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                    boolean isNormal = valueInThousands >= min && valueInThousands <= max;
                    System.out.println("WBC test result is " + (isNormal ? "Normal" : "Abnormal") +
                                    " (value: " + valueInThousands + " thousand, range: " + min + "-" + max + ")");
                    return isNormal;
                }
            }
            // Rest of the existing method remains the same...
            else if (testName.equalsIgnoreCase("RBC's") || testName.contains("RBC") || testName.contains("Red Blood Cell")) {
                System.out.println("Processing RBC test");
                // Handle RBC values regardless of units
                double valueInMillions = result;
                
                // If result has 6 or more digits, convert to millions
                if (result >= 1000000) {
                    valueInMillions = result / 1000000.0;
                    System.out.println("RBC test: Converting large value " + result + " to " + valueInMillions + " million");
                }
                // If result is moderate (thousands range) and normal range is in millions (typically 4-6)
                else if (result >= 1000 && normalRange.contains("-")) {
                    String[] parts = normalRange.split("-");
                    if (parts.length == 2) {
                        try {
                            double minRange = Double.parseDouble(parts[0].replaceAll("[^0-9.]", ""));
                            double maxRange = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                            // If normal range is in single digits (4-6 million), but result is in thousands
                            if (maxRange < 10 && result >= 1000) {
                                valueInMillions = result / 1000000.0;
                                System.out.println("RBC test: Converting thousand value " + result + " to " + valueInMillions + " million based on range");
                            }
                        } catch (Exception e) {
                            System.out.println("Could not parse RBC range for unit conversion: " + e.getMessage());
                        }
                    }
                }
                
                System.out.println("RBC value in millions: " + valueInMillions);
                
                if (normalRange.contains("-")) {
                    String[] parts = normalRange.split("-");
                    double min = Double.parseDouble(parts[0].replaceAll("[^0-9.]", ""));
                    double max = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                    boolean isNormal = valueInMillions >= min && valueInMillions <= max;
                    System.out.println("RBC test result is " + (isNormal ? "Normal" : "Abnormal") +
                                    " (value: " + valueInMillions + " million, range: " + min + "-" + max + ")");
                    return isNormal;
                }
            } else if (testName.equalsIgnoreCase("Total Cholesterol") || testName.contains("Cholesterol")) {
                System.out.println("Processing Cholesterol test");
                // Typical range: < 200 mg/dL is desirable
                if (unit.contains("mg") || unit.contains("mg%") || unit.contains("mg/dl")) {
                    if (normalRange.contains("<")) {
                        double max = Double.parseDouble(normalRange.replaceAll("[^0-9.]", ""));
                        return result < max;
                    } else if (normalRange.contains("-")) {
                        String[] parts = normalRange.split("-");
                        double min = Double.parseDouble(parts[0].replaceAll("[^0-9.]", ""));
                        double max = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                        return result >= min && result <= max;
                    }
                    // If no specific range format is found but we know it's cholesterol
                    // Use standard ranges: < 200 mg/dL is normal
                    return result < 200;
                }
            } else if (testName.contains("Glucose")) {
                System.out.println("Processing Glucose test");
                // Typical fasting reference range: 70-99 mg/dL
                if (unit.contains("mg") || unit.contains("mg%") || unit.contains("mg/dl")) {
                    if (normalRange.contains("-")) {
                        String[] parts = normalRange.split("-");
                        double min = Double.parseDouble(parts[0].replaceAll("[^0-9.]", ""));
                        double max = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                        return result >= min && result <= max;
                    }
                }
            }
              
            // Handle different range formats for other tests
            if (normalRange.contains("-")) {
                // Range in format "min-max"
                String[] rangeParts = normalRange.split("-");
                if (rangeParts.length != 2) {
                    System.out.println("Warning: Unexpected range format for test: " + testName + ", range: " + normalRange);
                    return false;
                }
                
                // Clean up range values
                String cleanMinRange = rangeParts[0].replaceAll("[^0-9.-]", "");
                String cleanMaxRange = rangeParts[1].replaceAll("[^0-9.-]", "");
                
                if (cleanMinRange.isEmpty() || cleanMaxRange.isEmpty()) {
                    System.out.println("Warning: Empty range values for test: " + testName + ", range: " + normalRange);
                    return false;
                }
                  
                double minRange = Double.parseDouble(cleanMinRange);
                double maxRange = Double.parseDouble(cleanMaxRange);
                
                // Check if result is within range (inclusive)
                boolean isNormal = result >= minRange && result <= maxRange;
                System.out.println("Test: " + testName + ", result: " + result + 
                                  ", range: " + minRange + "-" + maxRange + 
                                  ", normal: " + isNormal);
                return isNormal;            
            } else if (normalRange.contains("<")) {
                // Range in format "< max"
                String cleanMaxRange = normalRange.replaceAll("[^0-9.-]", "");
                if (cleanMaxRange.isEmpty()) {
                    System.out.println("Warning: Empty max range value for test: " + testName + ", range: " + normalRange);
                    return false;
                }
                
                double maxRange = Double.parseDouble(cleanMaxRange);
                boolean isNormal = result < maxRange;
                System.out.println("Test: " + testName + ", result: " + result + 
                                  ", max range: " + maxRange + 
                                  ", normal: " + isNormal);
                return isNormal;
            } else if (normalRange.contains(">")) {
                // Range in format "> min"
                String cleanMinRange = normalRange.replaceAll("[^0-9.-]", "");
                if (cleanMinRange.isEmpty()) {
                    System.out.println("Warning: Empty min range value for test: " + testName + ", range: " + normalRange);
                    return false;
                }
                
                double minRange = Double.parseDouble(cleanMinRange);
                boolean isNormal = result > minRange;
                System.out.println("Test: " + testName + ", result: " + result + 
                                  ", min range: " + minRange + 
                                  ", normal: " + isNormal);
                return isNormal;
            }
            
            // If range format is not recognized, default to abnormal
            System.out.println("Warning: Unrecognized range format for test: " + testName + ", range: " + normalRange);
            return false;        
        } catch (NumberFormatException | NullPointerException e) {
            // If there's any error in parsing, default to abnormal
            System.err.println("Error parsing test values for test '" + testName + "': " + e.getMessage());
            System.err.println("Problematic values - result: '" + resultValue + "', unit: '" + unit + "', range: '" + normalRange + "'");
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error processing test '" + testName + "': " + e.getMessage());
            System.err.println("Problematic values - result: '" + resultValue + "', unit: '" + unit + "', range: '" + normalRange + "'");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to check if a value is an appearance or color descriptor
     * commonly used in urinalysis reports
     */
    private boolean isAppearanceOrColorDescriptor(String value) {
        String lowerValue = value.toLowerCase();
        return lowerValue.equals("yellow") || 
               lowerValue.equals("pale yellow") || 
               lowerValue.equals("amber") ||
               lowerValue.equals("straw") ||
               lowerValue.equals("clear") ||
               lowerValue.equals("cloudy") ||
               lowerValue.equals("turbid") ||
               lowerValue.equals("hazy") ||
               lowerValue.equals("colorless") ||
               lowerValue.equals("dark yellow") ||
               lowerValue.equals("slightly turbid") ||
               lowerValue.equals("transparent");
    }
}
