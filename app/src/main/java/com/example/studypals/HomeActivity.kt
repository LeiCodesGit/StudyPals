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

    private lateinit var petImage: ImageView
    private lateinit var levelText: TextView
    private lateinit var xpText: TextView
    private lateinit var xpProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        petImage = findViewById(R.id.petImage)
        levelText = findViewById(R.id.levelText)
        xpText = findViewById(R.id.xpText)
        xpProgressBar = findViewById(R.id.xpProgressBar)

        val btnTodo = findViewById<ImageButton>(R.id.btnTodo)
        val btnCalendar = findViewById<ImageButton>(R.id.btnCalendar)
        val btnStartStudy = findViewById<Button>(R.id.startStudyBtn)
        val btnChat = findViewById<ImageButton>(R.id.btnMessage)
        val btnProfile = findViewById<ImageButton>(R.id.profileButton)

        btnTodo.setOnClickListener { startActivity(Intent(this, TodoActivity::class.java)) }
        btnCalendar.setOnClickListener { startActivity(Intent(this, CalendarActivity::class.java)) }
        btnStartStudy.setOnClickListener { startActivity(Intent(this, StudyModeActivity::class.java)) }
        btnChat.setOnClickListener { startActivity(Intent(this, UserListActivity::class.java)) }
        btnProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        refreshUserData()
    }

    private fun refreshUserData() {
        userRepository.getUserData { user, error ->
            if (user != null) {
                // Check for Level Up dialog first
                LevelManager.checkAndShowLevelUp(this, user)

                val threshold = LevelManager.getExpThreshold(user.level)
                val stage = LevelManager.getStage(user.level)
                
                levelText.text = "Level ${user.level}: ${user.petName} ($stage)"
                xpText.text = "${user.currentXP} / $threshold XP"

                xpProgressBar.max = threshold.toInt()
                xpProgressBar.progress = user.currentXP.toInt()

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
            } else if (error != null) {
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
