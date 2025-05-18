package com.example.pulmocare.data

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

data class MedicalDocument(
    val id: String,
    val name: String,
    val date: String,
    val type: String,
    val summary: String? = null,
    val status: String? = null
)

data class MedicalCategory(
    val id: String,
    val title: String,
    val description: String,
    val canUpload: Boolean,
    val documents: List<MedicalDocument>
)

data class Doctor(
    val id: String,
    val name: String,
    val specialty: String,
    val rating: Double,
    val reviews: Int,
    val availability: List<String>,
    val location: String,
    val phone: String,
    val email: String,
    val image: String,
    val bio: String,
    val availableTimes: Map<String, List<String>>
)

data class Appointment(
    val id: String,
    val doctor: String,
    val specialty: String,
    val date: String,
    val time: String,
    val location: String,
    val status: String,
    val summary: AppointmentSummary? = null,
    val reason: String = ""
)

data class AppointmentSummary(
    val diagnosis: String,
    val notes: String,
    val recommendations: String,
    val followUp: String,
    val prescriptions: List<Prescription>
)

data class Prescription(
    val name: String,
    val dosage: String,
    val duration: String
)

data class SymptomAssessment(
    val condition: String,
    val urgency: String,
    val recommendations: List<String>,
    val followUp: String
)

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
)

class MedicalRepository {
    val medicalCategories = mutableStateListOf(
        MedicalCategory(
            id = "history",
            title = "Medical History",
            description = "Your past medical conditions and treatments",
            canUpload = false,
            documents = listOf(
                MedicalDocument(
                    id = "h1",
                    name = "Annual Physical 2024",
                    date = "Feb 15, 2024",
                    type = "PDF"
                ),
                MedicalDocument(
                    id = "h2",
                    name = "Pulmonary Function Test",
                    date = "Jan 10, 2024",
                    type = "PDF"
                ),
                MedicalDocument(
                    id = "h3",
                    name = "Allergy Test Results",
                    date = "Nov 5, 2023",
                    type = "PDF"
                )
            )
        ),
        MedicalCategory(
            id = "prescriptions",
            title = "Prescriptions",
            description = "Your current and past medications",
            canUpload = false,
            documents = listOf(
                MedicalDocument(
                    id = "p1",
                    name = "Albuterol Inhaler",
                    date = "Mar 1, 2024",
                    type = "PDF"
                ),
                MedicalDocument(
                    id = "p2",
                    name = "Fluticasone",
                    date = "Feb 20, 2024",
                    type = "PDF"
                ),
                MedicalDocument(
                    id = "p3",
                    name = "Montelukast",
                    date = "Jan 15, 2024",
                    type = "PDF"
                )
            )
        ),
        MedicalCategory(
            id = "reports",
            title = "Medical Reports",
            description = "Detailed reports from your healthcare providers",
            canUpload = true,
            documents = listOf(
                MedicalDocument(
                    id = "r1",
                    name = "Pulmonologist Consultation",
                    date = "Mar 5, 2024",
                    type = "PDF",
                    summary = "Mild asthma symptoms, recommended continued use of maintenance inhaler and follow-up in 3 months."
                ),
                MedicalDocument(
                    id = "r2",
                    name = "Sleep Study Results",
                    date = "Feb 10, 2024",
                    type = "PDF",
                    summary = "Mild sleep apnea detected. Recommended lifestyle changes and follow-up in 6 months to reassess."
                ),
                MedicalDocument(
                    id = "r3",
                    name = "Respiratory Therapy Evaluation",
                    date = "Jan 20, 2024",
                    type = "PDF",
                    summary = "Breathing exercises showing improvement in lung capacity. Continue with prescribed regimen."
                )
            )
        ),
        MedicalCategory(
            id = "bloodtests",
            title = "Blood Tests",
            description = "Results from your blood work",
            canUpload = true,
            documents = listOf(
                MedicalDocument(
                    id = "b1",
                    name = "Complete Blood Count",
                    date = "Mar 10, 2024",
                    type = "PDF",
                    summary = "All values within normal range. White blood cell count slightly elevated but not concerning.",
                    status = "normal"
                ),
                MedicalDocument(
                    id = "b2",
                    name = "Metabolic Panel",
                    date = "Feb 10, 2024",
                    type = "PDF",
                    summary = "Glucose levels slightly elevated. Recommendation to monitor diet and retest in 3 months.",
                    status = "attention"
                ),
                MedicalDocument(
                    id = "b3",
                    name = "Inflammatory Markers",
                    date = "Jan 5, 2024",
                    type = "PDF",
                    summary = "C-reactive protein within normal limits. No signs of active inflammation.",
                    status = "normal"
                )
            )
        ),
        MedicalCategory(
            id = "xrays",
            title = "X-Rays",
            description = "Chest X-rays",
            canUpload = true,
            documents = listOf(
                MedicalDocument(
                    id = "x1",
                    name = "Chest X-Ray",
                    date = "Mar 15, 2024",
                    type = "PDF",
                    summary = "No abnormalities detected. Lungs clear with no signs of infection or masses.",
                    status = "normal"
                ),
                MedicalDocument(
                    id = "x2",
                    name = "CT Scan - Lungs",
                    date = "Feb 5, 2024",
                    type = "PDF",
                    summary = "Small nodule (4mm) detected in right lower lobe. Likely benign. Follow-up recommended in 6 months.",
                    status = "attention"
                ),
                MedicalDocument(
                    id = "x3",
                    name = "Bronchoscopy Images",
                    date = "Dec 20, 2023",
                    type = "PDF",
                    summary = "Airways appear normal with no obstructions or abnormal tissue. Mild inflammation noted.",
                    status = "normal"
                )
            )
        ), MedicalCategory(
            id = "medim",
            title = "Other Medical Imaging",
            description = "CT scans and other imaging",
            canUpload = false,
            documents = listOf(
                MedicalDocument(
                    id = "x2",
                    name = "CT Scan - Lungs",
                    date = "Feb 5, 2024",
                    type = "PDF",
                    summary = "Small nodule (4mm) detected in right lower lobe. Likely benign. Follow-up recommended in 6 months.",
                    status = "attention"
                ),
                MedicalDocument(
                    id = "x3",
                    name = "Bronchoscopy Images",
                    date = "Dec 20, 2023",
                    type = "PDF",
                    summary = "Airways appear normal with no obstructions or abnormal tissue. Mild inflammation noted.",
                    status = "normal"
                )
            )
        )
    )

