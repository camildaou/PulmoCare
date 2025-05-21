package com.example.pulmocare.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.pulmocare.data.model.Patient
import com.example.pulmocare.data.repository.PatientRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "UserRepository"

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val dateOfBirth: String,
    val weight: String,
    val height: String,
    val bloodType: String = "",
    val address: String,
    val phone: String,
    val insurance: String,
    val allergies: String = "",
    val chronicConditions: String = "",
    val profileImage: String? = null
)

class UserRepository(private val context: Context? = null) {
    // In a real app, this would use SharedPreferences, DataStore, or a database
    private val currentUser = mutableStateOf<User?>(null)
    private val sessionManager by lazy { context?.let { SessionManager(it) } }
    private val patientRepository = PatientRepository()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var fetchInProgress = false

    init {
        // Check if user is already logged in via session manager
        if (context != null && sessionManager?.isLoggedIn() == true) {
            // Fetch the actual patient data from the backend
            fetchCurrentUser()
        }
    }

    // Force fetch user data from backend
    fun refreshUserData() {
        if (context != null && sessionManager?.isLoggedIn() == true) {
            fetchCurrentUser()
        }
    }

    private fun fetchCurrentUser() {
        val patientId = sessionManager?.getPatientId() ?: return
        if (fetchInProgress) return
        
        fetchInProgress = true
        Log.d(TAG, "Fetching user data for patient ID: $patientId")
        
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val result = patientRepository.getPatientById(patientId).first()
                    result.fold(
                        onSuccess = { patient -> 
                            Log.d(TAG, "Successfully fetched patient data: ${patient.firstName}")
                            setUserFromPatient(patient) 
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Failed to fetch patient data", error)
                            setDefaultUser(patientId)
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while fetching patient data", e)
                setDefaultUser(patientId)
            } finally {
                fetchInProgress = false
            }
        }
    }
    
    private fun setUserFromPatient(patient: Patient) {
        patient.id?.let { id ->
            currentUser.value = User(
                id = id,
                firstName = patient.firstName ?: "Guest",
                lastName = patient.lastName ?: "",
                email = patient.email ?: "",
                dateOfBirth = patient.age?.toString() ?: "",
                weight = patient.weight?.toString() ?: "",
                height = patient.height?.toString() ?: "",
                bloodType = patient.bloodType ?: "",
                address = patient.location ?: "",
                phone = "", // Not available in Patient model
                insurance = patient.insuranceProvider ?: "",
                allergies = patient.allergies?.joinToString(", ") ?: "",
                chronicConditions = patient.chronicConditions?.joinToString(", ") ?: "",
                profileImage = patient.photo
            )
            Log.d(TAG, "Set current user to: ${currentUser.value?.firstName} ${currentUser.value?.lastName}")
        } ?: run {
            Log.e(TAG, "Received patient with null ID, cannot set user")
        }
    }

    private fun setDefaultUser(id: String) {
        Log.d(TAG, "Setting default user with ID: $id")
        currentUser.value = User(
            id = id,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            dateOfBirth = "1990-01-01",
            weight = "70",
            height = "175",
            bloodType = "O+",
            address = "123 Main St, Anytown, USA",
            phone = "555-123-4567",
            insurance = "HealthPlus Insurance",
            allergies = "Pollen, Dust",
            chronicConditions = "Asthma"
        )
    }    

    fun isLoggedIn(): Boolean {
        return sessionManager?.isLoggedIn() ?: (currentUser.value != null)
    }

    fun getCurrentUser(): User? {
        if (currentUser.value == null && sessionManager?.isLoggedIn() == true) {
            fetchCurrentUser()
        }
        return currentUser.value
    }

    fun login(email: String, password: String, patientId: String): Boolean {
        // Save session data
        sessionManager?.saveUserLoginSession(patientId)
        
        // Fetch the actual patient data
        fetchCurrentUser()
        
        return true
    }

    fun signup(userData: Map<String, String>, patientId: String): Boolean {
        // Set temporary user data
        currentUser.value = User(
            id = patientId,
            firstName = userData["firstName"] ?: "",
            lastName = userData["lastName"] ?: "",
            email = userData["email"] ?: "",
            dateOfBirth = userData["dateOfBirth"] ?: "",
            weight = userData["weight"] ?: "",
            height = userData["height"] ?: "",
            bloodType = userData["bloodType"] ?: "",
            address = userData["address"] ?: "",
            phone = userData["phone"] ?: "",
            insurance = userData["insurance"] ?: "",
            allergies = userData["allergies"] ?: "",
            chronicConditions = userData["chronicConditions"] ?: ""
        )
        
        // Save session data
        sessionManager?.saveUserLoginSession(patientId)
        
        // Fetch the actual patient data
        fetchCurrentUser()
        
        return true
    }

    fun logout() {
        currentUser.value = null
        sessionManager?.logoutUser()
    }
}

