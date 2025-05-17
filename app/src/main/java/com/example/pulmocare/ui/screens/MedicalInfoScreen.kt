// ui/screens/MedicalInfoScreen.kt
package com.example.pulmocare.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pulmocare.data.MedicalCategory
import com.example.pulmocare.data.MedicalDocument
import com.example.pulmocare.data.MedicalRepository
import com.example.pulmocare.data.PrescriptionTutorials
import com.example.pulmocare.ui.theme.LightMuted
import com.example.pulmocare.ui.theme.LightMutedText
import com.example.pulmocare.ui.theme.MedicalBlue
import com.example.pulmocare.ui.theme.MedicalRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalInfoScreen() {
    val medicalRepository = remember { MedicalRepository() }
    val categories = remember { medicalRepository.medicalCategories }
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }
    
    // Get prescriptions category
    val prescriptionsCategory = remember { 
        categories.find { it.id == "prescriptions" } 
    }

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
                            text = "Access and manage your medical records",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightMutedText
                        )
                    }
                },
                actions = {
                    if (showSearchField) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search prescriptions...") },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(vertical = 8.dp),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        )
                    }
                    
                    IconButton(onClick = { showSearchField = !showSearchField }) {
                        Icon(
                            imageVector = if (showSearchField) Icons.Default.ArrowDropDown else Icons.Default.Search,
                            contentDescription = if (showSearchField) "Hide search" else "Search prescriptions"
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
                        text = "Your Medical Records",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "View your medical documents",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightMutedText
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // If there's a search query, show only the prescriptions category with filtered items
                    if (searchQuery.isNotEmpty() && prescriptionsCategory != null) {
                        // Create a filtered category with only matching documents
                        val filteredDocuments = prescriptionsCategory.documents.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        }
                        
                        if (filteredDocuments.isNotEmpty()) {
                            Text(
                                text = "Search Results for \"$searchQuery\"",
                                style = MaterialTheme.typography.titleSmall,
                                color = MedicalBlue
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            filteredDocuments.forEach { document ->
                                MedicalDocumentItem(document = document)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            Text(
                                text = "No prescriptions found matching \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LightMutedText
                            )
                        }
                    } else {
                        // Show all categories normally
                        categories.forEach { category ->
                            MedicalCategoryItem(category = category)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun MedicalCategoryItem(category: MedicalCategory) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )
    
    // Show tutorial information card for prescriptions category
    val isPrescriptionsCategory = category.id == "prescriptions"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FilePresent,
                contentDescription = null,
                tint = MedicalBlue
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = LightMutedText
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.rotate(rotationState)
            )
        }

        // Content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Tutorial information card for prescriptions
                if (isPrescriptionsCategory) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MedicalBlue.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.OndemandVideo,
                                contentDescription = null,
                                tint = MedicalBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = "Tutorial Videos Available",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MedicalBlue
                                )
                                
                                Text(
                                    text = "Learn how to properly use your prescribed inhalers and devices through our video tutorials",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                category.documents.forEach { document ->
                    MedicalDocumentItem(document = document)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (category.canUpload) {
                    Button(
                        onClick = { /* Handle upload */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Upload to ${category.title}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalDocumentItem(document: MedicalDocument) {
    var showSummary by remember { mutableStateOf(false) }
    val tutorial = remember { PrescriptionTutorials.tutorials[document.name] }
    var showTutorial by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { showSummary = !showSummary }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {            Icon(
                imageVector = Icons.Default.FilePresent,
                contentDescription = null,
                tint = LightMutedText,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "${document.date} • ${document.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightMutedText
                )
                
                // Show tutorial badge if available
                if (tutorial != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = null,
                            tint = MedicalBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Tutorial available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedicalBlue
                        )
                    }
                }
            }
            
            IconButton(onClick = { /* Handle download */ }) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = MedicalBlue
                )
            }
            
            // Tutorial button if available
            if (tutorial != null) {
                IconButton(onClick = { showTutorial = !showTutorial }) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleOutline,
                        contentDescription = "Watch Tutorial",
                        tint = MedicalBlue
                    )
                }
            }
        }

        // Summary section
        if (document.summary != null && showSummary) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightMuted)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (document.status == "attention") {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MedicalRed,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MedicalBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = document.summary,
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (document.status != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Badge(
                                containerColor = if (document.status == "normal")
                                    Color(0xFF4CAF50) else MedicalRed
                            ) {
                                Text(
                                    text = if (document.status == "normal")
                                        "Normal" else "Needs Attention",                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Tutorial video section
        if (tutorial != null && showTutorial) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = tutorial.videoTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(
                            containerColor = MedicalBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = tutorial.medicationType,
                                color = MedicalBlue,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = tutorial.videoDescription,
                        style = MaterialTheme.typography.bodySmall
                    )
                      Spacer(modifier = Modifier.height(12.dp))
                      // Video thumbnail with play overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { uriHandler.openUri(tutorial.videoUrl) }
                    ) {                        // Thumbnail image
                        AsyncImage(
                            model = tutorial.thumbnailUrl,
                            contentDescription = "Video Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Play button overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TextButton(
                        onClick = { uriHandler.openUri(tutorial.videoUrl) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Watch on YouTube")
                    }
                    
                    if (tutorial.importantNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Divider()
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Important Usage Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tutorial.importantNotes.forEach { note ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MedicalBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Text(
                                        text = note,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
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