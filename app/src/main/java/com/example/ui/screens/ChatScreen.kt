package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiMode
import com.example.ui.EvViewModel
import com.example.ui.components.AiModeSelector
import com.example.ui.components.EvOrbIndicator
import com.example.ui.components.MessageBubble
import com.example.ui.theme.EvAmber
import com.example.ui.theme.EvCyan
import com.example.ui.theme.EvGoogleBlue
import com.example.ui.theme.EvTextPrimary
import com.example.ui.theme.EvTextSecondary
import com.example.ui.theme.EvViolet

@Composable
fun ChatScreen(
    viewModel: EvViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val currentMode by viewModel.currentAiMode.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Dialog for saving to Ev's Brain
    var showTeachDialog by remember { mutableStateOf(false) }
    var teachTitle by remember { mutableStateOf("") }
    var teachContent by remember { mutableStateOf("") }
    var teachCategory by remember { mutableStateOf("PREFERENCE") }

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EvOrbIndicator(
                    isThinking = isGenerating,
                    mode = currentMode,
                    sizeDp = 38
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ev",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EvTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    when (currentMode) {
                                        AiMode.HIGH_THINKING -> EvAmber.copy(alpha = 0.2f)
                                        AiMode.GOOGLE_SEARCH -> EvGoogleBlue.copy(alpha = 0.2f)
                                        AiMode.STANDARD -> EvCyan.copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentMode.modelName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (currentMode) {
                                    AiMode.HIGH_THINKING -> EvAmber
                                    AiMode.GOOGLE_SEARCH -> EvGoogleBlue
                                    AiMode.STANDARD -> EvCyan
                                }
                            )
                        }
                    }
                    Text(
                        text = if (isGenerating) "Ev is thinking..." else currentMode.subtitle,
                        fontSize = 11.5.sp,
                        color = EvTextSecondary
                    )
                }
            }

            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = "Clear Chat",
                    tint = EvTextSecondary
                )
            }
        }

        // Mode Switcher (Ev Fast, High Thinking, Google Search)
        AiModeSelector(
            selectedMode = currentMode,
            onSelectMode = { viewModel.setAiMode(it) }
        )

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    onTeachMemory = { content ->
                        teachTitle = "Learned from chat"
                        teachContent = content
                        showTeachDialog = true
                    }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = when (currentMode) {
                                AiMode.HIGH_THINKING -> EvAmber
                                AiMode.GOOGLE_SEARCH -> EvGoogleBlue
                                AiMode.STANDARD -> EvCyan
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (currentMode) {
                                AiMode.HIGH_THINKING -> "Ev is reasoning deeply (gemini-4-argon)..."
                                AiMode.GOOGLE_SEARCH -> "Ev is searching Google live for accurate facts..."
                                AiMode.STANDARD -> "Ev is formulating a helpful response..."
                            },
                            fontSize = 12.sp,
                            color = EvTextSecondary
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SuggestionChip(
                    onClick = {
                        viewModel.setAiMode(AiMode.HIGH_THINKING)
                        inputText = "Explain the step-by-step logic of how a compiler optimizes recursive functions into loops."
                    },
                    label = { Text("🧠 High Thinking logic", fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                SuggestionChip(
                    onClick = {
                        viewModel.setAiMode(AiMode.GOOGLE_SEARCH)
                        inputText = "What are the latest discoveries in astronomy from this week?"
                    },
                    label = { Text("🌐 Search live news", fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                SuggestionChip(
                    onClick = {
                        viewModel.setAiMode(AiMode.STANDARD)
                        inputText = "Ev, remember that I am preparing for a technical presentation next Friday."
                    },
                    label = { Text("💡 Teach Ev about me", fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Input Field Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = when (currentMode) {
                            AiMode.HIGH_THINKING -> "Ask a complex reasoning question..."
                            AiMode.GOOGLE_SEARCH -> "Search live Google facts or citations..."
                            AiMode.STANDARD -> "Ask, teach, or discuss anything with Ev..."
                        },
                        fontSize = 13.5.sp,
                        color = EvTextSecondary
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = when (currentMode) {
                        AiMode.HIGH_THINKING -> EvAmber
                        AiMode.GOOGLE_SEARCH -> EvGoogleBlue
                        AiMode.STANDARD -> EvCyan
                    },
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isGenerating) {
                        val text = inputText
                        inputText = ""
                        viewModel.sendMessage(text)
                    }
                },
                enabled = inputText.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (inputText.isNotBlank() && !isGenerating) {
                            when (currentMode) {
                                AiMode.HIGH_THINKING -> EvAmber
                                AiMode.GOOGLE_SEARCH -> EvGoogleBlue
                                AiMode.STANDARD -> EvCyan
                            }
                        } else Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank() && !isGenerating) Color.Black else Color.DarkGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Teach Ev Dialog
    if (showTeachDialog) {
        AlertDialog(
            onDismissRequest = { showTeachDialog = false },
            title = { Text("Teach Ev's Brain", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Ev will store this in its persistent memory and apply it to future tasks and answers.",
                        fontSize = 12.sp,
                        color = EvTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = teachTitle,
                        onValueChange = { teachTitle = it },
                        label = { Text("Concept or Rule Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = teachContent,
                        onValueChange = { teachContent = it },
                        label = { Text("What should Ev remember?") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.teachEvNewMemory(teachTitle, teachContent, teachCategory)
                        showTeachDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EvCyan)
                ) {
                    Text("Save to Ev", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTeachDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
