package com.example.studypals

data class Conversation(
    val friendId: String = "",
    val friendName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L
)