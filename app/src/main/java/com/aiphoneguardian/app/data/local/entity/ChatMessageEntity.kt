package com.aiphoneguardian.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val isError: Boolean = false,
    val language: String = "en"
)
