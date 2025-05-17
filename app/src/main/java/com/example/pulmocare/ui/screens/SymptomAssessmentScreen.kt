// ui/screens/SymptomAssessmentScreen.kt
package com.example.pulmocare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pulmocare.data.MedicalRepository
import com.example.pulmocare.data.SymptomAssessment
import com.example.pulmocare.ui.theme.LightMutedText
import com.example.pulmocare.ui.theme.MedicalBlue
import com.example.pulmocare.ui.theme.MedicalRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomAssessmentScreen() {
    val medicalRepository = remember { MedicalRepository() }

    var loading by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val symptoms = remember {
        mutableStateMapOf(
            "mainSymptom" to "",
            "duration" to "",
            "severity" to 5,
            "breathingDifficulty" to false,
            "coughing" to false,
            "wheezing" to false,
            "chestPain" to false,
            "fatigue" to false,
            "fever" to false,
            "additionalInfo" to "",
            "timeOfDay" to "",
            "triggers" to ""
        )
    }

    var assessment by remember { mutableStateOf<SymptomAssessment?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Symptom Assessment",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Describe your respiratory symptoms for a preliminary assessment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightMutedText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (!submitted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Respiratory Symptom Questionnaire",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Please provide detailed information about your symptoms",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightMutedText
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Main symptom
                        OutlinedTextField(
                            value = symptoms["mainSymptom"] as String,
                            onValueChange = { symptoms["mainSymptom"] = it },
                            label = { Text("What is your main symptom?") },
                            placeholder = { Text("e.g., Shortness of breath, coughing, etc.") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Duration
                        OutlinedTextField(
                            value = symptoms["duration"] as String,
                            onValueChange = { symptoms["duration"] = it },
                            label = { Text("How long have you had this symptom?") },
                            placeholder = { Text("e.g., 2 days, 1 week, etc.") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Severity
                        Text(
                            text = "How severe is your symptom? (1-10)",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = symptoms["severity"] as Int * 1f,
                            onValueChange = { symptoms["severity"] = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 9,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Mild (1)",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightMutedText
                            )
                            Text(
                                text = "Moderate (5)",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightMutedText
                            )
                            Text(
                                text = "Severe (10)",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightMutedText
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Time of day
                        Text(
                            text = "When do your symptoms occur?",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val timeOptions = listOf("morning", "afternoon", "evening", "night", "all-day")

                        timeOptions.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = symptoms["timeOfDay"] == option,
                                    onClick = { symptoms["timeOfDay"] = option }
                                )
                                Text(
                                    text = option.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Additional symptoms
                        Text(
                            text = "Do you have any of these additional symptoms?",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val symptomOptions = listOf(
                            "breathingDifficulty" to "Difficulty breathing",
                            "coughing" to "Coughing",
                            "wheezing" to "Wheezing",
                            "chestPain" to "Chest pain",
                            "fatigue" to "Fatigue",
                            "fever" to "Fever"
                        )

                        symptomOptions.forEach { (key, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = symptoms[key] as Boolean,
                                    onCheckedChange = { symptoms[key] = it }
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Triggers
                        OutlinedTextField(
                            value = symptoms["triggers"] as String,
                            onValueChange = { symptoms["triggers"] = it },
                            label = { Text("Do any specific activities or environments trigger your symptoms?") },
                            placeholder = { Text("e.g., Exercise, cold air, dust, etc.") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Additional info
                        OutlinedTextField(
                            value = symptoms["additionalInfo"] as String,
                            onValueChange = { symptoms["additionalInfo"] = it },
                            label = { Text("Additional information") },
                            placeholder = { Text("Please provide any other relevant information about your symptoms") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                loading = true
                                // Simulate API call
                                kotlinx.coroutines.MainScope().launch {
                                    delay(2000)
                                    assessment = medicalRepository.assessSymptoms(symptoms)
                                    loading = false
                                    submitted = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading && symptoms["mainSymptom"]?.toString()?.isNotBlank() == true
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Processing")
                            } else {
                                Text("Get Assessment")
                            }
                        }
                    }
                }
            }
        } else {
            // Assessment results
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Assessment card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
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
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = MedicalBlue
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Your Assessment",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Text(
                                text = "Based on the symptoms you've described",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightMutedText
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Possible Condition",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightMutedText
                                )

                                Text(
                                    text = assessment?.condition ?: "",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Urgency Level",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightMutedText
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                val (color, text) = when (assessment?.urgency) {
                                    "high" -> MedicalRed to "High - Seek immediate care"
                                    "medium" -> Color(0xFFF57C00) to "Medium - See doctor soon"
                                    else -> Color(0xFF4CAF50) to "Low - Monitor symptoms"
                                }

                                Badge(
                                    containerColor = color
                                ) {
                                    Text(
                                        text = text,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Follow-up Recommendation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightMutedText
                                )

                                Text(
                                    text = assessment?.followUp ?: "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = if (assessment?.urgency == "high")
                                            Icons.Default.Error else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (assessment?.urgency == "high")
                                            MedicalRed else Color(0xFF4CAF50),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = "Important Note",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "This is a preliminary assessment based on the information provided and should not replace professional medical advice. Always consult with a healthcare provider for proper diagnosis and treatment.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LightMutedText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recommendations card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Recommendations",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Steps you can take based on your symptoms",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightMutedText
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            assessment?.recommendations?.forEach { recommendation ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MedicalBlue,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = recommendation,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = { submitted = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Start New Assessment")
                            }
                        }
                    }
                }
            }
        }
    }
}