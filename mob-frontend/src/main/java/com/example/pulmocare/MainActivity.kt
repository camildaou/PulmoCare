// MainActivity.kt
package com.example.pulmocare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.pulmocare.data.SessionManager
import com.example.pulmocare.ui.theme.PulmoCareTheme
import com.example.pulmocare.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize auth view model and session manager
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        sessionManager = SessionManager(this)
        
        setContent {
            PulmoCareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PulmoCareApp()
                }
            }
        }
    }
}