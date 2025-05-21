package com.example.pulmocare.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.pulmocare.data.api.NetworkModule
import com.example.pulmocare.data.api.ParserResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Repository for medical document parsing
 */
class ParserRepository {
    private val parserApiService = NetworkModule.parserApiService()
    private val gson = Gson()

    /**
     * Analyze PDF document and extract structured data
     */
    suspend fun analyzePdf(context: Context, pdfUri: Uri): Flow<Result<ParserResponse>> = flow {
        try {
            Log.d(TAG, "Starting PDF analysis for URI: $pdfUri")

            // Create a temp file from the Uri
            val tempFile = createTempFileFromUri(context, pdfUri)
            Log.d(TAG, "Created temporary file: ${tempFile.absolutePath}")

            // Create multipart request
            val requestFile = tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            // Make API call with retry mechanism
            Log.d(TAG, "Sending PDF to backend API for analysis")
            val response = withRetry {
                parserApiService.analyzePdf(filePart)
            }

            if (response.isSuccessful) {
                val rawJson = response.body() ?: throw Exception("Empty response body")
                Log.d(TAG, "Raw response received: ${rawJson.take(100)}...")

                // Parse the Gemini response to extract the actual JSON
                val geminiResponse = extractJsonFromGeminiResponse(rawJson)
                Log.d(TAG, "Extracted JSON: ${geminiResponse.take(100)}...")

                // Validate the JSON structure contains required fields
                if (!geminiResponse.contains("\"tests\"") || !geminiResponse.contains("\"metadata\"")) {
                    throw Exception("Invalid JSON format: Missing required fields 'tests' or 'metadata'")
                }
                try {
                    // Convert to our data class and add flags
                    val jsonObject = JSONObject(geminiResponse)
                    // Check if tests field exists and is an array
                    if (!jsonObject.has("tests")) {
                        throw Exception("Invalid JSON format: 'tests' field is missing")
                    }

                    val testsArray = jsonObject.optJSONArray("tests")
                    if (testsArray == null || testsArray.length() == 0) {
                        throw Exception("Invalid JSON format: 'tests' field is empty or not an array")
                    }

                    // Check if metadata field exists
                    if (!jsonObject.has("metadata")) {
                        throw Exception("Invalid JSON format: 'metadata' field is missing")
                    }

                    // Check required metadata fields
                    val metadata = jsonObject.optJSONObject("metadata")
                    if (metadata == null ||
                        !metadata.has("patient_name") ||
                        !metadata.has("age") ||
                        !metadata.has("gender") ||
                        !metadata.has("date")) {
                        throw Exception("Invalid JSON format: Metadata is missing required fields")
                    }

                    addFlagsToTests(jsonObject)
                    val modifiedJson = jsonObject.toString()

                    // Convert to our data class
                    val parserResponse = gson.fromJson(modifiedJson, ParserResponse::class.java)
                    Log.d(TAG, "Successfully parsed response with ${parserResponse.tests.size} tests")
                    emit(Result.success(parserResponse))
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing JSON: ${e.message}", e)
                    throw Exception("Error processing medical data: ${e.message}")
                }
            } else {
                val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                emit(Result.failure(Exception(errorMsg)))            }            // Clean up temp file
            tempFile.delete().also { success ->
                Log.d(TAG, "Deleted temporary file: $success")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing PDF: ${e.message}", e)
            emit(Result.failure(e))
            return@flow  // Explicitly return from the flow
        }
    }.flowOn(Dispatchers.IO)    /**
     * Retry API calls with exponential backoff
     */
    private suspend fun <T> withRetry(
        maxRetries: Int = 3,
        initialDelayMillis: Long = 1000,
        maxDelayMillis: Long = 20000,  // Increased max delay
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMillis
        var lastException: Exception? = null
        
        repeat(maxRetries) { retryCount ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                
                // Check if we should retry based on the exception type
                if (retryCount == maxRetries - 1 || 
                    e.message?.contains("file not found", ignoreCase = true) == true ||
                    e.message?.contains("permission denied", ignoreCase = true) == true) {
                    Log.e(TAG, "API call failed permanently after ${retryCount + 1} attempts: ${e.message}")
                    throw e
                }
                
                Log.w(TAG, "API call failed (attempt ${retryCount + 1}/$maxRetries): ${e.message}")
                Log.w(TAG, "Retrying in ${currentDelay}ms...")
                
                // Wait before retrying
                delay(currentDelay)
                // Exponential backoff with a cap
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
            }
        }
        
        // This should not be reached due to the throw in the loop,
        // but we include it for completeness
        throw lastException ?: IllegalStateException("Retry mechanism failed")
    }/**
     * Creates a temporary file from a Uri
     */    private fun createTempFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Could not open input stream for URI: $uri")
        
