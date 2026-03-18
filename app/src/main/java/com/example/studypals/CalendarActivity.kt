package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var rvDayTasks: RecyclerView
    private lateinit var tvMonthYear: TextView
    private lateinit var tvSelectedDay: TextView
    private lateinit var llNoTasks: LinearLayout
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var taskAdapter: TaskAdapter
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val allTasks = mutableListOf<Task>()
    private var selectedDate: String = ""
    private val sdfFilter = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

        rvCalendar = findViewById(R.id.rvCalendarGrid)
        rvDayTasks = findViewById(R.id.rvDayTasks)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        tvSelectedDay = findViewById(R.id.tvSelectedDay)
        llNoTasks = findViewById(R.id.llNoTasks)
        btnAdd = findViewById(R.id.btnCalendarAdd)

        val calendar = Calendar.getInstance()
        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = sdfMonth.format(calendar.time).uppercase()

        // Default selected date is today
        selectedDate = sdfFilter.format(calendar.time)
        tvSelectedDay.text = SimpleDateFormat("d EEE", Locale.getDefault()).format(calendar.time).uppercase()

        setupRecyclerViews()
        setupCalendar()
        fetchTasks()

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
        }
    }

    private fun setupRecyclerViews() {
        taskAdapter = TaskAdapter(emptyList())
        rvDayTasks.layoutManager = LinearLayoutManager(this)
        rvDayTasks.adapter = taskAdapter
    }

    private fun setupCalendar() {
        val calendar = Calendar.getInstance()
        val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

        val daysList = mutableListOf<String>()
        repeat(firstDayOfWeek) { daysList.add("") }
        for (i in 1..daysInMonth) { daysList.add(i.toString()) }

        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvCalendar.adapter = CalendarAdapter(daysList, todayDay, allTasks) { day ->
            onDateSelected(day)
        }
    }

    private fun onDateSelected(day: String) {
        if (day.isEmpty()) return
        
        val calendar = Calendar.getInstance()
        val dayInt = day.toInt()
        calendar.set(Calendar.DAY_OF_MONTH, dayInt)
        
        selectedDate = sdfFilter.format(calendar.time)
        tvSelectedDay.text = "${dayInt} ${SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)}".uppercase()
        
        filterTasksForSelectedDate()
    }

    private fun fetchTasks() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("tasks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                allTasks.clear()
                value?.forEach { doc ->
                    val task = doc.toObject(Task::class.java)
                    allTasks.add(task.copy(id = doc.id))
                }
                
                (rvCalendar.adapter as? CalendarAdapter)?.updateTasks(allTasks)
                filterTasksForSelectedDate()
            }
    }

    private fun filterTasksForSelectedDate() {
        val filtered = allTasks.filter { it.date == selectedDate }
        taskAdapter.updateTasks(filtered.sortedBy { it.time })
        
        if (filtered.isEmpty()) {
            llNoTasks.visibility = View.VISIBLE
            rvDayTasks.visibility = View.GONE
        } else {
            llNoTasks.visibility = View.GONE
            rvDayTasks.visibility = View.VISIBLE
        }
    }
}
