package com.example.studypals

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore

object LevelManager {

    fun getExpThreshold(level: Int): Long {
        return when {
            level <= 5 -> 1000L
            level <= 10 -> 5000L
            level <= 15 -> 10000L
            else -> 20000L
        }
    }

    fun getStage(level: Int): String {
        return when {
            level <= 5 -> "Baby"
            level <= 15 -> "Young"
            else -> "Adult"
        }
    }

    fun addExp(context: Context, xpToAdd: Long, onComplete: () -> Unit = {}) {
        val userRepository = UserRepository()
        userRepository.getUserData { user, _ ->
            user?.let {
                var newXp = it.currentXP + xpToAdd
                var newLevel = it.level
                var leveledUp = false
                var evolved = false

                var threshold = getExpThreshold(newLevel)
                while (newXp >= threshold) {
                    newXp -= threshold
                    newLevel++
                    leveledUp = true
                    
                    // Check for evolution
                    val oldStage = getStage(newLevel - 1)
                    val newStage = getStage(newLevel)
                    if (oldStage != newStage) {
                        evolved = true
                    }
                    
                    threshold = getExpThreshold(newLevel)
                }

                val updatedUser = it.copy(
                    currentXP = newXp,
                    level = newLevel
                )

                FirebaseFirestore.getInstance().collection("users")
                    .document(it.uid).set(updatedUser)
                    .addOnSuccessListener {
                        if (evolved) {
                            showEvolutionDialog(context, getStage(newLevel))
                        } else if (leveledUp) {
                            showLevelUpDialog(context, newLevel)
                        }
                        onComplete()
                    }
            }
        }
    }

    private fun showLevelUpDialog(context: Context, newLevel: Int) {
        AlertDialog.Builder(context)
            .setTitle("🎉 Level Up!")
            .setMessage("Congratulations! Your Pal is now Level $newLevel!")
            .setPositiveButton("Awesome", null)
            .show()
    }

    private fun showEvolutionDialog(context: Context, newStage: String) {
        AlertDialog.Builder(context)
            .setTitle("✨ Evolution!")
            .setMessage("Amazing! Your Pal has evolved into an $newStage!")
            .setPositiveButton("Let's Go!", null)
            .show()
    }
}
