package com.example.studypals

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

        val rvCalendar = findViewById<RecyclerView>(R.id.rvCalendarGrid)

        val calendar = Calendar.getInstance()

        val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0

        val daysList = mutableListOf<String>()

        // add blank spaces before first day
        repeat(firstDayOfWeek) {
            daysList.add("")
        }

        // add real days
        for (i in 1..daysInMonth) {
            daysList.add(i.toString())
        }

        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvCalendar.adapter = CalendarAdapter(daysList, todayDay)
    }
}
