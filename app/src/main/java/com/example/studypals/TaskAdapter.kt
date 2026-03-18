package com.example.studypals

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class TaskAdapter(private var tasks: List<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTaskTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvTaskDesc: TextView = itemView.findViewById(R.id.tvDesc)
        val cbDone: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvTaskTitle.text = task.title
        holder.tvTaskTime.text = "${task.date} at ${task.time}"
        holder.tvTaskDesc.text = task.description
        
        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = task.completed
        updateTaskStyle(holder, task.completed)

        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            updateTaskStyle(holder, isChecked)
            FirebaseFirestore.getInstance().collection("tasks").document(task.id)
                .update("completed", isChecked)
                .addOnSuccessListener {
                    if (isChecked) {
                        // Add 10 XP for completing a task
                        LevelManager.addExp(holder.itemView.context, 10)
                    }
                }
        }
    }

    private fun updateTaskStyle(holder: TaskViewHolder, isCompleted: Boolean) {
        if (isCompleted) {
            holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTaskTitle.alpha = 0.6f
            holder.tvTaskDesc.alpha = 0.6f
        } else {
            holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvTaskTitle.alpha = 1.0f
            holder.tvTaskDesc.alpha = 1.0f
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
