package com.example.pulmocare.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pulmocare.data.api.MedicalTest
import com.example.pulmocare.ui.viewmodel.ParserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalReportScreen(navController: NavController) {
    val viewModel: ParserViewModel = viewModel()
    val parsingState by viewModel.parsingState.collectAsState()
    val parsedResponse by viewModel.parsedResponse.collectAsState()
    val context = LocalContext.current
    
    // PDF picker launcher
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.analyzePdf(context, it)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (parsingState) {
                is ParserViewModel.ParsingState.Idle -> {
                    UploadSection { pdfPicker.launch("application/pdf") }
                }
                  is ParserViewModel.ParsingState.Loading -> {
                    LoadingSection(onCancel = {
                        viewModel.cancelAnalysis()
                    })
                }
                
                is ParserViewModel.ParsingState.Success -> {
                    parsedResponse?.let { response ->
                        ResultsSection(
                            response = response,
                            onUploadAnother = {
                                viewModel.resetState()
                            }
                        )
                    }
                }
                
                is ParserViewModel.ParsingState.Error -> {
                    val errorMessage = (parsingState as ParserViewModel.ParsingState.Error).message
                    ErrorSection(
                        message = errorMessage,
                        onRetry = { viewModel.resetState() }
                    )
                }
            }
        }
    }
}

@Composable
fun UploadSection(onUploadClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { onUploadClick() },
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Upload PDF",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Upload Medical Report (PDF)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to select a PDF file",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LoadingSection(onCancel: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Analyzing medical report...",
            style = MaterialTheme.typography.bodyLarge
        )
        
        // Add loading information
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This may take up to a minute. Please wait.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        
        // Add cancel button
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onCancel,
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cancel")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsSection(
    response: com.example.pulmocare.data.api.ParserResponse,
    onUploadAnother: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Add scrolling capability
    ) {// Patient info section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Patient Information",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Patient Information",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Divider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                
                // Display patient name more prominently
                Text(
                    text = response.metadata.patient_name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                  // Patient details in a more organized format
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp) // Add more space between columns
                ) {
                    // Left column: Demographics
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Demographics",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        PatientInfoRow("Age", response.metadata.age)
                        PatientInfoRow("Gender", response.metadata.gender)
                    }
                    
                    // Right column: Test Information
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Test Information",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        PatientInfoRow("Date", response.metadata.date)
                        PatientInfoRow("Physician", response.metadata.physician)
                    }
                }
            }
        }// Test results
        val abnormalCount = response.tests.count { it.flag.equals("Abnormal", ignoreCase = true) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Test Results",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (abnormalCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$abnormalCount Abnormal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
          Spacer(modifier = Modifier.height(8.dp))        // Search filter
        var searchQuery by remember { mutableStateOf("") }
        var selectedFilter by remember { mutableStateOf("All") }
        
        // Title and info about search
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Search Tests",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Find specific tests by name, result value or units",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            placeholder = { Text("e.g., Cholesterol, Glucose, Hemoglobin...") },
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp), // More rounded corners
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
          // Filter options
        Text(
            text = "Filter by Status:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "All",
                onClick = { selectedFilter = "All" },
                label = { Text("All Tests") },
                leadingIcon = if (selectedFilter == "All") {
                    { Icon(Icons.Default.CheckCircle, contentDescription = "Selected") }
                } else null
            )
            
            FilterChip(
                selected = selectedFilter == "Abnormal",
                onClick = { selectedFilter = "Abnormal" },
                label = { Text("Abnormal Results") },
                leadingIcon = if (selectedFilter == "Abnormal") {
                    { Icon(Icons.Default.CheckCircle, contentDescription = "Selected") }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.error
                )
            )
            
            FilterChip(
                selected = selectedFilter == "Normal",
                onClick = { selectedFilter = "Normal" },
                label = { Text("Normal Results") },
                leadingIcon = if (selectedFilter == "Normal") {
                    { Icon(Icons.Default.CheckCircle, contentDescription = "Selected") }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Filtered test results
        val filteredTests = response.tests.filter { test ->            // Apply search filter
            val matchesSearch = searchQuery.isEmpty() || 
                test.test_name.contains(searchQuery, ignoreCase = true) ||
                (test.result_value?.toString()?.contains(searchQuery, ignoreCase = true) ?: false)
              // Apply category filter
            val matchesFilter = when (selectedFilter) {
                "Abnormal" -> test.flag.equals("Abnormal", ignoreCase = true)
                "Normal" -> test.flag.equals("Normal", ignoreCase = true)
                else -> true
            }
            
            matchesSearch && matchesFilter
        }
          if (filteredTests.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "No results",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No matching tests found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search criteria",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {            // Use a fixed height for the LazyColumn instead of weight
            // to prevent scrolling issues with nested scrollable containers
            LazyColumn(
                modifier = Modifier
                    .height(350.dp)
                    .fillMaxWidth()
            ) {
                items(filteredTests) { test ->
                    TestResultItem(test)
                }
            }
        }
          // Upload another button with improved visual styling
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Button(
                onClick = onUploadAnother,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Upload",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Upload Another Report",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            // Add note about analyzed reports
            Text(
                text = "Analysis results are not stored permanently. Please save important results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                fontWeight = FontWeight.Light
            )
        }
        // Add extra padding at the bottom to ensure content isn't cut off when scrolling
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PatientInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Label with improved visual distinction
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        
        // Value with some spacing
        Spacer(modifier = Modifier.height(2.dp))
        
        // Value with better visual prominence
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            // Use surface container color to create a subtle background
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )
    }
}

