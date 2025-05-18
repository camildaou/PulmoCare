package com.example.pulmocare.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.pulmocare.data.Appointment
import com.example.pulmocare.data.GoogleCalendarHelper
import com.example.pulmocare.data.MedicalRepository
import com.example.pulmocare.utils.rememberCalendarPermissionHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen() {
    val medicalRepository = remember { MedicalRepository() }
    val upcomingAppointments = remember { medicalRepository.upcomingAppointments }
    val pastAppointments = remember { medicalRepository.pastAppointments }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var showCOPDQuestionnaireDialog by remember { mutableStateOf(false) }
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }
    var showAppointmentConfirmationDialog by remember { mutableStateOf(false) }
    var selectedAppointmentId by remember { mutableStateOf<String?>(null) }
    var newlyScheduledAppointmentId by remember { mutableStateOf<String?>(null) }// Tab selection state
    var selectedTab by remember { mutableStateOf(0) } // 0 for upcoming, 1 for past

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Your Appointments",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showScheduleDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Schedule New Appointment")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tab buttons for switching between upcoming and past appointments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedTab == 0)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Upcoming")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedTab == 1)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Past")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Upcoming appointments
                    if (upcomingAppointments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No upcoming appointments",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            upcomingAppointments.forEach { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    onReschedule = {
                                        selectedAppointmentId = appointment.id
                                        showRescheduleDialog = true
                                    },                                    onCancel = {
                                        selectedAppointmentId = appointment.id
                                        showCancelConfirmationDialog = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
                1 -> {
                    // Past appointments
                    if (pastAppointments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No past appointments",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            pastAppointments.forEach { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    onReschedule = null,
                                    onCancel = null
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showScheduleDialog) {
        AppointmentReasonDialog(
            title = "Schedule Appointment",
            onDismiss = { showScheduleDialog = false },
            onConfirm = { reason, otherReason ->
                // Handle schedule appointment with reason
                // After scheduling, set the ID for the newly scheduled appointment
                val finalReason = if (reason == "Other") otherReason else reason                // For demo purposes, we'll just create a dummy appointment
                val newAppointment = medicalRepository.scheduleAppointment(
                    doctor = "Dr. Sarah Johnson",
                    specialty = "Pulmonologist",
                    date = "June 10, 2025",
                    time = "10:00 AM",
                    location = "PulmoCare Main Clinic",
                    reason = finalReason
                )                
                newlyScheduledAppointmentId = newAppointment.id
                showScheduleDialog = false
                showCOPDQuestionnaireDialog = true
            }
        )
    }

    if (showRescheduleDialog) {
        AppointmentReasonDialog(
            title = "Reschedule Appointment",
            onDismiss = { showRescheduleDialog = false },
            onConfirm = { reason, otherReason ->
                // Handle reschedule appointment with reason
                showRescheduleDialog = false
            }
        )
    }

    if (showCOPDQuestionnaireDialog && newlyScheduledAppointmentId != null) {
        COPDQuestionnaireDialog(
            appointmentId = newlyScheduledAppointmentId!!,            onDismiss = {
                showCOPDQuestionnaireDialog = false
                // Still show the calendar option even if they skip the questionnaire
                showAppointmentConfirmationDialog = true
            },
            onSubmit = { appointmentId, breathlessness, coughing, sputum, chestTightness,
                         activityLimitation, confidence, sleepQuality, energy ->                // Save the COPD questionnaire
                medicalRepository.saveCOPDQuestionnaire(
                    appointmentId = appointmentId,
                    breathlessness = breathlessness,
                    coughing = coughing,
                    sputumProduction = sputum,
                    chestTightness = chestTightness,
                    activityLimitation = activityLimitation,
                    confidence = confidence,
                    sleepQuality = sleepQuality,
                    energy = energy
                )

                // Show appointment confirmation dialog with calendar option
                showCOPDQuestionnaireDialog = false
                showAppointmentConfirmationDialog = true
            }
        )
    }
      // Appointment Confirmation Dialog with Calendar Option
    if (showAppointmentConfirmationDialog && newlyScheduledAppointmentId != null) {
        val appointment = remember { 
            medicalRepository.upcomingAppointments.find { it.id == newlyScheduledAppointmentId }
        }
        
        if (appointment != null) {
            val snackbarHostState = remember { SnackbarHostState() }
            
            // Create add to calendar function with permission handling
            val addToCalendar = rememberCalendarPermissionHandler(
                appointment = appointment,
                snackbarHostState = snackbarHostState
            )
            
            AlertDialog(
                onDismissRequest = { 
                    showAppointmentConfirmationDialog = false
                    newlyScheduledAppointmentId = null
                },
                title = { Text("Appointment Scheduled") },
                text = { 
                    Column {
                        Text("Your appointment has been scheduled successfully:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Doctor: ${appointment.doctor}")
                        Text("Date: ${appointment.date}")
                        Text("Time: ${appointment.time}")
                        Text("Location: ${appointment.location}")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Would you like to add this appointment to your Google Calendar?")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            addToCalendar()
                            showAppointmentConfirmationDialog = false
                            newlyScheduledAppointmentId = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4285F4) // Google Blue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Add to Calendar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { 
                            showAppointmentConfirmationDialog = false
                            newlyScheduledAppointmentId = null
                        }
                    ) {
                        Text("Not Now")
                    }
                }
            )
        }
    }
    
    if (showCancelConfirmationDialog && selectedAppointmentId != null) {
        ConfirmCancellationDialog(
            appointmentId = selectedAppointmentId!!,
            onDismiss = { showCancelConfirmationDialog = false },
            onConfirm = { appointmentId ->
                medicalRepository.cancelAppointment(appointmentId)
                showCancelConfirmationDialog = false
            }
        )
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onReschedule: (() -> Unit)?,
    onCancel: (() -> Unit)?
) {
    val medicalRepository = remember { MedicalRepository() }
    val copdQuestionnaire = remember { medicalRepository.getCOPDQuestionnaireByAppointmentId(appointment.id) }
    var showCOPDDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // White background for card
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.doctor,
                    style = MaterialTheme.typography.titleMedium
                )

                StatusChip(status = appointment.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = appointment.specialty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = appointment.date,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = appointment.time,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = appointment.location,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (appointment.reason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Reason: ${appointment.reason}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Show summary for completed appointments
            if (appointment.summary != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Appointment Summary",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Diagnosis: ${appointment.summary.diagnosis}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Notes: ${appointment.summary.notes}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Recommendations: ${appointment.summary.recommendations}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Follow-up: ${appointment.summary.followUp}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (appointment.summary.prescriptions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Prescriptions:",
                        style = MaterialTheme.typography.titleSmall
                    )

                    appointment.summary.prescriptions.forEach { prescription ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = prescription.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "Dosage: ${prescription.dosage}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = "Duration: ${prescription.duration}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            if (onReschedule != null || onCancel != null || copdQuestionnaire != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (copdQuestionnaire != null) {
                        Button(
                            onClick = { showCOPDDetails = !showCOPDDetails },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary // Blue color for COPD details
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (showCOPDDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text(if (showCOPDDetails) "Hide COPD Assessment" else "View COPD Assessment")
                        }
                    }
                    if (onReschedule != null) {
                        Button(
                            onClick = onReschedule,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50) // Green color for reschedule
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Reschedule")
                        }
                    }                    // Add to Calendar button for upcoming appointments
                    if (appointment.status == "confirmed" || appointment.status == "scheduled") {
                        val snackbarHostState = remember { SnackbarHostState() }
                        val addToCalendar = rememberCalendarPermissionHandler(
                            appointment = appointment,
                            snackbarHostState = snackbarHostState
                        )
                        
                        Button(
                            onClick = { addToCalendar() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4285F4) // Google Blue
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Add to Calendar")
                        }
                        
                        // Show snackbar host for permission messages
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (onCancel != null) {
                        Button(
                            onClick = onCancel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935) // Red color for cancel
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Cancel")
                        }
                    }
                }
            }

            // Show COPD Questionnaire details if available and expanded
            if (copdQuestionnaire != null && showCOPDDetails) {
                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "COPD Assessment (${copdQuestionnaire.date})",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Calculate total score (excluding confidence, sleep and energy which are reversed)
                val symptomScore = copdQuestionnaire.breathlessness +
                                  copdQuestionnaire.coughing +
                                  copdQuestionnaire.sputumProduction +
                                  copdQuestionnaire.chestTightness +
                                  copdQuestionnaire.activityLimitation

                val impactScore = (4 - copdQuestionnaire.confidence) +
                                 (4 - copdQuestionnaire.sleepQuality) +
                                 (4 - copdQuestionnaire.energy)

                val totalScore = symptomScore + impactScore

                val severityLevel = when {
                    totalScore < 10 -> "Low impact"
                    totalScore < 20 -> "Medium impact"
                    else -> "High impact"
                }

                val severityColor = when {
                    totalScore < 10 -> Color(0xFF4CAF50) // Green
                    totalScore < 20 -> Color(0xFFFFA000) // Amber
                    else -> Color(0xFFE53935) // Red
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total CAT Score: $totalScore/40",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )

                            Surface(
                                color = severityColor.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = severityLevel,
                                    color = severityColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Symptom Breakdown:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        COPDSymptomItem("Breathlessness", copdQuestionnaire.breathlessness)
                        COPDSymptomItem("Coughing", copdQuestionnaire.coughing)
                        COPDSymptomItem("Sputum Production", copdQuestionnaire.sputumProduction)
                        COPDSymptomItem("Chest Tightness", copdQuestionnaire.chestTightness)
                        COPDSymptomItem("Activity Limitation", copdQuestionnaire.activityLimitation)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Impact on Life:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Note: The scores below are inverted in display for better readability
                        COPDSymptomItem("Confidence Leaving Home", 4 - copdQuestionnaire.confidence)
                        COPDSymptomItem("Sleep Quality", 4 - copdQuestionnaire.sleepQuality)
                        COPDSymptomItem("Energy", 4 - copdQuestionnaire.energy)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "scheduled" -> MaterialTheme.colorScheme.primary
        "confirmed" -> MaterialTheme.colorScheme.tertiary
        "completed" -> MaterialTheme.colorScheme.secondary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentReasonDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val reasonOptions = listOf(
        "Regular Check-up",
        "Follow-up Appointment",
        "New Symptoms",
        "Medication Refill",
        "Lab Results Discussion",
        "Other"
    )

    var selectedReason by remember { mutableStateOf(reasonOptions[0]) }
    var otherReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Please select a reason for this appointment:")

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    reasonOptions.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = reason == selectedReason,
                                    onClick = { selectedReason = reason },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = reason == selectedReason,
                                onClick = null
                            )
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                if (selectedReason == "Other") {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = otherReason,
                        onValueChange = { otherReason = it },
                        label = { Text("Please specify") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalReason = if (selectedReason == "Other") otherReason else selectedReason
                    onConfirm(selectedReason, otherReason)
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun COPDQuestionnaireDialog(
    appointmentId: String,
    onDismiss: () -> Unit,
    onSubmit: (String, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit
) {
    val breathlessnessOptions = listOf(
        "0 - I only get breathless with strenuous exercise",
        "1 - I get short of breath when hurrying on level ground or walking up a slight hill",
        "2 - I walk slower than people of my age due to breathlessness",
        "3 - I stop for breath after walking about 100 meters",
        "4 - I am too breathless to leave the house"
    )

    val symptomOptions = listOf(
        "0 - Never",
        "1 - Rarely",
        "2 - Sometimes",
        "3 - Often",
        "4 - Almost always"
    )

    var breathlessnessRating by remember { mutableStateOf(0) }
    var coughingRating by remember { mutableStateOf(0) }
    var sputumRating by remember { mutableStateOf(0) }
    var chestTightnessRating by remember { mutableStateOf(0) }
    var activityLimitationRating by remember { mutableStateOf(0) }
    var confidenceRating by remember { mutableStateOf(0) }
    var sleepQualityRating by remember { mutableStateOf(0) }
    var energyRating by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("COPD Assessment Questionnaire")
                Text(
                    "This helps us track your COPD symptoms over time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Please rate your experience with the following symptoms:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Breathlessness section
                Text(
                    "1. Breathlessness:",
                    style = MaterialTheme.typography.titleSmall
                )

                Column {
                    breathlessnessOptions.forEachIndexed { index, description ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = index == breathlessnessRating,
                                    onClick = { breathlessnessRating = index },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = index == breathlessnessRating,
                                onClick = null
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Coughing section
                Text(
                    "2. Coughing:",
                    style = MaterialTheme.typography.titleSmall
                )

                RatingSelector(
                    options = symptomOptions,
                    selectedRating = coughingRating,
                    onRatingSelected = { coughingRating = it }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Sputum Production section
                Text(
                    "3. Bringing up phlegm or mucus:",
                    style = MaterialTheme.typography.titleSmall
                )

                RatingSelector(
                    options = symptomOptions,
                    selectedRating = sputumRating,
                    onRatingSelected = { sputumRating = it }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Chest Tightness section
                Text(
                    "4. Chest feels tight:",
                    style = MaterialTheme.typography.titleSmall
                )

                RatingSelector(
                    options = symptomOptions,
                    selectedRating = chestTightnessRating,
                    onRatingSelected = { chestTightnessRating = it }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Activity Limitation section
                Text(
                    "5. Limited in daily activities:",
                    style = MaterialTheme.typography.titleSmall
                )

                RatingSelector(
                    options = symptomOptions,
                    selectedRating = activityLimitationRating,
                    onRatingSelected = { activityLimitationRating = it }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Confidence section
                Text(
                    "6. Confident leaving home despite lung condition:",
                    style = MaterialTheme.typography.titleSmall
                )

                RatingSelector(
                    options = symptomOptions.reversed(), // Reversed since higher is better for confidence
                    selectedRating = confidenceRating,
                    onRatingSelected = { confidenceRating = it }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Sleep Quality section
                Text(
                    "7. Sleep soundly:",
                    style = MaterialTheme.typography.titleSmall
                )

                RatingSelector(
                    options = symptomOptions.reversed(), // Reversed since higher is better for sleep quality
                    selectedRating = sleepQualityRating,
                    onRatingSelected = { sleepQualityRating = it }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Energy section
                Text(
                    "8. Energy for daily activities:",
                    style = MaterialTheme.typography.titleSmall
                )

                RatingSelector(
                    options = symptomOptions.reversed(), // Reversed since higher is better for energy
                    selectedRating = energyRating,
                    onRatingSelected = { energyRating = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(
                        appointmentId,
                        breathlessnessRating,
                        coughingRating,
                        sputumRating,
                        chestTightnessRating,
                        activityLimitationRating,
                        confidenceRating,
                        sleepQualityRating,
                        energyRating
                    )
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

@Composable
fun RatingSelector(
    options: List<String>,
    selectedRating: Int,
    onRatingSelected: (Int) -> Unit
) {
    Column {
        options.forEachIndexed { index, description ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = index == selectedRating,
                        onClick = { onRatingSelected(index) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = index == selectedRating,
                    onClick = null
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun COPDSymptomItem(name: String, rating: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )

        Row {
            for (i in 0 until 5) {
                val color = if (i <= rating) {
                    when (rating) {
                        0 -> Color(0xFF4CAF50) // Green for no symptoms
                        1 -> Color(0xFF8BC34A) // Light green for mild
                        2 -> Color(0xFFFFC107) // Yellow for moderate
                        3 -> Color(0xFFFF9800) // Orange for severe
                        else -> Color(0xFFE53935) // Red for very severe
                    }
                } else {
                    Color.LightGray
                }

                Surface(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(horizontal = 1.dp),
                    shape = CircleShape,
                    color = color
                ) {
                    // Empty surface with color
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmCancellationDialog(
    appointmentId: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Appointment") },
        text = { 
            Text("Are you sure you want to cancel this appointment? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(appointmentId) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFE53935) // Red color for confirm cancellation
                )
            ) {
                Text("Yes, Cancel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No, Keep")
            }
        }
    )
}

