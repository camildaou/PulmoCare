package com.example.pulmocare.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * SessionManager handles authentication persistence using SharedPreferences.
 */
class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Save user login information and patient ID
     */
    fun saveUserLoginSession(patientId: String) {
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
        sharedPreferences.edit {
            clear()
            apply()
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false)
    }
    
    /**
     * Get the current patient ID
     */
    fun getPatientId(): String {
        return sharedPreferences.getString(KEY_PATIENT_ID, "") ?: ""
    }
    
    companion object {
        private const val PREF_NAME = "PulmoCareSessionPref"
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_PATIENT_ID = "patientId"
    }
}
