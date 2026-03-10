package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class TodoActivity : AppCompatActivity() {

    private lateinit var rvTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo)

        val appLogo = findViewById<ImageView>(R.id.appLogo)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAdd)
        val tvMainDate = findViewById<TextView>(R.id.tvMainDate)
        rvTasks = findViewById(R.id.rvTasks)

        // Set current date
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        tvMainDate.text = sdf.format(Date())

        // Setup RecyclerView
        taskAdapter = TaskAdapter(emptyList())
        rvTasks.adapter = taskAdapter

        // Back to Home
        appLogo.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Go to Add Task
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

        fetchTasks()
    }

    private fun fetchTasks() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("tasks")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                val taskList = value?.map { doc ->
                    val task = doc.toObject(Task::class.java)
                    task.copy(id = doc.id)
                } ?: emptyList()
                
                taskAdapter.updateTasks(taskList)
            }
    }
}
