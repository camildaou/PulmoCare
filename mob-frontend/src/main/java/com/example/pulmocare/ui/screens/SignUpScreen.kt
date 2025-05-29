package com.example.pulmocare.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pulmocare.data.model.PatientSignup
import com.example.pulmocare.ui.theme.MedicalRed
import com.example.pulmocare.ui.viewmodel.AuthViewModel
import java.io.ByteArrayOutputStream
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Use the AuthViewModel
    val authViewModel: AuthViewModel = viewModel()
    val signUpState by authViewModel.signUpState.collectAsState()
    
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var insurance by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    // New fields
    var bloodType by remember { mutableStateOf("") }
    var hasAllergies by remember { mutableStateOf(false) }
    var allergiesDetails by remember { mutableStateOf("") }
    var hasChronicConditions by remember { mutableStateOf(false) }
    var chronicConditionsDetails by remember { mutableStateOf("") }
    var hasPets by remember { mutableStateOf(false) }
    var isSmoking by remember { mutableStateOf(false) }
    var maritalStatus by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()    // Blood type options
    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var bloodTypeExpanded by remember { mutableStateOf(false) }
    
    // Gender options
    val genders = listOf("Male", "Female", "Other")
    var genderExpanded by remember { mutableStateOf(false) }
      // Marital status options
    val maritalStatuses = listOf("Single", "Married", "Divorced", "Widowed")
    var maritalStatusExpanded by remember { mutableStateOf(false) }
    
    // Context for accessing content providers
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }
    
    // Function to convert bitmap to base64 string
    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    // Get bitmap from Uri
    fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH) + 1
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        dateOfBirth = String.format("%04d-%02d-%02d", year, month, day)
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
      // Handle sign-up state changes
    LaunchedEffect(signUpState) {
        when (signUpState) {
            is AuthViewModel.SignUpState.Success -> {
                // Sign-up was successful, navigate to the next screen
                val patient = (signUpState as AuthViewModel.SignUpState.Success).patient
                onSignupSuccess(patient.id ?: "")
                // Reset the sign-up state to prevent re-triggering this effect
                authViewModel.resetSignUpState()
            }
            is AuthViewModel.SignUpState.Error -> {
                // Show error message
                error = (signUpState as AuthViewModel.SignUpState.Error).message
            }
            else -> {
                // Do nothing for Initial and Loading states
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MedicalRed,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Create an Account",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Sign up to access PulmoCare services",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }                    // Personal Information Section
                    SectionHeader(title = "Personal Information")

                    // Profile Photo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUri != null) {
                                val bitmap = remember(photoUri) {
                                    getBitmapFromUri(photoUri!!)
                                }
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Add profile photo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Tap to add photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // First name and last name in a row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date of birth with date picker
                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { },
                        label = { Text("Date of Birth") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = null
                            )
                        },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Select date"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Weight and height in a row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MonitorWeight,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Height,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Blood type dropdown
                    ExposedDropdownMenuBox(
                        expanded = bloodTypeExpanded,
                        onExpandedChange = { bloodTypeExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = bloodType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Blood Type") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Bloodtype,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = bloodTypeExpanded,
                            onDismissRequest = { bloodTypeExpanded = false }
                        ) {
                            bloodTypes.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        bloodType = option
                                        bloodTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gender dropdown
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            genders.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))                    // Contact Information Section
                    SectionHeader(title = "Contact Information")

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))                    
                    OutlinedTextField(
                        value = insurance,
                        onValueChange = { insurance = it },
                        label = { Text("Insurance Provider") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Address/Location field
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address/Location") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Medical Information Section
                    SectionHeader(title = "Medical Information")

                    // Allergies section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Do you have any allergies?",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hasAllergies,
                            onCheckedChange = { hasAllergies = it }
                        )
                    }

                    if (hasAllergies) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = allergiesDetails,
                            onValueChange = { allergiesDetails = it },
                            label = { Text("Please specify your allergies") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chronic conditions section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Do you have any chronic conditions?",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hasChronicConditions,
                            onCheckedChange = { hasChronicConditions = it }
                        )
                    }

                    if (hasChronicConditions) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = chronicConditionsDetails,
                            onValueChange = { chronicConditionsDetails = it },
                            label = { Text("Please specify your chronic conditions") },
                            modifier = Modifier.fillMaxWidth()
                        )                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Pets section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Do you have pets?",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hasPets,
                            onCheckedChange = { hasPets = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Smoking section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Do you smoke?",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isSmoking,
                            onCheckedChange = { isSmoking = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Marital Status dropdown
                    ExposedDropdownMenuBox(
                        expanded = maritalStatusExpanded,
                        onExpandedChange = { maritalStatusExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = maritalStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Marital Status") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = maritalStatusExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = maritalStatusExpanded,
                            onDismissRequest = { maritalStatusExpanded = false }
                        ) {
                            maritalStatuses.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        maritalStatus = option
                                        maritalStatusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Occupation field
                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Information Section
                    SectionHeader(title = "Account Information")

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = acceptTerms,
                            onCheckedChange = { acceptTerms = it }
                        )
                        Text(
                            text = "I agree to the Terms of Service and Privacy Policy",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            // Validate inputs
                            when {
                                firstName.isBlank() || lastName.isBlank() -> {
                                    error = "Please enter your full name"
                                }
                                email.isBlank() -> {
                                    error = "Please enter your email"
                                }
                                !email.contains("@") -> {
                                    error = "Please enter a valid email"
                                }                                gender.isBlank() -> {
                                    error = "Please select your gender"
                                }
                                dateOfBirth.isBlank() -> {
                                    error = "Please select your date of birth"
                                }
                                password.isBlank() -> {
                                    error = "Please enter a password"
                                }
                                password.length < 6 -> {
                                    error = "Password must be at least 6 characters"
                                }
                                password != confirmPassword -> {
                                    error = "Passwords do not match"
                                }
                                !acceptTerms -> {
                                    error = "You must accept the terms and conditions"
                                }                                else -> {
                                    // All validations passed, attempt to register
                                    // Calculate age from date of birth
                                    val birthYear = if (dateOfBirth.isNotBlank()) {
                                        dateOfBirth.split("-")[0].toInt()
                                    } else {
                                        0
                                    }
                                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                    val calculatedAge = currentYear - birthYear
                                      val heightDouble = height.toDoubleOrNull() ?: 0.0
                                    val weightDouble = weight.toDoubleOrNull() ?: 0.0
                                    
                                    // Convert photo to base64 if available
                                    val photoBase64 = if (photoUri != null) {
                                        val bitmap = getBitmapFromUri(photoUri!!)
                                        bitmapToBase64(bitmap)
                                    } else {
                                        null
                                    }
                                    
                                    val patientSignup = PatientSignup(
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        password = password,
                                        age = calculatedAge,
                                        gender = gender,
                                        bloodType = bloodType,
                                        height = heightDouble,
                                        weight = weightDouble,
                                        location = address,
                                        dateOfBirth = dateOfBirth,
                                        insuranceProvider = insurance,
                                        maritalStatus = maritalStatus,
                                        occupation = occupation,
                                        hasPets = hasPets,
                                        smoking = isSmoking,
                                        allergies = if (hasAllergies && allergiesDetails.isNotBlank()) 
                                                       listOf(allergiesDetails) else null,
                                        chronicConditions = if (hasChronicConditions && chronicConditionsDetails.isNotBlank()) 
                                                               listOf(chronicConditionsDetails) else null,
                                        photo = photoBase64
                                    )
                                    
                                    // Call the AuthViewModel to sign up the patient
                                    authViewModel.signUp(patientSignup)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = signUpState !is AuthViewModel.SignUpState.Loading
                    ) {
                        if (signUpState is AuthViewModel.SignUpState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Sign Up")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account?")
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Sign in")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

