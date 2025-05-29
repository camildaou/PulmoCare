package com.example.pulmocare.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a doctor
 */
data class Doctor(
    val id: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val age: Int? = null,
    val description: String = "",
    val location: String = "",
    val countryCode: String = "",
    val phone: String = "",
    val email: String = "",
    val medicalLicense: String = "",
    val specialization: String = "",
    val photo: String? = null,
    
    // Availability fields
    val availableDays: List<String> = emptyList(), // ["mon", "tue", "wed", "thu", "fri"]
    val availableTimeSlots: Map<String, List<TimeSlot>> = emptyMap(), // Map of day to list of time slots
    val unavailableDates: List<String> = emptyList() // Dates when the doctor is unavailable
) {
    /**
     * Utility method to get the doctor's full name
     */
    fun getFullName(): String {
        return "$firstName $lastName"
    }
    
    /**
     * Data class representing a time slot (30-minute period)
     */
    data class TimeSlot(
        val startTime: String, // "09:00"
        val endTime: String    // "09:30"
    ) {
        /**
         * Validate that the time slot is exactly 30 minutes
         * This is for local validation - the backend also enforces this
         */
        fun validateTimeSlot(): Boolean {
            if (!startTime.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) || 
                !endTime.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))) {
                return false
            }
            
            val startParts = startTime.split(":")
            val endParts = endTime.split(":")
            
            val startHour = startParts[0].toInt()
            val startMinute = startParts[1].toInt()
            val endHour = endParts[0].toInt()
            val endMinute = endParts[1].toInt()
            
            // Convert to minutes for easier calculation
            val startTotalMinutes = startHour * 60 + startMinute
            val endTotalMinutes = endHour * 60 + endMinute
            val duration = endTotalMinutes - startTotalMinutes
            
            return duration == 30
        }
        
        override fun toString(): String {
            return "$startTime - $endTime"
        }
    }
}
