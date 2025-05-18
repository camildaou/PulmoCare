package com.example.pulmocare.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.pulmocare.data.Appointment
import com.example.pulmocare.data.GoogleCalendarHelper
import kotlinx.coroutines.launch

/**
 * Composable function to handle calendar permissions in a simpler way
 * Returns a function that can be called to add an appointment to the calendar
 */
@Composable
fun rememberCalendarPermissionHandler(
    appointment: Appointment,
    snackbarHostState: SnackbarHostState? = null
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Create permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            openCalendarWithAppointment(context, appointment)
        } else {
            // Show snackbar if permissions denied
            snackbarHostState?.let {
                scope.launch {
                    it.showSnackbar(
                        message = "Calendar permission denied. Please enable in settings.",
                        actionLabel = "OK"
                    )
                }
            }
        }
    }
    
    // Return a function that will check permissions and open calendar
    return remember(appointment) {
        {
            if (hasCalendarPermissions(context)) {
                openCalendarWithAppointment(context, appointment)
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

// Helper function to check calendar permissions
private fun hasCalendarPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CALENDAR
    ) == PackageManager.PERMISSION_GRANTED &&
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_CALENDAR
    ) == PackageManager.PERMISSION_GRANTED
}

// Helper function to open calendar with appointment
private fun openCalendarWithAppointment(context: Context, appointment: Appointment) {
    val intent = GoogleCalendarHelper.createAddToCalendarIntent(context, appointment)
    context.startActivity(intent)
}
