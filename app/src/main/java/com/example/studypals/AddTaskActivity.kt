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
import java.text.SimpleDateFormat
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
        // Ensure we are using add_task.xml which has the updated UI
        setContentView(R.layout.add_task)

        etTaskTitle = findViewById(R.id.etTaskTitle)
        etTaskDate = findViewById(R.id.etTaskDate)
        etTaskTime = findViewById(R.id.etTaskTime)
        etTaskDesc = findViewById(R.id.etTaskDesc)
        btnCreateTask = findViewById(R.id.btnCreateTask)
        btnCloseTask = findViewById(R.id.btnCloseTask)

        // Handle pre-filled date from CalendarActivity
        val passedDate = intent.getStringExtra("selectedDate")
        if (passedDate != null) {
            etTaskDate.setText(passedDate)
        } else {
            // Default to today in MM-DD-YYYY
            val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
            etTaskDate.setText(sdf.format(Date()))
        }

        etTaskDate.setOnClickListener { showDatePicker() }
        etTaskTime.setOnClickListener { showTimePicker() }

        btnCloseTask.setOnClickListener { finish() }

        btnCreateTask.setOnClickListener {
            saveTaskToFirestore()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, day, 0, 0, 0)
            selectedDate.set(Calendar.MILLISECOND, 0)
            
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            if (selectedDate.before(today)) {
                Toast.makeText(this, "Cannot select a past date", Toast.LENGTH_SHORT).show()
            } else {
                etTaskDate.setText(String.format("%02d-%02d-%d", month + 1, day, year))
                // Clear time if date changed to today to force re-validation
                etTaskTime.text.clear()
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            val selectedTime = Calendar.getInstance()
            val dateStr = etTaskDate.text.toString()
            val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
            
            try {
                val date = sdf.parse(dateStr)
                if (date != null) {
                    selectedTime.time = date
                }
            } catch (e: Exception) {
                // fallback to now
            }
            
            selectedTime.set(Calendar.HOUR_OF_DAY, hour)
            selectedTime.set(Calendar.MINUTE, minute)
            selectedTime.set(Calendar.SECOND, 0)
            selectedTime.set(Calendar.MILLISECOND, 0)

            // If selected date is today, prevent past time
            val today = Calendar.getInstance()
            if (dateStr == sdf.format(today.time)) {
                if (selectedTime.before(today)) {
                    Toast.makeText(this, "Cannot select a past time for today", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }
            }

            val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            etTaskTime.setText(sdfTime.format(selectedTime.time))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
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
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
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

        btnCreateTask.isEnabled = false

        db.collection("tasks")
            .add(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task Created!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnCreateTask.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
