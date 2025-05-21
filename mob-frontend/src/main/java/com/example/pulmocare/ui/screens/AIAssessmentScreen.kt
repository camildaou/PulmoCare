package com.example.pulmocare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pulmocare.R
import com.example.pulmocare.data.repository.ChatbotRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chat message data class
 */
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Pulmonary Chatbot screen with enhanced UI
 */
@Composable
fun AIAssessmentScreen() {
    val context = LocalContext.current
    val chatbotRepository = remember { ChatbotRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    
    LaunchedEffect(Unit) {
        // Add welcome message
        messages.add(
            ChatMessage(
                content = "Hello! I'm your pulmonary health assistant. How can I help you with respiratory health information today?",
                isFromUser = false
            )
        )
    }
    
    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pulmonary Health Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
            
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
            }
            
            // Divider between chat and input
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // Input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Ask about pulmonary health...") },
                    enabled = !isLoading,
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp)
                )
                
                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userMessage = inputText.trim()
                            inputText = ""
                            
                            // Add user message to chat
                            messages.add(ChatMessage(content = userMessage, isFromUser = true))
                            
                            // Show loading
                            isLoading = true
                            
                            coroutineScope.launch {
                                chatbotRepository.askQuestion(userMessage).collect { result ->
                                    isLoading = false
                                    
                                    result.onSuccess { response ->
                                        messages.add(ChatMessage(content = response, isFromUser = false))
                                    }.onFailure { error ->
                                        messages.add(
                                            ChatMessage(
                                                content = "Sorry, I couldn't process your question. Please try again later.",
                                                isFromUser = false
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = if (inputText.isNotBlank() && !isLoading) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

/**
 * Enhanced chat bubble component with avatars
 */
@Composable
fun ChatBubble(message: ChatMessage) {
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { 
        dateFormat.format(Date(message.timestamp)) 
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (message.isFromUser) 60.dp else 0.dp,
                end = if (!message.isFromUser) 60.dp else 0.dp,
            ),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            // Robot avatar for AI
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SmartToy,
                    contentDescription = "AI Assistant",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = if (message.isFromUser) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                    bottomEnd = if (!message.isFromUser) 16.dp else 4.dp
                ),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Timestamp
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
        
        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }
}