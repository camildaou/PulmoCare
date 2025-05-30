package com.example.pulmocare.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pulmocare.data.repository.Doctor
import com.example.pulmocare.data.repository.DoctorRepository
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorsScreen(
    onBookAppointment: (String) -> Unit
) {
    val context = LocalContext.current
    val doctorRepository = remember { DoctorRepository(context) }
    val doctors = remember { doctorRepository.doctors }
    val isLoading = remember { doctorRepository.isLoading }
    val error = remember { doctorRepository.error }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    val coroutineScope = rememberCoroutineScope()
      // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filtered doctors based on search query
    val filteredDoctors = remember(doctors, searchQuery) {
        if (searchQuery.isBlank()) {
            doctors
        } else {
            doctors.filter { doctor ->
                doctor.name.contains(searchQuery, ignoreCase = true) ||
                doctor.location.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Fetch doctors when the screen is first displayed
    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            doctorRepository.fetchDoctors()
        }
    }

    Scaffold { paddingValues ->
        if (selectedDoctor != null) {
            // Doctor detail view
            DoctorDetailScreen(
                doctor = selectedDoctor!!,
                onBack = { selectedDoctor = null },
                onBookAppointment = { onBookAppointment(selectedDoctor!!.id) }
            )
        } else {
            // Doctors list view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading.value -> {
                        // Show loading indicator
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    error.value != null -> {
                        // Show error message
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error loading doctors: ${error.value}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        doctorRepository.fetchDoctors()
                                    }
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                    doctors.isEmpty() -> {
                        // Show empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No doctors available",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        // Show doctors list with search and filters
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Our Specialists",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Find the right specialist for your respiratory health needs",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                              // Search bar
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = { Text("Search doctors by name or location") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                active = isSearchActive,
                                onSearch = { isSearchActive = false },
                                onActiveChange = { isSearchActive = it }
                            ) {
                                // Search suggestions (empty for now)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Doctor cards - use LazyColumn for the scrolling list
                            if (filteredDoctors.isEmpty() && searchQuery.isNotEmpty()) {
                                // No results from search
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "No doctors found matching \"$searchQuery\"",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            } else {
                                // Results list
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {                                    items(filteredDoctors) { doctor ->
                                        DoctorCard(
                                            doctor = doctor,
                                            onClick = { selectedDoctor = doctor },
                                            onBookAppointment = onBookAppointment
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorCard(
    doctor: Doctor,
    onClick: () -> Unit,
    onBookAppointment: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Doctor Image with border
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                ) {
                    AsyncImage(
                        model = doctor.image,
                        contentDescription = "Doctor's photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {                    
                    Text(
                        text = doctor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Specialty with pill background
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = doctor.specialty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location and availability in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = doctor.location,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Availability days with pills
                if (doctor.availability.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            doctor.availability.take(3).forEach { day ->
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = day,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            
                            if (doctor.availability.size > 3) {
                                Text(
                                    text = "+${doctor.availability.size - 3}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
              // Book appointment button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onBookAppointment(doctor.id) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth, 
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Book Appointment")
                }
            }
        }
    }
}

@Composable
fun DoctorDetailScreen(
    doctor: Doctor,
    onBack: () -> Unit,
    onBookAppointment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top app bar with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Doctor Profile",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main content in a scrollable column
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                // Hero section with doctor image and name
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Doctor image with border
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                        ) {
                            AsyncImage(
                                model = doctor.image,
                                contentDescription = "Doctor's photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = doctor.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Specialty pill
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = doctor.specialty,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contact info section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Contact Information",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Location info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Clinic Location",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = doctor.location,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Phone info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Phone Number",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = doctor.phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Email info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Email Address",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = doctor.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // About section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = doctor.bio,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Availability section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Available Days",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Days chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(doctor.availability) { day ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = day,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        // Show available time slots if any
                        if (doctor.availableTimes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Available Time Slots",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Group time slots by day
                            doctor.availableTimes.entries.take(3).forEach { (day, slots) ->
                                Column(
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Text(
                                        text = day.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(slots.take(5)) { timeSlot ->
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text(
                                                    text = timeSlot,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        
                                        if (slots.size > 5) {
                                            item {                                                Text(
                                                    text = "+${slots.size - 5} more",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier
                                                        .padding(horizontal = 4.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if (doctor.availableTimes.size > 3) {
                                Text(
                                    text = "... and ${doctor.availableTimes.size - 3} more days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Book appointment button
                Button(
                    onClick = onBookAppointment,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Book Appointment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

