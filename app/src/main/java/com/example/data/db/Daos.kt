package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY id DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE isActive = 1 ORDER BY id DESC")
    suspend fun getActiveMemories(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface LearningDao {
    @Query("SELECT * FROM learning_topics ORDER BY lastStudiedAt DESC")
    fun getAllTopics(): Flow<List<LearningTopicEntity>>

    @Query("SELECT * FROM learning_topics WHERE id = :id")
    suspend fun getTopicById(id: Long): LearningTopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: LearningTopicEntity): Long

    @Update
    suspend fun updateTopic(topic: LearningTopicEntity)

    @Delete
    suspend fun deleteTopic(topic: LearningTopicEntity)

    // Flashcards
    @Query("SELECT * FROM flashcards WHERE topicId = :topicId")
    fun getFlashcardsForTopic(topicId: Long): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcard(id: Long)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM learned_tasks ORDER BY id ASC")
    fun getAllTasks(): Flow<List<LearnedTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<LearnedTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: LearnedTaskEntity): Long

    @Delete
    suspend fun deleteTask(task: LearnedTaskEntity)
}
