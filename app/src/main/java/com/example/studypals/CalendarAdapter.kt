package com.example.studypals

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(
    private val days: List<String>,
    private val today: Int,
    private val onDayClick: (String, Int) -> Unit // Added position to the callback
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    // Track which item is clicked. Initialized to -1 (nothing selected)
    private var selectedPosition: Int = -1

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
        holder.dayText.text = day

        // 1. Reset defaults
        holder.dayText.setBackgroundResource(0) // Removes any background
        holder.dayText.setTextColor(Color.parseColor("#333333"))

        if (day.isEmpty()) return

        // 2. Sunday Color
        if (position % 7 == 0) {
            holder.dayText.setTextColor(Color.parseColor("#FF5252"))
        }

        val dayNumber = day.toIntOrNull() ?: -1

        // 3. PRIORITY 1: Highlight Today (Dark Highlight)
        if (dayNumber == today) {
            holder.dayText.setBackgroundResource(R.drawable.today_highlight)
            holder.dayText.setTextColor(Color.WHITE)
        }
        // 4. PRIORITY 2: Highlight Selected (Light Grey Highlight)
        else if (position == selectedPosition) {
            holder.dayText.setBackgroundResource(R.drawable.selected_day_highlight)
            holder.dayText.setTextColor(Color.parseColor("#333333"))
        }

        // 5. Click Logic
        holder.itemView.setOnClickListener {
            if (day.isNotEmpty()) {
                val oldPosition = selectedPosition
                selectedPosition = holder.adapterPosition

                // Refresh only the items that changed to save performance
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)

                onDayClick(day, selectedPosition)
            }
        }
    }

    override fun getItemCount(): Int = days.size
}