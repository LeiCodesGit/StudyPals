package com.example.studypals

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = 0,
    val admin: Boolean = false,
    val petName: String = "",
    val petType: String = "Default",
    val currentXP: Long = 0L,
    val level: Int = 1,
    val totalFocusMinutes: Long = 0L,
    val currentStreak: Int = 0,
    val lastStudyDate: Timestamp? = null
)
