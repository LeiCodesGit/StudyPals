package com.example.studypals

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    
    private var selectedDate: String = ""
    private val sdfFilter = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo)

        val appLogo = findViewById<ImageView>(R.id.appLogo)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAdd)
        val tvMainDate = findViewById<TextView>(R.id.tvMainDate)
        rvTasks = findViewById(R.id.rvTasks)

        // Default to today
        selectedDate = sdfFilter.format(Date())

        setupDateSelector(tvMainDate)

        taskAdapter = TaskAdapter(emptyList())
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = taskAdapter

        appLogo.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        fetchTasks()
    }

    private fun setupDateSelector(tvMainDate: TextView) {
        val calendar = Calendar.getInstance()
        val todayStr = sdfFilter.format(calendar.time)
        
        val sdfMain = SimpleDateFormat("dd MMM", Locale.getDefault())
        tvMainDate.text = sdfMain.format(calendar.time)

        val dateLayouts = arrayOf(
            findViewById<LinearLayout>(R.id.dateContainer1),
            findViewById<LinearLayout>(R.id.dateContainer2),
            findViewById<LinearLayout>(R.id.dateContainer3),
            findViewById<LinearLayout>(R.id.dateContainer4),
            findViewById<LinearLayout>(R.id.dateContainer5)
        )
        
        val dateIds = arrayOf(R.id.tvDate1, R.id.tvDate2, R.id.tvDate3, R.id.tvDate4, R.id.tvDate5)
        val dayIds = arrayOf(R.id.tvDay1, R.id.tvDay2, R.id.tvDay3, R.id.tvDay4, R.id.tvDay5)

        val sdfDate = SimpleDateFormat("d", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())

        calendar.add(Calendar.DAY_OF_YEAR, -2)

        for (i in 0 until 5) {
            val dateStr = sdfFilter.format(calendar.time)
            val dateView = findViewById<TextView>(dateIds[i])
            val dayView = findViewById<TextView>(dayIds[i])
            val layout = dateLayouts[i]

            dateView.text = sdfDate.format(calendar.time)
            dayView.text = sdfDay.format(calendar.time)

            // Initial highlight for today
            if (dateStr == todayStr) {
                highlightDate(layout, dateView, dayView, true)
            }

            layout.setOnClickListener {
                selectedDate = dateStr
                // Reset all, then highlight selected
                for (j in 0 until 5) {
                    val dv = findViewById<TextView>(dateIds[j])
                    val dyv = findViewById<TextView>(dayIds[j])
                    highlightDate(dateLayouts[j], dv, dyv, false)
                }
                highlightDate(layout, dateView, dayView, true)
                fetchTasks() // Refresh list for new date
            }
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun highlightDate(layout: LinearLayout, dateView: TextView, dayView: TextView, isSelected: Boolean) {
        if (isSelected) {
            layout.setBackgroundResource(R.drawable.date_selected_bg)
            dateView.setTextColor(Color.WHITE)
            dayView.setTextColor(Color.WHITE)
        } else {
            layout.setBackgroundColor(Color.TRANSPARENT)
            dateView.setTextColor(Color.BLACK)
            dayView.setTextColor(Color.GRAY)
        }
    }

    private fun fetchTasks() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("tasks")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", selectedDate) // Filter by selected date
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                val taskList = value?.mapNotNull { doc ->
                    val task = doc.toObject(Task::class.java)
                    task.copy(id = doc.id)
                } ?: emptyList()
                
                taskAdapter.updateTasks(taskList.sortedBy { it.time })
            }
    }
}
