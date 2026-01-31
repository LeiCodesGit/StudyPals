package com.example.studypals

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View // ADD THIS IMPORT
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    private var selectedPetName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        //get the inputs
        val btnOpenPetPicker = findViewById<Button>(R.id.btn_open_pet_picker)
        val btnRegister = findViewById<Button>(R.id.registerButton)
        val firstNameInput = findViewById<EditText>(R.id.first_name_input)
        val lastNameInput = findViewById<EditText>(R.id.last_name_input)
        val ageInput = findViewById<EditText>(R.id.age_input)
        val txtBackToLogin = findViewById<TextView>(R.id.txt_back_to_login)
        val txtSelectedPet = findViewById<TextView>(R.id.txt_selected_pet)
        val petNameInput = findViewById<EditText>(R.id.pet_name_input)

        //Pet Selection
        btnOpenPetPicker.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_pet, null)
            val dialog = AlertDialog.Builder(this).create()
            dialog.setView(dialogView)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            //Maine Coon
            dialogView.findViewById<LinearLayout>(R.id.option_mainecoon).setOnClickListener {
                selectedPetName = "Maine Coon"

                // Show the text and update content
                txtSelectedPet.visibility = View.VISIBLE
                txtSelectedPet.text = "Your Study Pal: Maine Coon"

                btnOpenPetPicker.text = "Change Pal" // Updated button text
                dialog.dismiss()
            }

            //British Shorthair
            dialogView.findViewById<LinearLayout>(R.id.option_british).setOnClickListener {
                selectedPetName = "British Shorthair"

                txtSelectedPet.visibility = View.VISIBLE
                txtSelectedPet.text = "Your Study Pal: British Shorthair"

                btnOpenPetPicker.text = "Change Pal"
                dialog.dismiss()
            }

            //Golden Retriever
            dialogView.findViewById<LinearLayout>(R.id.option_golden).setOnClickListener {
                selectedPetName = "Golden Retriever"

                txtSelectedPet.visibility = View.VISIBLE
                txtSelectedPet.text = "Your Study Pal: Golden Retriever"

                btnOpenPetPicker.text = "Change Pal"
                dialog.dismiss()
            }

            dialog.show()
            dialog.window?.decorView?.alpha = 1f
            dialog.window?.setDimAmount(0.8f)
        }

        //Final Registration
        btnRegister.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val age = ageInput.text.toString().trim()
            val petName = petNameInput.text.toString().trim()

            //Check Input
            if (firstName.isEmpty()) {
                Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show()
            }
            else if (lastName.isEmpty()) {
                Toast.makeText(this, "Please enter your last name", Toast.LENGTH_SHORT).show()
            }
            else if (age.isEmpty()) {
                Toast.makeText(this, "Please enter your age", Toast.LENGTH_SHORT).show()
            }
            else if (selectedPetName.isEmpty()) {
                Toast.makeText(this, "Please pick a Study Pal", Toast.LENGTH_SHORT).show()
            }
            else if (petName.isEmpty()) {
                Toast.makeText(this, "Please enter your pet name", Toast.LENGTH_SHORT).show()
            }
            else {
                //register back-end here
                Toast.makeText(this, "Welcome $firstName $lastName! Enjoy your $selectedPetName.", Toast.LENGTH_LONG).show()
            }
        }

        //Back to Login
        txtBackToLogin.setOnClickListener {
            finish()
        }
    }
}