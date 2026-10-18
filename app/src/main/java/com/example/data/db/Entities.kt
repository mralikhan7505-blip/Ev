package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AiMode

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // PREFERENCE, RULE, KNOWLEDGE, SKILL
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "user" or "ev"
    val text: String,
    val mode: AiMode = AiMode.STANDARD,
    val thinkingProcess: String? = null,
    val searchSourcesJson: String? = null, // JSON array of sources
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_topics")
data class LearningTopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // e.g. "Coding", "Language", "Science", "General"
    val summary: String,
    val masteryPercent: Int = 0, // 0 - 100
    val keyTakeaways: String = "",
    val lastStudiedAt: Long = System.currentTimeMillis(),
    val flashcardCount: Int = 0
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topicId: Long,
    val front: String,
    val back: String,
    val isMastered: Boolean = false
)

@Entity(tableName = "learned_tasks")
data class LearnedTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconName: String,
    val preferredMode: AiMode,
    val promptTemplate: String,
    val requiresInput: Boolean = true
)
