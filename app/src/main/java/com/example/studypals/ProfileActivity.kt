package com.example.studypals

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnAdmin = findViewById<Button>(R.id.btnSettings)
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val btnAboutUs = findViewById<Button>(R.id.btnNotifications)

        // Fetch real user data
        userRepository.getUserData { user, error ->
            if (user != null) {
                tvUsername.text = user.username
                tvEmail.text = user.email

                // Show Admin Button ONLY if user is an admin
                if (user.admin) {
                    btnAdmin.text = "Admin Dashboard"
                    btnAdmin.visibility = View.VISIBLE
                    btnAdmin.setOnClickListener {
                        startActivity(Intent(this, AdminActivity::class.java))
                    }
                } else {
                    btnAdmin.visibility = View.GONE
                }

                btnEditProfile.setOnClickListener {
                    showEditProfileDialog(user)
                }
            }
        }

        btnAboutUs.setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showEditProfileDialog(user: User) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
        
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etUsername = dialogView.findViewById<EditText>(R.id.etEditUsername)
        val etFirstName = dialogView.findViewById<EditText>(R.id.etEditFirstName)
        val etLastName = dialogView.findViewById<EditText>(R.id.etEditLastName)
        val etAge = dialogView.findViewById<EditText>(R.id.etEditAge)
        val cbAdmin = dialogView.findViewById<CheckBox>(R.id.cbAdminStatus)
        val etPetName = dialogView.findViewById<EditText>(R.id.etEditPetName)
        val btnUpdate = dialogView.findViewById<Button>(R.id.btnUpdateUser)

        // Populate existing data
        etUsername.setText(user.username)
        etFirstName.setText(user.firstName)
        etLastName.setText(user.lastName)
        etAge.setText(user.age.toString())
        cbAdmin.isChecked = user.admin
        etPetName.setText(user.petName)

        // Hide admin checkbox if current user is not already an admin (security)
        if (!user.admin) {
            cbAdmin.visibility = View.GONE
        }

        btnUpdate.setOnClickListener {
            val updatedUser = user.copy(
                username = etUsername.text.toString(),
                firstName = etFirstName.text.toString(),
                lastName = etLastName.text.toString(),
                age = etAge.text.toString().toIntOrNull() ?: user.age,
                admin = cbAdmin.isChecked,
                petName = etPetName.text.toString()
            )

            db.collection("users").document(user.uid).set(updatedUser)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                    recreate()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        alertDialog.show()
    }
}