    val doctors = mutableStateListOf(
        Doctor(
            id = "1",
            name = "Dr. Sarah Johnson",
            specialty = "Pulmonologist",
            rating = 4.9,
            reviews = 124,
            availability = listOf("Mon", "Wed", "Fri"),
            location = "PulmoCare Main Clinic",
            phone = "555-123-4567",
            email = "sarah.johnson@pulmocare.com",
            image = "https://placeholder.com/100",
            bio = "Dr. Johnson is a board-certified pulmonologist with over 15 years of experience in treating respiratory conditions. She specializes in asthma, COPD, and sleep-related breathing disorders.",
            availableTimes = mapOf(
                "Mon" to listOf("9:00 AM", "10:00 AM", "2:00 PM", "3:00 PM"),
                "Wed" to listOf("10:00 AM", "11:00 AM", "1:00 PM", "4:00 PM"),
                "Fri" to listOf("9:00 AM", "11:00 AM", "2:00 PM", "3:00 PM")
            )
        ),
        Doctor(
            id = "2",
            name = "Dr. Michael Chen",
            specialty = "Pulmonologist",
            rating = 4.8,
            reviews = 98,
            availability = listOf("Tue", "Thu", "Sat"),
            location = "PulmoCare North Branch",
            phone = "555-987-6543",
            email = "michael.chen@pulmocare.com",
            image = "https://placeholder.com/100",
            bio = "Dr. Chen is a pulmonologist specializing in interventional pulmonology and advanced diagnostic techniques. He has expertise in treating complex respiratory conditions and lung cancer.",
            availableTimes = mapOf(
                "Tue" to listOf("9:00 AM", "10:00 AM", "2:00 PM", "3:00 PM"),
                "Thu" to listOf("10:00 AM", "11:00 AM", "1:00 PM", "4:00 PM"),
                "Sat" to listOf("9:00 AM", "10:00 AM", "11:00 AM")
            )
        ),
        Doctor(
            id = "3",
            name = "Dr. Emily Rodriguez",
            specialty = "Pulmonologist",
            rating = 4.7,
            reviews = 87,
            availability = listOf("Mon", "Tue", "Thu"),
            location = "PulmoCare Main Clinic",
            phone = "555-456-7890",
            email = "emily.rodriguez@pulmocare.com",
            image = "https://placeholder.com/100",
            bio = "Dr. Rodriguez specializes in pulmonary rehabilitation and management of chronic respiratory conditions. She takes a holistic approach to lung health, focusing on both medical treatment and lifestyle modifications.",
            availableTimes = mapOf(
                "Mon" to listOf("11:00 AM", "1:00 PM", "2:00 PM", "4:00 PM"),
                "Tue" to listOf("9:00 AM", "10:00 AM", "3:00 PM", "4:00 PM"),
                "Thu" to listOf("10:00 AM", "11:00 AM", "2:00 PM", "3:00 PM")
            )
        ),
        Doctor(
            id = "4",
            name = "Dr. James Wilson",
            specialty = "Pulmonologist",
            rating = 4.9,
            reviews = 112,
            availability = listOf("Wed", "Fri", "Sat"),
            location = "PulmoCare Sleep Center",
            phone = "555-789-0123",
            email = "james.wilson@pulmocare.com",
            image = "https://placeholder.com/100",
            bio = "Dr. Wilson is a pulmonologist with a subspecialty in sleep medicine. He diagnoses and treats sleep-related breathing disorders, including sleep apnea, and helps patients improve their respiratory health during sleep.",
            availableTimes = mapOf(
                "Wed" to listOf("9:00 AM", "10:00 AM", "2:00 PM", "3:00 PM"),
                "Fri" to listOf("11:00 AM", "1:00 PM", "2:00 PM", "4:00 PM"),
                "Sat" to listOf("9:00 AM", "10:00 AM", "11:00 AM")
            )
        )
    )

