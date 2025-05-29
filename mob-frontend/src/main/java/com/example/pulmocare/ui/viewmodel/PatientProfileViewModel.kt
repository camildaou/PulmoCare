package com.example.pulmocare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulmocare.data.model.Patient
import com.example.pulmocare.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * View model for patient profile management
 */
class PatientProfileViewModel : ViewModel() {
    private val patientRepository = PatientRepository()
    
    // State flow for patient profile
    private val _patientState = MutableStateFlow<PatientState>(PatientState.Initial)
    val patientState: StateFlow<PatientState> = _patientState
    
    // State flow for update operation
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState
    
    /**
     * Get patient profile by ID
     */
    fun getPatientProfile(id: String) {
        _patientState.value = PatientState.Loading
        
        viewModelScope.launch {
            patientRepository.getPatientById(id)
                .catch { 
                    _patientState.value = PatientState.Error(it.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { patient ->
                            _patientState.value = PatientState.Success(patient)
                        },
                        onFailure = { error ->
                            _patientState.value = PatientState.Error(error.message ?: "Failed to load profile")
                        }
                    )
                }
        }
    }
    
    /**
     * Update patient profile
     */
    fun updatePatientProfile(id: String, updatedPatient: Patient) {
        _updateState.value = UpdateState.Loading
        
        viewModelScope.launch {
            patientRepository.updatePatient(id, updatedPatient)
                .catch { 
                    _updateState.value = UpdateState.Error(it.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { patient ->
                            _updateState.value = UpdateState.Success(patient)
                            // Also update the patient state with the latest data
                            _patientState.value = PatientState.Success(patient)
                        },
                        onFailure = { error ->
                            _updateState.value = UpdateState.Error(error.message ?: "Failed to update profile")
                        }
                    )
                }
        }
    }
    
    /**
     * Reset update state after handling the result
     */
    fun resetUpdateState() {
        _updateState.value = UpdateState.Initial
    }
    
    // Sealed classes for profile states
    sealed class PatientState {
        object Initial : PatientState()
        object Loading : PatientState()
        data class Success(val patient: Patient) : PatientState()
        data class Error(val message: String) : PatientState()
    }
    
    sealed class UpdateState {
        object Initial : UpdateState()
        object Loading : UpdateState()
        data class Success(val patient: Patient) : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
}
