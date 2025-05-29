package com.example.pulmocare

import android.app.Application
import com.example.pulmocare.data.SessionManager

/**
 * Custom Application class for PulmoCare
 * Initializes global components like SessionManager
 */
class PulmoCareApplication : Application() {
    // Global SessionManager instance
    lateinit var sessionManager: SessionManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        // Initialize SessionManager
        sessionManager = SessionManager(applicationContext)
    }
}