    val upcomingAppointments = mutableStateListOf(
        Appointment(
            id = "1",
            doctor = "Dr. Sarah Johnson",
            specialty = "Pulmonologist",
            date = "March 30, 2025",
            time = "10:00 AM",
            location = "PulmoCare Main Clinic",
            status = "confirmed"
        ),
        Appointment(
            id = "2",
            doctor = "Dr. Michael Chen",
            specialty = "Pulmonologist",
            date = "April 15, 2025",
            time = "2:30 PM",
            location = "PulmoCare North Branch",
            status = "confirmed"
        )
    )

    val pastAppointments = mutableStateListOf(
        Appointment(
            id = "3",
            doctor = "Dr. Emily Rodriguez",
            specialty = "Pulmonologist",
            date = "February 12, 2025",
            time = "11:15 AM",
            location = "PulmoCare Main Clinic",
            status = "completed",
            summary = AppointmentSummary(
                diagnosis = "Mild Asthma Exacerbation",
                notes = "Patient presented with increased wheezing and shortness of breath over the past week. Lung examination revealed mild wheezing in all lung fields. Peak flow at 80% of personal best.",
                recommendations = "Increase use of rescue inhaler as needed. Continue current controller medication. Avoid known triggers.",
                followUp = "Return in 4 weeks for reassessment.",
                prescriptions = listOf(
                    Prescription(
                        name = "Albuterol Inhaler",
                        dosage = "2 puffs every 4-6 hours as needed",
                        duration = "As needed"
                    ),
                    Prescription(
                        name = "Fluticasone Inhaler",
                        dosage = "2 puffs twice daily",
                        duration = "Continue"
                    )
                )
            )
        ),
        Appointment(
            id = "4",
            doctor = "Dr. James Wilson",
            specialty = "Pulmonologist",
            date = "January 5, 2025",
            time = "9:00 AM",
            location = "PulmoCare Sleep Center",
            status = "completed",
            summary = AppointmentSummary(
                diagnosis = "Obstructive Sleep Apnea",
                notes = "Sleep study results reviewed. AHI of 18 events/hour indicating moderate OSA. Patient reports daytime fatigue and morning headaches.",
                recommendations = "CPAP therapy initiated. Settings: pressure 8-12 cm H2O. Sleep hygiene education provided.",
                followUp = "Return in 6 weeks to assess CPAP compliance and symptom improvement.",
                prescriptions = listOf(
                    Prescription(
                        name = "CPAP Device",
                        dosage = "Use nightly",
                        duration = "Continuous"
                    )
                )
            )
        )
    )

    fun assessSymptoms(symptoms: Map<String, Any>): SymptomAssessment {
        // In a real app, this would use a more sophisticated algorithm or API
        // Simple logic to determine condition and urgency
        val breathingDifficulty = symptoms["breathingDifficulty"] as? Boolean ?: false
        val severity = symptoms["severity"] as? Int ?: 5
        val coughing = symptoms["coughing"] as? Boolean ?: false
        val wheezing = symptoms["wheezing"] as? Boolean ?: false
        val fever = symptoms["fever"] as? Boolean ?: false

        var condition = "Unspecified respiratory condition"
        var urgency = "low"
        var recommendations = listOf("Rest and monitor symptoms")
        var followUp = "Follow up with your doctor if symptoms persist for more than a week."

        if (breathingDifficulty && severity > 7) {
            condition = "Possible acute respiratory distress"
            urgency = "high"
            recommendations = listOf(
                "Seek immediate medical attention",
                "Use rescue inhaler if prescribed",
                "Maintain upright position to ease breathing"
            )
            followUp = "Emergency evaluation recommended"
        } else if (coughing && wheezing) {
            condition = "Possible asthma or bronchitis"
            urgency = "medium"
            recommendations = listOf(
                "Use prescribed inhaler if available",
                "Avoid known triggers",
                "Stay hydrated",
                "Monitor oxygen levels if possible"
            )
            followUp = "Schedule appointment with pulmonologist within 3-5 days"
        } else if (coughing && fever) {
            condition = "Possible respiratory infection"
            urgency = "medium"
            recommendations = listOf(
                "Rest and stay hydrated",
                "Monitor temperature",
                "Take over-the-counter fever reducers as directed"
            )
            followUp = "Schedule appointment with primary care physician within 2-3 days"
        }

        return SymptomAssessment(
            condition = condition,
            urgency = urgency,
            recommendations = recommendations,
            followUp = followUp
        )
    }

