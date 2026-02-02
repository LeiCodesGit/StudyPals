package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        //Buttons
        val btnTodo = findViewById<ImageButton>(R.id.btnTodo)
        val btnCalendar = findViewById<ImageButton>(R.id.btnCalendar)
        val btnStartStudy = findViewById<Button>(R.id.startStudyBtn)
        val btnChat = findViewById<ImageButton>(R.id.btnMessage)
        val btnProfile = findViewById<ImageButton>(R.id.profileButton)

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
            val intent = Intent(this, StudyModeActivity::class.java)
            startActivity(intent)
        }

        //Profile Button Click
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
