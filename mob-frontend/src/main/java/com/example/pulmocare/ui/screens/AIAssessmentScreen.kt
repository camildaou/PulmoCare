package com.example.pulmocare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pulmocare.data.repository.ChatbotRepository
import kotlinx.coroutines.launch

/**
 * Chat message data class
 */
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Pulmonary Chatbot screen
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }
            
            // Scroll to bottom when new messages arrive

        }
        
        // Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
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
                maxLines = 3
            )
            
            IconButton(
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
                enabled = inputText.isNotBlank() && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
        
        // Loading indicator
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

/**
 * Chat bubble component
 */
@Composable
fun ChatBubble(message: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (message.isFromUser) 40.dp else 0.dp,
                end = if (!message.isFromUser) 40.dp else 0.dp
            ),
        contentAlignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (message.isFromUser) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 0.dp,
                bottomEnd = if (!message.isFromUser) 16.dp else 0.dp
            ),
            modifier = Modifier
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}