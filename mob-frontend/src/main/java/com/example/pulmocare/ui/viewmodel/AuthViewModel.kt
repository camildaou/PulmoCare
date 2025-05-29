package com.example.pulmocare.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulmocare.data.SessionManager
import com.example.pulmocare.data.model.Patient
import com.example.pulmocare.data.model.PatientSignup
import com.example.pulmocare.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * View model for authentication operations like sign in and sign up
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val patientRepository = PatientRepository()
    private val sessionManager = SessionManager(application.applicationContext)
    
    // State flows for sign in
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Initial)
    val signInState: StateFlow<SignInState> = _signInState
    
    // State flows for sign up
    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Initial)
    val signUpState: StateFlow<SignUpState> = _signUpState
      /**
     * Sign in a patient
     */
    fun signIn(email: String, password: String) {
        _signInState.value = SignInState.Loading
        
        viewModelScope.launch {
            patientRepository.signIn(email, password)
                .catch { 
                    _signInState.value = SignInState.Error(it.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { patient ->
                            // Save authenticated session
                            patient.id?.let { patientId ->
                                sessionManager.saveUserLoginSession(patientId)
                            }
                            _signInState.value = SignInState.Success(patient)
                        },
                        onFailure = { error ->
                            _signInState.value = SignInState.Error(error.message ?: "Authentication failed")
                        }
                    )
                }
        }
    }
      /**
     * Sign up a new patient
     */
    fun signUp(patientSignup: PatientSignup) {
        _signUpState.value = SignUpState.Loading
        
        viewModelScope.launch {
            patientRepository.signUp(patientSignup)
                .catch { 
                    _signUpState.value = SignUpState.Error(it.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { patient ->
                            // Save authenticated session
                            patient.id?.let { patientId ->
                                sessionManager.saveUserLoginSession(patientId)
                            }
                            _signUpState.value = SignUpState.Success(patient)
                        },
                        onFailure = { error ->
                            _signUpState.value = SignUpState.Error(error.message ?: "Registration failed")
                        }
                    )
                }
        }
    }
      /**
     * Reset sign in state (e.g., when navigating away)
     */
    fun resetSignInState() {
        _signInState.value = SignInState.Initial
    }
    
    /**
     * Reset sign up state (e.g., when navigating away)
     */
    fun resetSignUpState() {
        _signUpState.value = SignUpState.Initial
    }
    
    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        return sessionManager.isLoggedIn()
    }
    
    /**
     * Get current patient ID
     */
    fun getCurrentPatientId(): String {
        return sessionManager.getPatientId()
    }
    
    /**
     * Logout and clear authentication
     */
    fun logout() {
        sessionManager.logoutUser()
    }
    
    // Sealed classes for auth states
    sealed class SignInState {
        object Initial : SignInState()
        object Loading : SignInState()
        data class Success(val patient: Patient) : SignInState()
        data class Error(val message: String) : SignInState()
    }
    
    sealed class SignUpState {
        object Initial : SignUpState()
        object Loading : SignUpState()
        data class Success(val patient: Patient) : SignUpState()
        data class Error(val message: String) : SignUpState()
    }
}
