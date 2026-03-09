package com.example.studypals

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TodoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo)

        // 1. Initialize the Add Button from your todo.xml
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAdd)

        // 2. Set the click listener to trigger the pop-up
        btnAdd.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_add_task, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val alertDialog = builder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseTask)
        val btnCreate = dialogView.findViewById<Button>(R.id.btnCreateTask)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val etDate = dialogView.findViewById<EditText>(R.id.etTaskDate)
        val etTime = dialogView.findViewById<EditText>(R.id.etTaskTime)
        val etDesc = dialogView.findViewById<EditText>(R.id.etTaskDesc)

        btnClose.setOnClickListener {
            alertDialog.dismiss()
        }

        btnCreate.setOnClickListener {
            val title = etTitle.text.toString()
            if (title.isNotEmpty()) {
                Toast.makeText(this, "Task '$title' Created!", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            } else {
                etTitle.error = "Title is required"
            }
        }

        alertDialog.show()
    }
}