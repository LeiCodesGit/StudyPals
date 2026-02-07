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

    private val userRepository = UserRepository()

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
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userRepository.loginUser(email, password) { isSuccess, exception ->
                if (isSuccess) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    val message = when (exception) {
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                            "No account found with this email."

                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                            "Incorrect password. Please try again."

                        is com.google.firebase.FirebaseNetworkException ->
                            "No internet connection."

                        else -> "Login failed: ${exception?.localizedMessage}"
                    }

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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