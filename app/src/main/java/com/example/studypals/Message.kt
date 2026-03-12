package com.example.studypals

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)
