package com.example.pulmocare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pulmocare.ui.theme.MedicalBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssessmentScreen() {
    var currentStep by remember { mutableStateOf(0) }
    var userResponses by remember { mutableStateOf(mapOf<String, String>()) }
    var isAssessing by remember { mutableStateOf(false) }
    var assessmentResult by remember { mutableStateOf<AssessmentResult?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val steps = listOf(
        AssessmentStep(
            title = "Main Symptoms",
            question = "What are your main symptoms?",
            options = listOf(
                "Cough",
                "Shortness of breath",
                "Chest pain",
                "Wheezing",
                "Fatigue",
                "Fever"
            ),
            allowMultiple = true
        ),
        AssessmentStep(
            title = "Symptom Duration",
            question = "How long have you been experiencing these symptoms?",
            options = listOf(
                "Less than 24 hours",
                "1-3 days",
                "4-7 days",
                "1-2 weeks",
                "More than 2 weeks"
            )
        ),
        AssessmentStep(
            title = "Symptom Severity",
            question = "How would you rate the severity of your symptoms?",
            options = listOf(
                "Mild - noticeable but not interfering with daily activities",
                "Moderate - somewhat interfering with daily activities",
                "Severe - significantly interfering with daily activities",
                "Very severe - unable to perform daily activities"
            )
        ),
        AssessmentStep(
            title = "Triggers",
            question = "Do any of the following trigger or worsen your symptoms?",
            options = listOf(
                "Exercise or physical activity",
                "Cold air",
                "Allergens (dust, pollen, etc.)",
                "Smoke or strong odors",
                "Stress or anxiety",
                "None of the above"
            ),
            allowMultiple = true
        ),
        AssessmentStep(
            title = "Medical History",
            question = "Do you have any of the following conditions?",
            options = listOf(
                "Asthma",
                "COPD",
                "Bronchitis",
                "Pneumonia",
                "Heart disease",
                "None of the above"
            ),
            allowMultiple = true
        )
    )

    Scaffold { paddingValues ->
        if (assessmentResult != null) {
            // Show assessment result
            AssessmentResultScreen(
                result = assessmentResult!!,
                onStartNewAssessment = {
                    currentStep = 0
                    userResponses = mapOf()
                    assessmentResult = null
                },
                modifier = Modifier.padding(paddingValues)
            )
        } else if (isAssessing) {
            // Show loading screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MedicalBlue
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Analyzing your symptoms...",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else if (currentStep < steps.size) {
            // Show assessment steps
            val currentStepData = steps[currentStep]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                LinearProgressIndicator(
                    progress = (currentStep + 1).toFloat() / steps.size,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Step ${currentStep + 1} of ${steps.size}: ${currentStepData.title}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = currentStepData.question,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (currentStepData.allowMultiple) {
                    // Multiple choice options
                    val selectedOptions = remember { mutableStateListOf<String>() }

                    currentStepData.options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedOptions.contains(option),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedOptions.add(option)
                                    } else {
                                        selectedOptions.remove(option)
                                    }
                                }
                            )

                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentStep > 0) {
                                    currentStep--
                                }
                            },
                            enabled = currentStep > 0
                        ) {
                            Text("Previous")
                        }

                        Button(
                            onClick = {
                                userResponses = userResponses + (currentStepData.title to selectedOptions.joinToString(", "))
                                if (currentStep < steps.size - 1) {
                                    currentStep++
                                } else {
                                    // Final step, generate assessment
                                    isAssessing = true
                                    coroutineScope.launch {
                                        // Simulate AI processing
                                        delay(2000)
                                        assessmentResult = generateAssessmentResult(userResponses)
                                        isAssessing = false
                                    }
                                }
                            },
                            enabled = selectedOptions.isNotEmpty()
                        ) {
                            Text(if (currentStep < steps.size - 1) "Next" else "Get Assessment")
                        }
                    }
                } else {
                    // Single choice options
                    var selectedOption by remember { mutableStateOf<String?>(null) }

                    currentStepData.options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option }
                            )

                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentStep > 0) {
                                    currentStep--
                                }
                            },
                            enabled = currentStep > 0
                        ) {
                            Text("Previous")
                        }

                        Button(
                            onClick = {
                                selectedOption?.let { option ->
                                    userResponses = userResponses + (currentStepData.title to option)
                                    if (currentStep < steps.size - 1) {
                                        currentStep++
                                    } else {
                                        // Final step, generate assessment
                                        isAssessing = true
                                        coroutineScope.launch {
                                            // Simulate AI processing
                                            delay(2000)
                                            assessmentResult = generateAssessmentResult(userResponses)
                                            isAssessing = false
                                        }
                                    }
                                }
                            },
                            enabled = selectedOption != null
                        ) {
                            Text(if (currentStep < steps.size - 1) "Next" else "Get Assessment")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssessmentResultScreen(
    result: AssessmentResult,
    onStartNewAssessment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = when (result.urgencyLevel) {
                UrgencyLevel.LOW -> Icons.Default.CheckCircle
                UrgencyLevel.MEDIUM -> Icons.Default.Info
                UrgencyLevel.HIGH -> Icons.Default.Warning
                UrgencyLevel.EMERGENCY -> Icons.Default.Error
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = when (result.urgencyLevel) {
                UrgencyLevel.LOW -> MaterialTheme.colorScheme.tertiary
                UrgencyLevel.MEDIUM -> MaterialTheme.colorScheme.primary
                UrgencyLevel.HIGH -> MaterialTheme.colorScheme.secondary
                UrgencyLevel.EMERGENCY -> MaterialTheme.colorScheme.error
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = result.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Urgency: ${result.urgencyLevel.displayName}",
            style = MaterialTheme.typography.titleMedium,
            color = when (result.urgencyLevel) {
                UrgencyLevel.LOW -> MaterialTheme.colorScheme.tertiary
                UrgencyLevel.MEDIUM -> MaterialTheme.colorScheme.primary
                UrgencyLevel.HIGH -> MaterialTheme.colorScheme.secondary
                UrgencyLevel.EMERGENCY -> MaterialTheme.colorScheme.error
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Assessment",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = result.assessment,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Recommendations",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                result.recommendations.forEach { recommendation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowRight,
                            contentDescription = null,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartNewAssessment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start New Assessment")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Disclaimer: This assessment is for informational purposes only and does not constitute medical advice. Always consult with a healthcare professional for medical concerns.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

// Helper function to generate assessment result based on user responses
private fun generateAssessmentResult(responses: Map<String, String>): AssessmentResult {
    // This is a simplified example. In a real app, this would use a more sophisticated algorithm
    // or call an actual AI service.

    val symptoms = responses["Main Symptoms"] ?: ""
    val duration = responses["Symptom Duration"] ?: ""
    val severity = responses["Symptom Severity"] ?: ""

    val urgencyLevel = when {
        severity.contains("Very severe") -> UrgencyLevel.EMERGENCY
        severity.contains("Severe") -> UrgencyLevel.HIGH
        severity.contains("Moderate") && (duration.contains("More than 2 weeks") || duration.contains("1-2 weeks")) -> UrgencyLevel.HIGH
        severity.contains("Moderate") -> UrgencyLevel.MEDIUM
        severity.contains("Mild") && (duration.contains("More than 2 weeks") || duration.contains("1-2 weeks")) -> UrgencyLevel.MEDIUM
        else -> UrgencyLevel.LOW
    }

    val title = when (urgencyLevel) {
        UrgencyLevel.EMERGENCY -> "Seek Immediate Medical Attention"
        UrgencyLevel.HIGH -> "Consult a Doctor Soon"
        UrgencyLevel.MEDIUM -> "Monitor Symptoms and Consider Medical Consultation"
        UrgencyLevel.LOW -> "Self-Care Recommended"
    }

    val assessment = when (urgencyLevel) {
        UrgencyLevel.EMERGENCY -> "Your symptoms suggest a potentially serious respiratory condition that requires immediate medical attention."
        UrgencyLevel.HIGH -> "Your symptoms indicate a significant respiratory issue that should be evaluated by a healthcare professional soon."
        UrgencyLevel.MEDIUM -> "Your symptoms suggest a moderate respiratory condition. While not immediately urgent, medical consultation is recommended if symptoms persist or worsen."
        UrgencyLevel.LOW -> "Your symptoms appear to be mild and may be managed with self-care measures. Monitor for any changes in your condition."
    }

    val recommendations = when (urgencyLevel) {
        UrgencyLevel.EMERGENCY -> listOf(
            "Go to the nearest emergency room or call emergency services immediately.",
            "Do not drive yourself if you are experiencing severe shortness of breath.",
            "Inform medical staff of all your symptoms and any relevant medical history."
        )
        UrgencyLevel.HIGH -> listOf(
            "Schedule an appointment with your doctor within the next 1-2 days.",
            "If symptoms worsen before your appointment, seek immediate medical care.",
            "Rest and avoid strenuous activities until you see a doctor.",
            "Keep track of any changes in your symptoms to report to your doctor."
        )
        UrgencyLevel.MEDIUM -> listOf(
            "Schedule a routine appointment with your doctor in the next week.",
            "Rest and stay hydrated.",
            "Avoid known triggers that worsen your symptoms.",
            "Consider over-the-counter medications for symptom relief as appropriate.",
            "If symptoms worsen significantly, seek medical attention sooner."
        )
        UrgencyLevel.LOW -> listOf(
            "Rest and stay hydrated.",
            "Use over-the-counter medications as needed for symptom relief.",
            "Avoid known triggers that worsen your symptoms.",
            "Monitor your symptoms for any changes.",
            "If symptoms persist for more than two weeks or worsen, consult a healthcare provider."
        )
    }

    return AssessmentResult(
        title = title,
        urgencyLevel = urgencyLevel,
        assessment = assessment,
        recommendations = recommendations
    )
}

data class AssessmentStep(
    val title: String,
    val question: String,
    val options: List<String>,
    val allowMultiple: Boolean = false
)

data class AssessmentResult(
    val title: String,
    val urgencyLevel: UrgencyLevel,
    val assessment: String,
    val recommendations: List<String>
)

enum class UrgencyLevel(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    EMERGENCY("Emergency")
}

