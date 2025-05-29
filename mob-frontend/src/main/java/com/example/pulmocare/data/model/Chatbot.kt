package com.example.pulmocare.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request model for the chatbot API
 */
@JsonClass(generateAdapter = true)
data class ChatbotRequest(
    @Json(name = "question") val question: String
)

/**
 * Response model from the chatbot API
 */
@JsonClass(generateAdapter = true)
data class ChatbotResponse(
    @Json(name = "response") val response: String
)