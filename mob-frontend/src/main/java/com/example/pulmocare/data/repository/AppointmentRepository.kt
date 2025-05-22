package com.example.pulmocare.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale

/**
 * Repository class for handling appointment-related operations
 */
class AppointmentRepository(private val context: Context? = null) {
    private val appointmentApiService = NetworkModule.appointmentApiService()
    private val doctorApiService = NetworkModule.doctorApiService()
    private val TAG = "AppointmentRepository"
    
    // In-memory cache of appointments
    private val _upcomingAppointments = mutableStateListOf<Appointment>()
    val upcomingAppointments: List<Appointment> = _upcomingAppointments

    private val _pastAppointments = mutableStateListOf<Appointment>()
    val pastAppointments: List<Appointment> = _pastAppointments

    private val _currentAppointment = mutableStateOf<Appointment?>(null)
    val currentAppointment: Appointment? get() = _currentAppointment.value

    // In-memory cache for doctor availability
    private val _availableTimeSlots = mutableStateListOf<Doctor.TimeSlot>()
    val availableTimeSlots: List<Doctor.TimeSlot> = _availableTimeSlots

    private val sessionManager by lazy { context?.let { SessionManager(it) } }
    
    // In-memory cache of COPD questionnaires
    private val _copdQuestionnaires = mutableStateListOf<COPDQuestionnaire>()
    val copdQuestionnaires: List<COPDQuestionnaire> = _copdQuestionnaires    /**
     * Fetch all appointments for the current patient
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchAppointmentsForCurrentPatient() {
        val patientId = sessionManager?.getPatientId() ?: return
        fetchAppointmentsForPatient(patientId)
    }

    /**
     * Fetch appointments for a specific patient
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchAppointmentsForPatient(patientId: String) {
        withContext(Dispatchers.IO) {
            try {
                val upcomingResponse = appointmentApiService.getUpcomingAppointmentsByPatientId(patientId)
                if (upcomingResponse.isSuccessful) {
                    upcomingResponse.body()?.let { appointments ->
                        // Check if any "upcoming" appointments are actually in the past
                        val currentDate = LocalDate.now()
                        val currentTime = LocalTime.now()
                        
                        val (actuallyUpcoming, shouldBePast) = appointments.partition { appointment ->
                            val appointmentDate = LocalDate.parse(appointment.date)
                            
                            when {
                                appointmentDate.isAfter(currentDate) -> true // Future date
                                appointmentDate.isBefore(currentDate) -> false // Past date
                                else -> { // Same date, check time
                                    try {
                                        val appointmentTime = LocalTime.parse(appointment.hour)
                                        appointmentTime.isAfter(currentTime)
                                    } catch (e: Exception) {
                                        // If we can't parse the time, assume it's upcoming
                                        true
                                    }
                                }
                            }
                        }
                        
                        // Update the actually upcoming appointments
                        _upcomingAppointments.clear()
                        _upcomingAppointments.addAll(actuallyUpcoming)
                        
                        // Mark appointments that should be past as past
                        shouldBePast.forEach { appointment ->
                            try {
                                markAppointmentAsPast(appointment.id!!)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error marking appointment ${appointment.id} as past", e)
                            }
                        }
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
     * Create a new appointment with time slot validation
     */
    fun createAppointmentWithValidation(appointment: Appointment): Flow<Result<Appointment>> = flow {
        try {
            // First validate the appointment time slot
            validateAppointmentTimeSlot(appointment).collect { validationResult ->
                if (validationResult.isSuccess && validationResult.getOrNull() == true) {
                    // Time slot is available, proceed with creating the appointment
                    createAppointment(appointment).collect { creationResult ->
                        emit(creationResult)
                    }
                } else if (validationResult.isSuccess) {
                    // Time slot is not available
                    emit(Result.failure(IllegalArgumentException("The selected time slot is not available")))
                } else {
                    // Validation failed with an error
                    emit(Result.failure(validationResult.exceptionOrNull() ?: Exception("Unknown error during validation")))
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
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
     * Update an appointment with time slot validation
     * This checks if the new time slot is available before updating
     */
    fun updateAppointmentWithValidation(id: String, appointment: Appointment): Flow<Result<Appointment>> = flow {
        try {
            // Validate the new appointment time slot only if date or hour has changed
            val currentAppointment = _upcomingAppointments.find { it.id == id } ?: _pastAppointments.find { it.id == id }
            
            if (currentAppointment != null && 
                (currentAppointment.date != appointment.date || currentAppointment.hour != appointment.hour)) {
                // Date or time has changed, validate the new slot
                validateAppointmentTimeSlot(appointment).collect { validationResult ->
                    if (validationResult.isSuccess && validationResult.getOrNull() == true) {
                        // Time slot is available, proceed with updating the appointment
                        updateAppointment(id, appointment).collect { updateResult ->
                            emit(updateResult)
                        }
                    } else if (validationResult.isSuccess) {
                        // Time slot is not available
                        emit(Result.failure(IllegalArgumentException("The selected time slot is not available")))
                    } else {
                        // Validation failed with an error
                        emit(Result.failure(validationResult.exceptionOrNull() ?: Exception("Unknown error during validation")))
                    }
                }
            } else {
                // No change in date/time or appointment not found locally, proceed with update without validation
                updateAppointment(id, appointment).collect { updateResult ->
                    emit(updateResult)
                }
            }
        } catch (e: Exception) {
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
@RequiresApi(Build.VERSION_CODES.O)
suspend fun scheduleAppointment(doctor: Doctor, date: String, time: String, reason: String): Result<Appointment> {
    return withContext(Dispatchers.IO) {
        try {
            // Validate appointment is in the future
            val selectedDate = LocalDate.parse(date)
            val currentDate = LocalDate.now()
            
            // Extract start time and end time from the time slot string
            val timeParts = time.split(" - ")
            val startTime = timeParts[0].trim()
            
            // Validate time is in the future if date is today
            if (selectedDate.isEqual(currentDate)) {
                val currentTime = LocalTime.now()
                val appointmentTime = LocalTime.parse(startTime)
                
                if (appointmentTime.isBefore(currentTime)) {
                    return@withContext Result.failure(RuntimeException("Cannot schedule an appointment in the past. Please select a future time."))
                }
            } else if (selectedDate.isBefore(currentDate)) {
                return@withContext Result.failure(RuntimeException("Cannot schedule an appointment for a past date. Please select a future date."))
            }
            
            // Calculate end time if it's not in the format or add proper formatting if it is
            val endTime = if (timeParts.size > 1) {
                timeParts[1].trim()
            } else {
                // Calculate end time (30 minutes after start time)
                val hourParts = startTime.split(":")
                if (hourParts.size == 2) {
                    val hourInt = hourParts[0].toInt()
                    val minuteInt = hourParts[1].toInt()
                    
                    val endHour = if (minuteInt + 30 >= 60) hourInt + 1 else hourInt
                    val endMinute = (minuteInt + 30) % 60
                    String.format("%02d:%02d", endHour, endMinute)
                } else {
                    // Default to 30 minutes later if parsing fails
                    startTime
                }
            }
            
            Log.d("AppointmentRepository", "Scheduling appointment:")
            Log.d("AppointmentRepository", "- Doctor ID: ${doctor.id}")
            Log.d("AppointmentRepository", "- Date: $date")
            Log.d("AppointmentRepository", "- Time slot: $time")
            Log.d("AppointmentRepository", "- Start time: $startTime")
            Log.d("AppointmentRepository", "- End time: $endTime")
              // Create a simplified doctor object with only the ID to avoid validation issues
            val appointment = Appointment(
                date = date,
                hour = startTime,
                endTime = endTime, // Add end time to appointment
                patient = Patient(id = sessionManager?.getPatientId()),
                doctor = Doctor(id = doctor.id),
                reason = reason,
                location = doctor.location // Include the doctor's location
            )
            
            val response = appointmentApiService.createAppointment(appointment)
            if (response.isSuccessful && response.body() != null) {
                // Refresh local appointment data
                fetchAppointmentsForCurrentPatient()
                Log.d("AppointmentRepository", "Appointment scheduled successfully: ${response.body()?.id}")
                
                // Remove the booked time slot from the local cache to prevent double booking
                // Note: The proper solution is to refresh from the backend, which we do in the UI
                
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("AppointmentRepository", "Error scheduling appointment: $errorBody")
                Result.failure(RuntimeException("Failed to schedule appointment: $errorBody"))
            }        
        } catch (e: Exception) {
            Log.e("AppointmentRepository", "Error scheduling appointment", e)
            Result.failure(e)
        }
    }
}

    /**
     * Validate appointment time slot against doctor availability before creating
     * This should be called before creating an appointment
     */
    suspend fun validateAppointmentTimeSlot(appointment: Appointment): Flow<Result<Boolean>> = flow {
        try {
            // Extract doctor ID, date, and time from appointment
            val doctorId = appointment.doctor?.id ?: throw IllegalArgumentException("Doctor ID is required")
            val date = appointment.date
            val hour = appointment.hour
            
            // Calculate end time (30 minutes after start time)
            val hourParts = hour.split(":")
            val hourInt = hourParts[0].toInt()
            val minuteInt = hourParts[1].toInt()
            
            val endHour = if (minuteInt + 30 >= 60) hourInt + 1 else hourInt
            val endMinute = (minuteInt + 30) % 60
            val endTime = String.format("%02d:%02d", endHour, endMinute)
            
            // Check availability
            checkTimeSlotAvailability(doctorId, date, hour, endTime).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Check if a specific time slot is available for a doctor on a date
     */
    suspend fun checkTimeSlotAvailability(doctorId: String, date: String, startTime: String, endTime: String): Flow<Result<Boolean>> = flow {
        try {
            val response = appointmentApiService.checkTimeSlotAvailability(doctorId, date, startTime, endTime)
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    val isAvailable = data["available"] as Boolean
                    emit(Result.success(isAvailable))
                } ?: emit(Result.failure(IOException("No data received")))
            } else {
                emit(Result.failure(IOException("Error checking time slot availability: ${response.errorBody()?.string()}")))
            }
        } catch (e: IOException) {
            emit(Result.failure(IOException("Network error checking time slot availability", e)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }    /**
     * Get available time slots for a doctor on a specific date
     */
    suspend fun getAvailableTimeSlots(doctorId: String, date: LocalDate): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val dateStr = date.toString() // Convert LocalDate to String in ISO format (YYYY-MM-DD)
                Log.d(TAG, "Fetching available time slots for doctor $doctorId on date $dateStr")
                val response = appointmentApiService.getAvailableTimeSlots(doctorId, dateStr)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    // The API returns different formats, so we need to handle both possibilities
                    @Suppress("UNCHECKED_CAST")
                    val availableSlotsRaw = responseBody["availableSlots"]
                    
                    val result = when (availableSlotsRaw) {
                        // Handle list of strings format
                        is List<*> -> {
                            if (availableSlotsRaw.isEmpty()) {
                                emptyList()
                            } else if (availableSlotsRaw[0] is String) {
                                availableSlotsRaw as List<String>
                            } else {
                                // Handle list of maps format
                                (availableSlotsRaw as List<Map<String, String>>).map { slot ->
                                    "${slot["startTime"]} - ${slot["endTime"]}"
                                }
                            }
                        }
                        else -> emptyList()
                    }
                    
                    Log.d(TAG, "Retrieved ${result.size} available time slots: $result")
                    return@withContext result
                }
                
                Log.e(TAG, "Error getting available time slots: ${response.errorBody()?.string()}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting available time slots", e)
                emptyList()
            }
        }
    }

    /**
     * Get available time slots for a specific doctor and date
     */
    suspend fun getDoctorAvailableTimeSlots(doctorId: String, date: String): Flow<Result<List<Map<String, String>>>> = flow {
        try {
            val response = appointmentApiService.getAvailableTimeSlots(doctorId, date)
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val availableSlots = responseBody["availableSlots"] as? List<Map<String, String>> ?: emptyList()
                    emit(Result.success(availableSlots))
                } ?: emit(Result.success(emptyList()))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching time slots: $errorMessage")
                emit(Result.failure(IOException("Failed to fetch time slots: $errorMessage")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching available time slots", e)
            emit(Result.failure(e))
        }
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
    // Add this method to generate 30-minute time slots from a doctor's availability
private fun generateTimeSlots(timeSlots: List<Doctor.TimeSlot>): List<String> {
    val slots = mutableListOf<String>()
    
    timeSlots.forEach { slot ->
        // Parse start and end times
        val startParts = slot.startTime.split(":")
        val endParts = slot.endTime.split(":")
        
        if (startParts.size == 2 && endParts.size == 2) {
            val startHour = startParts[0].toIntOrNull() ?: return@forEach
            val startMinute = startParts[1].toIntOrNull() ?: return@forEach
            val endHour = endParts[0].toIntOrNull() ?: return@forEach
            val endMinute = endParts[1].toIntOrNull() ?: return@forEach
            
            // Calculate total minutes for start and end
            val startTotalMinutes = startHour * 60 + startMinute
            val endTotalMinutes = endHour * 60 + endMinute
            
            // Generate 30-minute slots
            var currentMinutes = startTotalMinutes
            while (currentMinutes < endTotalMinutes) {
                val hour = currentMinutes / 60
                val minute = currentMinutes % 60
                slots.add(String.format("%02d:%02d", hour, minute))
                currentMinutes += 30
            }
        }
    }
    
    return slots
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
