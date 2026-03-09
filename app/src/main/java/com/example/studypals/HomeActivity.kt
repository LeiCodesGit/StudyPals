package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    // Declare UI elements at the class level so they can be accessed in onResume
    private lateinit var petImage: ImageView
    private lateinit var levelText: TextView
    private lateinit var xpText: TextView
    private lateinit var xpProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        // Initialize UI Elements
        petImage = findViewById(R.id.petImage)
        levelText = findViewById(R.id.levelText)
        xpText = findViewById(R.id.xpText)
        xpProgressBar = findViewById(R.id.xpProgressBar)

        val btnTodo = findViewById<ImageButton>(R.id.btnTodo)
        val btnCalendar = findViewById<ImageButton>(R.id.btnCalendar)
        val btnStartStudy = findViewById<Button>(R.id.startStudyBtn)
        val btnChat = findViewById<ImageButton>(R.id.btnMessage)
        val btnProfile = findViewById<ImageButton>(R.id.profileButton)

        // Click Listeners stay in onCreate
        btnTodo.setOnClickListener { startActivity(Intent(this, TodoActivity::class.java)) }
        btnCalendar.setOnClickListener { startActivity(Intent(this, CalendarActivity::class.java)) }
        btnStartStudy.setOnClickListener { startActivity(Intent(this, StudyModeActivity::class.java)) }
        btnChat.setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }
        btnProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
    }

    // This runs every time you return to this screen (like after a swipe back)
    override fun onResume() {
        super.onResume()
        refreshUserData()
    }

    private fun refreshUserData() {
        userRepository.getUserData { user, error ->
            if (user != null) {
                // Update Text & Progress based on level logic
                levelText.text = "Level ${user.level}: ${user.petName}"

                // Show current level progress (0-1000)
                val currentLevelXP = user.currentXP % 1000
                xpText.text = "$currentLevelXP / 1000 XP"

                val progress = (currentLevelXP.toDouble() / 1000 * 100).toInt()
                xpProgressBar.progress = progress

                // Evolution Logic matches StudyMode
                val petResId = when (user.petType) {
                    "British Shorthair" -> when {
                        user.level >= 16 -> R.drawable.adult_british
                        user.level >= 6 -> R.drawable.baby_british
                        else -> R.drawable.egg_british
                    }
                    "Golden Retriever" -> when {
                        user.level >= 16 -> R.drawable.adult_golden
                        user.level >= 6 -> R.drawable.baby_golden
                        else -> R.drawable.egg_golden
                    }
                    "Maine Coon" -> when {
                        user.level >= 16 -> R.drawable.adult_mainecoon
                        user.level >= 6 -> R.drawable.baby_mainecoon
                        else -> R.drawable.egg_mainecoon
                    }
                    else -> R.drawable.egg_british
                }
                petImage.setImageResource(petResId)
            } else {
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}