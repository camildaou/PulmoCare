package com.example.pulmocare.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

/**
 * SessionManager handles authentication persistence using SharedPreferences.
 */
class SessionManager(context: Context) {
    private val TAG = "SessionManager"
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Save user login information and patient ID
     */
    fun saveUserLoginSession(patientId: String) {
        Log.d(TAG, "Saving login session with patient ID: $patientId")
        sharedPreferences.edit {
            putBoolean(IS_LOGGED_IN, true)
            putString(KEY_PATIENT_ID, patientId)
            apply()
        }
    }
    
    /**
     * Clear user session data on logout
     */
    fun logoutUser() {
        Log.d(TAG, "Logging out user")
        sharedPreferences.edit {
            clear()
            apply()
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        val isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN, false)
        Log.d(TAG, "isLoggedIn check: $isLoggedIn")
        return isLoggedIn
    }
    
    /**
     * Get the current patient ID
     */
    fun getPatientId(): String {
        val patientId = sharedPreferences.getString(KEY_PATIENT_ID, "") ?: ""
        Log.d(TAG, "Retrieved patient ID: $patientId")
        return patientId
    }
    
    companion object {
        private const val PREF_NAME = "PulmoCareSessionPref"
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_PATIENT_ID = "patientId"
    }
}
