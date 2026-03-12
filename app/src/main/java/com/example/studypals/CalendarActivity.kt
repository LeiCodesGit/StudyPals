package com.example.studypals

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var tvMonthYear: TextView
    private lateinit var tvSelectedDay: TextView

    // Tracks the currently viewed month
    private var currentDisplayCalendar = Calendar.getInstance()

    // Constants for the actual "Today"
    private val realToday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    private val realMonth = Calendar.getInstance().get(Calendar.MONTH)
    private val realYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

        rvCalendar = findViewById(R.id.rvCalendarGrid)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        tvSelectedDay = findViewById(R.id.tvSelectedDay)

        // Click header to select month/year
        tvMonthYear.setOnClickListener {
            showMonthYearPicker()
        }

        refreshCalendarUI()
    }

    private fun refreshCalendarUI() {
        // Update Headers
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = monthFormat.format(currentDisplayCalendar.time).uppercase()

        val displayFormat = SimpleDateFormat("MMM d - EEE", Locale.getDefault())
        tvSelectedDay.text = displayFormat.format(currentDisplayCalendar.time).uppercase()

        // Generate Grid (with offsets)
        val daysList = mutableListOf<String>()
        val monthCal = currentDisplayCalendar.clone() as Calendar
        monthCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        repeat(firstDayOfWeek) { daysList.add("") }
        for (i in 1..daysInMonth) { daysList.add(i.toString()) }

        val highlightDay = if (currentDisplayCalendar.get(Calendar.MONTH) == realMonth &&
            currentDisplayCalendar.get(Calendar.YEAR) == realYear) {
            realToday
        } else {
            -1
        }

        // Setup Adapter
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvCalendar.adapter = CalendarAdapter(daysList, highlightDay) { selectedDay, position ->
            if (selectedDay.isNotEmpty()) {
                val clickCal = currentDisplayCalendar.clone() as Calendar
                clickCal.set(Calendar.DAY_OF_MONTH, selectedDay.toInt())

                val displayFormat = SimpleDateFormat("MMM d - EEE", Locale.getDefault())
                tvSelectedDay.text = displayFormat.format(clickCal.time).uppercase()
            }
        }
    }

    private fun showMonthYearPicker() {
        val dialog = DatePickerDialog(
            this,
            R.style.CalendarDatePickerTheme,
            { _, year, month, day ->
                currentDisplayCalendar.set(Calendar.YEAR, year)
                currentDisplayCalendar.set(Calendar.MONTH, month)
                currentDisplayCalendar.set(Calendar.DAY_OF_MONTH, day)
                refreshCalendarUI()
            },
            currentDisplayCalendar.get(Calendar.YEAR),
            currentDisplayCalendar.get(Calendar.MONTH),
            currentDisplayCalendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }
}