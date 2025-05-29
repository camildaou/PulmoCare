package com.example.pulmocare.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * API Service for X-ray classification
 */
interface XrayApiService {
    /**
     * Upload an X-ray image and get its classification (Normal, Covid-19, Bacterial Pneumonia, etc.)
     *
     * @param file The X-ray image file to be classified
     * @return A response containing the classification result
     */
    @Multipart
    @POST("api/xray/classify")
    suspend fun classifyXray(
        @Part file: MultipartBody.Part
    ): Response<XrayClassificationResponse>
}

/**
 * Data class representing the X-ray classification response
 */
data class XrayClassificationResponse(
    val classification: String,
    val confidence: Float = 0.0f,
    val timestamp: String = ""
)
