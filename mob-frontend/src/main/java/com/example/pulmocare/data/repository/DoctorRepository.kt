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
                    response.body()?.let { backendDoctors ->
                        // Convert backend doctor model to UI doctor model
                        val uiDoctors = backendDoctors.map { backendDoctor ->
                            // Convert the backend doctor to our UI model
                            Doctor(
                                id = backendDoctor.id ?: "",
                                name = "Dr. ${backendDoctor.firstName.orEmpty().trim()} ${backendDoctor.lastName.orEmpty().trim()}",
                                specialty = backendDoctor.specialization.orEmpty().ifEmpty { "General Physician" },
                                rating = 4.8, // Default value or could be fetched from backend
                                reviews = 100, // Default value or could be fetched from backend
                                availability = listOf("Mon", "Wed", "Fri"), // Default value or could be fetched from backend
                                location = backendDoctor.location.orEmpty().ifEmpty { "Not specified" },
                                phone = backendDoctor.phone.orEmpty().ifEmpty { "Not available" },
                                email = backendDoctor.email.orEmpty().ifEmpty { "Not available" },
                                image = backendDoctor.photo ?: "https://via.placeholder.com/100",
                                bio = backendDoctor.description.orEmpty().ifEmpty { "No description available" },
                                availableTimes = mapOf( // Default value or could be fetched from backend
                                    "Mon" to listOf("9:00 AM", "10:00 AM", "2:00 PM", "3:00 PM"),
                                    "Wed" to listOf("10:00 AM", "11:00 AM", "1:00 PM", "4:00 PM"),
                                    "Fri" to listOf("9:00 AM", "11:00 AM", "2:00 PM", "3:00 PM")
                                )
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
                    response.body()?.let { backendDoctor ->
                        // Convert the backend doctor to our UI model
                        Doctor(
                            id = backendDoctor.id ?: "",
                            name = "Dr. ${backendDoctor.firstName.orEmpty().trim()} ${backendDoctor.lastName.orEmpty().trim()}",
                            specialty = backendDoctor.specialization.orEmpty().ifEmpty { "General Physician" },
                            rating = 4.8, // Default value or could be fetched from backend
                            reviews = 100, // Default value or could be fetched from backend
                            availability = listOf("Mon", "Wed", "Fri"), // Default value or could be fetched from backend
                            location = backendDoctor.location.orEmpty().ifEmpty { "Not specified" },
                            phone = backendDoctor.phone.orEmpty().ifEmpty { "Not available" },
                            email = backendDoctor.email.orEmpty().ifEmpty { "Not available" },
                            image = backendDoctor.photo ?: "https://via.placeholder.com/100",
                            bio = backendDoctor.description.orEmpty().ifEmpty { "No description available" },
                            availableTimes = mapOf( // Default value or could be fetched from backend
                                "Mon" to listOf("9:00 AM", "10:00 AM", "2:00 PM", "3:00 PM"),
                                "Wed" to listOf("10:00 AM", "11:00 AM", "1:00 PM", "4:00 PM"),
                                "Fri" to listOf("9:00 AM", "11:00 AM", "2:00 PM", "3:00 PM")
                            )
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
        // Split the name safely with null handling
        val nameParts = doctor.name.split(" ")
        
        return ModelDoctor(
            id = doctor.id,
            firstName = nameParts.getOrElse(1) { "" }, // Assuming format "Dr. FirstName LastName"
            lastName = nameParts.getOrElse(2) { "" },
            specialization = doctor.specialty,
            location = doctor.location,
            description = doctor.bio,
            email = doctor.email,
            phone = doctor.phone,
            photo = doctor.image
        )
    }
}
