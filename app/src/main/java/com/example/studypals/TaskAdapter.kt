package com.example.studypals

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

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

        // Click on Checkbox
        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            updateTaskStyle(holder, isChecked)
            FirebaseFirestore.getInstance().collection("tasks").document(task.id)
                .update("completed", isChecked)
                .addOnSuccessListener {
                    if (isChecked) {
                        showCompletionDialog(holder.itemView.context, task.title)
                    }
                }
        }

        // Click on Task Item (Whole Card) for Edit/Delete
        holder.itemView.setOnClickListener {
            showEditDeleteDialog(holder.itemView.context, task)
        }
    }

    private fun showCompletionDialog(context: Context, title: String) {
        AlertDialog.Builder(context)
            .setTitle("🎉 Great Job!")
            .setMessage("Congratulations on completing your task: $title!")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showEditDeleteDialog(context: Context, task: Task) {
        // Use add_task.xml which is the full-screen layout we updated
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_task, null)
        val dialog = AlertDialog.Builder(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
            .setView(dialogView)
            .create()

        val etTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val etDate = dialogView.findViewById<EditText>(R.id.etTaskDate)
        val etTime = dialogView.findViewById<EditText>(R.id.etTaskTime)
        val etDesc = dialogView.findViewById<EditText>(R.id.etTaskDesc)
        val btnSave = dialogView.findViewById<Button>(R.id.btnCreateTask)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseTask)

        // Pre-fill existing data
        etTitle.setText(task.title)
        etDate.setText(task.date)
        etTime.setText(task.time)
        etDesc.setText(task.description)
        btnSave.text = "Update Task"

        // Date Picker for editing
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            try {
                val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                val d = sdf.parse(task.date)
                if (d != null) cal.time = d
            } catch (e: Exception) {}

            DatePickerDialog(context, { _, y, m, d ->
                etDate.setText(String.format("%02d-%02d-%d", m + 1, d, y))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }

        // Time Picker for editing
        etTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(context, { _, h, min ->
                val c = Calendar.getInstance()
                c.set(Calendar.HOUR_OF_DAY, h)
                c.set(Calendar.MINUTE, min)
                etTime.setText(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(c.time))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        // Update Button
        btnSave.setOnClickListener {
            val updatedData = mapOf(
                "title" to etTitle.text.toString(),
                "date" to etDate.text.toString(),
                "time" to etTime.text.toString(),
                "description" to etDesc.text.toString()
            )
            FirebaseFirestore.getInstance().collection("tasks").document(task.id)
                .update(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Task Updated", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
        }

        // Add Delete Button dynamically
        // Find the container (the parent of btnSave)
        val container = btnSave.parent as? LinearLayout
        if (container != null) {
            val deleteBtn = Button(context).apply {
                text = "Delete Task"
                setTextColor(Color.RED)
                background = null
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16
                }
            }
            
            deleteBtn.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete") { _, _ ->
                        FirebaseFirestore.getInstance().collection("tasks").document(task.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            container.addView(deleteBtn)
        }

        dialog.show()
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
