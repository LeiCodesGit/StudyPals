package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val appLogo = findViewById<ImageView>(R.id.appLogo)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnAdmin = findViewById<Button>(R.id.btnSettings)

        // 1. Fetch real user data
        userRepository.getUserData { user, error ->
            if (user != null) {
                tvUsername.text = user.username
                tvEmail.text = user.email

                // 2. Show Admin Button ONLY if user is an admin
                if (user.admin) {
                    btnAdmin.text = "Admin Dashboard"
                    btnAdmin.visibility = View.VISIBLE
                    btnAdmin.setOnClickListener {
                        startActivity(Intent(this, AdminActivity::class.java))
                    }
                } else {
                    btnAdmin.visibility = View.GONE
                }
            }
        }

        appLogo.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