        val tempFile = File.createTempFile("temp_pdf_", ".pdf", context.cacheDir)
        
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.use { input ->
                val bytesCopied = input.copyTo(outputStream)
                // Log bytes copied if needed
                Log.d(TAG, "Copied $bytesCopied bytes from input stream to temp file")
            }
        }
        
        return tempFile
    }    /**
     * Extract JSON from Gemini API response
     */
    private fun extractJsonFromGeminiResponse(rawResponse: String): String {
        try {
            // Check if the response is empty or too short
            if (rawResponse.isBlank() || rawResponse.length < 10) {
                Log.w(TAG, "Response is too short or empty: '${rawResponse}'")
                throw Exception("Empty or incomplete API response")
            }
            
            // First try parsing the entire response as JSON
            try {
                val responseJson = JSONObject(rawResponse)
                
                // Check if it's a direct JSON response with tests
                if (responseJson.has("tests") && responseJson.has("metadata")) {
                    return rawResponse
                }
                
                // Check for error message in the response
                if (responseJson.has("error")) {
                    val errorMessage = responseJson.optJSONObject("error")?.optString("message") ?: "Unknown API error"
                    Log.e(TAG, "API returned an error: $errorMessage")
                    throw Exception("API error: $errorMessage")
                }
                
                // Check if it's Gemini's API format
                if (responseJson.has("candidates")) {
                    // Navigate to the content section
                    val candidates = responseJson.getJSONArray("candidates")
                    if (candidates.length() == 0) {
                        throw Exception("API response contains no candidates")
                    }
                    
                    val candidate = candidates.getJSONObject(0)
                    if (candidate.has("content")) {
                        val content = candidate.getJSONObject("content")
                        if (content.has("parts")) {
                            val parts = content.getJSONArray("parts")
                            if (parts.length() == 0) {
                                throw Exception("API response contains no content parts")
                            }
                            
                            val text = parts.getJSONObject(0).getString("text")
                            
                            // Extract JSON from the text - it might be wrapped in markdown code blocks
                            val jsonRegex = "```json\\s*(.+?)\\s*```".toRegex(RegexOption.DOT_MATCHES_ALL)
                            val matchResult = jsonRegex.find(text)
                            
                            return matchResult?.groupValues?.get(1)?.trim() ?: text.trim()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Not a standard JSON object, trying other parsing methods: ${e.message}")
            }            
            // Try to extract JSON from markdown code blocks directly
            val jsonRegex = "```json\\s*(.+?)\\s*```".toRegex(RegexOption.DOT_MATCHES_ALL)
            val matchResult = jsonRegex.find(rawResponse)
            if (matchResult != null) {
                val extractedJson = matchResult.groupValues[1].trim()
                if (extractedJson.isNotBlank()) {
                    Log.d(TAG, "Found JSON in markdown code block")
                    return extractedJson
                }
            }
            
            // Try to extract any JSON object
            val jsonObjectRegex = "\\{[\\s\\S]*?\\}".toRegex()
            val jsonMatch = jsonObjectRegex.find(rawResponse)
            if (jsonMatch != null) {
                val possibleJson = jsonMatch.value
                try {
                    // Validate that it's proper JSON
                    JSONObject(possibleJson)
                    Log.d(TAG, "Found JSON object in response text")
                    return possibleJson
                } catch (e: Exception) {
                    Log.d(TAG, "Found text that looks like JSON but isn't valid: ${e.message}")
                }
            }
            
            // If it looks like a JSON object already, return it as is
            if (rawResponse.trim().startsWith("{") && rawResponse.trim().endsWith("}")) {
                Log.d(TAG, "Response is already in JSON format")
                return rawResponse.trim()
            }
            
            // Try to find anything that might be a report
            if (rawResponse.contains("\"tests\"") && rawResponse.contains("\"metadata\"")) {
                Log.d(TAG, "Found text with tests and metadata, attempting to extract")
                
                // Try to extract a valid JSON substring
                val startIdx = rawResponse.indexOf("{")
                val endIdx = rawResponse.lastIndexOf("}")
                
                if (startIdx >= 0 && endIdx > startIdx) {
                    val possibleJson = rawResponse.substring(startIdx, endIdx + 1)
                    try {
                        // Validate
                        JSONObject(possibleJson)
                        return possibleJson
                    } catch (e: Exception) {
                        Log.e(TAG, "Found potential JSON but it's invalid: ${e.message}")
                    }
                }
            }
            
            // Return whatever we have as a last resort
            Log.w(TAG, "Could not extract proper JSON, returning raw response")
            return rawResponse
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting JSON: ${e.message}", e)
            // Return the original response if parsing fails
            return rawResponse
        }
    }

    companion object {
        private const val TAG = "ParserRepository"
    }    /**
     * Adds "flag" field to each test based on result value and normal range
     */
    private fun addFlagsToTests(jsonObject: JSONObject) {
        try {
            val testsArray = jsonObject.getJSONArray("tests")
            
            // Process each test
            for (i in 0 until testsArray.length()) {
                val test = testsArray.getJSONObject(i)
                  // Get values, handling potential nulls
                val testName = test.optString("test_name", "")
                val resultValueObj = test.opt("result_value")
                val resultValue = if (resultValueObj != JSONObject.NULL && resultValueObj != null) resultValueObj.toString() else null
                val normalRange = if (test.isNull("normal_range")) null else test.optString("normal_range")
                
                // Set default flag to Normal
                val flag = determineTestFlag(testName, resultValue, normalRange)
                
                // Add the flag to the test object
                test.put("flag", flag)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding flags to tests: ${e.message}")
        }
    }    /**
     * Determines whether a test result is normal or abnormal
     */
    private fun determineTestFlag(testName: String, resultValue: String?, normalRange: String?): String {
        try {
            // If result or range is null, we can't determine abnormality, so return Normal
            if (resultValue == null || resultValue.isEmpty() || normalRange == null || normalRange.isEmpty()) {
                Log.d(TAG, "Missing data for test '$testName': resultValue=$resultValue, normalRange=$normalRange - defaulting to Normal")
                return "Normal"
            }
            
            // Clean up the result value - remove any non-numeric characters except decimal point
            val cleanResult = resultValue.toString().replace(Regex("[^0-9.-]"), "")
            if (cleanResult.isEmpty()) {
                Log.d(TAG, "Could not parse numeric value from '$resultValue' for test '$testName' - defaulting to Normal")
                return "Normal" // Default if we can't parse
            }
            
            val result = cleanResult.toDoubleOrNull() 
            if (result == null) {
                Log.d(TAG, "Could not convert '$cleanResult' to numeric value for test '$testName' - defaulting to Normal")
                return "Normal"
            }
            
            // Handle different formats of normal ranges
            when {                // Special case for WBC (White Blood Cell) tests
                testName.contains("WBC", ignoreCase = true) || testName.contains("White Blood Cell", ignoreCase = true) -> {
                    Log.d(TAG, "Processing WBC test with value $result and range $normalRange")
                    
                    // Convert result to thousands if it's above 1000
                    val valueInThousands = if (result >= 1000) result / 1000.0 else result
                    
                    if (normalRange.contains("-")) {
                        try {
                            val parts = normalRange.split("-")
                            val min = parts[0].replace(Regex("[^0-9.-]"), "").toDoubleOrNull() ?: 4.0
                            val max = parts[1].replace(Regex("[^0-9.-]"), "").toDoubleOrNull() ?: 11.0
                            val isNormal = valueInThousands >= min && valueInThousands <= max
                            Log.d(TAG, "WBC test result is ${if (isNormal) "Normal" else "Abnormal"} " +
                                    "(value: $valueInThousands thousand, range: $min-$max)")
                            return if (isNormal) "Normal" else "Abnormal"
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing WBC range: ${e.message}")
                            return "Normal" // Default to normal if parsing fails
                        }
                    }
                    return "Normal" // Default to normal if range format not recognized
                }
                  // Special case for RBC (Red Blood Cell) tests
                testName.contains("RBC", ignoreCase = true) || testName.contains("Red Blood Cell", ignoreCase = true) -> {
                    Log.d(TAG, "Processing RBC test with value $result and range $normalRange")
                    
                    // Convert result to millions if it's above 1,000,000
                    val valueInMillions = if (result >= 1000000) result / 1000000.0 
                                         else if (result >= 1000) result / 1000.0
                                         else result
                    
                    if (normalRange.contains("-")) {
                        try {
                            val parts = normalRange.split("-")
                            val min = parts[0].replace(Regex("[^0-9.-]"), "").toDoubleOrNull() ?: 4.0
                            val max = parts[1].replace(Regex("[^0-9.-]"), "").toDoubleOrNull() ?: 6.0
                            val isNormal = valueInMillions >= min && valueInMillions <= max
                            Log.d(TAG, "RBC test result is ${if (isNormal) "Normal" else "Abnormal"} " +
                                    "(value: $valueInMillions million, range: $min-$max)")
                            return if (isNormal) "Normal" else "Abnormal"
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing RBC range: ${e.message}")
                            return "Normal" // Default to normal if parsing fails
                        }
                    }
                    return "Normal" // Default to normal if range format not recognized
                }
                
                // Special case for Triglycerides
                testName.contains("Triglycerides", ignoreCase = true) || testName.contains("TG", ignoreCase = true) -> {
                    // Special handling for triglycerides - ensure it's treated correctly
                    Log.d(TAG, "Processing Triglycerides test with value $result and range $normalRange")
                    val parts = normalRange.split("-")
                    if (parts.size == 2) {
                        val min = parts[0].replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 35.0
                        val max = parts[1].replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 135.0
                        val isNormal = result >= min && result <= max
                        Log.d(TAG, "Triglycerides test result is ${if (isNormal) "Normal" else "Abnormal"} " +
                               "(value: $result, range: $min-$max)")
                        return if (isNormal) "Normal" else "Abnormal"
                    } else if (normalRange.contains("<")) {
                        // Handle "< max" format
                        val max = normalRange.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 150.0
                        val isNormal = result < max
                        Log.d(TAG, "Triglycerides test result is ${if (isNormal) "Normal" else "Abnormal"} " +
                               "(value: $result, max: $max)")
                        return if (isNormal) "Normal" else "Abnormal"
                    }
                    // Default to normal for triglycerides if we can't determine
                    return "Normal"
                }
                
                // Range in format "min-max"
                normalRange.contains("-") -> {
                    val parts = normalRange.split("-")
                    if (parts.size == 2) {
                        val min = parts[0].replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: return "Normal"
                        val max = parts[1].replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: return "Normal"
                        return if (result >= min && result <= max) "Normal" else "Abnormal"
                    }
                }
                
                // Range in format "< max" or "> min"
                normalRange.contains("<") -> {
                    val max = normalRange.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: return "Normal"
                    return if (result < max) "Normal" else "Abnormal"
                }
                
                normalRange.contains(">") -> {
                    val min = normalRange.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: return "Normal"
                    return if (result > min) "Normal" else "Abnormal"
                }
            }
            
            // Default to Normal if we couldn't determine
            return "Normal"
        } catch (e: Exception) {
            Log.e(TAG, "Error determining test flag: ${e.message}")
            return "Normal" // Default to Normal if there's an error
        }
    }
}
