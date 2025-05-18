package com.example.pulmocare.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pulmocare.data.Appointment
import com.example.pulmocare.data.GoogleCalendarHelper

/**
 * Helper class for handling calendar permissions
 */
class CalendarPermissionHandler(private val context: Context) {
    
    companion object {
        private const val CALENDAR_PERMISSION_REQUEST_CODE = 100
    }
    
    /**
     * Check if calendar permissions are granted
     * @return true if permissions are granted, false otherwise
     */
    fun hasCalendarPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Request calendar permissions
     * @param activity The activity requesting permissions
     */
    fun requestCalendarPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            ),
            CALENDAR_PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * Add an appointment to calendar after checking permissions
     * @param activity The activity
     * @param appointment The appointment to add
     * @param onPermissionDenied Callback when permission is denied
     */
    fun addAppointmentToCalendar(
        activity: Activity,
        appointment: Appointment,
        onPermissionDenied: () -> Unit = {}
    ) {
        if (hasCalendarPermissions()) {
            val intent = GoogleCalendarHelper.createAddToCalendarIntent(context, appointment)
            context.startActivity(intent)
        } else {
            requestCalendarPermissions(activity)
            onPermissionDenied()
        }
    }
    
    /**
     * Simple method to add appointment to calendar without activity
     */
    fun addAppointmentToCalendarSimple(appointment: Appointment) {
        val intent = GoogleCalendarHelper.createAddToCalendarIntent(context, appointment)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

/**
 * Composable function that creates a handler for calendar permissions
 * @param appointment The appointment to add to calendar
 * @param onPermissionDenied Callback when permission is denied
 * @return A lambda function that can be called to add the appointment to calendar
 */
@Composable
fun rememberCalendarPermissionHandlerCompat(
    appointment: Appointment,
    onPermissionDenied: () -> Unit = {}
): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Create permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            val intent = GoogleCalendarHelper.createAddToCalendarIntent(context, appointment)
            context.startActivity(intent)
        } else {
            onPermissionDenied()
        }
    }
    
    // Function to check and request permissions
    return remember(appointment) {
        {
            val hasReadPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
            
            val hasWritePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
            
            if (hasReadPermission && hasWritePermission) {
                val intent = GoogleCalendarHelper.createAddToCalendarIntent(context, appointment)
                context.startActivity(intent)
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR
                    )
                )
            }
        }
    }
}
