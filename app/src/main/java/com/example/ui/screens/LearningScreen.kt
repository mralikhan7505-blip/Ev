package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FlashcardEntity
import com.example.data.db.LearningTopicEntity
import com.example.data.model.AiMode
import com.example.ui.EvViewModel
import com.example.ui.QuizSessionState
import com.example.ui.theme.EvAmber
import com.example.ui.theme.EvCyan
import com.example.ui.theme.EvMint
import com.example.ui.theme.EvTextPrimary
import com.example.ui.theme.EvTextSecondary
import com.example.ui.theme.EvViolet

@Composable
fun LearningScreen(
    viewModel: EvViewModel,
    onNavigateToChatWithPrompt: (String, AiMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val topics by viewModel.allTopics.collectAsState()
    val selectedTopicId by viewModel.selectedTopicId.collectAsState()
    val flashcards by viewModel.flashcardsForSelectedTopic.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showFlashcardsDialog by remember { mutableStateOf(false) }

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
                        text = "Learn Together",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = EvTextPrimary
                    )
                    Text(
                        text = "Explore concepts with Ev, review flashcards, and test skills",
                        fontSize = 12.sp,
                        color = EvTextSecondary
                    )
                }

                Surface(
                    color = EvCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = EvCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${topics.size} Topics",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EvCyan
                        )
                    }
                }
            }

            // Learning Topics List
            if (topics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = EvTextSecondary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No topics added yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EvTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Teach Ev what you're learning today (e.g., Coding, Science, Languages).",
                            fontSize = 13.sp,
                            color = EvTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(topics, key = { it.id }) { topic ->
                        LearningTopicCard(
                            topic = topic,
                            onStudyFlashcards = {
                                viewModel.selectTopic(topic.id)
                                showFlashcardsDialog = true
                            },
                            onStartQuiz = {
                                viewModel.startQuizForTopic(topic)
                            },
                            onDeepReasoning = {
                                onNavigateToChatWithPrompt(
                                    "Deep dive into '${topic.title}'. Break down the hardest concepts, underlying mechanisms, and common pitfalls with High Thinking reasoning.",
                                    AiMode.HIGH_THINKING
                                )
                            },
                            onGenerateCards = {
                                viewModel.selectTopic(topic.id)
                                viewModel.generateFlashcardsForCurrentTopic()
                            },
                            onDelete = { viewModel.deleteTopic(topic) }
                        )
                    }
                }
            }
        }

        // Add Topic Floating Action Button
        FloatingActionButton(
            onClick = { showAddTopicDialog = true },
            containerColor = EvCyan,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_topic_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Topic")
        }
    }

    // Add Topic Dialog
    if (showAddTopicDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Coding") }
        var newSummary by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("New Learning Topic", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Ev will synthesize flashcards and interactive questions based on this topic.",
                        fontSize = 12.sp,
                        color = EvTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Topic Title (e.g., Machine Learning)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Category (e.g., Coding, Science, Language)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSummary,
                        onValueChange = { newSummary = it },
                        label = { Text("Key focus or concepts to learn") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createLearningTopic(newTitle, newCategory, newSummary)
                        showAddTopicDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EvCyan)
                ) {
                    Text("Create & Teach Ev", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTopicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Flashcards Dialog
    if (showFlashcardsDialog) {
        FlashcardsStudyDialog(
            flashcards = flashcards,
            isGenerating = isGenerating,
            onToggleMastered = { viewModel.toggleFlashcardMastered(it) },
            onGenerateMore = { viewModel.generateFlashcardsForCurrentTopic() },
            onDismiss = { showFlashcardsDialog = false }
        )
    }

    // Interactive Quiz Dialog
    quizState?.let { state ->
        QuizSessionDialog(
            quizState = state,
            onSelectOption = { viewModel.selectQuizOption(it) },
            onSubmitAnswer = { viewModel.submitQuizAnswer() },
            onNextQuestion = { viewModel.nextQuizQuestion() },
            onClose = { viewModel.closeQuiz() }
        )
    }
}

@Composable
fun LearningTopicCard(
    topic: LearningTopicEntity,
    onStudyFlashcards: () -> Unit,
    onStartQuiz: () -> Unit,
    onDeepReasoning: () -> Unit,
    onGenerateCards: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = EvViolet.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = topic.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EvViolet,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = topic.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = EvTextPrimary
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = EvTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = topic.summary,
                fontSize = 13.sp,
                color = EvTextSecondary,
                lineHeight = 18.sp
            )

            if (topic.keyTakeaways.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = topic.keyTakeaways,
                        fontSize = 12.sp,
                        color = EvTextPrimary,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStudyFlashcards,
                    colors = ButtonDefaults.buttonColors(containerColor = EvCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cards (${topic.flashcardCount})", fontSize = 11.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onStartQuiz,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = EvMint,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quiz Me", fontSize = 11.5.sp, color = EvMint, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = onDeepReasoning,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = EvAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("🧠 Deep Thinking Breakdown with Ev", fontSize = 11.5.sp, color = EvAmber, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun FlashcardsStudyDialog(
    flashcards: List<FlashcardEntity>,
    isGenerating: Boolean,
    onToggleMastered: (FlashcardEntity) -> Unit,
    onGenerateMore: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val currentCard = flashcards.getOrNull(currentIndex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Flashcard Study (${if (flashcards.isNotEmpty()) currentIndex + 1 else 0}/${flashcards.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (flashcards.isEmpty()) {
                    Text(
                        text = "No flashcards generated yet.",
                        fontSize = 13.sp,
                        color = EvTextSecondary
                    )
                } else if (currentCard != null) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlipped) EvViolet.copy(alpha = 0.15f) else EvCyan.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(
                                1.dp,
                                if (isFlipped) EvViolet else EvCyan,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { isFlipped = !isFlipped }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isFlipped) "ANSWER / EXPLANATION" else "QUESTION / CONCEPT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFlipped) EvViolet else EvCyan
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isFlipped) currentCard.back else currentCard.front,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EvTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to flip",
                                    fontSize = 10.sp,
                                    color = EvTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (currentIndex > 0) {
                                    currentIndex--
                                    isFlipped = false
                                }
                            },
                            enabled = currentIndex > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("Prev")
                        }

                        IconButton(onClick = { onToggleMastered(currentCard) }) {
                            Icon(
                                imageVector = if (currentCard.isMastered) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = "Mastered",
                                tint = if (currentCard.isMastered) EvMint else EvTextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                if (currentIndex < flashcards.size - 1) {
                                    currentIndex++
                                    isFlipped = false
                                }
                            },
                            enabled = currentIndex < flashcards.size - 1,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onGenerateMore,
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = EvCyan)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                } else {
                    Text("+ Ev Generate More", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun QuizSessionDialog(
    quizState: QuizSessionState,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ev Quiz • ${quizState.topicTitle}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (quizState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = EvCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ev is crafting an interactive quiz...",
                            fontSize = 13.sp,
                            color = EvTextSecondary
                        )
                    }
                } else if (quizState.isComplete) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = EvMint,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quiz Completed!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EvTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Score: ${quizState.score} / ${quizState.questions.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EvCyan
                        )
                    }
                } else {
                    val currentQ = quizState.questions.getOrNull(quizState.currentIndex)
                    if (currentQ != null) {
                        Text(
                            text = "Question ${quizState.currentIndex + 1} of ${quizState.questions.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EvCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentQ.question,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EvTextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        currentQ.options.forEachIndexed { index, option ->
                            val isSelected = quizState.selectedOptionIndex == index
                            val isCorrect = index == currentQ.correctIndex

                            val borderColor = when {
                                !quizState.isAnswerSubmitted -> if (isSelected) EvCyan else MaterialTheme.colorScheme.outline
                                isCorrect -> EvMint
                                isSelected -> Color.Red
                                else -> MaterialTheme.colorScheme.outline
                            }

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) EvCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                    .clickable(enabled = !quizState.isAnswerSubmitted) {
                                        onSelectOption(index)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${('A'.code + index).toChar()}.",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EvCyan else EvTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option,
                                        fontSize = 13.5.sp,
                                        color = EvTextPrimary
                                    )
                                }
                            }
                        }

                        if (quizState.isAnswerSubmitted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (quizState.selectedOptionIndex == currentQ.correctIndex) "✅ Correct!" else "❌ Incorrect",
                                        fontWeight = FontWeight.Bold,
                                        color = if (quizState.selectedOptionIndex == currentQ.correctIndex) EvMint else Color.Red
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentQ.explanation,
                                        fontSize = 12.sp,
                                        color = EvTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!quizState.isLoading && !quizState.isComplete) {
                if (!quizState.isAnswerSubmitted) {
                    Button(
                        onClick = onSubmitAnswer,
                        enabled = quizState.selectedOptionIndex != null,
                        colors = ButtonDefaults.buttonColors(containerColor = EvCyan)
                    ) {
                        Text("Check Answer", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onNextQuestion,
                        colors = ButtonDefaults.buttonColors(containerColor = EvCyan)
                    ) {
                        Text(
                            text = if (quizState.currentIndex < quizState.questions.size - 1) "Next Question" else "Finish",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (quizState.isComplete) {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = EvCyan)
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!quizState.isComplete) {
                TextButton(onClick = onClose) {
                    Text("Quit Quiz")
                }
            }
        }
    )
}
