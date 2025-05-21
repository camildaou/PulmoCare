package com.example.pulmocare.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API Service for Google Gemini AI assessments
 */
interface GeminiApiService {
    /**
     * Generate a medical assessment based on the provided prompt
     *
     * @param request The request containing the prompt for Gemini
     * @return A response containing the generated assessment text
     */
    @POST("api/gemini/generate")
    suspend fun generateAssessment(
        @Body request: GeminiRequest
    ): Response<GeminiResponse>

    /**
     * Generate a medical assessment for an X-ray image based on its classification
     *
     * @param request The request containing X-ray classification and other details
     * @return A response containing the generated assessment text
     */
    @POST("api/gemini/xray-assessment")
    suspend fun generateXrayAssessment(
        @Body request: XrayAssessmentRequest
    ): Response<GeminiResponse>
}

/**
 * Data class representing a request to the Gemini API
 */
data class GeminiRequest(
    val prompt: String,
    val maxTokens: Int = 1024
)

/**
 * Data class representing a request for X-ray assessment
 */
data class XrayAssessmentRequest(
    val classification: String,
    val patientAge: Int? = null,
    val patientGender: String? = null,
    val symptoms: List<String>? = null,
    val additionalNotes: String? = null
)

/**
 * Data class representing the response from the Gemini API
 */
data class GeminiResponse(
    val text: String,
    val timestamp: String = ""
)