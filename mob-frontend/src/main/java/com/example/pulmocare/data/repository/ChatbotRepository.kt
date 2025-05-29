package com.example.pulmocare.data.repository

import android.content.Context
import android.util.Log
import com.example.pulmocare.data.api.ChatbotApiService
import com.example.pulmocare.data.model.ChatbotRequest
import com.example.pulmocare.data.api.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository for interacting with the pulmonary chatbot
 */
class ChatbotRepository(private val context: Context? = null) {
    private val TAG = "ChatbotRepository"
    private val chatbotApiService = NetworkModule.chatbotApiService()
    
    /**
     * Ask a question to the pulmonary chatbot
     * 
     * @param question The user's question about pulmonary health
     * @return Flow with the chatbot's response
     */
    suspend fun askQuestion(question: String): Flow<Result<String>> = flow {
        try {
            Log.d(TAG, "Asking chatbot: $question")
            
            val request = ChatbotRequest(question = question)
            
            // Log the request for debugging
            Log.d(TAG, "Sending request: ${request.question}")
            
            val response = chatbotApiService.askQuestion(request)
            
            // Log response details for debugging
            Log.d(TAG, "Response received - Status code: ${response.code()}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(TAG, "Raw response body: $responseBody")
                
                if (responseBody != null) {
                    Log.d(TAG, "Chatbot response received: $responseBody")
                    emit(Result.success(responseBody))
                } else {
                    Log.e(TAG, "Error: Response body is null")
                    emit(Result.failure(Exception("Empty response from chatbot")))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error from chatbot API: $errorMessage (code: ${response.code()})")
                emit(Result.failure(Exception("Failed to get response: $errorMessage")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception when asking chatbot", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}