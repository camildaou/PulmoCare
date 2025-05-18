// ui/screens/DashboardScreen.kt
package com.example.pulmocare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pulmocare.data.UserRepository
import com.example.pulmocare.ui.theme.LightMuted
import com.example.pulmocare.ui.theme.LightMutedText
import com.example.pulmocare.ui.theme.MedicalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAppointments: () -> Unit,
    onNavigateToMedicalInfo: () -> Unit,
    onNavigateToDoctors: () -> Unit,
    onNavigateToAIAssessment: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val userRepository = remember { UserRepository() }
    val user = remember { userRepository.getCurrentUser() }
    var showNotifications by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Welcome, ${user?.firstName ?: "Guest"}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Manage your respiratory health",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightMutedText
                        )
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            Badge { Text("3") }
                        }
                    ) {
                        IconButton(onClick = { showNotifications = !showNotifications }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showNotifications,
                        onDismissRequest = { showNotifications = false }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            NotificationItem(
                                title = "Appointment Rescheduled",
                                description = "Your appointment with Dr. Johnson has been moved to April 18, 2025"
                            )

                            NotificationItem(
                                title = "Upcoming Vaccine",
                                description = "Your annual flu vaccine is due in 2 weeks"
                            )

                            NotificationItem(
                                title = "New Test Results",
                                description = "Your recent pulmonary function test results are available"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Feature Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    title = "Appointments",
                    description = "View and manage your scheduled appointments",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAppointments
                )

                FeatureCard(
                    title = "Medical Info",
                    description = "Access your medical records and documents",
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMedicalInfo
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                title = "Consult Doctors",
                description = "Find and connect with pulmonary specialists",
                icon = Icons.Default.People,
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToDoctors
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Health Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Health Summary",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Your recent health metrics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightMutedText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HealthMetricCard(
                            title = "Oxygen Level",
                            value = "98%",
                            modifier = Modifier.weight(1f)
                        )

                        HealthMetricCard(
                            title = "Respiratory Rate",
                            value = "16 bpm",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HealthMetricCard(
                            title = "Last Check-up",
                            value = "2 weeks ago",
                            modifier = Modifier.weight(1f)
                        )

                        HealthMetricCard(
                            title = "Medication Adherence",
                            value = "92%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Floating action button for symptom assessment is in the main app layout
        }
    }
}

@Composable
fun NotificationItem(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* Handle click */ }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = LightMutedText
        )
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MedicalBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MedicalBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = LightMutedText
            )
        }
    }
}

@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = LightMuted
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = LightMutedText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MedicalBlue,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}