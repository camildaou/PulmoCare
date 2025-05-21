package com.example.pulmocare.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulmocare.data.api.MedicalTest
import com.example.pulmocare.data.api.ParserResponse
import com.example.pulmocare.data.repository.ParserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for medical document parsing functionality
 */
class ParserViewModel : ViewModel() {
    private val parserRepository = ParserRepository()
    
    // State flows for UI updates
    private val _parsingState = MutableStateFlow<ParsingState>(ParsingState.Idle)
    val parsingState: StateFlow<ParsingState> = _parsingState
    
    private val _parsedResponse = MutableStateFlow<ParserResponse?>(null)
    val parsedResponse: StateFlow<ParserResponse?> = _parsedResponse
    
    private var analysisJob: Job? = null
    
    /**
     * Analyze a PDF document
     */
    fun analyzePdf(context: Context, pdfUri: Uri) {
        _parsingState.value = ParsingState.Loading
        
        analysisJob = viewModelScope.launch {
            try {
                parserRepository.analyzePdf(context, pdfUri).collect { result ->
                    result.fold(
                        onSuccess = { response ->
                            _parsedResponse.value = response
                            _parsingState.value = ParsingState.Success
                            Log.d(TAG, "PDF analyzed successfully with ${response.tests.size} tests")
                        },                        onFailure = { error ->
                            val errorMessage = when {
                                error.message?.contains("timeout", ignoreCase = true) == true -> 
                                    "The server is taking too long to respond. The PDF might be complex or the server is busy. Try again later."
                                error.message?.contains("connection", ignoreCase = true) == true ->
                                    "Network connection error. Please check your internet connection and try again."
                                error.message?.contains("cast", ignoreCase = true) == true ||
                                error.message?.contains("type", ignoreCase = true) == true ->
                                    "This PDF may contain an unexpected format. Please try a different medical report."
                                error.message?.contains("JSON", ignoreCase = true) == true ||
                                error.message?.contains("missing", ignoreCase = true) == true ||
                                error.message?.contains("invalid", ignoreCase = true) == true ->
                                    "Could not properly extract data from this PDF. The report format may not be supported."
                                error.message?.contains("missing required fields", ignoreCase = true) == true ->
                                    "The medical report is missing some required information. Please try another report."
                                error.message?.contains("empty", ignoreCase = true) == true ->
                                    "The server returned an empty response. The PDF may be too complex or in an unsupported format."
                                else -> "Error analyzing PDF: ${error.message ?: "Unknown error"}"
                            }
                            _parsingState.value = ParsingState.Error(errorMessage)
                            Log.e(TAG, "Error analyzing PDF: ${error.message}", error)
                        }
                    )
                }            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                        "The server is taking too long to respond. The PDF might be complex or the server is busy. Try again later."
                    e.message?.contains("connection", ignoreCase = true) == true ->
                        "Network connection error. Please check your internet connection and try again."
                    e.message?.contains("cast", ignoreCase = true) == true ||
                    e.message?.contains("type", ignoreCase = true) == true ->
                        "This PDF may contain an unexpected format. Please try a different medical report."
                    e.message?.contains("JSON", ignoreCase = true) == true ||
                    e.message?.contains("missing", ignoreCase = true) == true ||
                    e.message?.contains("invalid", ignoreCase = true) == true ->
                        "Could not properly extract data from this PDF. The report format may not be supported."
                    e.message?.contains("missing required fields", ignoreCase = true) == true ->
                        "The medical report is missing some required information. Please try another report."
                    e.message?.contains("empty", ignoreCase = true) == true ->
                        "The server returned an empty response. The PDF may be too complex or in an unsupported format."
                    e.message?.contains("canceled", ignoreCase = true) == true ||
                    e.message?.contains("cancelled", ignoreCase = true) == true ->
                        "PDF analysis was cancelled."
                    else -> "Error analyzing PDF: ${e.message ?: "Unknown error"}"
                }
                _parsingState.value = ParsingState.Error(errorMessage)
                Log.e(TAG, "Exception during PDF analysis: ${e.message}", e)
            }
        }
    }
    
    /**
     * Cancel the ongoing PDF analysis
     */
    fun cancelAnalysis() {
        analysisJob?.cancel()
        _parsingState.value = ParsingState.Idle
        Log.d(TAG, "PDF analysis cancelled by user")
    }
    
    /**
     * Reset the parsing state
     */
    fun resetState() {
        _parsingState.value = ParsingState.Idle
        _parsedResponse.value = null
    }
    
    /**
     * Sealed class representing the current state of PDF parsing
     */
    sealed class ParsingState {
        object Idle : ParsingState()
        object Loading : ParsingState()
        object Success : ParsingState()
        data class Error(val message: String) : ParsingState()
    }
    
    companion object {
        private const val TAG = "ParserViewModel"
    }
}
