/*

@Composable
internal fun ChatRoute(
    modelID: Int,
    chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.getFactory(LocalContext.current.applicationContext, modelID)
    )
) {
    Log.d("ChatRoute", "Normal")
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val textInputEnabled by chatViewModel.isTextInputEnabled.collectAsStateWithLifecycle()
    ChatScreen(
        uiState,
        textInputEnabled
    ) { message ->
        chatViewModel.sendMessage(message)
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)

fun ChatScreen(
    uiState: UiState,
    textInputEnabled: Boolean = true,
    onSendMessage: (String) -> Unit,
) {
    var userMessage by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
           .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0E5D8), // Pampas color (start color)
                        Color(0xFFFFF8E1)  // Lighter color (end color, adjust as needed)
                    )
                )
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Messages Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(
                items = uiState.messages,
                key = { it.id } // Add key for better performance
            ) { chatMessage ->
                 ChatItem(chatMessage)
            }
        }

        // Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                label = {
                    Text("Type your message")
                },
                colors = TextFieldDefaults.textFieldColors(
                   focusedTextColor = Color(0xFF0D47A1), // Set the input text color when focused
                   unfocusedTextColor = Color.Black,
                    containerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF0D47A1),
                    unfocusedIndicatorColor = Color.LightGray,
                    cursorColor = Color(0xFF0D47A1)
                ),
                modifier = Modifier.weight(0.85f),
                enabled = textInputEnabled,
                shape = RoundedCornerShape(12.dp)
            )

IconButton(
    onClick = {
        if (userMessage.isNotBlank()) {
            onSendMessage(userMessage)
            userMessage = ""
        }
    },
    modifier = Modifier
        .padding(start = 16.dp)
        .size(64.dp)
        .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
    enabled = textInputEnabled
) {
    Icon(
        imageVector = Icons.AutoMirrored.Default.Send,
        contentDescription = "Send",
        tint = if (textInputEnabled) Color(0xFF0D47A1) else Color.Gray,
        modifier = Modifier.size(32.dp)  // Increase icon size
    )
}
        }
    }
}

@Composable
fun ChatItem(
    chatMessage: ChatMessage
) {
    val backgroundColor = if (chatMessage.isFromUser) Color(0xFFB6AA7C) else Color(0xFFF2F0E8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .wrapContentSize(
                align = if (chatMessage.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
            )
    ) {
        // Chat label
        Text(
            text = if (chatMessage.isFromUser)
                stringResource(R.string.user_label)
            else
                stringResource(R.string.model_label),
            style = MaterialTheme.typography.bodySmall,
            color = if (chatMessage.isFromUser) Color.White else Color.Black,

             modifier =  Modifier.padding(12.dp)
        )

        // Chat message container
        Row {
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = RoundedCornerShape(
                        topStart = if (chatMessage.isFromUser) 20.dp else 4.dp,
                        topEnd = if (chatMessage.isFromUser) 4.dp else 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    ),
                    modifier = Modifier.widthIn(0.dp, maxWidth * 0.9f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (chatMessage.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                text = chatMessage.message,
                                color = if (chatMessage.isFromUser) Color.White else Color.Black // Set proper contrast
                            )

                            // Show performance metrics for model messages
                            if (!chatMessage.isFromUser && chatMessage.executionTime > 0) {
                                Spacer(modifier = Modifier.size(8.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                                Spacer(modifier = Modifier.size(8.dp))

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Performance Metrics",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Time: ${chatMessage.executionTime}ms",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Threads: ${chatMessage.threadCount}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Energy Consumed: ${chatMessage.estimatedEnergy} mAh",
                                            style = MaterialTheme.typography.bodySmall
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
}
*/

package com.llm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.llm.R

@Composable
internal fun ChatRoute(
    modelID: Int,
    onNewSession: () -> Unit,
    chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.getFactory(LocalContext.current.applicationContext, modelID)
    )
)
{
    // Effect to handle cleanup when the route changes
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.clearUiState()
        }
    }

    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val textInputEnabled by chatViewModel.isTextInputEnabled.collectAsStateWithLifecycle()

    // Key helps force recomposition when session is reset
    key(uiState.messages.size) {
        ChatScreen(
            uiState = uiState,
            textInputEnabled = textInputEnabled,
            onSendMessage = { message ->
                chatViewModel.sendMessage(message)
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatScreen(
    uiState: UiState,
    textInputEnabled: Boolean = true,
    onSendMessage: (String) -> Unit,
) {
    var userMessage by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
             .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0E5D8), // Pampas color (start color)
                        Color(0xFFFFF8E1)  // Lighter color (end color, adjust as needed)
                    )
                )
            ),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Messages Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),

            reverseLayout = true
        ) {
            items(
                items = uiState.messages,
                key = { it.id }
            ) { chatMessage ->
                ChatItem(chatMessage)
            }
        }

        // Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                label = { Text("Type your message") },
                modifier = Modifier.weight(1f),
                enabled = textInputEnabled,
                shape = RoundedCornerShape(12.dp)
            )

            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        onSendMessage(userMessage)
                        userMessage = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
                enabled = textInputEnabled && userMessage.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    contentDescription = stringResource(R.string.action_send)
                )
            }
        }
    }
}

@Composable
fun ChatItem(
    chatMessage: ChatMessage
) {
    val backgroundColor = if (chatMessage.isFromUser) Color(0xFFB6AA7C) else Color(0xFFF2F0E8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .wrapContentSize(
                align = if (chatMessage.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
            )
    ) {
        // Chat label
        Text(
            text = if (chatMessage.isFromUser)
                stringResource(R.string.user_label)
            else
                stringResource(R.string.model_label),
            style = MaterialTheme.typography.bodySmall,
            color = if (chatMessage.isFromUser) Color.White else Color.Black,

             modifier =  Modifier.padding(12.dp)
        )

        // Chat message container
        Row {
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = RoundedCornerShape(
                        topStart = if (chatMessage.isFromUser) 20.dp else 4.dp,
                        topEnd = if (chatMessage.isFromUser) 4.dp else 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    ),
                    modifier = Modifier.widthIn(0.dp, maxWidth * 0.9f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (chatMessage.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                text = chatMessage.message,
                                color = if (chatMessage.isFromUser) Color.White else Color.Black // Set proper contrast
                            )

                            // Show performance metrics for model messages
                            if (!chatMessage.isFromUser && chatMessage.executionTime > 0) {
                                Spacer(modifier = Modifier.size(8.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                                Spacer(modifier = Modifier.size(8.dp))

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Performance Metrics",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Time: ${chatMessage.executionTime}ms",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Threads: ${chatMessage.threadCount}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Energy Consumed: ${chatMessage.estimatedEnergy} mAh",
                                            style = MaterialTheme.typography.bodySmall
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
}
