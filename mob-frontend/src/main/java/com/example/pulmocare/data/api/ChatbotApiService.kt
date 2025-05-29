package com.example.pulmocare.data.api

import com.example.pulmocare.data.model.ChatbotRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatbotApiService {
    @POST("api/ask") // Make sure this matches your backend endpoint
    suspend fun askQuestion(@Body request: ChatbotRequest): Response<String>
}