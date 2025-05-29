package com.example.pulmocare.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * API Service for parsing medical documents like blood test PDFs
 */
interface ParserApiService {
    /**
     * Upload a PDF file to be analyzed and parsed
     *
     * @param file The PDF file to be analyzed
     * @return A response containing the parsed data
     */
    @Multipart
    @POST("api/parser/analyze")
    suspend fun analyzePdf(
        @Part file: MultipartBody.Part
    ): Response<String>
}

/**
 * Data class representing the medical test metadata
 */
data class MedicalTestMetadata(
    val patient_name: String,
    val age: String,
    val gender: String,
    val date: String,
    val physician: String
)

/**
 * Data class representing a single medical test result
 */
data class MedicalTest(
    val test_name: String,
    val result_value: Any?,     // Changed from String to Any? to handle null and numeric values
    val unit: String?,          // Changed from String to String? to handle null
    val normal_range: String?,  // Changed from String to String? to handle null
    val flag: String = "Normal" // Default value in case flag is not in the response
)

/**
 * Data class representing the complete parsed response
 */
data class ParserResponse(
    val metadata: MedicalTestMetadata,
    val tests: List<MedicalTest>
)
