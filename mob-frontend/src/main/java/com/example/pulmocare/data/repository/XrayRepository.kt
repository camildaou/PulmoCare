package com.example.pulmocare.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.pulmocare.data.api.GeminiResponse
import com.example.pulmocare.data.api.NetworkModule
import com.example.pulmocare.data.api.XrayAssessmentRequest
import com.example.pulmocare.data.api.XrayClassificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Repository for X-ray classification and assessment
 */
class XrayRepository {
    private val xrayApiService = NetworkModule.xrayApiService()
    private val geminiApiService = NetworkModule.geminiApiService()
    
    /**
     * Process X-ray image and get classification
     */
    suspend fun classifyXray(context: Context, imageUri: Uri): Flow<Result<XrayClassificationResponse>> = flow {
        try {
            // Create a temp file from the Uri
            val tempFile = createTempFileFromUri(context, imageUri)
            
            // Create multipart request
            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            
            // Make API call
            val response = xrayApiService.classifyXray(filePart)
              if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Classification failed: ${response.code()}")))
            }
            
            // Clean up temp file - ignore the return value
            tempFile.delete().let { /* Ignore Boolean result */ }
            
        } catch (e: Exception) {
            Log.e("XrayRepository", "Error classifying X-ray", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get assessment for X-ray classification
     */
    suspend fun getXrayAssessment(
        classification: String,
        patientAge: Int? = null,
        patientGender: String? = null,
        symptoms: List<String>? = null,
        additionalNotes: String? = null
    ): Flow<Result<GeminiResponse>> = flow {
        try {
            val request = XrayAssessmentRequest(
                classification = classification,
                patientAge = patientAge,
                patientGender = patientGender,
                symptoms = symptoms,
                additionalNotes = additionalNotes
            )
            
            val response = geminiApiService.generateXrayAssessment(request)
              if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Assessment failed: ${response.code()}")))
            }
        } catch (e: Exception) {
            Log.e("XrayRepository", "Error getting X-ray assessment", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Create a temporary file from a content Uri
     */
    private fun createTempFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Failed to open image input stream")
        
        val tempFile = File.createTempFile("xray_upload_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }
        
        return tempFile
    }
}
