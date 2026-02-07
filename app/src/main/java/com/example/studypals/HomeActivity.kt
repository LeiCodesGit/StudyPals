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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        // UI Elements
        val petImage = findViewById<ImageView>(R.id.petImage)
        val levelText = findViewById<TextView>(R.id.levelText)
        val xpText = findViewById<TextView>(R.id.xpText)
        val xpProgressBar = findViewById<ProgressBar>(R.id.xpProgressBar)

        val btnTodo = findViewById<ImageButton>(R.id.btnTodo)
        val btnCalendar = findViewById<ImageButton>(R.id.btnCalendar)
        val btnStartStudy = findViewById<Button>(R.id.startStudyBtn)
        val btnChat = findViewById<ImageButton>(R.id.btnMessage)
        val btnProfile = findViewById<ImageButton>(R.id.profileButton)

        //Fetch Real Data from Firestore
        userRepository.getUserData { user, error ->
            if (user != null) {
                // Update Text & Progress
                levelText.text = "Level ${user.level}: ${user.petName}"
                xpText.text = "${user.currentXP} / 1000 XP"
                val progress = (user.currentXP.toDouble() / 1000 * 100).toInt()
                xpProgressBar.progress = progress

                // Evolution Logic
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
                    else -> R.drawable.egg_british // Default fallback
                }

                petImage.setImageResource(petResId)

            } else {
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }

        //To-Do Button Click
        btnTodo.setOnClickListener {
            val intent = Intent(this, TodoActivity::class.java)
            startActivity(intent)
        }

        //Calendar Button Click
        btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        //Study Mode Button Click
        btnStartStudy.setOnClickListener {
            val intent = Intent(this, StudyModeActivity::class.java)
            startActivity(intent)
        }

        //Chat Button Click
        btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        //Profile Button Click
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
