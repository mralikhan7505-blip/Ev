package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.db.ChatDao
import com.example.data.db.ChatMessageEntity
import com.example.data.db.EvDatabase
import com.example.data.db.FlashcardEntity
import com.example.data.db.LearningDao
import com.example.data.db.LearningTopicEntity
import com.example.data.db.LearnedTaskEntity
import com.example.data.db.MemoryDao
import com.example.data.db.MemoryEntity
import com.example.data.db.TaskDao
import com.example.data.model.AiMode
import com.example.data.model.AiResponseResult
import com.example.data.model.SearchSource
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class EvRepository(
    private val database: EvDatabase,
    private val apiClient: GeminiApiClient = GeminiApiClient()
) {
    private val memoryDao: MemoryDao = database.memoryDao()
    private val chatDao: ChatDao = database.chatDao()
    private val learningDao: LearningDao = database.learningDao()
    private val taskDao: TaskDao = database.taskDao()

    // Memories & Learning directives
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun addMemory(title: String, content: String, category: String): Long {
        return memoryDao.insertMemory(
            MemoryEntity(
                title = title.trim(),
                content = content.trim(),
                category = category.uppercase()
            )
        )
    }

    suspend fun updateMemory(memory: MemoryEntity) {
        memoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        memoryDao.deleteMemory(memory)
    }

    // Chat
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun sendMessage(
        prompt: String,
        mode: AiMode,
        customApiKey: String? = null
    ): AiResponseResult {
        // Save user message
        chatDao.insertMessage(
            ChatMessageEntity(
                sender = "user",
                text = prompt.trim(),
                mode = mode
            )
        )

        // Gather active memories for Ev's personality and learned directives
        val activeMemories = memoryDao.getActiveMemories()
        val directives = activeMemories.joinToString("\n") { mem ->
            "- [${mem.category} - ${mem.title}]: ${mem.content}"
        }

        // Generate response using Gemini API
        val result = apiClient.generateContent(
            prompt = prompt,
            mode = mode,
            systemDirectives = directives,
            customApiKey = customApiKey
        )

        // Serialize search sources if any
        val sourcesJson = if (result.searchSources.isNotEmpty()) {
            val array = JSONArray()
            result.searchSources.forEach { s ->
                array.put(JSONObject().apply {
                    put("title", s.title)
                    put("url", s.url)
                })
            }
            array.toString()
        } else null

        // Save Ev's response
        chatDao.insertMessage(
            ChatMessageEntity(
                sender = "ev",
                text = result.text,
                mode = mode,
                thinkingProcess = result.thinkingProcess,
                searchSourcesJson = sourcesJson
            )
        )

        return result
    }

    suspend fun clearChat() {
        chatDao.clearHistory()
    }

    // Learning topics & Flashcards
    val allTopics: Flow<List<LearningTopicEntity>> = learningDao.getAllTopics()

    fun getFlashcards(topicId: Long): Flow<List<FlashcardEntity>> {
        return learningDao.getFlashcardsForTopic(topicId)
    }

    suspend fun addTopic(title: String, category: String, summary: String): Long {
        return learningDao.insertTopic(
            LearningTopicEntity(
                title = title.trim(),
                category = category.trim(),
                summary = summary.trim()
            )
        )
    }

    suspend fun updateTopic(topic: LearningTopicEntity) {
        learningDao.updateTopic(topic)
    }

    suspend fun deleteTopic(topic: LearningTopicEntity) {
        learningDao.deleteTopic(topic)
    }

    suspend fun updateFlashcard(card: FlashcardEntity) {
        learningDao.updateFlashcard(card)
    }

    suspend fun generateFlashcardsForTopic(
        topic: LearningTopicEntity,
        customApiKey: String? = null
    ): List<FlashcardEntity> {
        val prompt = """
            We are learning about "${topic.title}" (${topic.category}).
            Summary: ${topic.summary}
            
            Generate 4 high-yield study flashcards for this topic.
            Output ONLY valid JSON in the following format with no markdown wrappers:
            [
              {"front": "Question or prompt", "back": "Clear concise answer"}
            ]
        """.trimIndent()

        val response = apiClient.generateContent(
            prompt = prompt,
            mode = AiMode.STANDARD,
            systemDirectives = "Return strictly a JSON array of flashcards with keys 'front' and 'back'.",
            customApiKey = customApiKey
        )

        val generatedCards = mutableListOf<FlashcardEntity>()
        try {
            val cleanJson = response.text
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()
            val array = JSONArray(cleanJson)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                generatedCards.add(
                    FlashcardEntity(
                        topicId = topic.id,
                        front = item.optString("front"),
                        back = item.optString("back")
                    )
                )
            }
            if (generatedCards.isNotEmpty()) {
                learningDao.insertFlashcards(generatedCards)
                val updatedTopic = topic.copy(
                    flashcardCount = topic.flashcardCount + generatedCards.size,
                    lastStudiedAt = System.currentTimeMillis()
                )
                learningDao.updateTopic(updatedTopic)
            }
        } catch (e: Exception) {
            // Fallback manual flashcards if parsing failed
            val fallback = listOf(
                FlashcardEntity(
                    topicId = topic.id,
                    front = "Core Concept of ${topic.title}",
                    back = topic.summary.ifBlank { "Fundamental principles and application." }
                )
            )
            learningDao.insertFlashcards(fallback)
            learningDao.updateTopic(topic.copy(flashcardCount = topic.flashcardCount + 1))
        }

        return generatedCards
    }

    // Tasks that Ev has learned to execute
    val allTasks: Flow<List<LearnedTaskEntity>> = taskDao.getAllTasks()

    suspend fun addTask(
        name: String,
        description: String,
        mode: AiMode,
        template: String
    ): Long {
        return taskDao.insertTask(
            LearnedTaskEntity(
                name = name.trim(),
                description = description.trim(),
                iconName = when (mode) {
                    AiMode.HIGH_THINKING -> "Psychology"
                    AiMode.GOOGLE_SEARCH -> "Search"
                    AiMode.STANDARD -> "AutoAwesome"
                },
                preferredMode = mode,
                promptTemplate = template.trim(),
                requiresInput = template.contains("{input}")
            )
        )
    }

    suspend fun deleteTask(task: LearnedTaskEntity) {
        taskDao.deleteTask(task)
    }
}
