package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

        // 1. Set dynamic dates for the 5-day selector
        setupDateSelector(tvMainDate)

        // 2. Setup the Task List (RecyclerView)
        taskAdapter = TaskAdapter(emptyList())
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = taskAdapter

        // Navigation
        appLogo.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        // 3. Start fetching tasks from database
        fetchTasks()
    }

    private fun setupDateSelector(tvMainDate: TextView) {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        // Set Main Date at the top (e.g., 14 Sept)
        val sdfMain = SimpleDateFormat("dd MMM", Locale.getDefault())
        tvMainDate.text = sdfMain.format(today)

        // We want to show: Today-2, Today-1, Today, Today+1, Today+2
        val dateIds = arrayOf(R.id.tvDate1, R.id.tvDate2, R.id.tvDate3, R.id.tvDate4, R.id.tvDate5)
        val dayIds = arrayOf(R.id.tvDay1, R.id.tvDay2, R.id.tvDay3, R.id.tvDay4, R.id.tvDay5)

        val sdfDate = SimpleDateFormat("d", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())

        // Start from 2 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -2)

        for (i in 0 until 5) {
            findViewById<TextView>(dateIds[i]).text = sdfDate.format(calendar.time)
            findViewById<TextView>(dayIds[i]).text = sdfDay.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to next day
        }
    }

    private fun fetchTasks() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("tasks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                val taskList = value?.mapNotNull { doc ->
                    val task = doc.toObject(Task::class.java)
                    task.copy(id = doc.id)
                } ?: emptyList()
                
                // Sort so newest tasks appear at the top
                val sortedList = taskList.sortedByDescending { it.timestamp }
                taskAdapter.updateTasks(sortedList)
            }
    }
}
