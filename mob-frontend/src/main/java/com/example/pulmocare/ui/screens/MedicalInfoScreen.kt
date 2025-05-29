package com.example.pulmocare.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pulmocare.ui.theme.LightMuted
import com.example.pulmocare.ui.theme.LightMutedText
import com.example.pulmocare.ui.theme.MedicalBlue
import com.example.pulmocare.ui.theme.MedicalRed
import com.example.pulmocare.ui.viewmodel.XrayViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalInfoScreen(
    onNavigateToMedicalReports: () -> Unit = {},
    onNavigateToXrayAnalysis: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Medical Information",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Access and analyze your medical data",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // X-ray Analysis Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable { onNavigateToXrayAnalysis() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Column {
                        Text(
                            text = "X-ray Analysis",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MedicalBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upload and analyze your chest X-rays with AI technology",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightMutedText
                        )
                    }
                    
                    // Bottom section with icon and button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(MedicalBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MedicalBlue
                            )
                        }
                        
                        Button(
                            onClick = { onNavigateToXrayAnalysis() }
                        ) {
                            Text("Analyze X-rays")
                        }
                    }
                }
            }
            
            // Medical Reports Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable { onNavigateToMedicalReports() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Column {
                        Text(
                            text = "Medical Reports",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MedicalBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upload and analyze your blood tests and laboratory reports",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightMutedText
                        )
                    }
                    
                    // Bottom section with icon and button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(MedicalBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MedicalBlue
                            )
                        }
                        
                        Button(
                            onClick = { onNavigateToMedicalReports() }
                        ) {
                            Text("View Reports")
                        }
                    }
                }
            }
            
            // Information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = LightMuted
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Why analyze your medical data?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Regular monitoring of your respiratory health helps in early detection of complications and allows for timely intervention.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightMutedText
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Disclaimer: AI analysis is not a substitute for professional medical diagnosis. Always consult with your healthcare provider.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedicalRed
                    )
                }
            }
        }
    }
}


//@Composable
//fun XrayUploadSection(onUploadClick: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(200.dp)
//            .border(
//                width = 1.dp,
//                color = LightMuted,
//                shape = RoundedCornerShape(8.dp)
//            )
//            .clip(RoundedCornerShape(8.dp))
//            .background(Color(0xFFF8F9FA))
//            .clickable { onUploadClick() },
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Icon(
//                imageVector = Icons.Default.CloudUpload,
//                contentDescription = "Upload X-ray",
//                modifier = Modifier.size(48.dp),
//                tint = MedicalBlue
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Upload X-ray Image",
//                style = MaterialTheme.typography.titleSmall,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = "Click to select a file from your device",
//                style = MaterialTheme.typography.bodySmall,
//                color = LightMutedText
//            )
//        }
//    }
//}
//
//@Composable
//fun XrayLoadingSection() {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(200.dp)
//            .border(
//                width = 1.dp,
//                color = LightMuted,
//                shape = RoundedCornerShape(8.dp)
//            )
//            .clip(RoundedCornerShape(8.dp))
//            .background(Color(0xFFF8F9FA)),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            CircularProgressIndicator(
//                color = MedicalBlue,
//                modifier = Modifier.size(48.dp)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "Analyzing X-ray...",
//                style = MaterialTheme.typography.titleSmall,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = "This may take a few moments",
//                style = MaterialTheme.typography.bodySmall,
//                color = LightMutedText
//            )
//        }
//    }
//}
//
//@Composable
//fun XrayErrorSection(
//    error: String,
//    onRetryClick: () -> Unit,
//    onNewImageClick: () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Icon(
//            imageVector = Icons.Default.Error,
//            contentDescription = "Error",
//            tint = MedicalRed,
//            modifier = Modifier.size(48.dp)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text(
//            text = "Error Processing X-ray",
//            style = MaterialTheme.typography.titleMedium,
//            color = MedicalRed
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = error,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurface
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Button(
//                onClick = onRetryClick,
//                modifier = Modifier.weight(1f)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Refresh,
//                    contentDescription = "Retry",
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(modifier = Modifier.size(4.dp))
//                Text("Retry")
//            }
//
//            Button(
//                onClick = onNewImageClick,
//                modifier = Modifier.weight(1f)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Image,
//                    contentDescription = "New Image",
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(modifier = Modifier.size(4.dp))
//                Text("New Image")
//            }
//        }
//    }
//}
//
//@Composable
//fun XrayResultSection(
//    imageUri: Uri?,
//    classification: String,
//////    assessment: String,
////    isAssessmentLoading: Boolean,
//    onNewXrayClick: () -> Unit
//) {
//    Column(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        // X-ray image
//        imageUri?.let { uri ->
//            AsyncImage(
//                model = uri,
//                contentDescription = "X-ray Image",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .clip(RoundedCornerShape(8.dp)),
//                contentScale = ContentScale.Crop
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Classification result
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Classification:",
//                style = MaterialTheme.typography.titleSmall,
//                fontWeight = FontWeight.Medium
//            )
//
//            Spacer(modifier = Modifier.size(8.dp))
//
//            Text(
//                text = classification,
//                style = MaterialTheme.typography.titleSmall,
//                color = when(classification) {
//                    "Normal" -> Color(0xFF28A745)
//                    "Covid-19" -> MedicalRed
//                    "Bacterial Pneumonia" -> Color(0xFFE67E22)
//                    else -> MaterialTheme.colorScheme.onSurface
//                },
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            Icon(
//                imageVector = Icons.Default.Check,
//                contentDescription = "Classified",
//                tint = Color(0xFF28A745),
//                modifier = Modifier.size(20.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
////        // AI Assessment Section
////        Card(
////            modifier = Modifier.fillMaxWidth(),
////            colors = CardDefaults.cardColors(
////                containerColor = Color(0xFFF8F9FA)
////            )
////        ) {
////            Column(
////                modifier = Modifier.padding(16.dp)
////            ) {
////                Row(
////                    verticalAlignment = Alignment.CenterVertically
////                ) {
////                    Text(
////                        text = "Gemini AI Assessment",
////                        style = MaterialTheme.typography.titleSmall,
////                        fontWeight = FontWeight.Medium
////                    )
////
////                    if (isAssessmentLoading) {
////                        Spacer(modifier = Modifier.size(8.dp))
////                        CircularProgressIndicator(
////                            modifier = Modifier.size(16.dp),
////                            strokeWidth = 2.dp
////                        )
////                    }
////                }
////
////                Spacer(modifier = Modifier.height(8.dp))
////
////                Text(
////                    text = assessment,
////                    style = MaterialTheme.typography.bodyMedium
////                )
////            }
////        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Upload new X-ray button
//        Button(
//            onClick = onNewXrayClick,
//            modifier = Modifier.align(Alignment.End)
//        ) {
//            Icon(
//                imageVector = Icons.Default.CloudUpload,
//                contentDescription = "Upload new X-ray",
//                modifier = Modifier.size(16.dp)
//            )
//            Spacer(modifier = Modifier.size(4.dp))
//            Text("Upload New X-ray")
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Medical disclaimer
//        Text(
//            text = "Disclaimer: This AI analysis is not a substitute for professional medical diagnosis. Please consult with a healthcare provider for proper diagnosis and treatment.",
//            style = MaterialTheme.typography.bodySmall,
//            color = LightMutedText
//        )
//    }
//}

// Include existing composables here - MedicalCategoryItem, MedicalDocumentItem, etc.
