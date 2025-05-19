package com.example.pulmocare.data.repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.pulmocare.data.SessionManager
import com.example.pulmocare.data.api.NetworkModule
import com.example.pulmocare.data.model.Appointment
import com.example.pulmocare.data.model.COPDQuestionnaire
import com.example.pulmocare.data.model.Doctor
import com.example.pulmocare.data.model.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    // In-memory cache of COPD questionnaires
    private val _copdQuestionnaires = mutableStateListOf<COPDQuestionnaire>()
    val copdQuestionnaires: List<COPDQuestionnaire> = _copdQuestionnaires

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
    }    /**
     * Get appointment by ID from API
     */
    fun fetchAppointmentById(id: String): Flow<Result<Appointment>> = flow {
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
     * Schedule a new appointment
     */
    suspend fun scheduleAppointment(
        doctorId: String,
        date: String,
        time: String,
        reason: String
    ): Appointment? {
        val patientId = sessionManager?.getPatientId() ?: return null
        
        // Get doctor info to include location
        val doctorInfo = getDoctorInfo(doctorId)
        
        // Create appointment object
        val newAppointment = Appointment(
            date = date,
            hour = time,
            doctor = Doctor(id = doctorId),
            patient = Patient(id = patientId),
            reason = reason,
            location = doctorInfo?.location ?: "Clinic",
            isUpcoming = true
        )
        
        // Call the API to create the appointment
        var createdAppointment: Appointment? = null
        try {
            withContext(Dispatchers.IO) {
                val response = appointmentApiService.createAppointment(newAppointment)
                if (response.isSuccessful) {
                    response.body()?.let { appointment ->
                        // Add to local cache
                        _upcomingAppointments.add(appointment)
                        createdAppointment = appointment
                    }
                } else {
                    Log.e(TAG, "Error scheduling appointment: ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling appointment", e)
        }
        
        return createdAppointment
    }
      /**
     * Get doctor information by ID
     * This helper method is used to get doctor details when scheduling an appointment
     */
    private suspend fun getDoctorInfo(doctorId: String): com.example.pulmocare.data.model.Doctor? {
        try {
            return withContext(Dispatchers.IO) {
                val response = NetworkModule.doctorApiService().getDoctorById(doctorId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching doctor info", e)
            return null
        }
    }
    
    /**
     * Cancel an appointment by ID
     */
    suspend fun cancelAppointment(appointmentId: String): Boolean {
        return try {
            var success = false
            withContext(Dispatchers.IO) {
                val response = appointmentApiService.deleteAppointment(appointmentId)
                if (response.isSuccessful) {
                    // Remove from local cache
                    _upcomingAppointments.removeIf { it.id == appointmentId }
                    _pastAppointments.removeIf { it.id == appointmentId }
                    if (_currentAppointment.value?.id == appointmentId) {
                        _currentAppointment.value = null
                    }
                    success = true
                } else {
                    Log.e(TAG, "Error cancelling appointment: ${response.errorBody()?.string()}")
                }
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling appointment", e)
            false
        }
    }
    
    /**
     * Reschedule an existing appointment
     */
    suspend fun rescheduleAppointment(
        appointmentId: String,
        newDate: String,
        newTime: String
    ): Appointment? {
        // Find the appointment to reschedule
        val existingAppointment = _upcomingAppointments.find { it.id == appointmentId }
            ?: return null
            
        // Create updated appointment object
        val updatedAppointment = existingAppointment.copy(
            date = newDate,
            hour = newTime
        )
        
        // Call the API to update the appointment
        var rescheduledAppointment: Appointment? = null
        try {
            withContext(Dispatchers.IO) {
                val response = appointmentApiService.updateAppointment(appointmentId, updatedAppointment)
                if (response.isSuccessful) {
                    response.body()?.let { appointment ->
                        // Update in local cache
                        updateLocalAppointment(appointment)
                        rescheduledAppointment = appointment
                    }
                } else {
                    Log.e(TAG, "Error rescheduling appointment: ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling appointment", e)
        }
        
        return rescheduledAppointment
    }
      /**
     * Find an appointment by ID from local cache
     */
    fun findAppointmentById(id: String): Appointment? {
        return _upcomingAppointments.find { it.id == id }
            ?: _pastAppointments.find { it.id == id }
            ?: _currentAppointment.value?.takeIf { it.id == id }
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

    /**
     * Save a COPD questionnaire for a specific appointment
     */
    fun saveCOPDQuestionnaire(
        appointmentId: String,
        breathlessness: Int,
        coughing: Int,
        sputumProduction: Int,
        chestTightness: Int,
        activityLimitation: Int,
        confidence: Int,
        sleepQuality: Int,
        energy: Int
    ): COPDQuestionnaire {
        val currentDate = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
        
        val questionnaire = COPDQuestionnaire(
            appointmentId = appointmentId,
            date = currentDate,
            breathlessness = breathlessness,
            coughing = coughing,
            sputumProduction = sputumProduction,
            chestTightness = chestTightness,
            activityLimitation = activityLimitation,
            confidence = confidence,
            sleepQuality = sleepQuality,
            energy = energy
        )
        
        // Add to local cache
        _copdQuestionnaires.add(questionnaire)
        
        // TODO: In a real application, this would make an API call to persist the questionnaire
        
        return questionnaire
    }
    
    /**
     * Get a COPD questionnaire by appointment ID
     */
    fun getCOPDQuestionnaireByAppointmentId(appointmentId: String): COPDQuestionnaire? {
        return _copdQuestionnaires.find { it.appointmentId == appointmentId }
    }
    
    /**
     * Get all COPD questionnaires for the current patient
     */
    fun getAllCOPDQuestionnaires(): List<COPDQuestionnaire> {
        val patientId = sessionManager?.getPatientId() ?: return emptyList()
        
        // Get all appointments for this patient
        val allAppointments = _upcomingAppointments + _pastAppointments
        val patientAppointmentIds = allAppointments
            .filter { it.patient?.id == patientId }
            .mapNotNull { it.id }
            
        // Return questionnaires for this patient's appointments
        return _copdQuestionnaires.filter { it.appointmentId in patientAppointmentIds }
    }
}
