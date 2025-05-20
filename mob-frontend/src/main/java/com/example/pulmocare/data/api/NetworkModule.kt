package com.example.pulmocare.data.api

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network module that provides Retrofit instances and API services
 */
object NetworkModule {
    // For emulator, use 10.0.2.2 to access localhost
    // For real device testing on same network, use your computer's IP address
    // Default port for Spring Boot is 8081 (check application.properties)
    private const val BASE_URL = "http://10.0.2.2:8081/" 
    
    // Alternative URL for real device testing (replace with your computer's IP)
    // private const val BASE_URL = "http://192.168.1.X:8081/"
    
    private const val TAG = "NetworkModule"
    
    // For dev debug mode, change to false for production
    private const val ENABLE_LOGS = true
    
    /**
     * Create OkHttpClient with proper logging and timeouts
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (ENABLE_LOGS) 
                HttpLoggingInterceptor.Level.BODY 
            else 
                HttpLoggingInterceptor.Level.NONE
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                try {
                    val response = chain.proceed(request)
                    Log.d(TAG, "Response successful: ${response.code}")
                    response
                } catch (e: Exception) {
                    Log.e(TAG, "Network error: ${e.message}")
                    throw e
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS) // Increased timeout
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // Enable retries
            .build()
    }
    
    /**
     * Create Retrofit instance with proper configuration
     */
    private fun createRetrofit(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()
            
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(ScalarsConverterFactory.create()) // Add this for string responses
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * Get PatientApiService instance
     */
    fun patientApiService(): PatientApiService {
        return createRetrofit().create(PatientApiService::class.java)
    }
    
    /**
     * Get AppointmentApiService instance
     */
    fun appointmentApiService(): AppointmentApiService {
        return createRetrofit().create(AppointmentApiService::class.java)
    }
    
    /**
     * Get DoctorApiService instance
     */
    fun doctorApiService(): DoctorApiService {
        return createRetrofit().create(DoctorApiService::class.java)
    }
    
    /**
     * Get ChatbotApiService instance
     */
    fun chatbotApiService(): ChatbotApiService {
        return createRetrofit().create(ChatbotApiService::class.java)
    }
}