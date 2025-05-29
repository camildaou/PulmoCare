// ui/screens/ProfileScreen.kt
package com.example.pulmocare.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pulmocare.data.model.Patient
import com.example.pulmocare.ui.theme.LightMutedText
import com.example.pulmocare.ui.viewmodel.PatientProfileViewModel
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    patientId: String,
    onLogout: () -> Unit
) {
    // Use the PatientProfileViewModel
    val viewModel: PatientProfileViewModel = viewModel()
    val patientState by viewModel.patientState.collectAsState()
    
    // Load patient data when screen is first displayed
    LaunchedEffect(patientId) {
        viewModel.getPatientProfile(patientId)
    }    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Your Profile",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "View and manage your personal information",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle different states from the ViewModel
            when (patientState) {
                is PatientProfileViewModel.PatientState.Loading -> {
                    Spacer(modifier = Modifier.height(100.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading profile...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                is PatientProfileViewModel.PatientState.Error -> {
                    val errorState = patientState as PatientProfileViewModel.PatientState.Error
                    Spacer(modifier = Modifier.height(100.dp))
                    Text(
                        text = "Error loading profile: ${errorState.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.getPatientProfile(patientId) }) {
                        Text("Retry")
                    }
                }
                
                is PatientProfileViewModel.PatientState.Success -> {
                    // Display patient profile when data is successfully loaded
                    val patient = (patientState as PatientProfileViewModel.PatientState.Success).patient
                    PatientProfileContent(patient = patient)
                }
                
                else -> { /* Initial state, do nothing */ }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Log Out")
            }
        }
    }
}

@Composable
private fun PatientProfileContent(patient: Patient) {
    val viewModel: PatientProfileViewModel = viewModel()
    val updateState by viewModel.updateState.collectAsState()
    
    // Context for accessing content providers
    val context = LocalContext.current
    
    // State for photo URI
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
        if (uri != null) {
            // When photo is selected, convert to base64 and update patient
            val bitmap = getBitmapFromUri(context, uri)
            val base64Photo = bitmapToBase64(bitmap)
            
            // Create a copy of patient with updated photo
            val updatedPatient = patient.copy(photo = base64Photo)
            
            // Call viewModel to update patient
            patient.id?.let { viewModel.updatePatientProfile(it, updatedPatient) }
        }
    }
    
    // Handle update state changes
    LaunchedEffect(updateState) {
        if (updateState is PatientProfileViewModel.UpdateState.Success) {
            // Reset state after successful update
            viewModel.resetUpdateState()
        }
    }
    
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Profile picture card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Profile Picture",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
                if (updateState is PatientProfileViewModel.UpdateState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(120.dp)
                    )
                } else {
                    if (patient.photo != null) {
                        // Convert base64 string to bitmap and display
                        val bitmap = base64ToBitmap(patient.photo)
                        if (bitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Fallback if base64 decoding fails
                            AsyncImage(
                                model = "https://via.placeholder.com/150",
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        // No photo available
                        AsyncImage(
                            model = "https://via.placeholder.com/150",
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${patient.firstName} ${patient.lastName}",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Picture",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Photo")
                }
                
                // Show error if update failed
                if (updateState is PatientProfileViewModel.UpdateState.Error) {
                    val error = (updateState as PatientProfileViewModel.UpdateState.Error).message
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Show error if update failed
                if (updateState is PatientProfileViewModel.UpdateState.Error) {
                    val error = (updateState as PatientProfileViewModel.UpdateState.Error).message
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Profile info card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Personal Info",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contact Information
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = patient.email ?: "Not provided",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Age: ${patient.age}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = patient.location ?: "Not provided",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Medical Information Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Medical Information",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show allergies if available
            patient.allergies?.let { allergies -> 
                if (allergies.isNotEmpty()) {
                    Text(
                        text = "Allergies:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = allergies.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Show chronic conditions if available
            patient.chronicConditions?.let { conditions -> 
                if (conditions.isNotEmpty()) {
                    Text(
                        text = "Chronic Conditions:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = conditions.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Blood type
            Text(
                text = "Blood Type: ${patient.bloodType ?: "Not provided"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Height and weight
            Row {
                Text(
                    text = "Height: ${patient.height?.toString() ?: "Not provided"} cm",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Weight: ${patient.weight?.toString() ?: "Not provided"} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lifestyle information
            Row {
                Text(
                    text = "Smoking: ${if (patient.smoking == true) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Pets: ${if (patient.hasPets == true) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Insurance Information Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Insurance Information",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Provider: ${patient.insuranceProvider ?: "Not provided"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LightMutedText
        )

        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            leadingIcon = leadingIcon,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Helper function to convert bitmap to base64 string
fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

// Helper function to get bitmap from Uri
fun getBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

// Helper function to convert base64 string to bitmap
fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}