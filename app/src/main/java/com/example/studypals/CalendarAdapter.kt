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
    private var tasks: List<Task>,
    private val onDayClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var selectedPosition: Int = -1

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.tvDayNumber)
        val taskIndicator: View = view.findViewById(R.id.taskIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.dayText.text = day

        // 1. Reset defaults for recycling
        holder.dayText.setBackgroundResource(0)
        holder.dayText.setTextColor(Color.parseColor("#333333"))
        holder.taskIndicator.visibility = View.GONE

        if (day.isEmpty()) return

        val dayNumber = day.toIntOrNull() ?: -1

        // 2. Sunday Color
        if (position % 7 == 0) {
            holder.dayText.setTextColor(Color.parseColor("#FF5252"))
        }

        // 3. Today highlight (Priority 1)
        if (dayNumber == today) {
            holder.dayText.setBackgroundResource(R.drawable.today_highlight)
            holder.dayText.setTextColor(Color.WHITE)
        }
        // 4. Selected highlight (Priority 2)
        else if (position == selectedPosition) {
            // You might want to create this drawable or use a standard one
            holder.dayText.setBackgroundColor(Color.LTGRAY) 
        }

        // 5. Task Indicator logic
        val hasTasks = tasks.any { task ->
            try {
                // Matches format "dd.MM.yyyy" from AddTaskActivity
                val taskDay = task.date.split(".")[0].toInt()
                taskDay == dayNumber
            } catch (e: Exception) {
                false
            }
        }
        if (hasTasks) {
            holder.taskIndicator.visibility = View.VISIBLE
        }

        // 6. Click Listener
        holder.itemView.setOnClickListener {
            if (day.isNotEmpty()) {
                val oldPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onDayClick(day)
            }
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }
}
