package com.example.pulmocare.data.repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.pulmocare.data.api.NetworkModule
import com.example.pulmocare.data.model.Doctor as ModelDoctor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate

data class Doctor(
    val id: String,
    val name: String,
    val specialty: String,
    val rating: Double,
    val reviews: Int,
    val availability: List<String>,
    val location: String,
    val phone: String,
    val email: String,
    val image: String,
    val bio: String,
    val availableTimes: Map<String, List<String>>
)

class DoctorRepository(private val context: Context? = null) {
    private val TAG = "DoctorRepository"
    private val doctorApiService = NetworkModule.doctorApiService()
    
    // State to hold doctors fetched from backend
    private val _doctors = mutableStateListOf<Doctor>()
    val doctors: List<Doctor> = _doctors
    
    // State to track loading and error states
    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error = _error
    
    // Function to fetch doctors from backend
    suspend fun fetchDoctors() {
        _isLoading.value = true
        _error.value = null
        
        try {
            withContext(Dispatchers.IO) {
                val response = doctorApiService.getAllDoctors()
                if (response.isSuccessful) {
                    response.body()?.let { backendDoctors ->                        // Convert backend doctor model to UI doctor model
                        val uiDoctors = backendDoctors.map { backendDoctor ->
                            // Convert the backend doctor to our UI model
                            Doctor(
                                id = backendDoctor.id ?: "",
                                name = "Dr. ${backendDoctor.firstName.orEmpty().trim()} ${backendDoctor.lastName.orEmpty().trim()}",
                                specialty = backendDoctor.specialization.orEmpty().ifEmpty { "General Physician" },
                                rating = 4.8, // Default value or could be fetched from backend
                                reviews = 100, // Default value or could be fetched from backend
                                availability = backendDoctor.availableDays.map { day ->
                                    // Capitalize first letter and show first 3 letters
                                    day.replaceFirstChar { it.uppercase() }.take(3)
                                },
                                location = backendDoctor.location.orEmpty().ifEmpty { "Not specified" },
                                phone = backendDoctor.phone.orEmpty().ifEmpty { "Not available" },
                                email = backendDoctor.email.orEmpty().ifEmpty { "Not available" },
                                image = backendDoctor.photo ?: "https://via.placeholder.com/100",
                                bio = backendDoctor.description.orEmpty().ifEmpty { "No description available" },
                                availableTimes = backendDoctor.availableTimeSlots.mapValues { (_, timeSlots) ->
                                    timeSlots.map { slot -> "${slot.startTime} - ${slot.endTime}" }
                                }
                            )
                        }
                        
                        // Update the doctors list
                        _doctors.clear()
                        _doctors.addAll(uiDoctors)
                    }
                } else {
                    // Handle error response
                    Log.e(TAG, "Error fetching doctors: ${response.errorBody()?.string()}")
                    _error.value = "Failed to fetch doctors: ${response.code()}"
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching doctors", e)
            _error.value = "Network error: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching doctors", e)
            _error.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    // Function to get a doctor by ID
    suspend fun fetchDoctorById(id: String): Doctor? {
        _isLoading.value = true
        _error.value = null
        
        try {
            return withContext(Dispatchers.IO) {
                val response = doctorApiService.getDoctorById(id)
                if (response.isSuccessful) {
                    response.body()?.let { backendDoctor ->                        // Convert the backend doctor to our UI model
                        Doctor(
                            id = backendDoctor.id ?: "",
                            name = "Dr. ${backendDoctor.firstName.orEmpty().trim()} ${backendDoctor.lastName.orEmpty().trim()}",
                            specialty = backendDoctor.specialization.orEmpty().ifEmpty { "General Physician" },
                            rating = 4.8, // Default value or could be fetched from backend
                            reviews = 100, // Default value or could be fetched from backend
                            availability = backendDoctor.availableDays.map { day ->
                                // Capitalize first letter and show first 3 letters
                                day.replaceFirstChar { it.uppercase() }.take(3)
                            },
                            location = backendDoctor.location.orEmpty().ifEmpty { "Not specified" },
                            phone = backendDoctor.phone.orEmpty().ifEmpty { "Not available" },
                            email = backendDoctor.email.orEmpty().ifEmpty { "Not available" },
                            image = backendDoctor.photo ?: "https://via.placeholder.com/100",
                            bio = backendDoctor.description.orEmpty().ifEmpty { "No description available" },
                            availableTimes = backendDoctor.availableTimeSlots.mapValues { (_, timeSlots) ->
                                timeSlots.map { slot -> "${slot.startTime} - ${slot.endTime}" }
                            }
                        )
                    }
                } else {
                    // Handle error response
                    Log.e(TAG, "Error fetching doctor by ID: ${response.errorBody()?.string()}")
                    _error.value = "Failed to fetch doctor: ${response.code()}"
                    null
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching doctor by ID", e)
            _error.value = "Network error: ${e.message}"
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching doctor by ID", e)
            _error.value = "Error: ${e.message}"
            return null
        } finally {
            _isLoading.value = false
        }
    }
    
    // Get a doctor by ID from the local cache
    fun getDoctorById(id: String): Doctor? {
        return doctors.find { it.id == id }
    }
    /**
 * Convert repository Doctor objects to model Doctor objects
 * Used for compatibility with appointment scheduling
 */
fun getModelDoctors(): List<ModelDoctor> {
    return doctors.map { repoDoctor ->
        ModelDoctor(
            id = repoDoctor.id,
            firstName = repoDoctor.name.split(" ").firstOrNull() ?: "",
            lastName = repoDoctor.name.split(" ").drop(1).joinToString(" "),
            specialization = repoDoctor.specialty,
            location = repoDoctor.location,
            email = repoDoctor.email,
            phone = repoDoctor.phone,
            description = repoDoctor.bio,
            photo = repoDoctor.image,
            availableDays = repoDoctor.availability,
            // Convert the string-based time slots to the TimeSlot model
            availableTimeSlots = repoDoctor.availableTimes.mapValues { (_, times) ->
                times.map { timeString ->
                    val parts = timeString.split(" - ")
                    if (parts.size == 2) {
                        ModelDoctor.TimeSlot(startTime = parts[0], endTime = parts[1])
                    } else {
                        // Default to a 30-minute slot if format is unexpected
                        ModelDoctor.TimeSlot(startTime = timeString, endTime = timeString)
                    }
                }
            }
        )
    }
}
    
    // Function to get available appointment times for a doctor on a specific date
    fun getAvailableTimesForDoctor(doctorId: String, date: String): List<String> {
        val doctor = getDoctorById(doctorId) ?: return emptyList()
        
        // Convert date to day of week (this is simplified, in a real app you'd parse the date)
        val dayOfWeek = when {
            date.contains("Mon") -> "Mon"
            date.contains("Tue") -> "Tue"
            date.contains("Wed") -> "Wed"
            date.contains("Thu") -> "Thu"
            date.contains("Fri") -> "Fri"
            date.contains("Sat") -> "Sat"
            date.contains("Sun") -> "Sun"
            else -> return emptyList()
        }
        
        // Get available times for that day
        return doctor.availableTimes[dayOfWeek] ?: emptyList()
    }
      // Function to convert between backend Doctor model and UI Doctor model if needed
    fun convertToModelDoctor(doctor: Doctor): ModelDoctor {
        // Split the name into first and last name
        val nameParts = doctor.name.split(" ")
        
        return ModelDoctor(
            id = doctor.id,
            firstName = nameParts.firstOrNull() ?: "",
            lastName = nameParts.drop(1).joinToString(" "),
            specialization = doctor.specialty,
            location = doctor.location,
            description = doctor.bio,
            email = doctor.email,
            phone = doctor.phone,
            photo = doctor.image,
            availableDays = doctor.availability,
            // Convert the string-based time slots to the TimeSlot model
            availableTimeSlots = doctor.availableTimes.mapValues { (_, times) ->
                times.map { timeString ->
                    val parts = timeString.split(" - ")
                    if (parts.size == 2) {
                        ModelDoctor.TimeSlot(startTime = parts[0], endTime = parts[1])
                    } else {
                        // Default to a 30-minute slot if format is unexpected
                        ModelDoctor.TimeSlot(startTime = timeString, endTime = timeString)
                    }
                }
            }
        )
    }
    
    // Method to get doctor availability
    suspend fun getDoctorAvailability(doctorId: String) {
        _isLoading.value = true
        _error.value = null
        
        try {
            withContext(Dispatchers.IO) {
                val response = doctorApiService.getDoctorAvailability(doctorId)
                if (response.isSuccessful) {
                    response.body()?.let { availabilityData ->
                        // Process availability data if needed
                        Log.d(TAG, "Successfully fetched doctor availability")
                    }
                } else {
                    // Handle error response
                    Log.e(TAG, "Error fetching doctor availability: ${response.errorBody()?.string()}")
                    _error.value = "Failed to fetch doctor availability: ${response.code()}"
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching doctor availability", e)
            _error.value = "Network error: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching doctor availability", e)
            _error.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    // Method to set standard schedule for a doctor
    suspend fun setStandardSchedule(doctorId: String, workDays: List<String>, workHours: Map<String, String>) {
        _isLoading.value = true
        _error.value = null
        
        try {
            withContext(Dispatchers.IO) {
                val scheduleDetails = mapOf(
                    "workDays" to workDays,
                    "workHours" to workHours
                )
                
                val response = doctorApiService.setStandardSchedule(doctorId, scheduleDetails)
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully set standard schedule for doctor")
                } else {
                    // Handle error response
                    Log.e(TAG, "Error setting standard schedule: ${response.errorBody()?.string()}")
                    _error.value = "Failed to set standard schedule: ${response.code()}"
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error setting standard schedule", e)
            _error.value = "Network error: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "Error setting standard schedule", e)
            _error.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    // Method to add a time slot for a doctor
    suspend fun addTimeSlot(doctorId: String, day: String, startTime: String, endTime: String) {
        _isLoading.value = true
        _error.value = null
        
        try {
            withContext(Dispatchers.IO) {
                val timeSlotDetails = mapOf(
                    "day" to day,
                    "startTime" to startTime,
                    "endTime" to endTime
                )
                
                val response = doctorApiService.addTimeSlot(doctorId, timeSlotDetails)
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully added time slot for doctor")
                } else {
                    // Handle error response
                    Log.e(TAG, "Error adding time slot: ${response.errorBody()?.string()}")
                    _error.value = "Failed to add time slot: ${response.code()}"
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error adding time slot", e)
            _error.value = "Network error: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "Error adding time slot", e)
            _error.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
      // Method to remove a time slot for a doctor
    suspend fun removeTimeSlot(doctorId: String, day: String, startTime: String) {
        _isLoading.value = true
        _error.value = null
        
        try {
            withContext(Dispatchers.IO) {
                val response = doctorApiService.removeTimeSlot(doctorId, day, startTime)
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully removed time slot for doctor")
                } else {
                    // Handle error response
                    Log.e(TAG, "Error removing time slot: ${response.errorBody()?.string()}")
                    _error.value = "Failed to remove time slot: ${response.code()}"
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error removing time slot", e)
            _error.value = "Network error: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "Error removing time slot", e)
            _error.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
      /**
     * Refresh all doctors data from the backend
     * This should be called after creating an appointment to get updated availability
     */
    suspend fun fetchAllDoctors() {
        return fetchDoctors()
    }
    
    /**
     * Get available time slots for a doctor on a specific date
     * This method fetches 30-minute time slots directly from the backend
     */
    suspend fun getTimeSlotsByDoctorAndDay(doctorId: String, date: LocalDate): List<String> {
        _isLoading.value = true
        val result = mutableListOf<String>()
        
        try {
            val dayOfWeek = date.dayOfWeek.toString().toLowerCase().substring(0, 3)
            Log.d(TAG, "Fetching time slots for doctor $doctorId on $date ($dayOfWeek)")
            
            val response = doctorApiService.getDoctorAvailability(doctorId)
            if (response.isSuccessful && response.body() != null) {
                val availabilityData = response.body()!!
                
                @Suppress("UNCHECKED_CAST")
                val availableTimeSlots = availabilityData["availableTimeSlots"] as? Map<String, List<Map<String, String>>>
                
                if (availableTimeSlots != null) {
                    val dayTimeSlots = availableTimeSlots[dayOfWeek] ?: emptyList()
                    
                    // Convert the backend format to our display format
                    for (slot in dayTimeSlots) {
                        val startTime = slot["startTime"]
                        val endTime = slot["endTime"]
                        if (startTime != null && endTime != null) {
                            result.add("$startTime - $endTime")
                        }
                    }
                    
                    Log.d(TAG, "Retrieved ${result.size} time slots for $dayOfWeek: $result")
                }
            } else {
                Log.e(TAG, "Error fetching time slots: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching time slots", e)
        } finally {
            _isLoading.value = false
        }
        
        return result
    }
}