    // Updated function to get all appointments
    fun getAllAppointments(): List<Appointment> {
        return upcomingAppointments + pastAppointments
    }

    // Updated function to schedule an appointment
    fun scheduleAppointment(
        doctor: String,
        specialty: String,
        date: String,
        time: String,
        location: String,
        reason: String
    ): Appointment {
        val newAppointment = Appointment(
            id = UUID.randomUUID().toString(),
            doctor = doctor,
            specialty = specialty,
            date = date,
            time = time,
            location = location,
            status = "scheduled",
            reason = reason
        )

        upcomingAppointments.add(newAppointment)
        return newAppointment
    }

    // Updated function to reschedule an appointment
    fun rescheduleAppointment(
        id: String,
        newDate: String,
        newTime: String,
        reason: String
    ): Appointment? {
        // Find the appointment in the upcoming appointments
        val appointmentIndex = upcomingAppointments.indexOfFirst { it.id == id }

        if (appointmentIndex != -1) {
            val oldAppointment = upcomingAppointments[appointmentIndex]
            val updatedAppointment = oldAppointment.copy(
                date = newDate,
                time = newTime,
                reason = if (reason.isNotEmpty()) reason else oldAppointment.reason
            )

            upcomingAppointments[appointmentIndex] = updatedAppointment
            return updatedAppointment
        }

        return null
    }

    // Updated function to cancel an appointment
    fun cancelAppointment(id: String): Boolean {
        // Find the appointment in the upcoming appointments
        val appointmentIndex = upcomingAppointments.indexOfFirst { it.id == id }

        if (appointmentIndex != -1) {
            val oldAppointment = upcomingAppointments[appointmentIndex]
            val cancelledAppointment = oldAppointment.copy(status = "cancelled")

            // Remove from upcoming and add to past
            upcomingAppointments.removeAt(appointmentIndex)
            pastAppointments.add(cancelledAppointment)

            return true
        }

        return false
    }

    // Function to get a doctor by ID
    fun getDoctorById(id: String): Doctor? {
        return doctors.find { it.id == id }
    }

    // Function to get available appointment times for a doctor on a specific date
    fun getAvailableTimesForDoctor(doctorId: String, date: String): List<String> {
        val doctor = getDoctorById(doctorId) ?: return emptyList()

        // Convert date to day of week (this is simplified, in a real app you'd parse the date)
        val dayOfWeek = when {
            date.contains("Mon") -> "Mon"
            date.contains("Tue") -> "Tue"
            date.contains("Wed") -> "Wed"
            date.contains("Thu") -> "Thu"
            date.contains("Fri") -> "Fri"
            date.contains("Sat") -> "Sat"
            date.contains("Sun") -> "Sun"
            else -> return emptyList()
        }

        // Get available times for that day
        return doctor.availableTimes[dayOfWeek] ?: emptyList()
    }

    // List to store COPD questionnaires
    val copdQuestionnaires = mutableStateListOf<COPDQuestionnaire>()

    // Function to save COPD questionnaire
    fun saveCOPDQuestionnaire(
        appointmentId: String,
        breathlessness: Int,
        coughing: Int,
        sputumProduction: Int,
        chestTightness: Int,
        activityLimitation: Int,
        confidence: Int,
        sleepQuality: Int,
        energy: Int
    ): COPDQuestionnaire {
        val questionnaire = COPDQuestionnaire(
            appointmentId = appointmentId,
            date = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.US).format(java.util.Date()),
            breathlessness = breathlessness,
            coughing = coughing,
            sputumProduction = sputumProduction,
            chestTightness = chestTightness,
            activityLimitation = activityLimitation,
            confidence = confidence,
            sleepQuality = sleepQuality,
            energy = energy
        )

        copdQuestionnaires.add(questionnaire)
        return questionnaire
    }

    // Function to get COPD questionnaire by appointment ID
    fun getCOPDQuestionnaireByAppointmentId(appointmentId: String): COPDQuestionnaire? {
        return copdQuestionnaires.find { it.appointmentId == appointmentId }
    }
}

