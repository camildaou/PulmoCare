package com.example.pulmocare.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulmocare.data.api.GeminiResponse
import com.example.pulmocare.data.api.XrayClassificationResponse
import com.example.pulmocare.data.repository.XrayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class XrayViewModel : ViewModel() {
    private val xrayRepository = XrayRepository()
    
    // State flows
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _classificationResult = MutableStateFlow<XrayClassificationResponse?>(null)
    val classificationResult: StateFlow<XrayClassificationResponse?> = _classificationResult.asStateFlow()
    
    private val _assessmentResult = MutableStateFlow<GeminiResponse?>(null)
    val assessmentResult: StateFlow<GeminiResponse?> = _assessmentResult.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Upload and classify an X-ray image
     */
    fun classifyXray(context: Context, imageUri: Uri) {
        _isLoading.value = true
        _error.value = null
        _classificationResult.value = null
        _assessmentResult.value = null
        
        viewModelScope.launch {
            xrayRepository.classifyXray(context, imageUri).collectLatest { result ->
                _isLoading.value = false
                
                result.fold(
                    onSuccess = { classification ->
                        _classificationResult.value = classification
                    },
                    onFailure = { exception ->
                        _error.value = "Failed to classify X-ray: ${exception.message}"
                    }
                )
            }
        }
    }
    
    /**
     * Get an assessment for the X-ray classification
     */
//    private fun getXrayAssessment(
//        classification: String,
//        patientAge: Int? = null,
//        patientGender: String? = null,
//        symptoms: List<String>? = null,
//        additionalNotes: String? = null
//    ) {
//        _isLoading.value = true
//        _error.value = null
//
//        viewModelScope.launch {
//            xrayRepository.getXrayAssessment(
//                classification = classification,
//                patientAge = patientAge,
//                patientGender = patientGender,
//                symptoms = symptoms,
//                additionalNotes = additionalNotes
//            ).collectLatest { result ->
//                _isLoading.value = false
//
//                result.fold(
//                    onSuccess = { assessment ->
//                        _assessmentResult.value = assessment
//                    },
//                    onFailure = { exception ->
//                        _error.value = "Failed to get assessment: ${exception.message}"
//                    }
//                )
//            }
//        }
//    }
    
    /**
     * Reset the state when the user wants to upload a new X-ray
     */
    fun resetState() {
        _classificationResult.value = null
        _assessmentResult.value = null
        _error.value = null
    }
}
