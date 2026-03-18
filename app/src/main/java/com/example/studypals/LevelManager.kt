package com.example.studypals

import android.app.Activity
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
                var currentXp = it.currentXP + xpToAdd
                var currentLevel = it.level

                // Process potential multiple level ups
                while (currentXp >= getExpThreshold(currentLevel)) {
                    currentXp -= getExpThreshold(currentLevel)
                    currentLevel++
                }

                val updatedUser = it.copy(
                    currentXP = currentXp,
                    level = currentLevel
                )

                FirebaseFirestore.getInstance().collection("users")
                    .document(it.uid).set(updatedUser)
                    .addOnSuccessListener {
                        onComplete()
                    }
            }
        }
    }

    /**
     * Checks if the user has leveled up or evolved since the last time they were on this screen.
     * Uses SharedPreferences to track the "last seen" level.
     */
    fun checkAndShowLevelUp(activity: Activity, user: User) {
        val prefs = activity.getSharedPreferences("LevelPrefs", Context.MODE_PRIVATE)
        val lastSeenLevel = prefs.getInt("lastSeenLevel_${user.uid}", user.level)
        
        if (user.level > lastSeenLevel) {
            val oldStage = getStage(lastSeenLevel)
            val newStage = getStage(user.level)

            if (oldStage != newStage) {
                showEvolutionDialog(activity, user.level, newStage)
            } else {
                showLevelUpDialog(activity, user.level)
            }
            
            // Update the last seen level so we don't show it again
            prefs.edit().putInt("lastSeenLevel_${user.uid}", user.level).apply()
        } else if (!prefs.contains("lastSeenLevel_${user.uid}")) {
            // First time initialization
            prefs.edit().putInt("lastSeenLevel_${user.uid}", user.level).apply()
        }
    }

    private fun showLevelUpDialog(context: Context, newLevel: Int) {
        AlertDialog.Builder(context)
            .setTitle("🎉 Level Up!")
            .setMessage("Congratulations! Your Pal is now Level $newLevel!")
            .setPositiveButton("Awesome", null)
            .setCancelable(false)
            .show()
    }

    private fun showEvolutionDialog(context: Context, newLevel: Int, newStage: String) {
        AlertDialog.Builder(context)
            .setTitle("✨ EVOLUTION! ✨")
            .setMessage("Incredible! Your Pal is now Level $newLevel and has evolved into an $newStage!")
            .setPositiveButton("Let's Go!", null)
            .setCancelable(false)
            .show()
    }
}
