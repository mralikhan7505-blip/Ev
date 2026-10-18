package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.db.LearnedTaskEntity
import com.example.data.model.AiMode
import com.example.ui.EvViewModel
import com.example.ui.components.ThinkingProcessCard
import com.example.ui.theme.EvAmber
import com.example.ui.theme.EvCyan
import com.example.ui.theme.EvGoogleBlue
import com.example.ui.theme.EvMint
import com.example.ui.theme.EvTextPrimary
import com.example.ui.theme.EvTextSecondary
import com.example.ui.theme.EvViolet

@Composable
fun ActionsScreen(
    viewModel: EvViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsState()
    val isExecuting by viewModel.isExecutingTask.collectAsState()
    val executionResult by viewModel.taskExecutionResult.collectAsState()

    var activeTaskToRun by remember { mutableStateOf<LearnedTaskEntity?>(null) }
    var taskInputText by remember { mutableStateOf("") }
    var showCreateTaskDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Ev Actions",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = EvTextPrimary
                    )
                    Text(
                        text = "Run tasks and actions you have taught Ev to execute",
                        fontSize = 12.sp,
                        color = EvTextSecondary
                    )
                }

                Surface(
                    color = EvMint.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = EvMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${tasks.size} Actions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EvMint
                        )
                    }
                }
            }

            // Task List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onRun = {
                            activeTaskToRun = task
                            taskInputText = ""
                        },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }

        // Add Action Floating Action Button
        FloatingActionButton(
            onClick = { showCreateTaskDialog = true },
            containerColor = EvMint,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_action_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Teach Action")
        }
    }

    // Run Task Dialog / Modal
    activeTaskToRun?.let { task ->
        var isThinkingExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                if (!isExecuting) {
                    activeTaskToRun = null
                    viewModel.clearTaskResult()
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (task.preferredMode) {
                                AiMode.HIGH_THINKING -> Icons.Default.Psychology
                                AiMode.GOOGLE_SEARCH -> Icons.Default.Search
                                AiMode.STANDARD -> Icons.Default.AutoAwesome
                            },
                            contentDescription = null,
                            tint = when (task.preferredMode) {
                                AiMode.HIGH_THINKING -> EvAmber
                                AiMode.GOOGLE_SEARCH -> EvGoogleBlue
                                AiMode.STANDARD -> EvCyan
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = task.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    if (!isExecuting) {
                        IconButton(onClick = {
                            activeTaskToRun = null
                            viewModel.clearTaskResult()
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = EvTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (executionResult == null) {
                        OutlinedTextField(
                            value = taskInputText,
                            onValueChange = { taskInputText = it },
                            label = { Text("Task Input or Prompt") },
                            placeholder = { Text("Enter details for Ev to process...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 5,
                            enabled = !isExecuting
                        )

                        if (isExecuting) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = EvCyan
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Ev is executing this action...",
                                    fontSize = 12.sp,
                                    color = EvTextSecondary
                                )
                            }
                        }
                    } else {
                        // Display completed task result
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                executionResult?.thinkingProcess?.let { thinking ->
                                    ThinkingProcessCard(
                                        thinkingText = thinking,
                                        isExpanded = isThinkingExpanded,
                                        onToggle = { isThinkingExpanded = !isThinkingExpanded }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Text(
                                    text = executionResult?.text.orEmpty(),
                                    fontSize = 13.5.sp,
                                    color = EvTextPrimary,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (executionResult == null) {
                    Button(
                        onClick = { viewModel.executeTask(task, taskInputText) },
                        enabled = (!task.requiresInput || taskInputText.isNotBlank()) && !isExecuting,
                        colors = ButtonDefaults.buttonColors(containerColor = EvMint)
                    ) {
                        Text("Execute Action", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            activeTaskToRun = null
                            viewModel.clearTaskResult()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EvCyan)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (executionResult == null && !isExecuting) {
                    TextButton(onClick = { activeTaskToRun = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Teach Ev New Action Dialog
    if (showCreateTaskDialog) {
        var actionName by remember { mutableStateOf("") }
        var actionDesc by remember { mutableStateOf("") }
        var selectedMode by remember { mutableStateOf(AiMode.STANDARD) }
        var promptTemplate by remember { mutableStateOf("Please complete this task according to our learned rules: {input}") }

        AlertDialog(
            onDismissRequest = { showCreateTaskDialog = false },
            title = { Text("Teach Ev a New Action", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Teach Ev how you want specific recurring tasks done. Use {input} where user details will go.",
                        fontSize = 12.sp,
                        color = EvTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = actionName,
                        onValueChange = { actionName = it },
                        label = { Text("Action Name (e.g., Code Reviewer)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = actionDesc,
                        onValueChange = { actionDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = promptTemplate,
                        onValueChange = { promptTemplate = it },
                        label = { Text("Prompt Template (include {input})") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Execution Engine:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { selectedMode = AiMode.STANDARD },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMode == AiMode.STANDARD) EvCyan else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Fast", fontSize = 11.sp, color = if (selectedMode == AiMode.STANDARD) Color.Black else EvTextPrimary)
                        }
                        Button(
                            onClick = { selectedMode = AiMode.HIGH_THINKING },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMode == AiMode.HIGH_THINKING) EvAmber else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Thinking", fontSize = 11.sp, color = if (selectedMode == AiMode.HIGH_THINKING) Color.Black else EvTextPrimary)
                        }
                        Button(
                            onClick = { selectedMode = AiMode.GOOGLE_SEARCH },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMode == AiMode.GOOGLE_SEARCH) EvGoogleBlue else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Search", fontSize = 11.sp, color = if (selectedMode == AiMode.GOOGLE_SEARCH) Color.Black else EvTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNewLearnedTask(actionName, actionDesc, selectedMode, promptTemplate)
                        showCreateTaskDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EvMint)
                ) {
                    Text("Teach Action", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskCard(
    task: LearnedTaskEntity,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = when (task.preferredMode) {
                            AiMode.HIGH_THINKING -> EvAmber.copy(alpha = 0.2f)
                            AiMode.GOOGLE_SEARCH -> EvGoogleBlue.copy(alpha = 0.2f)
                            AiMode.STANDARD -> EvCyan.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (task.preferredMode) {
                        AiMode.HIGH_THINKING -> Icons.Default.Psychology
                        AiMode.GOOGLE_SEARCH -> Icons.Default.Search
                        AiMode.STANDARD -> Icons.Default.AutoAwesome
                    },
                    contentDescription = null,
                    tint = when (task.preferredMode) {
                        AiMode.HIGH_THINKING -> EvAmber
                        AiMode.GOOGLE_SEARCH -> EvGoogleBlue
                        AiMode.STANDARD -> EvCyan
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = EvTextPrimary
                )
                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = EvTextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mode: ${task.preferredMode.title}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (task.preferredMode) {
                        AiMode.HIGH_THINKING -> EvAmber
                        AiMode.GOOGLE_SEARCH -> EvGoogleBlue
                        AiMode.STANDARD -> EvCyan
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onRun,
                colors = ButtonDefaults.buttonColors(containerColor = EvMint),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Run", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
