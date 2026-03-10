package com.example.studypals

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTaskTitle: EditText
    private lateinit var etTaskDate: EditText
    private lateinit var etTaskTime: EditText
    private lateinit var etTaskDesc: EditText
    private lateinit var btnCreateTask: Button
    private lateinit var btnCloseTask: ImageButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        etTaskTitle = findViewById(R.id.etTaskTitle)
        etTaskDate = findViewById(R.id.etTaskDate)
        etTaskTime = findViewById(R.id.etTaskTime)
        etTaskDesc = findViewById(R.id.etTaskDesc)
        btnCreateTask = findViewById(R.id.btnCreateTask)
        btnCloseTask = findViewById(R.id.btnCloseTask)

        etTaskDate.setOnClickListener { showDatePicker() }
        etTaskTime.setOnClickListener { showTimePicker() }

        btnCloseTask.setOnClickListener { finish() }

        btnCreateTask.setOnClickListener {
            saveTaskToFirestore()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            etTaskDate.setText(String.format("%02d.%02d.%d", day, month + 1, year))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            etTaskTime.setText(String.format("%02d:%02d", hour, minute))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun saveTaskToFirestore() {
        val title = etTaskTitle.text.toString().trim()
        val date = etTaskDate.text.toString().trim()
        val time = etTaskTime.text.toString().trim()
        val desc = etTaskDesc.text.toString().trim()
        val user = auth.currentUser

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (user == null) {
            Toast.makeText(this, "User not authenticated. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val task = hashMapOf(
            "title" to title,
            "date" to date,
            "time" to time,
            "description" to desc,
            "completed" to false,
            "userId" to user.uid,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        btnCreateTask.isEnabled = false // Prevent double-clicks

        db.collection("tasks")
            .add(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task Created!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnCreateTask.isEnabled = true
                Log.e("AddTaskActivity", "Error adding document", e)
                // Showing the specific error helps the user identify permission issues
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
