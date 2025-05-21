package com.example.pulmocare.data.repository

import android.util.Log
import com.example.pulmocare.data.api.NetworkModule
import com.example.pulmocare.data.model.LoginCredentials
import com.example.pulmocare.data.model.Patient
import com.example.pulmocare.data.model.PatientSignup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository that handles all patient-related data operations
 */
class PatientRepository {
    private val patientApiService = NetworkModule.patientApiService()
    private val TAG = "PatientRepository"
    
    /**
     * Sign in a patient with email and password
     */
    fun signIn(email: String, password: String): Flow<Result<Patient>> = flow {
        try {
            val response = patientApiService.signIn(LoginCredentials(email, password))
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Authentication failed: ${response.errorBody()?.string()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign in", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Sign up a new patient
     */
    fun signUp(patientSignup: PatientSignup): Flow<Result<Patient>> = flow {
        try {
            val response = patientApiService.signUp(patientSignup)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Sign up failed: ${response.errorBody()?.string()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign up", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get patient by ID
     */    fun getPatientById(id: String): Flow<Result<Patient>> = flow {
        try {
            Log.d(TAG, "Fetching patient with ID: $id")
            val response = patientApiService.getPatientById(id)
            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d(TAG, "Successfully fetched patient data: ${it.firstName}, ${it.lastName}, email: ${it.email}")
                    emit(Result.success(it))
                } ?: run {
                    Log.e(TAG, "Empty response body when fetching patient with ID: $id")
                    emit(Result.failure(Exception("Empty response body")))
                }
            } else {
                val errorMsg = "Failed to get patient: ${response.errorBody()?.string()}"
                Log.e(TAG, errorMsg + ", status code: ${response.code()}")
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting patient by ID: $id", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Update patient information
     */
    fun updatePatient(id: String, patient: Patient): Flow<Result<Patient>> = flow {
        try {
            val response = patientApiService.updatePatient(id, patient)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Failed to update patient: ${response.errorBody()?.string()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating patient", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get all patients (admin function)
     */
    fun getAllPatients(): Flow<Result<List<Patient>>> = flow {
        try {
            val response = patientApiService.getAllPatients()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Failed to get patients: ${response.errorBody()?.string()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all patients", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get patient count (admin function)
     */
    fun getPatientCount(): Flow<Result<Long>> = flow {
        try {
            val response = patientApiService.getPatientCount()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Failed to get patient count: ${response.errorBody()?.string()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting patient count", e)
            emit(Result.failure(e))
        }
    }
}
