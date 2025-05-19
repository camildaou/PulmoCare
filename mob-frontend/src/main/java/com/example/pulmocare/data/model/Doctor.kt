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
    val photo: String? = null
) {
    /**
     * Utility method to get the doctor's full name
     */
    fun getFullName(): String {
        return "$firstName $lastName"
    }
}
