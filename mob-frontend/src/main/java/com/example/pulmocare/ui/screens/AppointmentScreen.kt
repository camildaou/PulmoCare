package com.example.pulmocare.ui.screens

import android.os.Build
import androidx.compose.ui.text.font.FontWeight
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.FlowRow
import coil.compose.AsyncImage
import com.example.pulmocare.data.repository.AppointmentRepository
import com.example.pulmocare.data.model.Doctor
import com.example.pulmocare.data.repository.DoctorRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen() {
    val context = LocalContext.current
    val appointmentRepository = remember { AppointmentRepository(context) }
    val doctorRepository = remember { DoctorRepository(context) }
    
    val upcomingAppointments = remember { appointmentRepository.upcomingAppointments }
    val pastAppointments = remember { appointmentRepository.pastAppointments }
    val doctors = remember { doctorRepository.doctors }
    val isLoading = remember { doctorRepository.isLoading }
    val error = remember { doctorRepository.error }
    
    var showScheduleDialog by remember { mutableStateOf(false) }
    var availableTimeSlots by remember { mutableStateOf<List<String>>(emptyList()) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var appointmentToCancel by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 for upcoming, 1 for past
    
    // For doctor selection and appointment scheduling
    var selectedDoctor by remember { mutableStateOf<com.example.pulmocare.data.model.Doctor?>(null) }
    var schedulingStep by remember { mutableStateOf(0) } // 0 = select doctor, 1 = select date/time, 2 = confirm
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf("") }
    var appointmentReason by remember { mutableStateOf("") }
    
    // For time slot pagination
    var currentTimeSlotsPage by remember { mutableStateOf(0) }
    val timeSlotsPerPage = 9 // Show 9 time slots per page (3x3 grid)
    
    // Common appointment reasons
    val commonReasons = listOf(
        "Routine Check-up",
        "Follow-up Appointment",
        "Respiratory Issues",
        "Asthma Management",
        "COPD Management",
        "Sleep Apnea",
        "Lung Cancer Screening",
        "Pneumonia",
        "Tuberculosis",
        "Shortness of Breath",
        "Chronic Cough",
        "Other (please specify)"
    )
    
    val coroutineScope = rememberCoroutineScope()
      // Fetch doctors and appointments when the screen loads
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            doctorRepository.fetchAllDoctors()
            appointmentRepository.fetchAppointmentsForCurrentPatient()
            Log.d("AppointmentScreen", "Loaded initial doctors and appointments data")
        }
    }
    
    // Cancel confirmation dialog
    if (showCancelDialog && appointmentToCancel != null) {
        AlertDialog(
            onDismissRequest = { 
                showCancelDialog = false 
                appointmentToCancel = null
            },
            title = { Text("Cancel Appointment") },
            text = { Text("Are you sure you want to cancel this appointment?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            appointmentToCancel?.let {
                                appointmentRepository.cancelAppointment(it)
                            }
                            showCancelDialog = false
                            appointmentToCancel = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showCancelDialog = false 
                        appointmentToCancel = null
                    }
                ) {
                    Text("No, Keep It")
                }
            }
        )
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Reset scheduling state
                    selectedDoctor = null
                    schedulingStep = 0
                    selectedDate = LocalDate.now()
                    selectedTime = ""
                    appointmentReason = ""
                    showScheduleDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Schedule Appointment"
                )
            }
        }
    ) { paddingValues -> 
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
            
            // Tabs for Upcoming and Past appointments
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Upcoming") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Past") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Appointments list based on selected tab
            when (selectedTab) {
                0 -> {
                    // Upcoming appointments
                    if (upcomingAppointments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No upcoming appointments. Schedule one now!")
                        }
                    } else {                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(upcomingAppointments) { appointment -> 
                                AppointmentCard(
                                    doctorName = appointment.doctor?.getFullName() ?: "Unknown Doctor",
                                    specialty = appointment.doctor?.specialization ?: "",
                                    date = appointment.date,
                                    time = appointment.hour,
                                    location = appointment.location,
                                    onCancel = {
                                        // Show cancel confirmation dialog
                                        appointment.id?.let {
                                            appointmentToCancel = it
                                            showCancelDialog = true
                                        }
                                    },
                                    appointment = appointment
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Past appointments
                    if (pastAppointments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No past appointments.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pastAppointments) { appointment -> 
                                AppointmentCard(
                                    doctorName = appointment.doctor?.getFullName() ?: "Unknown Doctor",
                                    specialty = appointment.doctor?.specialization ?: "",
                                    date = appointment.date,
                                    time = appointment.hour,
                                    location = appointment.location,
                                    isPast = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Schedule appointment dialog
    if (showScheduleDialog) {
        ScheduleAppointmentDialog(
            onDismiss = { showScheduleDialog = false },
            doctors = doctorRepository.getModelDoctors(),
            isLoading = isLoading.value,
            error = error.value,
            selectedDoctor = selectedDoctor,            
            onDoctorSelected = { doctor -> 
                selectedDoctor = doctor
                // Fetch time slots for the selected doctor
                coroutineScope.launch {
                    availableTimeSlots = doctorRepository.getTimeSlotsByDoctorAndDay(doctor.id ?: "", selectedDate)
                    currentTimeSlotsPage = 0 // Reset to first page when doctor changes
                    Log.d("AppointmentScreen", "Fetched ${availableTimeSlots.size} time slots for newly selected doctor")
                }
                schedulingStep = 1
            },
            schedulingStep = schedulingStep,
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            onTimeSelected = { selectedTime = it },                
            availableTimes = availableTimeSlots,
            currentPage = currentTimeSlotsPage,
            onPageChanged = { currentTimeSlotsPage = it },
            timeSlotsPerPage = timeSlotsPerPage,
            onDateSelected = { newDate ->
                selectedDate = newDate
                // Fetch time slots directly from the backend when date changes
                coroutineScope.launch {
                    selectedDoctor?.id?.let { doctorId ->
                        val slots = doctorRepository.getTimeSlotsByDoctorAndDay(doctorId, selectedDate)
                        availableTimeSlots = slots
                        currentTimeSlotsPage = 0 // Reset to first page when date changes
                        Log.d("AppointmentScreen", "Fetched ${slots.size} time slots for doctor $doctorId on $selectedDate")
                    }
                }
            },
            appointmentReason = appointmentReason,
            onReasonChanged = { appointmentReason = it },
            commonReasons = commonReasons,
            onScheduleAppointment = {
                coroutineScope.launch {
                    selectedDoctor?.let { doctor ->
                        val formattedDate = selectedDate.format(DateTimeFormatter.ISO_DATE)
                        val startTime = selectedTime.split(" - ")[0].trim()
                        Log.d("AppointmentScreen", "Scheduling appointment:")
                        Log.d("AppointmentScreen", "- Doctor: ${doctor.id} (${doctor.firstName} ${doctor.lastName})")
                        Log.d("AppointmentScreen", "- Date: $formattedDate")
                        Log.d("AppointmentScreen", "- Full time slot: $selectedTime")
                        Log.d("AppointmentScreen", "- Day of week: ${selectedDate.dayOfWeek.toString().toLowerCase().substring(0, 3)}")
                        Log.d("AppointmentScreen", "- Reason: $appointmentReason")
                        Log.d("AppointmentScreen", "- Doctor available days: ${doctor.availableDays}")
                        Log.d("AppointmentScreen", "- Doctor time slots: ${doctor.availableTimeSlots}")
                        
                        // Add a small delay for logs to be displayed
//                        delay(100)
                        
                        val result = appointmentRepository.scheduleAppointment(
                            doctor = doctor,
                            date = formattedDate,
                            time = selectedTime, // Send the full time slot string
                            reason = appointmentReason
                        )
                        
                        result.onSuccess { appointment ->
                            // Show success message
                            Toast.makeText(context, "Appointment scheduled successfully!", Toast.LENGTH_SHORT).show()
                            
                            // Update local state to avoid double-booking
                            // Refresh the doctor list to get updated availability
                            coroutineScope.launch {
                                doctorRepository.fetchAllDoctors()
                                appointmentRepository.fetchAppointmentsForCurrentPatient()
                                Log.d("AppointmentScreen", "Refreshed doctor and appointment data after scheduling")
                            }
                            
                            // Reset and close dialog
                            showScheduleDialog = false
                            schedulingStep = 0
                        }.onFailure { error ->
                            // Show error message
                            Toast.makeText(context, "Failed to schedule: ${error.message}", Toast.LENGTH_LONG).show()
                            Log.e("AppointmentScreen", "Failed to schedule appointment", error)
                        }
                    }
                }
            },
            onBackPressed = {
                when (schedulingStep) {
                    0 -> showScheduleDialog = false
                    else -> schedulingStep--
                }
            },
            onConfirmTimeAndDate = { schedulingStep = 2 }
        )
    }
}

@Composable
fun AppointmentCard(
    doctorName: String,
    specialty: String,
    date: String,
    time: String,
    location: String,
    isPast: Boolean = false,
    onCancel: (() -> Unit)? = null,
    appointment: com.example.pulmocare.data.model.Appointment? = null
) {
    val context = LocalContext.current
      Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = MaterialTheme.shapes.medium,
        border = if (!isPast) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ){
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card header with doctor info and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Doctor avatar placeholder (can be replaced with actual image)
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = doctorName.take(1),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = doctorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = specialty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status chip with improved styling
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isPast) 
                        MaterialTheme.colorScheme.surfaceVariant 
                    else 
                        MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isPast) 
                            MaterialTheme.colorScheme.outline 
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPast) Icons.Default.Check else Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isPast) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPast) "Completed" else "Upcoming",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isPast) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Enhanced appointment details with visual separation
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .animateContentSize() // Add smooth animation when content changes
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Date and time with improved visual
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Date & Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$date at $time",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Location with prominent styling
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Location",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (!isPast) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // Action buttons with improved styling
            if (!isPast) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add to Google Calendar button
                    if (appointment != null) {
                        OutlinedButton(
                            onClick = {
                                val intent = com.example.pulmocare.data.GoogleCalendarHelper.createAddToCalendarIntent(context, appointment)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Add to Google Calendar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Add to Calendar",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Cancel button
                    if (onCancel != null) {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel appointment",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Cancel",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    doctors: List<Doctor>,
    isLoading: Boolean,
    error: String?,
    selectedDoctor: Doctor?,
    onDoctorSelected: (Doctor) -> Unit,
    schedulingStep: Int,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    availableTimes: List<String>,
    currentPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
    timeSlotsPerPage: Int = 9,
    appointmentReason: String,
    onReasonChanged: (String) -> Unit,
    commonReasons: List<String> = emptyList(),
    onScheduleAppointment: () -> Unit,
    onBackPressed: () -> Unit,
    onConfirmTimeAndDate: () -> Unit
) {
    // For dropdown menu control
    var isReasonDropdownExpanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (schedulingStep) {
                    0 -> "Select a Doctor"
                    1 -> "Select Date and Time"
                    else -> "Confirm Appointment"
                }
            )
        },
        text = {
            when (schedulingStep) {
                0 -> {
                    // Select doctor step
                    Box(modifier = Modifier.height(400.dp)) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            error != null -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Error loading doctors: $error",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            doctors.isEmpty() -> {
                                Text(
                                    text = "No doctors available",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            else -> {
                                LazyColumn {
                                    items(doctors) { doctor -> 
                                        DoctorSelectionItem(
                                            doctor = doctor,
                                            isSelected = doctor == selectedDoctor,
                                            onClick = { onDoctorSelected(doctor) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Date and time selection step
                    Column {
                        // Simple date picker (you can use a more sophisticated date picker)
                        Text(
                            text = "Select Date:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Display a few dates (you can implement a calendar here)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val today = LocalDate.now()
                            for (i in 0..4) {
                                val date = today.plusDays(i.toLong())
                                val isSelected = date == selectedDate
                                
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.small,
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                            else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary 
                                               else MaterialTheme.colorScheme.outline
                                    ),
                                    onClick = { onDateSelected(date) }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = date.dayOfWeek.toString().take(3),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Time selection header with refresh button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available Time Slots:",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                              
                            // Add refresh button to allow manual refresh of doctor availability
                            IconButton(onClick = {
                                selectedDoctor?.id?.let { doctorId ->
                                    Log.d("AppointmentScreen", "Manually refreshing doctor time slots")
                                    // Directly call the refreshed endpoint
                                    onDateSelected(selectedDate) // This will trigger the time slot fetch
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh time slots",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        if (availableTimes.isEmpty()) {
                            Text(
                                text = "No available times for selected date",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            // Add a note about the time slot duration
                            Text(
                                text = "All appointments are 30-minute slots",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // Pagination implementation
                            val totalPages = (availableTimes.size + timeSlotsPerPage - 1) / timeSlotsPerPage
                            val startIndex = currentPage * timeSlotsPerPage
                            val endIndex = minOf(startIndex + timeSlotsPerPage, availableTimes.size)
                            val currentPageSlots = availableTimes.subList(startIndex, endIndex)
                            
                // Display available time slots in a grid with pagination
                            Column {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    maxItemsInEachRow = 3
                                ) {
                                    // Each time slot is a 30-minute block (validated in backend)
                                    currentPageSlots.forEach { time -> 
                                        val isSelected = time == selectedTime
                                        
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                                    else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary 
                                                    else MaterialTheme.colorScheme.outline
                                            ),
                                            onClick = { 
                                                onTimeSelected(time) 
                                                Log.d("AppointmentScreen", "Selected time slot: $time")
                                            }
                                        ) {
                                            Text(
                                                text = time,
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 8.dp
                                                ),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                                
                                // Only show pagination if more than one page
                                if (totalPages > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { 
                                                if (currentPage > 0) onPageChanged(currentPage - 1) 
                                            },
                                            enabled = currentPage > 0
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = "Previous page",
                                                tint = if (currentPage > 0) 
                                                    MaterialTheme.colorScheme.primary
                                                else 
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                            )
                                        }
                                        
                                        Text(
                                            text = "Page ${currentPage + 1}/$totalPages",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        
                                        IconButton(
                                            onClick = { 
                                                if (currentPage < totalPages - 1) onPageChanged(currentPage + 1) 
                                            },
                                            enabled = currentPage < totalPages - 1
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = "Next page",
                                                tint = if (currentPage < totalPages - 1) 
                                                    MaterialTheme.colorScheme.primary
                                                else 
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Reason for appointment dropdown
                        Column {
                            Text(
                                text = "Reason for Visit:",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Dropdown field
                            ExposedDropdownMenuBox(
                                expanded = isReasonDropdownExpanded,
                                onExpandedChange = { isReasonDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = appointmentReason,
                                    onValueChange = onReasonChanged,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    readOnly = commonReasons.isNotEmpty(),
                                    label = { Text("Select or enter reason") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isReasonDropdownExpanded)
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                )
                                
                                // Only show dropdown if we have common reasons
                                if (commonReasons.isNotEmpty()) {
                                    ExposedDropdownMenu(
                                        expanded = isReasonDropdownExpanded,
                                        onDismissRequest = { isReasonDropdownExpanded = false }
                                    ) {
                                        commonReasons.forEach { reason ->
                                            DropdownMenuItem(
                                                text = { Text(reason) },
                                                onClick = {
                                                    onReasonChanged(reason)
                                                    isReasonDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Confirmation step
                    Column {
                        Text(
                            text = "Appointment Details",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AppointmentDetailItem(
                            icon = Icons.Default.Schedule,
                            label = "Time",
                            value = selectedTime
                        )
                        
                        AppointmentDetailItem(
                            icon = Icons.Default.MedicalServices,
                            label = "Specialty",
                            value = selectedDoctor?.specialization ?: ""
                        )
                        
                        AppointmentDetailItem(
                            icon = Icons.Default.CalendarToday,
                            label = "Date",
                            value = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
                        )
                        
                        AppointmentDetailItem(
                            icon = Icons.Default.LocationOn,
                            label = "Location",
                            value = selectedDoctor?.location ?: ""
                        )
                        
                        if (appointmentReason.isNotEmpty()) {
                            AppointmentDetailItem(
                                icon = Icons.Default.Info,
                                label = "Reason",
                                value = appointmentReason
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Availability warning
                        if (selectedDoctor != null && selectedTime.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "This appointment will be scheduled for a 30-minute slot. Please arrive on time.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (schedulingStep < 2) {
                TextButton(
                    onClick = {
                        when (schedulingStep) {
                            0 -> {
                                if (selectedDoctor != null) {
                                    // Proceed to date/time selection
                                    // schedulingStep = 1 - Handled by onDoctorSelected
                                }
                            }
                            1 -> {
                                if (selectedTime.isNotEmpty()) {
                                    // Proceed to confirmation
                                    onConfirmTimeAndDate()
                                }
                            }
                        }
                    },
                    enabled = when (schedulingStep) {
                        0 -> selectedDoctor != null
                        1 -> selectedTime.isNotEmpty()
                        else -> true
                    }
                ) {
                    Text("Next")
                }
            } else {
                Button(
                    onClick = onScheduleAppointment,
                    enabled = selectedDoctor != null && selectedTime.isNotEmpty()
                ) {
                    Text("Schedule Appointment")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onBackPressed) {
                Text(if (schedulingStep == 0) "Cancel" else "Back")
            }
        }
    )
}

@Composable
fun DoctorSelectionItem(
    doctor: Doctor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            AsyncImage(
//                model = doctor.image,
//                contentDescription = "Doctor's photo",
//                modifier = Modifier
//                    .size(56.dp)
//                    .clip(CircleShape),
//                contentScale = ContentScale.Crop
//            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {                Text(
                    text = doctor.firstName + " " + doctor.lastName,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = doctor.specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                          Text(
                    text = doctor.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Display availability days
                if (doctor.availableDays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Available: ${doctor.availableDays.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AppointmentDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints -> 
        val itemWidth = (constraints.maxWidth / maxItemsInEachRow.coerceAtMost(measurables.size))
            .coerceAtMost(constraints.maxWidth)
        
        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowWidth = 0
        
        measurables.forEach { measurable -> 
            val placeable = measurable.measure(
                androidx.compose.ui.unit.Constraints(
                    minWidth = 0,
                    maxWidth = itemWidth
                )
            )
            
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            
            currentRow.add(placeable)
            currentRowWidth += placeable.width
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }
        
        val height = rows.sumOf { row -> 
            row.maxOfOrNull { it.height } ?: 0
        } + (rows.size - 1) * 8 // Add vertical spacing
        
        layout(constraints.maxWidth, height) {
            var y = 0
            
            rows.forEach { row -> 
                val rowHeight = row.maxOfOrNull { it.height } ?: 0
                val horizontalSpacing = 8 // Fixed spacing between items
                
                var x = 0 // Starting from the left
                
                row.forEach { placeable -> 
                    placeable.placeRelative(x, y)
                    x += placeable.width + horizontalSpacing
                }
                
                y += rowHeight + 8 // Add vertical spacing between rows
            }
        }
    }
}

// Helper function to get day of week from LocalDate
@RequiresApi(Build.VERSION_CODES.O)
fun getDayOfWeek(date: LocalDate): String {
    return date.dayOfWeek.toString().take(3)
}

// Helper function to split doctor's time slots into 30-minute increments
fun splitInto30MinuteSlots(timeSlots: List<Doctor.TimeSlot>): List<String> {
    Log.d("AppointmentScreen", "Processing time slots: $timeSlots")
    
    val formattedSlots = timeSlots.map { "${it.startTime} - ${it.endTime}" }
    Log.d("AppointmentScreen", "Formatted time slots for display: $formattedSlots")
    
    // The backend expects time slots to be exactly 30 minutes
    // TimeSlot objects are already validated in the Doctor class to be 30-min slots
    // The validator ensures each time slot is exactly 30 minutes (endTime - startTime = 30min)
    return formattedSlots
}