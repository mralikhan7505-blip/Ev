package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ChatMessageEntity
import com.example.data.db.EvDatabase
import com.example.data.db.FlashcardEntity
import com.example.data.db.LearningTopicEntity
import com.example.data.db.LearnedTaskEntity
import com.example.data.db.MemoryEntity
import com.example.data.model.AiMode
import com.example.data.model.AiResponseResult
import com.example.data.model.QuizQuestion
import com.example.data.repository.EvRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.data.firestore.FirestoreRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

data class QuizSessionState(
    val topicTitle: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val score: Int = 0,
    val isAnswerSubmitted: Boolean = false,
    val isComplete: Boolean = false,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class EvViewModel(application: Application) : AndroidViewModel(application) {
    private val database = EvDatabase.getDatabase(application, viewModelScope)
    private val repository = EvRepository(database)
    private val firestoreRepository by lazy { FirestoreRepository.create(application) }

    // Current AI Mode (Fast Standard, High Thinking with gemini-4-argon, or Google Search with gemini-3.8-flash)
    private val _currentAiMode = MutableStateFlow(AiMode.STANDARD)
    val currentAiMode: StateFlow<AiMode> = _currentAiMode.asStateFlow()

    // Generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Status / Snackbar message
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    // Custom API Key (optional override)
    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Chat
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Ev's Learned Memory Brain
    val allMemories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Learning topics
    val allTopics: StateFlow<List<LearningTopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Selected topic for detail / flashcards
    private val _selectedTopicId = MutableStateFlow<Long?>(null)
    val selectedTopicId: StateFlow<Long?> = _selectedTopicId.asStateFlow()

    val flashcardsForSelectedTopic: StateFlow<List<FlashcardEntity>> = _selectedTopicId
        .flatMapLatest { topicId ->
            if (topicId != null) repository.getFlashcards(topicId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Learned Tasks
    val allTasks: StateFlow<List<LearnedTaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Interactive Quiz Session
    private val _quizState = MutableStateFlow<QuizSessionState?>(null)
    val quizState: StateFlow<QuizSessionState?> = _quizState.asStateFlow()

    // Task Execution Result
    private val _taskExecutionResult = MutableStateFlow<AiResponseResult?>(null)
    val taskExecutionResult: StateFlow<AiResponseResult?> = _taskExecutionResult.asStateFlow()

    private val _isExecutingTask = MutableStateFlow(false)
    val isExecutingTask: StateFlow<Boolean> = _isExecutingTask.asStateFlow()

    fun setAiMode(mode: AiMode) {
        _currentAiMode.value = mode
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key.trim()
        showFeedback("API Key saved.")
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    fun showFeedback(msg: String) {
        _userFeedback.value = msg
    }

    // Chat
    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _isGenerating.value) return
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val result = repository.sendMessage(
                    prompt = prompt,
                    mode = _currentAiMode.value,
                    customApiKey = _customApiKey.value.takeIf { it.isNotBlank() }
                )
                if (Firebase.auth.currentUser != null) {
                    runCatching {
                        firestoreRepository.saveChatMessage(
                            sender = "user",
                            text = prompt,
                            mode = _currentAiMode.value.name
                        )
                        firestoreRepository.saveChatMessage(
                            sender = "ev",
                            text = result.text,
                            mode = _currentAiMode.value.name,
                            thinkingProcess = result.thinkingProcess
                        )
                    }
                }
            } catch (e: Exception) {
                showFeedback("Error: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            showFeedback("Conversation cleared.")
        }
    }

    fun syncWithFirestore() {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            try {
                firestoreRepository.syncUserProfile(
                    email = user.email.orEmpty(),
                    displayName = user.displayName.orEmpty()
                )
            } catch (e: Exception) {
                Log.w("EvViewModel", "Firestore sync warning: ${e.message}")
            }
        }
    }

    // Memories & Learning
    fun teachEvNewMemory(title: String, content: String, category: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.addMemory(title, content, category)
            showFeedback("Ev learned: '$title'!")
            if (Firebase.auth.currentUser != null) {
                runCatching {
                    firestoreRepository.saveMemory(title, content, category, true)
                }
            }
        }
    }

    fun toggleMemoryActive(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.updateMemory(memory.copy(isActive = !memory.isActive))
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
            showFeedback("Memory removed from Ev's brain.")
        }
    }

    // Learning Topics
    fun selectTopic(topicId: Long?) {
        _selectedTopicId.value = topicId
    }

    fun createLearningTopic(title: String, category: String, summary: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newId = repository.addTopic(title, category, summary)
            _selectedTopicId.value = newId
            showFeedback("Learning topic '$title' created!")
            // Automatically prompt Ev to generate flashcards
            generateFlashcardsForCurrentTopic()
        }
    }

    fun deleteTopic(topic: LearningTopicEntity) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
            if (_selectedTopicId.value == topic.id) {
                _selectedTopicId.value = null
            }
            showFeedback("Topic deleted.")
        }
    }

    fun toggleFlashcardMastered(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.updateFlashcard(card.copy(isMastered = !card.isMastered))
        }
    }

    fun generateFlashcardsForCurrentTopic() {
        val topicId = _selectedTopicId.value ?: return
        val currentTopic = allTopics.value.firstOrNull { it.id == topicId } ?: return
        viewModelScope.launch {
            _isGenerating.value = true
            showFeedback("Ev is synthesizing flashcards for '${currentTopic.title}'...")
            try {
                repository.generateFlashcardsForTopic(
                    topic = currentTopic,
                    customApiKey = _customApiKey.value.takeIf { it.isNotBlank() }
                )
                showFeedback("Flashcards generated!")
            } catch (e: Exception) {
                showFeedback("Failed to generate flashcards: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // Interactive Quiz with Ev
    fun startQuizForTopic(topic: LearningTopicEntity) {
        viewModelScope.launch {
            _quizState.value = QuizSessionState(
                topicTitle = topic.title,
                isLoading = true
            )
            try {
                val prompt = """
                    Create an interactive 3-question multiple choice quiz on the topic "${topic.title}".
                    Summary: ${topic.summary}
                    
                    Return ONLY valid JSON with this exact structure and no markdown code fences:
                    [
                      {
                        "question": "Question text here",
                        "options": ["Option A", "Option B", "Option C", "Option D"],
                        "correctIndex": 0,
                        "explanation": "Why this option is correct."
                      }
                    ]
                """.trimIndent()

                val result = repository.sendMessage(
                    prompt = prompt,
                    mode = AiMode.STANDARD,
                    customApiKey = _customApiKey.value.takeIf { it.isNotBlank() }
                )

                val cleanJson = result.text
                    .removePrefix("```json").removePrefix("```")
                    .removeSuffix("```").trim()

                val questions = mutableListOf<QuizQuestion>()
                val array = JSONArray(cleanJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val optsArray = obj.getJSONArray("options")
                    val opts = (0 until optsArray.length()).map { optsArray.getString(it) }
                    questions.add(
                        QuizQuestion(
                            question = obj.getString("question"),
                            options = opts,
                            correctIndex = obj.getInt("correctIndex"),
                            explanation = obj.optString("explanation")
                        )
                    )
                }

                if (questions.isNotEmpty()) {
                    _quizState.value = QuizSessionState(
                        topicTitle = topic.title,
                        questions = questions,
                        isLoading = false
                    )
                } else {
                    _quizState.value = null
                    showFeedback("Could not generate quiz questions. Try again.")
                }
            } catch (e: Exception) {
                _quizState.value = null
                showFeedback("Quiz generation error: ${e.localizedMessage}")
            }
        }
    }

    fun selectQuizOption(index: Int) {
        val current = _quizState.value ?: return
        if (current.isAnswerSubmitted) return
        _quizState.value = current.copy(selectedOptionIndex = index)
    }

    fun submitQuizAnswer() {
        val current = _quizState.value ?: return
        val selected = current.selectedOptionIndex ?: return
        val currentQ = current.questions.getOrNull(current.currentIndex) ?: return
        val isCorrect = selected == currentQ.correctIndex
        val newScore = if (isCorrect) current.score + 1 else current.score

        _quizState.value = current.copy(
            isAnswerSubmitted = true,
            score = newScore
        )
    }

    fun nextQuizQuestion() {
        val current = _quizState.value ?: return
        val nextIdx = current.currentIndex + 1
        if (nextIdx < current.questions.size) {
            _quizState.value = current.copy(
                currentIndex = nextIdx,
                selectedOptionIndex = null,
                isAnswerSubmitted = false
            )
        } else {
            _quizState.value = current.copy(isComplete = true)
        }
    }

    fun closeQuiz() {
        _quizState.value = null
    }

    // Execute Learned Task
    fun executeTask(task: LearnedTaskEntity, userInput: String) {
        viewModelScope.launch {
            _isExecutingTask.value = true
            _taskExecutionResult.value = null
            try {
                val fullPrompt = task.promptTemplate.replace("{input}", userInput.trim())
                val result = repository.sendMessage(
                    prompt = fullPrompt,
                    mode = task.preferredMode,
                    customApiKey = _customApiKey.value.takeIf { it.isNotBlank() }
                )
                _taskExecutionResult.value = result
                showFeedback("Ev completed '${task.name}'!")
            } catch (e: Exception) {
                showFeedback("Task failed: ${e.message}")
            } finally {
                _isExecutingTask.value = false
            }
        }
    }

    fun clearTaskResult() {
        _taskExecutionResult.value = null
    }

    fun addNewLearnedTask(name: String, desc: String, mode: AiMode, template: String) {
        if (name.isBlank() || template.isBlank()) return
        viewModelScope.launch {
            repository.addTask(name, desc, mode, template)
            showFeedback("Ev learned action '$name'!")
        }
    }

    fun deleteTask(task: LearnedTaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            showFeedback("Action removed.")
        }
    }
}
