package com.example.pulmocare.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalTime

/**
 * Data class representing an appointment
 */
data class Appointment(
    val id: String? = null,
    
    // Schedule information
    val date: String, // In ISO format: YYYY-MM-DD
    val hour: String, // In format: HH:MM
    
    // References to patient and doctor
    val patient: Patient? = null,
    val doctor: Doctor? = null,
    
    // Assessment information
    val assessmentInfo: String? = null,
    
    @SerializedName("reportPending")
    val isReportPending: Boolean = false,
    
    // Medical details
    val diagnosis: String? = null,
    val personalNotes: String? = null, // doctor's notes
    val plan: String? = null,
    
    // Appointment details
    val location: String = "",
    val reason: String = "",
    
    @SerializedName("upcoming")
    val isUpcoming: Boolean = true, // true if upcoming, false if past
    
    @SerializedName("isVaccine")
    val isVaccine: Boolean = false // true if this is a vaccine appointment
)
