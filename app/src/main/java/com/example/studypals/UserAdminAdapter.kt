package com.example.studypals

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class UserAdminAdapter(private var userList: MutableList<User>) :
    RecyclerView.Adapter<UserAdminAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tvAdminUsername)
        val emailText: TextView = view.findViewById(R.id.tvAdminUserEmail)
        val editBtn: ImageButton = view.findViewById(R.id.btnEditUser)
        val deleteBtn: ImageButton = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_admin, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        // Setting the data from your User class
        holder.nameText.text = user.username
        holder.emailText.text = user.email

        // Edit Button Click - Triggers the Dialog
        holder.editBtn.setOnClickListener {
            showEditDialog(holder.itemView.context, user, position)
        }

        // Delete Button Click
        holder.deleteBtn.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete User")
                .setMessage("Delete ${user.username}?")
                .setPositiveButton("Delete") { _, _ ->
                    userList.removeAt(position)
                    notifyItemRemoved(position)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount() = userList.size

    // Function to inflate and show dialog_edit.xml
    private fun showEditDialog(context: android.content.Context, user: User, position: Int) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_edit)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Find views in your dialog_edit.xml
        val etUsername = dialog.findViewById<EditText>(R.id.etEditUsername)
        val etFirstName = dialog.findViewById<EditText>(R.id.etEditFirstName)
        val etLastName = dialog.findViewById<EditText>(R.id.etEditLastName)
        val btnSave = dialog.findViewById<Button>(R.id.btnUpdateUser)

        // Pre-fill current data
        etUsername.setText(user.username)
        etFirstName.setText(user.firstName)
        etLastName.setText(user.lastName)

        btnSave.setOnClickListener {
            // Update the user object in the list
            val updatedUser = user.copy(
                username = etUsername.text.toString(),
                firstName = etFirstName.text.toString(),
                lastName = etLastName.text.toString()
            )

            userList[position] = updatedUser
            notifyItemChanged(position) // Refreshes the "Sami" card on the screen

            Toast.makeText(context, "Changes Saved Locally", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}