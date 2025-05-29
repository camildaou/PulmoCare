package com.example.pulmocare.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulmocare.data.repository.AppointmentRepository
import com.example.pulmocare.data.SessionManager
import com.example.pulmocare.data.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel for handling appointment-related operations
 */
class AppointmentViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val appointmentRepository = AppointmentRepository(application.applicationContext)
    
    private val _appointmentsState = MutableStateFlow<AppointmentsState>(AppointmentsState.Loading)
    val appointmentsState: StateFlow<AppointmentsState> = _appointmentsState.asStateFlow()
    
    var selectedAppointment by mutableStateOf<Appointment?>(null)
        private set
    
    private val sessionManager = SessionManager(application.applicationContext)
    private val TAG = "AppointmentViewModel"
    
    init {
        loadAppointments()
    }
      /**
     * Load appointments for the current patient
     */
    fun loadAppointments() {
        viewModelScope.launch {
            _appointmentsState.value = AppointmentsState.Loading
            try {
                appointmentRepository.fetchAppointmentsForCurrentPatient()
                _appointmentsState.value = AppointmentsState.Success(
                    upcomingAppointments = appointmentRepository.upcomingAppointments,
                    pastAppointments = appointmentRepository.pastAppointments
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading appointments", e)
                _appointmentsState.value = AppointmentsState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Create a new appointment
     */
    fun createAppointment(appointment: Appointment, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            appointmentRepository.createAppointment(appointment)
                .catch { 
                    Log.e(TAG, "Error creating appointment", it)
                    onError(it.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { 
                            loadAppointments()
                            onSuccess()
                        },
                        onFailure = { error ->
                            onError(error.message ?: "Failed to create appointment")
                        }
                    )
                }
        }
    }
      /**
     * Get appointment by ID
     */
    fun getAppointmentById(id: String, onSuccess: (Appointment) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            appointmentRepository.fetchAppointmentById(id)
                .catch { 
                    Log.e(TAG, "Error getting appointment", it)
                    onError(it.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { appointment ->
                            selectedAppointment = appointment
                            onSuccess(appointment)
                        },
                        onFailure = { error ->
                            onError(error.message ?: "Failed to get appointment")
                        }
                    )
                }
        }
    }
      /**
     * Update an existing appointment
     */
    fun updateAppointment(id: String, appointment: Appointment, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            appointmentRepository.updateAppointment(id, appointment)
                .catch { 
                    Log.e(TAG, "Error updating appointment", it)
                    onError(it.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { 
                            loadAppointments()
                            onSuccess()
                        },
                        onFailure = { error ->
                            onError(error.message ?: "Failed to update appointment")
                        }
                    )
                }
        }
    }
    
    /**
     * Delete an appointment
     */
    fun deleteAppointment(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            appointmentRepository.deleteAppointment(id)
                .catch { 
                    Log.e(TAG, "Error deleting appointment", it)
                    onError(it.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { 
                            loadAppointments()
                            onSuccess()
                        },
                        onFailure = { error ->
                            onError(error.message ?: "Failed to delete appointment")
                        }
                    )
                }
        }
    }
    
    /**
     * Mark an appointment as past (completed)
     */
    fun markAppointmentAsPast(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            appointmentRepository.markAppointmentAsPast(id)
                .catch { 
                    Log.e(TAG, "Error marking appointment as past", it)
                    onError(it.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { 
                            loadAppointments()
                            onSuccess()
                        },
                        onFailure = { error ->
                            onError(error.message ?: "Failed to mark appointment as past")
                        }
                    )
                }
        }
    }
    
    /**
     * Select an appointment for viewing details
     */
    fun selectAppointment(appointment: Appointment) {
        selectedAppointment = appointment
    }
    
    /**
     * Clear the selected appointment
     */
    fun clearSelectedAppointment() {
        selectedAppointment = null
    }
}

/**
 * State for the appointments list
 */
sealed class AppointmentsState {
    object Loading : AppointmentsState()
    data class Success(val upcomingAppointments: List<Appointment>, val pastAppointments: List<Appointment>) : AppointmentsState()
    data class Error(val message: String) : AppointmentsState()
}