@Composable
fun TestResultItem(test: MedicalTest) {
    val isAbnormal = test.flag.equals("Abnormal", ignoreCase = true)
    val backgroundColor = if (isAbnormal) 
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
    else 
        MaterialTheme.colorScheme.surfaceVariant
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp), // Increased vertical padding for better spacing
        shape = RoundedCornerShape(12.dp), // Slightly more rounded corners
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        // Add slight elevation for better visual hierarchy
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isAbnormal) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Increased padding inside the card
        ) {
            // Test name and status header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = test.test_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAbnormal) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = test.flag,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp)) // Increased spacing
            
            Divider(
                color = if (isAbnormal)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                thickness = if (isAbnormal) 2.dp else 1.dp
            )
            
            Spacer(modifier = Modifier.height(12.dp)) // Increased spacing
            
            // Result details with more prominence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) { // Added weight to ensure proper spacing
                    Text(
                        text = "Result",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {                        Text(
                            text = test.result_value?.toString() ?: "--",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isAbnormal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = test.unit ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Normal range
                Column(modifier = Modifier.weight(1f)) { // Added weight to ensure proper spacing
                    Text(
                        text = "Normal Range",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                      Text(
                        text = test.normal_range ?: "--",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // If abnormal, show an explanatory note with improved visibility
            if (isAbnormal) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, 
                            contentDescription = "Information",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "This result is outside the normal range",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorSection(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error Analyzing Report",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Display error message with a better format
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {                Text(
                    text = if (message.contains("timeout", ignoreCase = true) || 
                             message.contains("failed to connect", ignoreCase = true)) {
                        "Connection timeout. The server might be busy processing your request."
                    } else if (message.contains("cast", ignoreCase = true)) {
                        "There was a problem with the data format. Please try with a different medical report."
                    } else if (message.contains("parsing", ignoreCase = true) || 
                              message.contains("JSON", ignoreCase = true)) {
                        "Could not recognize the report format. Please ensure this is a standard medical report."
                    } else {
                        message
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "What can you try:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Text(
                    text = if (message.contains("timeout", ignoreCase = true) || 
                             message.contains("connection", ignoreCase = true)) {
                        "• Check your internet connection\n• Try a smaller PDF file\n• Wait a moment and try again"
                    } else if (message.contains("cast", ignoreCase = true) || 
                              message.contains("parsing", ignoreCase = true) || 
                              message.contains("JSON", ignoreCase = true)) {
                        "• Try uploading a different medical report\n• Ensure the PDF is a standard lab report format\n• Try uploading a clearer scan of the document"
                    } else {
                        "• Check your internet connection\n• Try a smaller PDF file\n• Wait a moment and try again"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}
