package com.example.pulmocare.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import coil.compose.AsyncImage
import com.example.pulmocare.data.repository.AppointmentRepository
import com.example.pulmocare.data.repository.Doctor
import com.example.pulmocare.data.repository.DoctorRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    var showCancelDialog by remember { mutableStateOf(false) }
    var appointmentToCancel by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 for upcoming, 1 for past
    
    // For doctor selection and appointment scheduling
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var schedulingStep by remember { mutableStateOf(0) } // 0 = select doctor, 1 = select date/time, 2 = confirm
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf("") }
    var appointmentReason by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Fetch doctors when the screen is first displayed
    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            doctorRepository.fetchDoctors()
            appointmentRepository.fetchAppointmentsForCurrentPatient()
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
                    } else {
                        LazyColumn(
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
                                    }
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
            doctors = doctors,
            isLoading = isLoading.value,
            error = error.value,
            selectedDoctor = selectedDoctor,
            onDoctorSelected = { selectedDoctor = it; schedulingStep = 1 },
            schedulingStep = schedulingStep,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            selectedTime = selectedTime,
            onTimeSelected = { selectedTime = it },
            availableTimes = selectedDoctor?.availableTimes?.get(getDayOfWeek(selectedDate)) ?: emptyList(),
            appointmentReason = appointmentReason,
            onReasonChanged = { appointmentReason = it },
            onScheduleAppointment = {
                coroutineScope.launch {
                    selectedDoctor?.id?.let { doctorId -> 
                        appointmentRepository.scheduleAppointment(
                            doctorId = doctorId,
                            date = selectedDate.format(DateTimeFormatter.ISO_DATE),
                            time = selectedTime,
                            reason = appointmentReason
                        )
                        // Reset and close dialog
                        showScheduleDialog = false
                        schedulingStep = 0
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
    onCancel: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = doctorName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = specialty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status chip
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isPast) MaterialTheme.colorScheme.surfaceVariant 
                            else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (isPast) "Completed" else "Upcoming",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant 
                               else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$date at $time",
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
                    text = location,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (!isPast && onCancel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel Appointment")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    appointmentReason: String,
    onReasonChanged: (String) -> Unit,
    onScheduleAppointment: () -> Unit,
    onBackPressed: () -> Unit,
    onConfirmTimeAndDate: () -> Unit
) {
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
                        
                        // Time selection
                        Text(
                            text = "Select Time:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (availableTimes.isEmpty()) {
                            Text(
                                text = "No available times for selected date",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = 3
                            ) {
                                availableTimes.forEach { time -> 
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
                                        onClick = { onTimeSelected(time) }
                                    ) {
                                        Text(
                                            text = time,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 8.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Reason for appointment
                        OutlinedTextField(
                            value = appointmentReason,
                            onValueChange = onReasonChanged,
                            label = { Text("Reason for visit") },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                            icon = Icons.Default.Person,
                            label = "Doctor",
                            value = selectedDoctor?.name ?: ""
                        )
                        
                        AppointmentDetailItem(
                            icon = Icons.Default.MedicalServices,
                            label = "Specialty",
                            value = selectedDoctor?.specialty ?: ""
                        )
                        
                        AppointmentDetailItem(
                            icon = Icons.Default.CalendarToday,
                            label = "Date",
                            value = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
                        )
                        
                        AppointmentDetailItem(
                            icon = Icons.Default.Schedule,
                            label = "Time",
                            value = selectedTime
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
                    }
                }
            }
        },
        confirmButton = {            if (schedulingStep < 2) {
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
            AsyncImage(
                model = doctor.image,
                contentDescription = "Doctor's photo",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = doctor.specialty,
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
        }
        
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
fun getDayOfWeek(date: LocalDate): String {
    return date.dayOfWeek.toString().take(3)
}

