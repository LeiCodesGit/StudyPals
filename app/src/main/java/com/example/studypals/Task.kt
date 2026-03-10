package com.example.studypals

import com.google.firebase.Timestamp

data class Task(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val completed: Boolean = false,
    val userId: String = "",
    val timestamp: Timestamp? = null
)
