package com.example.studypals

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(
    private val days: List<String>,
    private val today: Int
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.tvDayNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {

        val day = days[position]

        // ALWAYS RESET FIRST (important for RecyclerView recycling)
        holder.dayText.setBackgroundColor(Color.TRANSPARENT)
        holder.dayText.setTextColor(Color.parseColor("#333333"))

        holder.dayText.text = day

        // blank cell
        if (day.isEmpty()) {
            holder.dayText.text = ""
            return
        }

        val dayNumber = day.toInt()

        // Sunday color
        if (position % 7 == 0) {
            holder.dayText.setTextColor(Color.parseColor("#FF5252"))
        }

        // Today highlight
        if (dayNumber == today) {
            holder.dayText.setBackgroundResource(R.drawable.today_highlight)
            holder.dayText.setTextColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int = days.size
}
