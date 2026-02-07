package com.example.studypals

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = 0,

    // Pet Attributes
    val petName: String = "",
    val petType: String = "Default",
    val currentXP: Long = 0,
    val level: Int = 1,

    // Progress Tracking
    val totalFocusMinutes: Long = 0,
    val currentStreak: Int = 0,
    val lastStudyDate: com.google.firebase.Timestamp? = null
)
