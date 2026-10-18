package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepository"

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val context: Context
) {
    companion object {
        fun create(context: Context): FirestoreRepository {
            val dbId = context.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(dbId)
            return FirestoreRepository(firestore, context)
        }
    }

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google to access Ev Cloud.")
    }

    // User Profile
    suspend fun syncUserProfile(email: String, displayName: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            val payload = mapOf(
                "userId" to uid,
                "email" to email,
                "displayName" to displayName,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        }
    }

    // Memories
    fun observeMemories(): Flow<List<FirestoreMemory>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("users").document(uid).collection("memories")
                .snapshots()
                .map { it.toObjects(FirestoreMemory::class.java) }
        )
    }

    suspend fun saveMemory(title: String, content: String, category: String, isActive: Boolean) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("memories").document()
        val payload = mapOf(
            "title" to title,
            "content" to content,
            "category" to category,
            "isActive" to isActive,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun updateMemoryActive(memoryId: String, currentMemory: FirestoreMemory, isActive: Boolean) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("memories").document(memoryId)
        val payload = mapOf(
            "title" to currentMemory.title,
            "content" to currentMemory.content,
            "category" to currentMemory.category,
            "isActive" to isActive,
            "createdAt" to (currentMemory.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun deleteMemory(memoryId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("memories").document(memoryId).delete().await()
    }

    // Topics
    fun observeTopics(): Flow<List<FirestoreTopic>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("users").document(uid).collection("topics")
                .snapshots()
                .map { it.toObjects(FirestoreTopic::class.java) }
        )
    }

    suspend fun saveTopic(title: String, category: String, summary: String): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("topics").document()
        val payload = mapOf(
            "title" to title,
            "category" to category,
            "summary" to summary,
            "masteryPercent" to 0,
            "keyTakeaways" to "",
            "flashcardCount" to 0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return docRef.id
    }

    suspend fun deleteTopic(topicId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("topics").document(topicId).delete().await()
    }

    // Flashcards
    fun observeFlashcards(topicId: String): Flow<List<FirestoreFlashcard>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("users").document(uid).collection("topics").document(topicId)
                .collection("flashcards")
                .snapshots()
                .map { it.toObjects(FirestoreFlashcard::class.java) }
        )
    }

    suspend fun saveFlashcards(topicId: String, cards: List<Pair<String, String>>) {
        val uid = requireUserId()
        val batch = db.batch()
        val topicRef = db.collection("users").document(uid).collection("topics").document(topicId)

        cards.forEach { (front, back) ->
            val cardRef = topicRef.collection("flashcards").document()
            batch.set(
                cardRef,
                mapOf(
                    "front" to front,
                    "back" to back,
                    "isMastered" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        batch.commit().await()
    }

    // Chat messages
    fun observeChatMessages(): Flow<List<FirestoreChatMessage>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("users").document(uid).collection("chat_messages")
                .orderBy("createdAt")
                .snapshots()
                .map { it.toObjects(FirestoreChatMessage::class.java) }
        )
    }

    suspend fun saveChatMessage(
        sender: String,
        text: String,
        mode: String,
        thinkingProcess: String? = null,
        searchSourcesJson: String? = null
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("chat_messages").document()
        val payload = mutableMapOf<String, Any>(
            "sender" to sender,
            "text" to text,
            "mode" to mode,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (thinkingProcess != null) payload["thinkingProcess"] = thinkingProcess
        if (searchSourcesJson != null) payload["searchSourcesJson"] = searchSourcesJson
        docRef.set(payload).await()
    }

    // Tasks
    fun observeTasks(): Flow<List<FirestoreTask>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("users").document(uid).collection("tasks")
                .snapshots()
                .map { it.toObjects(FirestoreTask::class.java) }
        )
    }

    suspend fun saveTask(name: String, description: String, mode: String, template: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("tasks").document()
        val payload = mapOf(
            "name" to name,
            "description" to description,
            "iconName" to "AutoAwesome",
            "preferredMode" to mode,
            "promptTemplate" to template,
            "requiresInput" to template.contains("{input}"),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun deleteTask(taskId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("tasks").document(taskId).delete().await()
    }
}
