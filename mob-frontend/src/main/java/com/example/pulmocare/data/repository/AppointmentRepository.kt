package com.example.pulmocare.data.repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.pulmocare.data.SessionManager
import com.example.pulmocare.data.api.NetworkModule
import com.example.pulmocare.data.model.Appointment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository class for handling appointment-related operations
 */
class AppointmentRepository(private val context: Context? = null) {
    private val appointmentApiService = NetworkModule.appointmentApiService()
    private val TAG = "AppointmentRepository"
    
    // In-memory cache of appointments
    private val _upcomingAppointments = mutableStateListOf<Appointment>()
    val upcomingAppointments: List<Appointment> = _upcomingAppointments

    private val _pastAppointments = mutableStateListOf<Appointment>()
    val pastAppointments: List<Appointment> = _pastAppointments

    private val _currentAppointment = mutableStateOf<Appointment?>(null)
    val currentAppointment: Appointment? get() = _currentAppointment.value

    private val sessionManager by lazy { context?.let { SessionManager(it) } }

    /**
     * Fetch all appointments for the current patient
     */
    suspend fun fetchAppointmentsForCurrentPatient() {
        val patientId = sessionManager?.getPatientId() ?: return
        fetchAppointmentsForPatient(patientId)
    }

    /**
     * Fetch appointments for a specific patient
     */
    suspend fun fetchAppointmentsForPatient(patientId: String) {
        withContext(Dispatchers.IO) {
            try {
                val upcomingResponse = appointmentApiService.getUpcomingAppointmentsByPatientId(patientId)
                if (upcomingResponse.isSuccessful) {
                    upcomingResponse.body()?.let { appointments ->
                        _upcomingAppointments.clear()
                        _upcomingAppointments.addAll(appointments)
                    }
                } else {
                    Log.e(TAG, "Error fetching upcoming appointments: ${upcomingResponse.errorBody()?.string()}")
                }
                
                val pastResponse = appointmentApiService.getPastAppointmentsByPatientId(patientId)
                if (pastResponse.isSuccessful) {
                    pastResponse.body()?.let { appointments ->
                        _pastAppointments.clear()
                        _pastAppointments.addAll(appointments)
                    }
                } else {
                    Log.e(TAG, "Error fetching past appointments: ${pastResponse.errorBody()?.string()}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error fetching appointments", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching appointments", e)
            }
        }
    }

    /**
     * Create a new appointment
     */
    fun createAppointment(appointment: Appointment): Flow<Result<Appointment>> = flow {
        try {
            val response = appointmentApiService.createAppointment(appointment)
            if (response.isSuccessful) {
                response.body()?.let { newAppointment ->
                    // Add to local cache if it's for the current patient
                    if (newAppointment.patient?.id == sessionManager?.getPatientId()) {
                        if (newAppointment.isUpcoming) {
                            _upcomingAppointments.add(newAppointment)
                        } else {
                            _pastAppointments.add(newAppointment)
                        }
                    }
                    emit(Result.success(newAppointment))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("API error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating appointment", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Get appointment by ID
     */
    fun getAppointmentById(id: String): Flow<Result<Appointment>> = flow {
        try {
            val response = appointmentApiService.getAppointmentById(id)
            if (response.isSuccessful) {
                response.body()?.let { appointment ->
                    _currentAppointment.value = appointment
                    emit(Result.success(appointment))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("API error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching appointment with ID: $id", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Update an existing appointment
     */
    fun updateAppointment(id: String, appointment: Appointment): Flow<Result<Appointment>> = flow {
        try {
            val response = appointmentApiService.updateAppointment(id, appointment)
            if (response.isSuccessful) {
                response.body()?.let { updatedAppointment ->
                    // Update local cache
                    updateLocalAppointment(updatedAppointment)
                    emit(Result.success(updatedAppointment))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("API error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating appointment", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Delete an appointment
     */
    fun deleteAppointment(id: String): Flow<Result<Boolean>> = flow {
        try {
            val response = appointmentApiService.deleteAppointment(id)
            if (response.isSuccessful) {
                // Remove from local cache
                _upcomingAppointments.removeIf { it.id == id }
                _pastAppointments.removeIf { it.id == id }
                if (_currentAppointment.value?.id == id) {
                    _currentAppointment.value = null
                }
                emit(Result.success(true))
            } else {
                emit(Result.failure(Exception("API error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting appointment", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Mark an appointment as past (completed)
     */
    fun markAppointmentAsPast(id: String): Flow<Result<Appointment>> = flow {
        try {
            val response = appointmentApiService.markAppointmentAsPast(id)
            if (response.isSuccessful) {
                response.body()?.let { updatedAppointment ->
                    // Update local cache
                    val appointmentToMove = _upcomingAppointments.find { it.id == id }
                    appointmentToMove?.let {
                        _upcomingAppointments.remove(it)
                        _pastAppointments.add(updatedAppointment)
                    }
                    emit(Result.success(updatedAppointment))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("API error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking appointment as past", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Update a local appointment in the cache
     */
    private fun updateLocalAppointment(updatedAppointment: Appointment) {
        if (updatedAppointment.isUpcoming) {
            val index = _upcomingAppointments.indexOfFirst { it.id == updatedAppointment.id }
            if (index != -1) {
                _upcomingAppointments[index] = updatedAppointment
            } else {
                // It might have moved from past to upcoming
                _pastAppointments.removeIf { it.id == updatedAppointment.id }
                _upcomingAppointments.add(updatedAppointment)
            }
        } else {
            val index = _pastAppointments.indexOfFirst { it.id == updatedAppointment.id }
            if (index != -1) {
                _pastAppointments[index] = updatedAppointment
            } else {
                // It might have moved from upcoming to past
                _upcomingAppointments.removeIf { it.id == updatedAppointment.id }
                _pastAppointments.add(updatedAppointment)
            }
        }
        
        // Update current appointment if needed
        if (_currentAppointment.value?.id == updatedAppointment.id) {
            _currentAppointment.value = updatedAppointment
        }
    }
}
