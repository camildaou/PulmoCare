package com.example.pulmocare.data.model

import java.util.UUID

/**
 * Data class representing a COPD questionnaire filled out by patients
 */
data class COPDQuestionnaire(
    val id: String = UUID.randomUUID().toString(),
    val appointmentId: String,
    val date: String,
    val breathlessness: Int, // 0-4 scale (mMRC dyspnea scale)
    val coughing: Int, // 0-4 scale
    val sputumProduction: Int, // 0-4 scale
    val chestTightness: Int, // 0-4 scale
    val activityLimitation: Int, // 0-4 scale
    val confidence: Int, // 0-4 scale
    val sleepQuality: Int, // 0-4 scale
    val energy: Int // 0-4 scale
) {
    /**
     * Calculate the overall score of the COPD questionnaire
     * Range: 0-32, where higher indicates more severe symptoms
     */
    fun getTotalScore(): Int {
        return breathlessness + coughing + sputumProduction + chestTightness + 
               activityLimitation + confidence + sleepQuality + energy
    }
    
    /**
     * Determine severity level based on total score
     */
    fun getSeverityLevel(): String {
        val totalScore = getTotalScore()
        return when {
            totalScore < 8 -> "Mild"
            totalScore < 16 -> "Moderate"
            totalScore < 24 -> "Severe"
            else -> "Very Severe"
        }
    }
}
