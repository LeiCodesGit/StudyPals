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

        // 1. Initialize all views
        val btnTodo = findViewById<ImageButton>(R.id.btnTodo)
        val btnCalendar = findViewById<ImageButton>(R.id.btnCalendar)
        val startStudyBtn = findViewById<Button>(R.id.startStudyBtn)

        // 2. To-Do Button Click
        btnTodo.setOnClickListener {
            val intent = Intent(this, TodoActivity::class.java)
            startActivity(intent)
        }

        // 3. Calendar Button Click
        btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        // 4. Study Mode Button Click
        startStudyBtn.setOnClickListener {
            val intent = Intent(this, StudyModeActivity::class.java)
            startActivity(intent)
        }
        // 4. Chat Button Click

        class ChatActivity : AppCompatActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.chat)

                // 4. Profile Button Click
                class ProfileActivity : AppCompatActivity() {

                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContentView(R.layout.profile)

                    }
                }
            }
        }


    }
}
