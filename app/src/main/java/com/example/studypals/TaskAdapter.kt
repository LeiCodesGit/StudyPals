package com.example.studypals

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
        
        // Remove listener before setting state to avoid trigger on recycle
        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = task.completed

        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            FirebaseFirestore.getInstance().collection("tasks").document(task.id)
                .update("completed", isChecked)
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
