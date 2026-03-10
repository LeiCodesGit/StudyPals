package com.example.studypals

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class UserAdminAdapter(private var userList: MutableList<User>) :
    RecyclerView.Adapter<UserAdminAdapter.UserViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

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

        holder.nameText.text = user.username
        holder.emailText.text = user.email

        holder.editBtn.setOnClickListener {
            showEditDialog(holder.itemView.context, user, position)
        }

        holder.deleteBtn.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete User")
                .setMessage("Delete ${user.username}?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteUserFromFirestore(holder.itemView.context, user, position)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount() = userList.size

    private fun showEditDialog(context: Context, user: User, position: Int) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_edit)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 1. Initialize ALL views based on User attributes
        val etUsername = dialog.findViewById<EditText>(R.id.etEditUsername)
        val etFirstName = dialog.findViewById<EditText>(R.id.etEditFirstName)
        val etLastName = dialog.findViewById<EditText>(R.id.etEditLastName)
        val etAge = dialog.findViewById<EditText>(R.id.etEditAge)
        val cbAdmin = dialog.findViewById<CheckBox>(R.id.cbAdminStatus)
        val etPetName = dialog.findViewById<EditText>(R.id.etEditPetName)
        val etXP = dialog.findViewById<EditText>(R.id.etEditXP)
        val btnSave = dialog.findViewById<Button>(R.id.btnUpdateUser)

        // 2. Populate UI with User data
        etUsername.setText(user.username)
        etFirstName.setText(user.firstName)
        etLastName.setText(user.lastName)
        etAge.setText(user.age.toString())
        cbAdmin.isChecked = user.admin
        etPetName.setText(user.petName)
        etXP.setText(user.currentXP.toString())

        btnSave.setOnClickListener {
            val newXP = etXP.text.toString().toLongOrNull() ?: user.currentXP
            // Auto-calculate level: Level 1 for 0-999, Level 2 for 1000+, etc.
            val newLevel = (newXP / 1000).toInt() + 1

            val updates = mapOf(
                "username" to etUsername.text.toString(),
                "firstName" to etFirstName.text.toString(),
                "lastName" to etLastName.text.toString(),
                "age" to (etAge.text.toString().toIntOrNull() ?: user.age),
                "admin" to cbAdmin.isChecked,
                "petName" to etPetName.text.toString(),
                "currentXP" to newXP,
                "level" to newLevel
            )

            db.collection("users").document(user.uid)
                .update(updates)
                .addOnSuccessListener {
                    // Update local list for immediate UI feedback
                    userList[position] = user.copy(
                        username = etUsername.text.toString(),
                        firstName = etFirstName.text.toString(),
                        lastName = etLastName.text.toString(),
                        age = (etAge.text.toString().toIntOrNull() ?: user.age),
                        admin = cbAdmin.isChecked,
                        petName = etPetName.text.toString(),
                        currentXP = newXP,
                        level = newLevel
                    )
                    notifyItemChanged(position)
                    dialog.dismiss()
                    Toast.makeText(context, "All User & Pet stats synced!", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun deleteUserFromFirestore(context: Context, user: User, position: Int) {
        db.collection("users").document(user.uid)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "User Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("UserAdminAdapter", "Error deleting user", e)
                Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
