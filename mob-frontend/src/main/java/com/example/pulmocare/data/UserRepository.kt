package com.example.pulmocare.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf

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

    init {
        // Check if user is already logged in via session manager
        if (context != null && sessionManager?.isLoggedIn() == true) {
            // In a real app, you would load the user data from local storage or from API
            // For now, we'll set a default user
            setDefaultUser(sessionManager!!.getPatientId())
        }
    }

    private fun setDefaultUser(id: String) {
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
    }    fun isLoggedIn(): Boolean {
        return sessionManager?.isLoggedIn() ?: (currentUser.value != null)
    }

    fun getCurrentUser(): User? {
        return currentUser.value
    }

    fun login(email: String, password: String, patientId: String): Boolean {
        // In a real app, this would validate credentials against a backend
        // For demo purposes, we'll just set the default user
        setDefaultUser(patientId)
        
        // Save session data
        sessionManager?.saveUserLoginSession(patientId)
        
        return true
    }

    fun signup(userData: Map<String, String>, patientId: String): Boolean {
        // In a real app, this would send the data to a backend
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
        
        return true
    }

    fun logout() {
        currentUser.value = null
        sessionManager?.logoutUser()
    }
}

