package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        //get UI elements
        val edtEmail = findViewById<EditText>(R.id.email_input)
        val edtPassword = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val txtCreateAccount = findViewById<TextView>(R.id.txt_create_account)

        //Login Button Click
        loginButton.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    //error checking
                    Log.e("LoginError", "Navigation failed", e)
                }
            }
        }

        //Create Account
        txtCreateAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}