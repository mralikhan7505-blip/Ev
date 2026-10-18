package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AiMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MemoryEntity::class,
        ChatMessageEntity::class,
        LearningTopicEntity::class,
        FlashcardEntity::class,
        LearnedTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EvDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao
    abstract fun learningDao(): LearningDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: EvDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): EvDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EvDatabase::class.java,
                    "ev_assistant_database"
                )
                    .addCallback(EvDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class EvDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(db: EvDatabase) {
            // Initial memories demonstrating Ev's learning adaptability
            db.memoryDao().insertMemory(
                MemoryEntity(
                    title = "Assistant Persona",
                    content = "Ev is a friendly, curious learning partner. Always explain concepts with clarity, use simple analogies, and suggest practical tasks we can execute together.",
                    category = "RULE"
                )
            )
            db.memoryDao().insertMemory(
                MemoryEntity(
                    title = "Interactive Teaching",
                    content = "When I teach Ev something new, acknowledge it, summarize the key insight, and remember it for future tasks.",
                    category = "SKILL"
                )
            )

            // Initial Chat greeting
            db.chatDao().insertMessage(
                ChatMessageEntity(
                    sender = "ev",
                    text = "Hello! I'm Ev, your personal AI assistant that learns with you! 🌟\n\nYou can teach me your preferences, concepts, or rules in 'Ev's Brain'. We can explore topics together in 'Learn Together', or have me perform tasks using deep reasoning (High Thinking) and live Google Search grounding.\n\nWhat would you like to explore or teach me today?",
                    mode = AiMode.STANDARD,
                    thinkingProcess = null,
                    searchSourcesJson = null
                )
            )

            // Initial learning topics
            val topic1Id = db.learningDao().insertTopic(
                LearningTopicEntity(
                    title = "Kotlin & Modern Android",
                    category = "Coding",
                    summary = "Jetpack Compose, Coroutines, StateFlow, and Declarative UI architecture.",
                    masteryPercent = 45,
                    keyTakeaways = "1. Compose state drives UI recomposition.\n2. Use remember and derivedStateOf for performance.\n3. Scaffold provides standard M3 layout structure.",
                    flashcardCount = 3
                )
            )
            db.learningDao().insertFlashcards(
                listOf(
                    FlashcardEntity(
                        topicId = topic1Id,
                        front = "What is Recomposition in Jetpack Compose?",
                        back = "Recomposition is calling your composable functions again when inputs/state change so the UI reflects new data."
                    ),
                    FlashcardEntity(
                        topicId = topic1Id,
                        front = "Why use StateFlow with collectAsStateWithLifecycle?",
                        back = "It ensures state updates are only collected when the lifecycle is active, preventing background resource leaks."
                    ),
                    FlashcardEntity(
                        topicId = topic1Id,
                        front = "What is the role of Modifier in Compose?",
                        back = "Modifiers decorate or augment composables (layout, appearance, gesture handling, accessibility)."
                    )
                )
            )

            val topic2Id = db.learningDao().insertTopic(
                LearningTopicEntity(
                    title = "Conversational Spanish Basics",
                    category = "Language",
                    summary = "Core greetings, verbs, food ordering, and everyday phrases.",
                    masteryPercent = 30,
                    keyTakeaways = "1. 'Hola, ¿cómo estás?' for friendly greetings.\n2. 'Por favor' and 'Gracias' for politeness.\n3. 'Quisiera...' when ordering food politely.",
                    flashcardCount = 2
                )
            )
            db.learningDao().insertFlashcards(
                listOf(
                    FlashcardEntity(
                        topicId = topic2Id,
                        front = "¿Cómo se dice 'Where is the library?' en español?",
                        back = "¿Dónde está la biblioteca?"
                    ),
                    FlashcardEntity(
                        topicId = topic2Id,
                        front = "¿Qué significa 'Mucho gusto'?",
                        back = "Nice to meet you (literally 'much pleasure')."
                    )
                )
            )

            // Initial learned tasks that Ev can do!
            db.taskDao().insertTasks(
                listOf(
                    LearnedTaskEntity(
                        name = "Deep Logic & Problem Solving",
                        description = "Solves complex math, code, or strategic dilemmas with gemini-4-argon deep reasoning.",
                        iconName = "Psychology",
                        preferredMode = AiMode.HIGH_THINKING,
                        promptTemplate = "Analyze this complex problem with high thinking rigor, step-by-step logic, and edge-case evaluation:\n\n{input}",
                        requiresInput = true
                    ),
                    LearnedTaskEntity(
                        name = "Live Web Grounded Research",
                        description = "Searches the live web using gemini-3.8-flash and Google Search to find current facts and citations.",
                        iconName = "Search",
                        preferredMode = AiMode.GOOGLE_SEARCH,
                        promptTemplate = "Research up-to-date real-world facts, current updates, and authoritative information on this topic using Google Search:\n\n{input}",
                        requiresInput = true
                    ),
                    LearnedTaskEntity(
                        name = "Interactive Quiz Generator",
                        description = "Generates a 3-question quiz with multiple choice options based on what we are learning.",
                        iconName = "Quiz",
                        preferredMode = AiMode.STANDARD,
                        promptTemplate = "Create an interactive 3-question multiple choice quiz with answers and explanations to test my understanding on:\n\n{input}",
                        requiresInput = true
                    ),
                    LearnedTaskEntity(
                        name = "Draft in My Learned Style",
                        description = "Writes messages, emails, or summaries respecting all active rules in Ev's Brain.",
                        iconName = "Edit",
                        preferredMode = AiMode.STANDARD,
                        promptTemplate = "Apply all my learned style guidelines and rules from Ev's memory to draft the following:\n\n{input}",
                        requiresInput = true
                    )
                )
            )
        }
    }
}
