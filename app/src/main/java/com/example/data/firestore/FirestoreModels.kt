package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreMemory(
    @DocumentId val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "PREFERENCE",
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreTopic(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "Coding",
    val summary: String = "",
    val masteryPercent: Int = 0,
    val keyTakeaways: String = "",
    val flashcardCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreFlashcard(
    @DocumentId val id: String = "",
    val front: String = "",
    val back: String = "",
    val isMastered: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreChatMessage(
    @DocumentId val id: String = "",
    val sender: String = "user",
    val text: String = "",
    val mode: String = "STANDARD",
    val thinkingProcess: String? = null,
    val searchSourcesJson: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreTask(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconName: String = "AutoAwesome",
    val preferredMode: String = "STANDARD",
    val promptTemplate: String = "",
    val requiresInput: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
