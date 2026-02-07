package com.example.studypals

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    private var selectedPetType: String = ""
    private val userRepository by lazy { UserRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // Find views
        val btnOpenPetPicker = findViewById<Button>(R.id.btn_open_pet_picker)
        val btnRegister = findViewById<Button>(R.id.registerButton)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirm_password_input) // New
        val firstNameInput = findViewById<EditText>(R.id.first_name_input)
        val lastNameInput = findViewById<EditText>(R.id.last_name_input)
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val ageInput = findViewById<EditText>(R.id.age_input)
        val txtBackToLogin = findViewById<TextView>(R.id.txt_back_to_login)
        val txtSelectedPet = findViewById<TextView>(R.id.txt_selected_pet)
        val petNameInput = findViewById<EditText>(R.id.pet_name_input)

        // Pet Selection Dialog
        btnOpenPetPicker.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_pet, null)
            val dialog = AlertDialog.Builder(this).create()
            dialog.setView(dialogView)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            fun selectPet(type: String) {
                selectedPetType = type
                txtSelectedPet.visibility = View.VISIBLE
                txtSelectedPet.text = "Your Study Pal: $type"
                btnOpenPetPicker.text = "Change Pal"
                dialog.dismiss()
            }

            dialogView.findViewById<LinearLayout>(R.id.option_mainecoon).setOnClickListener { selectPet("Maine Coon") }
            dialogView.findViewById<LinearLayout>(R.id.option_british).setOnClickListener { selectPet("British Shorthair") }
            dialogView.findViewById<LinearLayout>(R.id.option_golden).setOnClickListener { selectPet("Golden Retriever") }

            dialog.show()
        }

        // Final Registration Logic
        btnRegister.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val ageString = ageInput.text.toString().trim()
            val petNameCustom = petNameInput.text.toString().trim()

            // Validations
            if (email.isEmpty()) { emailInput.error = "Email is required"; return@setOnClickListener }

            // Password & Confirm Password Checks
            if (password.isEmpty()) { passwordInput.error = "Password is required"; return@setOnClickListener }
            if (password.length < 6) { passwordInput.error = "Min 6 characters required"; return@setOnClickListener }
            if (password != confirmPassword) { confirmPasswordInput.error = "Passwords do not match"; return@setOnClickListener }

            if (username.isEmpty()) { usernameInput.error = "Username is required"; return@setOnClickListener }
            if (firstName.isEmpty()) { firstNameInput.error = "First name required"; return@setOnClickListener }
            if (selectedPetType.isEmpty()) { Toast.makeText(this, "Pick a Study Pal!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (petNameCustom.isEmpty()) { petNameInput.error = "Name your pet!"; return@setOnClickListener }

            // Age validation (String to Int)
            val ageInt = ageString.toIntOrNull() ?: 0
            if (ageInt <= 0) { ageInput.error = "Enter a valid age"; return@setOnClickListener }

            // Create the User Data Object
            val newUser = User(
                email = email,
                username = username,
                firstName = firstName,
                lastName = lastName,
                age = ageInt,
                petType = selectedPetType,
                petName = petNameCustom,
                currentXP = 0,
                level = 1
            )

            // Save to Firebase via Repository
            userRepository.registerUser(newUser, password) { success, error ->
                if (success) {
                    Toast.makeText(this, "Welcome to StudyPals!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed: $error", Toast.LENGTH_LONG).show()
                }
            }
        }

        txtBackToLogin.setOnClickListener { finish() }
    }
}