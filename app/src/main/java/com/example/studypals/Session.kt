package com.example.studypals

data class Session(
    val sessionId: String = "",
    val roomCode: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostPetName: String = "",
    val hostPetType: String = "",
    val hostPetLevel: Int = 1,
    val hostPetXP: Long = 0,
    
    val partnerId: String? = null,
    val partnerName: String? = null,
    val partnerPetName: String? = null,
    val partnerPetType: String? = null,
    val partnerPetLevel: Int? = null,
    val partnerPetXP: Long? = null,
    
    val selectedMode: String = "",
    val timerRunning: Boolean = false,
    val status: String = "waiting" // waiting, active, finished
)
