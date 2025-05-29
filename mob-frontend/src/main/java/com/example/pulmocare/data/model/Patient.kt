package com.example.pulmocare.data.model

import com.google.gson.annotations.SerializedName

data class Patient(
    val id: String? = null,
    
    // Basic Information
    val photo: String? = null,
    val insuranceProvider: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val email: String? = null,
    val password: String? = null,
    val bloodType: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val location: String? = null,
    val maritalStatus: String? = null,
    val occupation: String? = null,
    val hasPets: Boolean? = null,
    @SerializedName("isSmoking")
    val smoking: Boolean? = null,
    
    // Medical History
    val previousDiagnosis: List<String>? = null,
    val previousPlans: List<String>? = null,
    val previousPrescriptions: List<String>? = null,
    val previousResources: List<String>? = null,
    val symptomsAssessment: String? = null,
    val report: String? = null,
    
    // Medical Tests
    val bloodTests: List<Map<String, Any>>? = null,
    val xRays: List<Map<String, Any>>? = null,
    val otherImaging: List<Map<String, Any>>? = null,
    
    // Vaccination and Vitals
    val vaccinationHistory: List<Map<String, Any>>? = null,
    val vitals: Map<String, Any>? = null,
    
    // Medical Conditions
    val allergies: List<String>? = null,
    val chronicConditions: List<String>? = null,
    val surgeriesHistory: List<Map<String, Any>>? = null
)

// Simple login credentials data class
data class LoginCredentials(
    val email: String,
    val password: String
)

// Simple signup data class for minimal patient creation
data class PatientSignup(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val age: Int,
    val gender: String,
    val bloodType: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val location: String? = null,
    val dateOfBirth: String? = null,
    val insuranceProvider: String? = null,
    val maritalStatus: String? = null,
    val occupation: String? = null,
    val hasPets: Boolean? = false,
    val smoking: Boolean? = false,
    val allergies: List<String>? = null,
    val chronicConditions: List<String>? = null,
    val photo: String? = null
)
